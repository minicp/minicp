package minicp.examples;

import com.github.guillaumederval.javagrading.Grade;
import com.github.guillaumederval.javagrading.GradeClass;
import com.github.guillaumederval.javagrading.GradingRunnerWithParametersFactory;
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
import minicp.cp.BranchingScheme;

@RunWith(Enclosed.class)
public class TSPTest {

    /**
     * Verifies a solution to a {@link TSP} and returns its objective value
     *
     * @param tsp Traveling Salesman Problem
     * @return distance traveled by the path defined in the successors {@link TSP#succ}
     */
    public static int assertValidSolution(TSP tsp) {
        int objective = 0;
        for (IntVar s : tsp.succ)
            assertTrue("A node does not have a fixed successor", s.isFixed());
        assertTrue("The total distance is not fixed", tsp.totalDist.isFixed());
        for (int i = 0; i < tsp.n; i++)
            assertNotEquals("No self loops", tsp.succ[i].min(), i);
        int i = tsp.succ[0].min();
        HashSet<Integer> visited = new HashSet<>();
        while (!visited.contains(i)) {
            objective += tsp.distanceMatrix[i][tsp.succ[i].min()];
            visited.add(i);
            i = tsp.succ[i].min();
        }
        assertEquals("Not all nodes were visited", visited.size(), tsp.n);
        assertEquals("Your objective value is not correctly computed", objective, tsp.totalDist.min());
        return objective;
    }

    @GradeClass(totalValue = 1, defaultCpuTimeout = 1000)
    @RunWith(Parameterized.class)
    @Parameterized.UseParametersRunnerFactory(GradingRunnerWithParametersFactory.class)
    public static class CompareWithFirstFailTest {

        TSP customModel; // custom model to test
        TSP basicModel; // model using a first-fail strategy

        /**
         * @param instance path to the instance that needs to be solved
         */
        public CompareWithFirstFailTest(String instance) {
            customModel = new TSP(instance);
            basicModel = new TSP(instance);
        }

        @Parameterized.Parameters
        public static Object[][] instance() {
            return new Object[][] {
                    {"data/tsp/tsp_15.txt"},
                    {"data/tsp/tsp_17.txt"},
                    {"data/tsp/tsp_26.txt"},
            };
        }

        @Test(timeout = 600)
        @Grade(cpuTimeout = 200, customPermissions = DataPermissionFactory.class)
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
                basicModel.dfs = Factory.makeDfs(basicModel.totalDist.getSolver(), BranchingScheme.firstFail(basicModel.succ));
                AtomicInteger objectiveFirstFail = new AtomicInteger();
                basicModel.dfs.onSolution(() -> {
                    objectiveFirstFail.set(assertValidSolution(basicModel));
                });
                SearchStatistics firstFailSearchStats = basicModel.solve(false, s -> s.numberOfSolutions() == 1);
                assertEquals("Your model fails to find a solution with a simple first-fail search",
                        1, firstFailSearchStats.numberOfSolutions());
                assertTrue("Your objective value is not lower with your custom " +
                        "search than when using a simple first-fail search", objectiveCustom.get() < objectiveFirstFail.get());
            } catch (InconsistencyException | NullPointerException e) {
                fail("No inconsistency should happen when creating the constraints and performing the search " + e);
            } catch (NotImplementedException e) {
                NotImplementedExceptionAssume.fail(e);
            }
        }
    }

    @GradeClass(totalValue = 1)
    public static class FullSearchTest {
        @Test(timeout = 1500)
        @Grade(cpuTimeout = 500, customPermissions = DataPermissionFactory.class)
        public void testOptimality() {
            try {
                String instance = "data/tsp/tsp_15.txt";
                TSP customModel = new TSP(instance);
                customModel.buildModel();
                AtomicInteger sol = new AtomicInteger();
                customModel.dfs.onSolution(() -> {
                    sol.set(assertValidSolution(customModel));
                });
                SearchStatistics customSearchStats = customModel.solve(false);
                assertTrue("You need to explore the entire search space", customSearchStats.isCompleted());
                assertEquals("You did not find the optimal solution", 291, sol.get());
                assertTrue("Your custom search should explore less nodes than a simple first-fail",
                        customSearchStats.numberOfNodes() < 7032);
                assertTrue("Your custom search should provoke less failures than a simple first-fail",
                        customSearchStats.numberOfFailures() < 3517);
            } catch (InconsistencyException | NullPointerException e) {
                fail("No inconsistency should happen when creating the constraints and performing the search " + e);
            } catch (NotImplementedException e) {
                NotImplementedExceptionAssume.fail(e);
            }
        }
    }

}
