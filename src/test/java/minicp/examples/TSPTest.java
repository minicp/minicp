package minicp.examples;

import minicp.cp.Factory;
import minicp.engine.core.IntVar;
import minicp.search.SearchStatistics;
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import org.javagrader.Allow;
import org.javagrader.Grade;

import java.io.File;
import java.time.Duration;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import minicp.cp.BranchingScheme;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Grade
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
            assertTrue(s.isFixed(), "A node does not have a fixed successor");
        assertTrue(tsp.totalDist.isFixed(), "The total distance is not fixed");
        for (int i = 0; i < tsp.n; i++)
            assertNotEquals(tsp.succ[i].min(), i, "No self loops");
        int i = tsp.succ[0].min();
        HashSet<Integer> visited = new HashSet<>();
        while (!visited.contains(i)) {
            objective += tsp.distanceMatrix[i][tsp.succ[i].min()];
            visited.add(i);
            i = tsp.succ[i].min();
        }
        assertEquals(visited.size(), tsp.n, "Not all nodes were visited");
        assertEquals(objective, tsp.totalDist.min(), "Your objective value is not correctly computed");
        return objective;
    }

    @Grade(cpuTimeout = 500, unit = TimeUnit.MILLISECONDS)
    @ParameterizedTest
    @ValueSource(strings = "data/tsp/tsp_15.txt")
    public void testOptimality(String instance) {
        try {
            TSP customModel = new TSP(instance);
            customModel.buildModel();
            AtomicInteger sol = new AtomicInteger();
            customModel.dfs.onSolution(() -> {
                sol.set(assertValidSolution(customModel));
            });
            SearchStatistics customSearchStats = customModel.solve(false);
            assertTrue(customSearchStats.isCompleted(), "You need to explore the entire search space");
            assertEquals(291, sol.get(), "You did not find the optimal solution");
            assertTrue(customSearchStats.numberOfNodes() < 7032,
                    "Your custom search should explore less nodes than a simple first-fail");
            assertTrue(customSearchStats.numberOfFailures() < 3517,
                    "Your custom search should provoke less failures than a simple first-fail");
        } catch (InconsistencyException | NullPointerException e) {
            fail("No inconsistency should happen when creating the constraints and performing the search " + e);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Grade(cpuTimeout = 200, unit = TimeUnit.MILLISECONDS)
    @ParameterizedTest(name = "{0}")
    @MethodSource("getModelPair")
    public void testAgainstFirstFail(TSP customModel, TSP basicModel) {
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
            basicModel.dfs = Factory.makeDfs(basicModel.totalDist.getSolver(), BranchingScheme.firstFail(basicModel.succ));
            AtomicInteger objectiveFirstFail = new AtomicInteger();
            basicModel.dfs.onSolution(() -> {
                objectiveFirstFail.set(assertValidSolution(basicModel));
            });
            SearchStatistics firstFailSearchStats = basicModel.solve(false, s -> s.numberOfSolutions() == 1);
            assertEquals(1, firstFailSearchStats.numberOfSolutions(), "Your model fails to find a solution with a simple first-fail search");
            assertTrue(objectiveCustom.get() < objectiveFirstFail.get(),
                    "Your objective value is not lower with your custom " +
                            "search than when using a simple first-fail search");
        } catch (InconsistencyException | NullPointerException e) {
            fail("No inconsistency should happen when creating the constraints and performing the search " + e);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Grade(2)
    @ParameterizedTest(name = "[{index}] {0} - beat {1} in {2}s")
    @MethodSource("getLNSInstances")
    @Allow("java.lang.Thread")
    public void testLNS(String instance, int objectiveToBeat, double maxRunTimeS) {
        try {
            long maxRunTime = (long) (maxRunTimeS * 1000);
            TSP tsp = new TSP(instance);
            tsp.buildModel();
            tsp.dfs = Factory.makeDfs(tsp.totalDist.getSolver(), BranchingScheme.firstFail(tsp.succ));
            AtomicInteger sol = new AtomicInteger(Integer.MAX_VALUE);
            AtomicInteger nSol = new AtomicInteger(0);
            tsp.dfs.onSolution(() -> {
                int value = assertValidSolution(tsp);
                assertTrue(value < sol.get());
                sol.set(value);
                nSol.incrementAndGet();
            });
            long start = System.currentTimeMillis();
            long maxTime = (long) (maxRunTime * 0.9);
            assertTimeoutPreemptively(Duration.ofMillis(maxRunTime), () -> tsp.lns(false, i -> i < objectiveToBeat || System.currentTimeMillis() - start > maxTime));
            assertTrue(nSol.get() > 2);
            assertTrue(sol.get() < objectiveToBeat);
        } catch (InconsistencyException | NullPointerException e) {
            fail("No inconsistency should happen when creating the constraints and performing the search " + e);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }
    
    public static Stream<Arguments> getModelPair() {
        return Stream.of(new String[] {"data/tsp/tsp_15.txt", "data/tsp/tsp_17.txt", "data/tsp/tsp_26.txt"})
                .map(s -> arguments(named(new File(s).getName(), new TSP(s)), new TSP(s)));
    }

    private static class LNSRun {

        private String instance;
        private int objectiveToBeat;
        private double maxRunTime;

        public LNSRun(String instance, int objectiveToBeat, double maxRunTime) {
            this.instance = instance;
            this.objectiveToBeat = objectiveToBeat;
            this.maxRunTime = maxRunTime;
        }

        public String getName() {
            return new File(instance).getName();
        }

    }

    public static Stream<Arguments> getLNSInstances() {
        double maxRunTime = 3; // in s
        return Stream.of(new LNSRun[] {
                    new LNSRun("data/tsp/tsp_61.txt", 357, maxRunTime),
                    new LNSRun("data/tsp/tsp_101.txt", 450, maxRunTime),
                    new LNSRun("data/tsp/tsp_81.txt", 390, maxRunTime),
                })
                .map(run -> arguments(named(run.getName(), run.instance), run.objectiveToBeat, run.maxRunTime));
    }

}
