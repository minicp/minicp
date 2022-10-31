package minicp.examples;

import com.github.guillaumederval.javagrading.Grade;
import com.github.guillaumederval.javagrading.GradeClass;
import com.github.guillaumederval.javagrading.GradingRunnerWithParametersFactory;
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

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class RCPSPTest {

    public static int assertValidSolution(RCPSP rcpsp) {
        for (IntVar s: rcpsp.start)
            assertTrue("You have an unfixed variable", s.isFixed());
        for (IntVar e: rcpsp.end)
            assertTrue("You have an unfixed variable", e.isFixed());
        for (int a = 0; a < rcpsp.nActivities; a++) {
            for (int succ : rcpsp.successors[a]) {
                assertTrue(String.format("Activity %d must precede %d", a, succ), rcpsp.end[a].min() <= rcpsp.start[succ].min());
            }
        }
        for (int r = 0; r < rcpsp.nResources; r++) {
            for (int i = 0; i < rcpsp.horizon; i++) {
                int cumulative = 0;
                for (int a = 0; a < rcpsp.nActivities; a++) {
                    if (rcpsp.start[a].min() <= i && i < rcpsp.start[a].min() + rcpsp.duration[a]) {
                        cumulative += rcpsp.consumption[r][a];
                    }
                }
                assertTrue(String.format("Exceeding capacity %d for resource %d at time %d", rcpsp.capa[r], r, i), cumulative <= rcpsp.capa[r]);
            }
        }
        return java.util.Arrays.stream(rcpsp.end).map(IntVar::min).max(Integer::compareTo).orElse(Integer.MAX_VALUE);
    }

    @GradeClass(totalValue = 1, defaultCpuTimeout = 1000)
    @RunWith(Parameterized.class)
    @Parameterized.UseParametersRunnerFactory(GradingRunnerWithParametersFactory.class)
    public static class FindOptimalityTest {

        RCPSP model;
        int bestSolution;

        @Parameterized.Parameters
        public static Object[][] instance() {
            return new Object[][] {
                    {"data/rcpsp/j30_1_1.rcp", 43},
                    {"data/rcpsp/j30_1_2.rcp", 47},
                    {"data/rcpsp/j30_1_3.rcp", 47},
            };
        }

        public FindOptimalityTest(String instance, int bestSolution) {
            model = new RCPSP(instance);
            this.bestSolution = bestSolution;
        }

        @Test(timeout = 1000)
        @Grade(cpuTimeout = 400, customPermissions = DataPermissionFactory.class)
        public void testOptimality() {
            try {
                model.buildModel();
                AtomicInteger foundObjective = new AtomicInteger();
                model.dfs.onSolution(() -> {
                    foundObjective.set(assertValidSolution(model));
                });
                SearchStatistics searchStatistics = model.solve(false);
                assertTrue("You did not find any solution", searchStatistics.numberOfSolutions() >= 1);
                assertTrue("You need to perform some kind of search", searchStatistics.numberOfNodes() >= 1);
                assertEquals(bestSolution, foundObjective.get());
            } catch (InconsistencyException | NullPointerException e) {
                fail("No inconsistency should happen when creating the constraints and performing the search " + e);
            } catch (NotImplementedException e) {
                NotImplementedExceptionAssume.fail(e);
            }
        }
    }

    @GradeClass(totalValue = 1, defaultCpuTimeout = 1000)
    @RunWith(Parameterized.class)
    @Parameterized.UseParametersRunnerFactory(GradingRunnerWithParametersFactory.class)
    public static class FindFirstSolTest {

        RCPSP model;

        @Parameterized.Parameters
        public static Object[] instance() {
            return new Object[] {
                    "data/rcpsp/j30_1_1.rcp",
                    "data/rcpsp/j30_1_2.rcp",
                    "data/rcpsp/j30_1_3.rcp",
                    "data/rcpsp/j60_1_1.rcp",
                    "data/rcpsp/j60_1_2.rcp",
                    "data/rcpsp/j60_1_3.rcp",
                    "data/rcpsp/j90_1_1.rcp",
                    "data/rcpsp/j90_1_2.rcp",
                    "data/rcpsp/j90_1_3.rcp",
                    "data/rcpsp/j120_1_1.rcp",
                    "data/rcpsp/j120_1_2.rcp",
                    "data/rcpsp/j120_1_3.rcp",
            };
        }

        public FindFirstSolTest(String instance) {
            model = new RCPSP(instance);
        }

        @Test(timeout = 2000)
        @Grade(cpuTimeout = 1000, customPermissions = DataPermissionFactory.class)
        public void testFirstSol() {
            try {
                model.buildModel();
                model.dfs.onSolution(() -> {
                    assertValidSolution(model);
                });
                SearchStatistics searchStatistics = model.solve(false, s -> s.numberOfSolutions() >= 1);
                assertTrue("You did not find any solution", searchStatistics.numberOfSolutions() >= 1);
                assertTrue("You need to perform some kind of search", searchStatistics.numberOfNodes() >= 1);
            } catch (InconsistencyException | NullPointerException e) {
                fail("No inconsistency should happen when creating the constraints and performing the search " + e);
            } catch (NotImplementedException e) {
                NotImplementedExceptionAssume.fail(e);
            }
        }
    }


}
