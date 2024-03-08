/*
 * mini-cp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License  v3
 * as published by the Free Software Foundation.
 *
 * mini-cp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 * See the GNU Lesser General Public License  for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with mini-cp. If not, see http://www.gnu.org/licenses/lgpl-3.0.en.html
 *
 * Copyright (c)  2018. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */

package minicp.engine.constraints;

import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.SearchStatistics;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import minicp.util.NotImplementedExceptionAssume;
import org.javagrader.Allow;
import org.javagrader.Forbid;
import org.javagrader.Grade;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.IntStream;
import java.time.Duration;
import java.util.Random;
import java.util.stream.Stream;

import static minicp.cp.BranchingScheme.firstFail;
import static minicp.cp.Factory.*;
import static org.junit.jupiter.api.Assertions.*;

@Grade(cpuTimeout = 1)
@Forbid("minicp.engine.constraints.Element2D")
public class Element1DTest {

    public static Stream<String> getSolver() {
        return Stream.of("Trailer", "Copier");
    }

    private static Solver solver(String type) {
        return type.equals("Copier") ? makeSolver(true) : makeSolver(false);
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void element1dInit(String solver) {
        try {

            Solver cp = solver(solver);
            IntVar y = makeIntVar(cp, -3, 10);
            IntVar z = makeIntVar(cp, -20, 20);

            int[] T = new int[]{3, 2, 1, -1, 0};

            new Element1D(T, y, z).post();

            assertEquals(0, y.min());
            assertEquals(4, y.max());


            assertEquals(-1, z.min());
            assertEquals(3, z.max());

            z.removeAbove(1);
            cp.fixPoint();

            assertEquals(2, y.min());


            y.remove(3);
            cp.fixPoint();

            assertEquals(1, z.max());
            assertEquals(0, z.min());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void element1dTest1(String solver) {
        try {

            Solver cp = solver(solver);
            IntVar y = makeIntVar(cp, -3, 10);
            IntVar z = makeIntVar(cp, -20, 20);

            int[] T = new int[]{3, 2, 1, -1, 0};

            new Element1D(T, y, z).post();

            assertEquals(0, y.min());
            assertEquals(4, y.max());


            assertEquals(-1, z.min());
            assertEquals(3, z.max());

            z.removeAbove(1);
            cp.fixPoint();

            assertEquals(2, y.min());


            y.remove(3);
            cp.fixPoint();

            assertEquals(1, z.max());
            assertEquals(0, z.min());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void element1dTest2(String solver) {
        try {

            Solver cp = solver(solver);
            IntVar y = makeIntVar(cp, -3, 10);
            IntVar z = makeIntVar(cp, -20, 40);

            int[] T = new int[]{3, 2, 1, -1, 0};

            new Element1D(T, y, z).post();

            DFSearch dfs = makeDfs(cp, firstFail(y, z));
            dfs.onSolution(() ->
                    assertEquals(T[y.min()], z.min())
            );
            SearchStatistics stats = dfs.solve();

            assertEquals(5, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void element1dTest3(String solver) {
        try {

            Solver cp = solver(solver);
            IntVar y = makeIntVar(cp, 0, 4);
            IntVar z = makeIntVar(cp, -1, 3);


            int[] T = new int[]{3, 2, 1, -1, 0};

            Element1D element1D = new Element1D(T, y, z);
            element1D.post();

            y.remove(3); //T[3]=-1
            y.remove(0); //T[0]=3

            element1D.propagate();

            assertEquals(0, z.min());
            assertEquals(2, z.max());
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void element1dTest4(String solver) {
        try {

            Solver cp = solver(solver);
            IntVar y = makeIntVar(cp, 0, 4);
            IntVar z = makeIntVar(cp, -1, 3);

            int[] T = new int[]{3, 2, 1, -1, 0};

            Element1D element1D = new Element1D(T, y, z);
            element1D.post();

            z.remove(3); // new max is 2
            z.remove(-1); // new min is 0
            element1D.propagate();

            assertFalse(y.contains(0));
            assertFalse(y.contains(3));
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void element1dTest5(String solver) {
        try {

            Solver cp = solver(solver);
            IntVar y = makeIntVar(cp, -1, 16);
            IntVar z = makeIntVar(cp, -1, 16);
            // permutation of { 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4 }
            int[] T = { 0, 3, 2, 2, 0, 4, 1, 4, 2, 1, 1, 0, 3, 3, 4 };

            Element1D element1D = new Element1D(T, y, z);
            element1D.post();

            assertEquals(y.size(), T.length);
            assertEquals(z.size(), 5);
            assertEquals(z.min(), 0);
            assertEquals(z.max(), 4);

            cp.getStateManager().saveState();
            y.removeAbove(0);
            z.remove(T[0]);
            try {
                element1D.propagate();
                fail();
            } catch (InconsistencyException ignored) {

            }
            cp.getStateManager().restoreState();

            // permutation of { 0, 1, ..., 14 }
            int[] indices = { 3, 4, 7, 6, 10, 12, 13, 11, 1, 14, 5, 8, 0, 9, 2 };

            int[] valCount = new int[z.size()]; // valCount[i] == number of occurrences of i in z
            for (int val : T) {
                ++valCount[val];
            }

            // Skip the first index, as z will be fixed to T[indices[0]]
            // after the for loop.
            for (int i = 1; i < indices.length; i++) {
                final int index = indices[i];
                final int val = T[index];
                assertTrue(y.contains(index));
                y.remove(index);
                assertEquals(y.size(), T.length - i);
                element1D.propagate();
                --valCount[val]; // one occurrence has been removed
                assertTrue(valCount[val] >= 0);
                if (valCount[val] > 0 || (val > z.min() && val < z.max())) {
                    assertTrue(z.contains(val), String.format("z should still contain %d", val));
                } else if (valCount[val] == 0) {
                    if (val == z.min()) {
                        for (int v = val; v <= valCount.length && valCount[v] == 0; v++) {
                            assertFalse(z.contains(v), String.format("min value update: z does not contain anymore %d", v));
                        }
                    }
                    if (val == z.max()) {
                        for (int v = val; v >= 0 && valCount[v] == 0; v--) {
                            assertFalse(z.contains(v), String.format("max value update: z does not contain anymore %d", v));
                        }
                    }
                }
            }

            final int index = indices[0];
            final int val = T[index];
            for (int v = 0; v < valCount.length; v++) {
                if (v == val) {
                    assertEquals(valCount[v], 1);
                } else {
                    assertEquals(valCount[v], 0);
                }
            }
            assertTrue(y.isFixed());
            assertEquals(y.min(), index);
            assertTrue(z.isFixed());
            assertEquals(z.min(), val);

        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    @Allow("java.lang.Thread")
    public void element1dTest6(String solver) {
        try {

            int n = 1_000_000;
            int w = 1000;
            Solver cp = solver(solver);
            IntVar y = makeIntVar(cp, n/2 - w/2, n/2 + w/2);
            IntVar z = makeIntVar(cp, 0, n-1);

            int[] T = IntStream.range(0, n).toArray();
            cp.post(new Element1D(T, y, z));

            Random random = new Random(42);
            assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
                for (int i = 0 ; i < w ; ++i) {
                    if (random.nextBoolean())
                        cp.post(notEqual(y, y.min()));
                    else
                        cp.post(notEqual(y, y.max()));
                    // the array is sorted in increasing order here
                    assertEquals(T[y.min()], z.min());
                    assertEquals(T[y.max()], z.max());
                }
            }, "Are you using the StateInt low and up? You should use them to iterate over a part of the array instead of its entirety");

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }
}
