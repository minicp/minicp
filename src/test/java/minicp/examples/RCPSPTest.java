package minicp.examples;

import minicp.engine.core.IntVar;
import minicp.search.SearchStatistics;
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import org.javagrader.Grade;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Grade
public class RCPSPTest {

    public static int assertValidSolution(RCPSP rcpsp) {
        for (IntVar s: rcpsp.start)
            assertTrue(s.isFixed(), "You have an unfixed variable");
        for (IntVar e: rcpsp.end)
            assertTrue(e.isFixed(), "You have an unfixed variable");
        for (int a = 0; a < rcpsp.nActivities; a++) {
            for (int succ : rcpsp.successors[a]) {
                assertTrue(rcpsp.end[a].min() <= rcpsp.start[succ].min(), String.format("Activity %d must precede %d", a, succ));
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
                assertTrue(cumulative <= rcpsp.capa[r], String.format("Exceeding capacity %d for resource %d at time %d", rcpsp.capa[r], r, i));
            }
        }
        return java.util.Arrays.stream(rcpsp.end).map(IntVar::min).max(Integer::compareTo).orElse(Integer.MAX_VALUE);
    }

    @Grade(cpuTimeout = 2)
    @ParameterizedTest
    @CsvSource({
            "data/rcpsp/j30_1_1.rcp, 43",
            "data/rcpsp/j30_1_2.rcp, 47",
            "data/rcpsp/j30_1_3.rcp, 47",
    })
    public void testOptimality(String instance, int bestSolution) {
        try {
            RCPSP model = new RCPSP(instance);
            model.buildModel();
            AtomicInteger foundObjective = new AtomicInteger();
            model.dfs.onSolution(() -> {
                foundObjective.set(assertValidSolution(model));
            });
            SearchStatistics searchStatistics = model.solve(false);
            assertTrue(searchStatistics.numberOfSolutions() >= 1, "You did not find any solution");
            assertTrue(searchStatistics.numberOfNodes() >= 1, "You need to perform some kind of search");
            assertEquals(bestSolution, foundObjective.get());
        } catch (InconsistencyException | NullPointerException e) {
            fail("No inconsistency should happen when creating the constraints and performing the search " + e);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Grade(cpuTimeout = 2)
    @ParameterizedTest
    @MethodSource("getAllInstances")
    public void testFirstSolution(String instancePath) {
        try {
            RCPSP model = new RCPSP(instancePath);
            model.buildModel();
            model.dfs.onSolution(() -> {
                assertValidSolution(model);
            });
            SearchStatistics searchStatistics = model.solve(false, s -> s.numberOfSolutions() >= 1);
            assertTrue(searchStatistics.numberOfSolutions() >= 1,"You did not find any solution");
            assertTrue(searchStatistics.numberOfNodes() >= 1, "You need to perform some kind of search");
        } catch (InconsistencyException | NullPointerException e) {
            fail("No inconsistency should happen when creating the constraints and performing the search " + e);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }
    
    public static Stream<Arguments> getAllInstances() {
        return Stream.of(Objects.requireNonNull(new File("data/rcpsp/").listFiles()))
                .filter(file -> !file.isDirectory())
                .map(s -> arguments(named(s.getName(), s.getPath())));
    }

}
