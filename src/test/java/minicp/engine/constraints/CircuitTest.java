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
import minicp.state.StateInt;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import minicp.util.NotImplementedExceptionAssume;
import org.javagrader.Grade;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.IntStream;

import static minicp.cp.BranchingScheme.*;
import static minicp.cp.Factory.*;
import static org.junit.jupiter.api.Assertions.*;

@Grade(cpuTimeout = 1)
public class CircuitTest extends SolverTest {


    int[] validCircuit1 = new int[]{1, 2, 3, 4, 5, 0};
    int[] validCircuit2 = new int[]{1, 2, 3, 4, 5, 0};

    int[] invalidCircuit1 = new int[]{1, 2, 3, 4, 5, 2};
    int[] invalidCircuit2 = new int[]{1, 2, 0, 4, 5, 3};

    public static boolean checkHamiltonian(int[] circuit) {
        int[] count = new int[circuit.length];
        for (int v : circuit) {
            count[v]++;
            if (count[v] > 1) return false;
        }
        boolean[] visited = new boolean[circuit.length];
        int c = circuit[0];
        for (int i = 0; i < circuit.length; i++) {
            visited[c] = true;
            c = circuit[c];
        }
        for (int i = 0; i < circuit.length; i++) {
            if (!visited[i]) return false;
        }
        return true;
    }

    public static IntVar[] instantiate(Solver cp, int[] circuit) {
        IntVar[] x = new IntVar[circuit.length];
        for (int i = 0; i < circuit.length; i++) {
            x[i] = makeIntVar(cp, circuit[i], circuit[i]);
        }
        return x;
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testValidIds(Solver cp) {
        try {
            IntVar[] x = makeIntVarArray(cp, 5, -10, 10);
            cp.post(new Circuit(x));
            for (int i = 0 ; i < 5 ; ++i) {
                assertFalse(x[i].contains(i), "Can the successor of variable x[i] be i?");
                assertEquals(i == 0 ? 1 : 0, x[i].min(), "A successor is an index within an array");
                assertEquals(i == 4 ? 3 : 4, x[i].max(), "A successor is an index within an array");
                assertEquals(4, x[i].size());
            }
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testValidCircuit(Solver cp) {

        try {
            cp.post(new Circuit(instantiate(cp, validCircuit1)));
            cp.post(new Circuit(instantiate(cp, validCircuit2)));
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testInvalidCircuit1(Solver cp) {
        try {
            cp.post(new Circuit(instantiate(cp, invalidCircuit1)));
            fail("You should have thrown an inconsistency when given an invalid circuit");
        } catch (InconsistencyException ignored) {

        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testInvalidCircuit2(Solver cp) {
        try {
            cp.post(new Circuit(instantiate(cp, invalidCircuit2)));
            fail("You should have thrown an inconsistency when given an invalid circuit");
        } catch (InconsistencyException ignored) {

        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testPost(Solver cp) {
        int n = 10;
        int min = -1;
        int max = n + 1;
        IntVar[] x = makeIntVarArray(cp, n, min, max);
        try {
            cp.post(new Circuit(x));
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
        int[] dom = new int[n - 1];
        for (int index = 0; index < n; index++) {
            final int i = index;
            int len = x[i].fillArray(dom);
            assertEquals(n - 1, len);
            int[] expected = IntStream.range(0, n - 1).map(j -> j < i ? j : j + 1).toArray();
            Arrays.sort(dom);
            assertArrayEquals(expected, dom);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testAllSolutions(Solver cp) {
        try {
            IntVar[] x = makeIntVarArray(cp, 5, 5);
            cp.post(new Circuit(x));


            DFSearch dfs = makeDfs(cp, firstFail(x));

            dfs.onSolution(() -> {
                        int[] sol = new int[x.length];
                        for (int i = 0; i < x.length; i++) {
                            sol[i] = x[i].min();
                        }
                        assertTrue(checkHamiltonian(sol), "Solution is not an hamiltonian Circuit");
                    }
            );
            SearchStatistics stats = dfs.solve();
            assertEquals(24, stats.numberOfSolutions());
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testCompletesCycle(Solver cp) {
        IntVar[] x = makeIntVarArray(cp, 5, 5);
        Circuit circuit = new Circuit(x);
        try {
            cp.post(circuit);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

        DFSearch dfs = makeDfs(cp, () -> {
            IntVar v = null;
            int nU = 0;
            for (IntVar xi : x) {
                if (xi.isFixed()) {
                    continue;
                }
                nU++;
                if (v == null) {
                    v = xi;
                }
            }
            if (v == null) {
                return EMPTY;
            }
            final IntVar var = v;
            final int val = var.min();
            final int numUnfixed = nU;

            // some people use dest[i] = dest[j] instead of calling setValue
            // this compares the objects references to be sure that it is not the case
            for (int i = 0 ; i < x.length; ++i) {
                StateInt origI = circuit.orig[i];
                for (int j = 0 ; j < x.length; ++j) {
                    if (i != j) {
                        assertNotSame(origI, circuit.orig[j], "Use orig[i].setValue(...) to set the StateInt, not orig[i] = ...");
                    }
                    assertNotSame(origI, circuit.dest[j], "Use orig[i].setValue(...) to set the StateInt, not orig[i] = ...");
                }
            }
            for (int i = 0 ; i < x.length; ++i) {
                StateInt destI = circuit.dest[i];
                for (int j = 0 ; j < x.length; ++j) {
                    if (i != j) {
                        assertNotSame(destI, circuit.dest[j], "Use dest[i].setValue(...) to set the StateInt, not dest[i] = ...");
                    }
                    assertNotSame(destI, circuit.orig[j], "Use dest[i].setValue(...) to set the StateInt, not dest[i] = ...");
                }
            }

            assertTrue(numUnfixed > 1);

            return branch(
                    () -> {
                        assertDoesNotThrow(() -> cp.post(equal(var, val)));
                        if (numUnfixed == 2) {
                            for (IntVar xi : x) {
                                assertTrue(xi.isFixed());
                            }
                        }
                    },
                    () -> {
                        assertDoesNotThrow(() -> cp.post(notEqual(var, val)));
                        if (var.isFixed() && numUnfixed == 2) {
                            for (IntVar xi : x) {
                                assertTrue(xi.isFixed());
                            }
                        }
                    });
        });

        SearchStatistics stats = dfs.solve();
        assertEquals(24, stats.numberOfSolutions());
    }


}
