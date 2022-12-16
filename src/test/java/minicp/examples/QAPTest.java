package minicp.examples;

import minicp.cp.BranchingScheme;
import minicp.cp.Factory;
import minicp.engine.core.IntVar;
import minicp.search.SearchStatistics;
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import org.javagrader.Grade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Grade
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
            assertTrue(x.isFixed(), "A location does not have a fixed assigned facility");
            int j = x.min();
            assertFalse(assigned.contains(j), "You modified the model: a facility was assigned to more than one location");
            assigned.add(j);
            location[i] = j;
        }
        for (int i = 0 ; i < qap.n ; ++i) {
            for (int j = 0 ; j < qap.n ; ++j) {
                objective += qap.weights[i][j] * qap.distances[location[i]][location[j]];
            }
        }
        assertEquals(qap.n, assigned.size(), "You modified the model: a facility was assigned to more than one location");
        assertTrue(qap.totCost.isFixed(), "The total cost is not fixed");
        assertEquals(objective, qap.totCost.min(), "Your objective value is not correctly computed");
        return objective;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getModelPair")
    @Grade(cpuTimeout = 1)
    public void testFirstSolution(QAP customModel, QAP basicModel) {
        try {
            customModel.buildModel();
            AtomicInteger objectiveCustom = new AtomicInteger();
            customModel.dfs.onSolution(() -> {
                objectiveCustom.set(assertValidSolution(customModel));
            });
            SearchStatistics customSearchStats = customModel.solve(false, s -> s.numberOfSolutions() == 1);
            assertEquals(1, customSearchStats.numberOfSolutions(), "You did not find any solution");
            assertTrue(customSearchStats.numberOfNodes() > 1, "You need to perform some kind of search");

            basicModel.buildModel();
            basicModel.dfs = Factory.makeDfs(basicModel.totCost.getSolver(), BranchingScheme.firstFail(basicModel.x));
            AtomicInteger objectiveFirstFail = new AtomicInteger();
            basicModel.dfs.onSolution(() -> {
                objectiveFirstFail.set(assertValidSolution(basicModel));
            });
            SearchStatistics firstFailSearchStats = basicModel.solve(false, s -> s.numberOfSolutions() == 1);
            assertEquals(1, firstFailSearchStats.numberOfSolutions(),
                    "Your model fails to find a solution with a simple first-fail search");
            assertTrue(objectiveCustom.get() < objectiveFirstFail.get(),
                    "Your objective value is not lower with your custom search than when using a simple first-fail search");
        } catch (InconsistencyException e) {
            fail("No inconsistency should happen when creating the constraints and performing the search " + e);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Grade(cpuTimeout = 7)
    @Test
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
            assertTrue(customSearchStats.isCompleted(), "You need to explore the entire search space");
            assertEquals(9552, sol.get(), "You did not find the optimal solution");
            assertTrue(customSearchStats.numberOfNodes() < 144846, "Your custom search should explore less nodes than a simple first-fail");
            assertTrue(customSearchStats.numberOfFailures() < 72424, "Your custom search should provoke less failures than a simple first-fail");
        } catch (InconsistencyException | NullPointerException e) {
            fail("No inconsistency should happen when creating the constraints and performing the search " + e);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    public static Stream<Arguments> getModelPair() {
        return Stream.of(new String[] {"data/qap.txt", "data/qap25.txt"})
                .map(s -> arguments(named(new File(s).getName(), new QAP(s)), new QAP(s)));
    }

}
