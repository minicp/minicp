package minicp.examples;

import minicp.engine.core.IntVar;
import minicp.search.SearchStatistics;
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import org.javagrader.ConditionalOrderingExtension;
import org.javagrader.Grade;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@Grade
@ExtendWith(ConditionalOrderingExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EternityTest {

    @Grade(value = 1, cpuTimeout = 1)
    @Test
    @Order(1)
    public void testTableContent() {
        try {
            String instance = "data/eternity/brendan/pieces_03x03.txt";
            Eternity model = new Eternity(instance);
            model.buildModel();
            // check the content of table
            int[][] table = model.table;
            // find the column where the student put the id
            // this instance contains side types between 0 and 4 (included)
            int idColumn = -1;
            for (int i = 0 ; i < table.length && idColumn == -1 ; ++i) {
                for (int j = 0 ; j < table[i].length && idColumn == -1 ; ++j) {
                    if (table[i][j] < 0 || table[i][j] > 4) {
                        idColumn = j;
                    }
                }
            }
            // expected permutations
            Map<List<Integer>, Integer> permutation = new HashMap<>();
            permutation.put(Arrays.asList(0, 0, 1, 1), 0); // piece 1
            permutation.put(Arrays.asList(0, 1, 1, 0), 0);
            permutation.put(Arrays.asList(1, 1, 0, 0), 0);
            permutation.put(Arrays.asList(1, 0, 0, 1), 0);
            permutation.put(Arrays.asList(0, 0, 1, 2), 0); // piece 2
            permutation.put(Arrays.asList(0, 1, 2, 0), 0);
            permutation.put(Arrays.asList(1, 2, 0, 0), 0);
            permutation.put(Arrays.asList(2, 0, 0, 1), 0);
            permutation.put(Arrays.asList(0, 0, 2, 1), 0); // piece 3
            permutation.put(Arrays.asList(0, 2, 1, 0), 0);
            permutation.put(Arrays.asList(2, 1, 0, 0), 0);
            permutation.put(Arrays.asList(1, 0, 0, 2), 0);
            permutation.put(Arrays.asList(0, 0, 2, 2), 0); // piece 4
            permutation.put(Arrays.asList(0, 2, 2, 0), 0);
            permutation.put(Arrays.asList(2, 2, 0, 0), 0);
            permutation.put(Arrays.asList(2, 0, 0, 2), 0);
            permutation.put(Arrays.asList(0, 1, 3, 2), 0); // piece 6
            permutation.put(Arrays.asList(1, 3, 2, 0), 0);
            permutation.put(Arrays.asList(3, 2, 0, 1), 0);
            permutation.put(Arrays.asList(2, 0, 1, 3), 0);
            permutation.put(Arrays.asList(0, 1, 4, 1), 0); // piece 7
            permutation.put(Arrays.asList(1, 4, 1, 0), 0);
            permutation.put(Arrays.asList(4, 1, 0, 1), 0);
            permutation.put(Arrays.asList(1, 0, 1, 4), 0);
            permutation.put(Arrays.asList(0, 2, 3, 1), 0); // piece 8
            permutation.put(Arrays.asList(2, 3, 1, 0), 0);
            permutation.put(Arrays.asList(3, 1, 0, 2), 0);
            permutation.put(Arrays.asList(1, 0, 2, 3), 0);
            permutation.put(Arrays.asList(0, 2, 4, 2), 0); // piece 9
            permutation.put(Arrays.asList(2, 4, 2, 0), 0);
            permutation.put(Arrays.asList(4, 2, 0, 2), 0);
            permutation.put(Arrays.asList(2, 0, 2, 4), 0);
            permutation.put(Arrays.asList(3, 3, 4, 4), 0); // piece 10
            permutation.put(Arrays.asList(3, 4, 4, 3), 0);
            permutation.put(Arrays.asList(4, 4, 3, 3), 0);
            permutation.put(Arrays.asList(4, 3, 3, 4), 0);
            // loop through the table, creates the permutation and check if it is an expected one
            for (int[] ints : table) {
                ArrayList<Integer> entry = new ArrayList<>();
                for (int j = 0; j < ints.length; ++j) {
                    if (j != idColumn) {
                        entry.add(ints[j]);
                    }
                }
                assertTrue(permutation.containsKey(entry), "You generated the permutation " + entry + " which should not appear within the table");
                // piece has been seen, increment the counter
                permutation.put(entry, permutation.get(entry) + 1);
            }
            for (List<Integer> perm: permutation.keySet()) {
                int nSeen = permutation.get(perm);
                assertTrue(nSeen > 0, "The permutation " + perm + " should appear within your table but does not");
            }
        } catch (InconsistencyException | NullPointerException e) {
            fail("No inconsistency should happen when creating the constraints and performing the search " + e);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    private static class Solution {
        final int numPieces;
        final List<Integer> allEntries;
        Solution(Eternity e) {
            this.numPieces = e.pieces.length;
            allEntries = new ArrayList<>();
            for (IntVar[] xs: new IntVar[][] {
                    Eternity.flatten(e.id),
                    Eternity.flatten(e.u),
                    Eternity.flatten(e.l),
                    Eternity.flatten(e.r),
                    Eternity.flatten(e.d),
            }) {
                for (IntVar x: xs)
                    allEntries.add(x.min());
            }
            check(e);
        }

        private void check(Eternity e) {
            HashSet<Integer> usedIds = new HashSet<>();
            for (IntVar[] xs: new IntVar[][] {
                    Eternity.flatten(e.id),
                    Eternity.flatten(e.u),
                    Eternity.flatten(e.l),
                    Eternity.flatten(e.r),
                    Eternity.flatten(e.d),
            }) {
                for (IntVar x: xs)
                    assertTrue(x.isFixed(), "You have an unfixed variable in your solution");
            }
            for (int i = 0; i < e.n; i++) {
                for (int j = 0; j < e.m; j++) {
                    assertTrue(0 <= e.id[i][j].min() && e.id[i][j].min() < e.pieces.length,
                            String.format("Tile at position %d,%d has incorrect id %d. Ids should range between %d and %d", i, j, e.id[i][j].min(), 0, numPieces - 1));
                    assertFalse(usedIds.contains(e.id[i][j].min()),
                            String.format("Tile at position %d,%d with id %d appears more than once", i, j, e.id[i][j].min()));
                    usedIds.add(e.id[i][j].min());
                    if (i == 0) {
                        assertEquals(0, e.u[i][j].min(),
                                String.format("Tile at position %d,%d with id %d should have 0 facing up", i, j, e.id[i][j].min()));
                    } else if (i == e.n - 1) {
                        assertEquals(0, e.d[i][j].min(),
                                String.format("Tile at position %d,%d with id %d should have 0 facing down", i, j, e.id[i][j].min()));
                    } else {
                        assertEquals(e.d[i][j].min(), e.u[i + 1][j].min(),
                                String.format("Tiles at positions %d,%d and %d,%d with ids %d and %d should have the same value for down and up respectively.", i, j, i + 1, j, e.id[i][j].min(), e.id[i + 1][j].min()));
                    }
                    if (j == 0) {
                        assertEquals(0, e.l[i][j].min(),
                                String.format("Tile at position %d,%d with id %d should have 0 facing left", i, j, e.id[i][j].min()));
                    } else if (j == e.m - 1) {
                        assertEquals(0, e.r[i][j].min(),
                                String.format("Tile at position %d,%d with id %d should have 0 facing right", i, j, e.id[i][j].min()));
                    } else {
                        assertEquals(e.r[i][j].min(), e.l[i][j + 1].min(),
                                String.format("Tiles at positions %d,%d and %d,%d with ids %d and %d should have the same value for right and left respectively.", i, j, i, j + 1, e.id[i][j].min(), e.id[i][j + 1].min()));
                    }
                    String expected = Arrays.stream(e.pieces[e.id[i][j].min()]).mapToObj(Integer::toString).collect(Collectors.joining(","));
                    expected += "," + expected;
                    final int[] curPiece = new int[] {e.id[i][j].min(), e.u[i][j].min(), e.r[i][j].min(), e.d[i][j].min(), e.l[i][j].min()};
                    String actual = IntStream.range(1, 5).mapToObj(c -> Integer.toString(curPiece[c])).collect(Collectors.joining(","));
                    assertTrue(expected.contains(actual),
                            String.format("Tile at position %d,%d with id %d does not have a valid rotation", i, j, e.id[i][j].min()));
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Solution solution = (Solution) o;
            return numPieces == solution.numPieces && Objects.equals(allEntries, solution.allEntries);
        }

        @Override
        public int hashCode() {
            return Objects.hash(numPieces, allEntries);
        }
    }

    @Grade(value = 1, cpuTimeout = 1)
    @Order(2)
    @Test
    @Tag("slow")
    public void testTinyInstance() {
        try {
            String instance = "data/eternity/brendan/pieces_03x03.txt";
            Eternity model = new Eternity(instance);
            model.buildModel();
            AtomicInteger nSol = new AtomicInteger(0);
            model.dfs.onSolution(() -> {
                nSol.incrementAndGet();
                int id = model.pieces.length - 1; // piece with no 0 in its corner
                assertTrue(model.id[1][1].isFixed());
                assertEquals(id, model.id[1][1].min(),
                        String.format("only the piece with id=%d should be placed in the middle " +
                                "(it is the only one with no 0 in its corner)", id));
                new Solution(model);
            });
            SearchStatistics searchStatistics = model.solve(false);
            assertTrue(searchStatistics.isCompleted(), "Your search needs to be completed");
            assertEquals(16, searchStatistics.numberOfSolutions(), String.format("You need to find all %d valid solutions", 16));
            assertEquals(16, nSol.get(), String.format("You need to find all %d valid solutions", 16));
        } catch (InconsistencyException | NullPointerException e) {
            fail("No inconsistency should happen when creating the constraints and performing the search " + e);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Grade(value = 1, cpuTimeout = 3)
    @Order(3)
    @ParameterizedTest
    @CsvSource({
            "data/eternity/brendan/pieces_03x03.txt",
            "data/eternity/brendan/pieces_04x03.txt",
            "data/eternity/brendan/pieces_04x04.txt",
            "data/eternity/brendan/pieces_05x03.txt",
            "data/eternity/brendan/pieces_05x04.txt",
            "data/eternity/brendan/pieces_05x05.txt",
            "data/eternity/brendan/pieces_06x03.txt",
            "data/eternity/brendan/pieces_06x04.txt",
    })
    @Tag("slow")
    public void testFirstSolution(String instance) {
        try {
            Eternity model = new Eternity(instance);
            model.buildModel();
            AtomicInteger nSol = new AtomicInteger(0);
            model.dfs.onSolution(() -> {
                nSol.incrementAndGet();
                new Solution(model);
            });
            SearchStatistics searchStatistics = model.solve(false, s -> s.numberOfSolutions() >= 1);
            assertEquals(nSol.get(), searchStatistics.numberOfSolutions(), "You need to find one valid solution");
        } catch (InconsistencyException | NullPointerException e) {
            fail("No inconsistency should happen when creating the constraints and performing the search " + e);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Grade(value = 10, cpuTimeout = 15)
    @Order(4)
    @Test
    @Tag("slow")
    public void testFirstFewSolution() {
        try {
            int nSol = 3;
            String instance = "data/eternity/eternity7x7.txt";
            Eternity model = new Eternity(instance);
            model.buildModel();
            AtomicInteger nSolFound = new AtomicInteger(0);
            HashSet<Solution> sols = new HashSet<>();
            model.dfs.onSolution(() -> {
                nSolFound.incrementAndGet();
                Solution s = new Solution(model);
                sols.add(s);
            });
            SearchStatistics searchStatistics = model.solve(false, s -> s.numberOfSolutions() == nSol);
            assertFalse(searchStatistics.isCompleted());
            assertEquals(nSol, searchStatistics.numberOfSolutions(), String.format("You need to find %d valid solutions", nSol));
            assertEquals(nSol, nSolFound.get(), String.format("You need to find %d valid solutions", nSol));
            assertEquals(nSol, sols.size(), String.format("You need to find %d valid different solutions", nSol));
            assertTrue(searchStatistics.numberOfNodes() > 100, "You need to perform some kind of search");
            assertTrue(searchStatistics.numberOfFailures() > 1, "You need to perform some kind of search");
        } catch (InconsistencyException | NullPointerException e) {
            fail("No inconsistency should happen when creating the constraints and performing the search " + e);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

}
