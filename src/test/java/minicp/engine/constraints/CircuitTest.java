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

import com.github.guillaumederval.javagrading.GradeClass;
import minicp.engine.SolverTest;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.SearchStatistics;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import minicp.util.NotImplementedExceptionAssume;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

import static minicp.cp.BranchingScheme.*;
import static minicp.cp.Factory.*;
import static org.junit.Assert.*;

@GradeClass(totalValue = 1, defaultCpuTimeout = 1000)
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

    @Test
    public void testValidCircuit() {

        try {
            Solver cp = solverFactory.get();
            cp.post(new Circuit(instantiate(cp, validCircuit1)));
            cp.post(new Circuit(instantiate(cp, validCircuit2)));
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Test
    public void testInvalidCircuit() {
        try {
            try {
                Solver cp = solverFactory.get();
                cp.post(new Circuit(instantiate(cp, invalidCircuit1)));
                fail("should fail");
            } catch (InconsistencyException ignored) {
            }
            try {
                Solver cp = makeSolver();
                cp.post(new Circuit(instantiate(cp, invalidCircuit2)));
                fail("should fail");
            } catch (InconsistencyException ignored) {
            }
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void testPost() {
        int n = 10;
        int min = -1;
        int max = n + 1;
        Solver cp = makeSolver();
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

    @Test
    public void testAllSolutions() {

        try {
            Solver cp = solverFactory.get();
            IntVar[] x = makeIntVarArray(cp, 5, 5);
            cp.post(new Circuit(x));


            DFSearch dfs = makeDfs(cp, firstFail(x));

            dfs.onSolution(() -> {
                        int[] sol = new int[x.length];
                        for (int i = 0; i < x.length; i++) {
                            sol[i] = x[i].min();
                        }
                        assertTrue("Solution is not an hamiltonian Circuit", checkHamiltonian(sol));
                    }
            );
            SearchStatistics stats = dfs.solve();
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void testCompletesCycle() {
        Solver cp = solverFactory.get();
        IntVar[] x = makeIntVarArray(cp, 5, 5);
        try {
            cp.post(new Circuit(x));
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

            assertTrue(numUnfixed > 1);

            return branch(
                    () -> {
                        try {
                            cp.post(equal(var, val));
                        } catch (InconsistencyException ignored) {
                            fail();
                        }
                        if (numUnfixed == 2) {
                            for (IntVar xi : x) {
                                assertTrue(xi.isFixed());
                            }
                        }
                    },
                    () -> {
                        try {
                            cp.post(notEqual(var, val));
                        } catch (InconsistencyException ignored) {
                            fail();
                        }
                        if (var.isFixed() && numUnfixed == 2) {
                            for (IntVar xi : x) {
                                assertTrue(xi.isFixed());
                            }
                        }
                    });
        });

        dfs.solve();

    }


}
