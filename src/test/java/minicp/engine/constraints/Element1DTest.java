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

import minicp.engine.SolverTest;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.SearchStatistics;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import minicp.util.NotImplementedExceptionAssume;
import org.javagrader.Grade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.IntStream;

import static minicp.cp.BranchingScheme.firstFail;
import static minicp.cp.Factory.*;
import static org.junit.jupiter.api.Assertions.*;

@Grade(cpuTimeout = 1)
public class Element1DTest extends SolverTest {

    @ParameterizedTest
    @MethodSource("getSolver")
    public void element1dTest1(Solver cp) {

        try {

            IntVar y = makeIntVar(cp, -3, 10);
            IntVar z = makeIntVar(cp, 2, 40);

            int[] T = new int[]{9, 8, 7, 5, 6};

            cp.post(new Element1D(T, y, z));

            assertEquals(0, y.min());
            assertEquals(4, y.max());


            assertEquals(5, z.min());
            assertEquals(9, z.max());

            z.removeAbove(7);
            cp.fixPoint();

            assertEquals(2, y.min());


            y.remove(3);
            cp.fixPoint();

            assertEquals(7, z.max());
            assertEquals(6, z.min());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void element1dTest2(Solver cp) {

        try {

            IntVar y = makeIntVar(cp, -3, 10);
            IntVar z = makeIntVar(cp, -20, 40);

            int[] T = new int[]{9, 8, 7, 5, 6};

            cp.post(new Element1D(T, y, z));

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
    public void element1dTest3(Solver cp) {
        try {

            IntVar y = makeIntVar(cp, 0, 4);
            IntVar z = makeIntVar(cp, 5, 9);


            int[] T = new int[]{9, 8, 7, 5, 6};

            cp.post(new Element1D(T, y, z));

            y.remove(3); //T[4]=5
            y.remove(0); //T[0]=9

            cp.fixPoint();

            assertEquals(6, z.min());
            assertEquals(8, z.max());
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void element1dTest4(Solver cp) {
        try {

            IntVar y = makeIntVar(cp, 0, 4);
            IntVar z = makeIntVar(cp, 5, 9);

            int[] T = new int[]{9, 8, 7, 5, 6};

            cp.post(new Element1D(T, y, z));

            z.remove(9); //new max is 8
            z.remove(5); //new min is 6
            cp.fixPoint();

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
    public void element1dTest5(Solver cp) {
        try {
            IntVar y = makeIntVar(cp, -1, 16);
            IntVar z = makeIntVar(cp, -1, 16);
            // permutation of { 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4 }
            int[] T = { 0, 3, 2, 2, 0, 4, 1, 4, 2, 1, 1, 0, 3, 3, 4 };

            cp.post(new Element1D(T, y, z));

            assertEquals(y.size(), T.length);
            assertEquals(z.size(), 5);
            assertEquals(z.min(), 0);
            assertEquals(z.max(), 4);

            cp.getStateManager().saveState();
            y.removeAbove(0);
            z.remove(T[0]);
            try {
                cp.fixPoint();
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
                cp.fixPoint();
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

}
