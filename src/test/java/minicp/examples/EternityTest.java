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

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@Grade
@ExtendWith(ConditionalOrderingExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EternityTest {

    static class Solution {
        final int numPieces;
        Solution(Eternity e) {
            this.numPieces = e.pieces.length;
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
    }

    @Grade(value = 3, cpuTimeout = 1)
    @Order(1)
    @Test
    @Tag("slow")
    public void testTinyInstance() {
        try {
            String instance = "data/eternity/brendan/pieces_03x03.txt";
            Eternity model = new Eternity(instance);
            model.buildModel();
            model.dfs.onSolution(() -> {
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
        } catch (InconsistencyException | NullPointerException e) {
            fail("No inconsistency should happen when creating the constraints and performing the search " + e);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Grade(value = 7, cpuTimeout = 15)
    @Order(2)
    @Test
    @Tag("slow")
    public void testFirstFewSolution() {
        try {
            int nSol = 3;
            String instance = "data/eternity/eternity7x7.txt";
            Eternity model = new Eternity(instance);
            model.buildModel();
            model.dfs.onSolution(() -> {
                new Solution(model);
            });
            SearchStatistics searchStatistics = model.solve(false, s -> s.numberOfSolutions() == nSol);
            assertFalse(searchStatistics.isCompleted());
            assertEquals(nSol, searchStatistics.numberOfSolutions(), String.format("You need to find %d valid solutions", nSol));
            assertTrue(searchStatistics.numberOfNodes() > 100, "You need to perform some kind of search");
            assertTrue(searchStatistics.numberOfFailures() > 1, "You need to perform some kind of search");
        } catch (InconsistencyException | NullPointerException e) {
            fail("No inconsistency should happen when creating the constraints and performing the search " + e);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

}
