package minicp.examples;

import com.github.guillaumederval.javagrading.Grade;
import com.github.guillaumederval.javagrading.GradeClass;
import com.github.guillaumederval.javagrading.GradingRunnerWithParametersFactory;
import minicp.cp.BranchingScheme;
import minicp.cp.Factory;
import minicp.engine.core.IntVar;
import minicp.search.SearchStatistics;
import minicp.util.DataPermissionFactory;
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class QAPTest {

    /**
     * Verifies a solution to a {@link QAP} and returns its objective value
     *
     * @param qap Quadratic Assignment Problem
     * @return sum of distances multiplied by the corresponding flows
     */
    public static int assertValidSolution(QAP qap) {
        int objective = 0;
        HashSet<Integer> assigned = new HashSet<>();
        int[] location = new int[qap.n];
        for (int i = 0 ; i < qap.n; ++i) {
            IntVar x = qap.x[i];
            assertTrue("A location does not have a fixed assigned facility", x.isFixed());
            int j = x.min();
            assertFalse("You modified the model: a facility was assigned to more than one location",
                    assigned.contains(j));
            assigned.add(j);
            location[i] = j;
        }
        for (int i = 0 ; i < qap.n ; ++i) {
            for (int j = 0 ; j < qap.n ; ++j) {
                objective += qap.weights[i][j] * qap.distances[location[i]][location[j]];
            }
        }
        assertEquals("You modified the model: a facility was assigned to more than one location",
                qap.n, assigned.size());
        assertTrue("The total cost is not fixed", qap.totCost.isFixed());
        assertEquals("Your objective value is not correctly computed", objective, qap.totCost.min());
        return objective;
    }

    @GradeClass(totalValue = 1, defaultCpuTimeout = 1000)
    @RunWith(Parameterized.class)
    @Parameterized.UseParametersRunnerFactory(GradingRunnerWithParametersFactory.class)
    public static class CompareWithFirstFailTest {

        QAP customModel; // custom model to test
        QAP basicModel; // model using a first-fail strategy

        /**
         * @param instance path to the instance that needs to be solved
         */
        public CompareWithFirstFailTest(String instance) {
            customModel = new QAP(instance);
            basicModel = new QAP(instance);
        }

        @Parameterized.Parameters
        public static Object[][] instance() {
            return new Object[][] {
                    {"data/qap.txt"},
                    {"data/qap25.txt"},
            };
        }

        @Test(timeout = 2000)
        @Grade(cpuTimeout = 1000, customPermissions = DataPermissionFactory.class)
        public void testFirstSolution() {
            try {
                customModel.buildModel();
                AtomicInteger objectiveCustom = new AtomicInteger();
                customModel.dfs.onSolution(() -> {
                    objectiveCustom.set(assertValidSolution(customModel));
                });
                SearchStatistics customSearchStats = customModel.solve(false, s -> s.numberOfSolutions() == 1);
                assertEquals("You did not find any solution", 1, customSearchStats.numberOfSolutions());
                assertTrue("You need to perform some kind of search", customSearchStats.numberOfNodes() > 1);

                basicModel.buildModel();
                basicModel.dfs = Factory.makeDfs(basicModel.totCost.getSolver(), BranchingScheme.firstFail(basicModel.x));
                AtomicInteger objectiveFirstFail = new AtomicInteger();
                basicModel.dfs.onSolution(() -> {
                    objectiveFirstFail.set(assertValidSolution(basicModel));
                });
                SearchStatistics firstFailSearchStats = basicModel.solve(false, s -> s.numberOfSolutions() == 1);
                assertEquals("Your model fails to find a solution with a simple first-fail search",
                        1, firstFailSearchStats.numberOfSolutions());
                assertTrue("Your objective value is not lower with your custom " +
                        "search than when using a simple first-fail search", objectiveCustom.get() < objectiveFirstFail.get());
            } catch (InconsistencyException e) {
                fail("No inconsistency should happen when creating the constraints and performing the search " + e);
            } catch (NotImplementedException e) {
                NotImplementedExceptionAssume.fail(e);
            }
        }
    }

    @GradeClass(totalValue = 1)
    public static class FullSearchTest {
        @Test(timeout = 6000)
        @Grade(cpuTimeout = 4500, customPermissions = DataPermissionFactory.class)
        public void testOptimality() {
            try {
                String instance = "data/qap.txt";
                QAP customModel = new QAP(instance);
                customModel.buildModel();
                AtomicInteger sol = new AtomicInteger();
                customModel.dfs.onSolution(() -> {
                    sol.set(assertValidSolution(customModel));
                });
                SearchStatistics customSearchStats = customModel.solve(false);
                assertTrue("You need to explore the entire search space", customSearchStats.isCompleted());
                assertEquals("You did not find the optimal solution", 9552, sol.get());
                assertTrue("Your custom search should explore less nodes than a simple first-fail",
                        customSearchStats.numberOfNodes() < 144846);
                assertTrue("Your custom search should provoke less failures than a simple first-fail",
                        customSearchStats.numberOfFailures() < 72424);
            } catch (InconsistencyException | NullPointerException e) {
                fail("No inconsistency should happen when creating the constraints and performing the search " + e);
            } catch (NotImplementedException e) {
                NotImplementedExceptionAssume.fail(e);
            }
        }
    }

}
