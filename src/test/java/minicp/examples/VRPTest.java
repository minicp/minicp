package minicp.examples;

import minicp.engine.core.IntVar;
import minicp.search.SearchStatistics;
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import minicp.util.io.InputReader;
import org.javagrader.ConditionalOrderingExtension;
import org.javagrader.Grade;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Grade
@ExtendWith(ConditionalOrderingExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VRPTest {

    private final static int nVehicle = 3;

    /**
     * Verifies a solution to a {@link VRP} and returns its objective value
     *
     * @param vrp Vehicle Routing Problem
     * @return distance traveled by the path defined in the successors {@link VRP#succ}
     */
    public int assertValidSolution(VRP vrp) {
        int totalObjective = 0;
        for (IntVar s : vrp.succ)
            assertTrue(s.isFixed(), "A node does not have a fixed successor");
        assertTrue(vrp.totalDist.isFixed(), "The total distance is not fixed");
        for (int i = 0; i < vrp.n; i++)
            assertNotEquals(vrp.succ[i].min(), i, "No self loops");
        int i = vrp.succ[0].min();
        HashSet<Integer> visited = new HashSet<>();
        while (!visited.contains(i)) {
            totalObjective += vrp.distanceMatrix[i][vrp.succ[i].min()];
            visited.add(i);
            i = vrp.succ[i].min();
        }
        assertEquals(visited.size(), vrp.n, "Not all nodes were visited");
        assertEquals(totalObjective, vrp.totalDist.min(), "Your total distance value is not correctly computed");
        return totalObjective;
    }

    @Grade(cpuTimeout = 500, unit = TimeUnit.MILLISECONDS)
    @ParameterizedTest(name = "[{index}] {0} - {1} vehicle(s)")
    @MethodSource("getInstanceAndVehicles")
    @Order(1)
    public void testDistanceMatrix(String file, int nVehicle) {
        try {
            InputReader reader = new InputReader(file);
            int n = reader.getInt();
            int[][] initialDistanceMatrix = reader.getMatrix(n, n);
            VRP vrp = new VRP(file, nVehicle);
            vrp.buildModel();
            int[][] distanceMatrix = vrp.distanceMatrix;
            for (int depot = 0; depot < nVehicle; ++depot) {
                // depot -> depot
                for (int otherDepot = 0; otherDepot < nVehicle; ++otherDepot) {
                    assertEquals(0, distanceMatrix[depot][otherDepot],
                            String.format("the distance between two depots should be zero (between node %d and %d)",
                                    depot, otherDepot));
                }
                // depot -> city
                for (int j = 1; j < n; ++j) {
                    int expected = initialDistanceMatrix[0][j];
                    int actual = distanceMatrix[depot][j + nVehicle - 1];
                    assertEquals(expected, actual, String.format("incorrect distance between depot %d and city %d", depot, j));
                }
                // city -> depot
                for (int j = 1; j < n; ++j) {
                    int expected = initialDistanceMatrix[j][0];
                    int actual = distanceMatrix[j + nVehicle - 1][depot];
                    assertEquals(expected, actual, String.format("incorrect distance between city %d and depot %d", j, depot));
                }
            }
            // city -> city
            for (int i = 1; i < n; ++i) {
                for (int j = 1; j < n; ++j) {
                    int expected = initialDistanceMatrix[i][j];
                    int actual = distanceMatrix[nVehicle - 1 + i][nVehicle - 1 + j];
                    assertEquals(expected, actual, String.format("incorrect distance between city %d and %d", i, j));
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            fail("Failed to read your matrix at a supposed valid index " + e);
        } catch (InconsistencyException e) {
            fail("The instance given as input is valid but you have thrown an inconsistency");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Order(2)
    @Grade(cpuTimeout = 500, unit = TimeUnit.MILLISECONDS)
    @ParameterizedTest
    @MethodSource("getInstances")
    public void testFirstFewSolutionsTotalDistance(String instance) {
        int nSolutions = 3;
        try {
            VRP vrp = new VRP(instance, nVehicle);
            vrp.buildModel();
            AtomicInteger sol = new AtomicInteger(Integer.MAX_VALUE);
            vrp.dfs.onSolution(() -> {
                int value = assertValidSolution(vrp);
                assertTrue(value < sol.get());
                sol.set(value);
            });
            SearchStatistics customSearchStats = vrp.solve(false, statistics -> statistics.numberOfSolutions() >= nSolutions);
            assertTrue(customSearchStats.numberOfSolutions() > 2);
        } catch (InconsistencyException | NullPointerException e) {
            fail("No inconsistency should happen when creating the constraints and performing the search " + e);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Order(2)
    @Grade(cpuTimeout = 2)
    @ParameterizedTest
    @CsvSource({
            "data/tsp/tsp_15.txt, 291"
    })
    @Tag("slow")
    public void testOptimality(String instance, int bestSol) {
        try {
            VRP vrp = new VRP(instance, nVehicle);
            vrp.buildModel();
            AtomicInteger sol = new AtomicInteger(Integer.MAX_VALUE);
            vrp.dfs.onSolution(() -> {
                int value = assertValidSolution(vrp);
                assertTrue(value < sol.get());
                sol.set(value);
            });
            SearchStatistics customSearchStats = vrp.solve(false);
            assertTrue(customSearchStats.isCompleted(), "You need to explore the entire search space");
            assertEquals(bestSol, sol.get(), "You did not find the optimal solution");
            assertTrue(customSearchStats.numberOfNodes() > 1042,
                    "You need to perform some kind of search");
        } catch (InconsistencyException | NullPointerException e) {
            fail("No inconsistency should happen when creating the constraints and performing the search " + e);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    private static class InstanceParam {
        File file;
        int nVehicle;

        public InstanceParam(File file, int nVehicle) {
            this.file = file;
            this.nVehicle = nVehicle;
        }
    }

    public static Stream<Arguments> getInstances() {
        return Stream.of(Objects.requireNonNull(new File("data/tsp/").listFiles()))
                .filter(file -> !file.isDirectory())
                .map(file -> arguments(named(file.getName(), file.getPath())));
    }

    public static Stream<Arguments> getInstanceAndVehicles() {
        return Stream.of(Objects.requireNonNull(new File("data/tsp/").listFiles()))
                .filter(file -> !file.isDirectory())
                .map(s -> {
                    List<InstanceParam> l = new ArrayList<>();
                    for (int v = 1; v < 4 ; ++v) {
                        l.add(new InstanceParam(s, v));
                    }
                    return l;
                })
                .flatMap(Collection::stream)
                .map(p -> arguments(named(p.file.getName(), p.file.getPath()), p.nVehicle));
    }
}
