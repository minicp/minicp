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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static minicp.cp.BranchingScheme.*;
import static minicp.cp.Factory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Named.named;

@Grade(cpuTimeout = 1)
public class CircuitTest extends SolverTest {

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

    public int[] circuit(String circuit) {
        return Arrays.stream(circuit.split(", ")).mapToInt(Integer::valueOf).toArray();
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
            for (int i = 0; i < 5; ++i) {
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
    @ValueSource(strings = {
            "1, 2, 3, 4, 5, 0",
            "2, 3, 1, 5, 0, 4"
    })
    public void testValidCircuit(String circuit) {
        try {
            Solver cp = makeSolver();
            cp.post(new Circuit(instantiate(cp, circuit(circuit))));
        } catch (InconsistencyException e) {
            fail("should not fail");
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

    @ParameterizedTest(name = "[{index}] {0} nodes")
    @ValueSource(ints = {7, 8, 9, 10, 15, 20, 30, 40, 50})
    public void testArbitraryBranching(int n) {
        try {
            Solver cp = makeSolver();
            IntVar[] x = makeIntVarArray(cp, n, n);
            cp.post(new Circuit(x));
            Random random = new Random(69); // used for selecting a variable and a value at random
            int[] domain = new int[n]; // used for iterating over the domain
            AtomicReference<String> branchingDecisions = new AtomicReference<>(""); // for having an easier error message in case of debugging
            Integer[] indices = IntStream.range(0, n).boxed().toArray(Integer[]::new); // indices of variables to select

            DFSearch dfs = makeDfs(cp, () -> {
                Integer index = selectMin(indices, idx -> !x[idx].isFixed(), idx -> random.nextInt());
                if (index == null) {
                    return EMPTY;
                } else {
                    IntVar succ = x[index];
                    String currentBranching = branchingDecisions.get();
                    int size = succ.fillArray(domain);
                    int successor = domain[random.nextInt(size)];
                    return branch(() -> {
                                cp.post(equal(succ, successor));
                                branchingDecisions.set(currentBranching + String.format("%n x[%d] = %d", index, successor));
                            },
                            () -> {
                                cp.post(notEqual(succ, successor));
                                branchingDecisions.set(currentBranching + String.format("%n x[%d] != %d", index, successor));
                            });
                }
            });

            dfs.onSolution(() -> {
                        int[] sol = new int[x.length];
                        for (int i = 0; i < x.length; i++) {
                            sol[i] = x[i].min();
                        }
                        if (!checkHamiltonian(sol)) {
                            String vars = Arrays.stream(x).map(xi -> String.valueOf(xi.min())).collect(Collectors.joining(", "));
                            fail("You said that a circuit was hamiltonian when it was not the case" +
                                    "\n Variables are set to: " + vars + "\n Branching decisions are: " + branchingDecisions.get());
                        }
                    }
            );
            int searchLimit = 1000;
            SearchStatistics stats = dfs.solve(statistics -> statistics.numberOfSolutions() >= searchLimit);
            // number of solutions is the number of permutations over n elements, or the searchLimit that was imposed
            int nSolutions;
            if (n >= 8) {
                nSolutions = searchLimit;
            } else {
                nSolutions = fact(n - 1);
            }
            assertEquals(nSolutions, stats.numberOfSolutions());
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    public static Stream<Arguments> getInvalidCircuits() {
        return Arrays.stream(new Arguments[]{
                Arguments.arguments(named("2 sub-circuits: 0 -> 1 -> 2 -> 0 and 3 -> 4 -> 5 -> 3", "1, 2, 0, 4, 5, 3")),
                Arguments.arguments(named("circuit but one node is never visited", "1, 2, 3, 4, 5, 2")),
                Arguments.arguments(named("circuit but one node is never visited", "1, 2, 3, 4, 5, 6, 0, 7")),
                Arguments.arguments(named("nodes 6 and 7 are disconnected (0 -> 1 -> 2 -> 3 -> 4 -> 5 -> 0)", "1, 2, 3, 4, 5, 0, 7, 6")),
                Arguments.arguments(named("one node is visited twice", "1, 2, 3, 4, 5, 6, 3, 7")),
                Arguments.arguments(named("2 sub-circuits: 0 -> 1 -> 2 -> 3 -> 4 -> 0 and 5 -> 6 -> 7 -> 5", "1, 2, 3, 4, 0, 6, 7, 5")),
                Arguments.arguments(named("2 sub-circuits: 0 -> 1 -> 3 -> 2 -> 0 and 4 -> 5 -> 6 -> 4, and node 7 is not connected", "1, 3, 0, 2, 5, 6, 4, 7")),
                Arguments.arguments(named("3 sub-circuits: 0 -> 1 -> 2 -> 0 and 3 -> 5 -> 4 -> 3 and 6 -> 7 -> 6", "1, 2, 0, 5, 3, 4, 7, 6")),
                Arguments.arguments(named("3 sub-circuits: 0 -> 1 -> 0 and 2 -> 3 -> 2 and 4 -> 6 -> 5 -> 4, node 7 is not connected", "1, 0, 3, 2, 6, 4, 5, 7")),
                Arguments.arguments(named("4 sub-circuits: 0 -> 4 -> 0 and 1 -> 5 -> 1 and 2 -> 3 -> 2 and 6 -> 7 -> 6", "4, 5, 3, 2, 0, 1, 7, 6")),
                Arguments.arguments(named("2 sub-circuits: 0 -> 3 -> 7 -> 1 -> 0 and 2 -> 5 -> 6 -> 4 -> 2", "3, 0, 5, 7, 2, 6, 4, 1")),
                Arguments.arguments(named("2 sub-circuits: 0 -> 3 -> 4 -> 6 -> 0 and 1 -> 5 -> 7 -> 2 -> 1", "3, 5, 1, 4, 6, 7, 0, 2")),
        });
    }

    @ParameterizedTest
    @MethodSource("getInvalidCircuits")
    public void testFailInvalidInstantiate(String invalidCircuit) {
        try {
            Solver cp = makeSolver();
            int[] circuit = circuit(invalidCircuit);
            cp.post(new Circuit(instantiate(cp, circuit)));
            fail("You should have thrown an inconsistency when given an invalid circuit");
        } catch (InconsistencyException ignored) {

        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getInvalidCircuits")
    public void testFailInvalidIncremental(String invalidCircuit) {
        try {
            Solver cp = makeSolver();
            int[] circuit = circuit(invalidCircuit);
            IntVar[] x = new IntVar[circuit.length];
            for (int i = 0; i < circuit.length; i++) {
                x[i] = makeIntVar(cp, 0, circuit.length);
            }
            cp.post(new Circuit(x));
            // fix the variables in an arbitrary order
            List<Integer> indices = IntStream.range(0, circuit.length).boxed().collect(Collectors.toList());
            Collections.shuffle(indices);
            for (int i : indices) {
                cp.post(equal(x[i], circuit[i]));
            }
            fail("You should have thrown an inconsistency when given an invalid circuit");
        } catch (InconsistencyException ignored) {

        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    private static int fact(int n) {
        if (n <= 1) {
            return 1;
        } else {
            return n * fact(n - 1);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testConnectingInvalidSubCircuit(Solver cp) {
        try {
            // attempt to form circuit when connecting 2 independent path fragments
            //  the circuit formed will not allow to close the path, and must result in a failure
            int n = 8;
            IntVar[] x = new IntVar[n];
            for (int i = 0; i < n; i++) {
                x[i] = makeIntVar(cp, 0, n - 1);
            }
            cp.post(new Circuit(x));
            cp.post(equal(x[0], 1));
            assertFalse(x[1].contains(0), "The last node of a sub-path cannot have the origin of the sub-path as a successor");
            cp.post(equal(x[2], 3));
            assertFalse(x[3].contains(2), "The last node of a sub-path cannot have the origin of the sub-path as a successor");
            cp.post(equal(x[4], 5));
            assertFalse(x[5].contains(4), "The last node of a sub-path cannot have the origin of the sub-path as a successor");
            cp.post(equal(x[6], 7));
            assertFalse(x[7].contains(6), "The last node of a sub-path cannot have the origin of the sub-path as a successor");

            // fragments created 0 -> 1, 2 -> 3, 4 -> 5, 6 -> 7
            // links the fragments in an order making a valid circuit, and check that the origin of the path is deleted
            cp.post(equal(x[1], 4));
            assertFalse(x[5].contains(0), "The last node of a sub-path cannot have the origin of the sub-path as a successor");
            assertFalse(x[5].isFixed(), "The circuit is not fixed at this point");
            // 0 -> 1 -> 4 -> 5. Fragments remaining: 2 -> 3, 6 -> 7

            cp.post(equal(x[5], 6));
            assertFalse(x[7].contains(0), "The last node of a sub-path cannot have the origin of the sub-path as a successor");
            assertTrue(x[7].isFixed(), "The circuit is fixed at this point");
            assertTrue(x[3].isFixed());
        } catch (InconsistencyException ignored) {
            fail();
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
            for (int i = 0; i < x.length; ++i) {
                StateInt origI = circuit.orig[i];
                for (int j = 0; j < x.length; ++j) {
                    if (i != j) {
                        assertNotSame(origI, circuit.orig[j], "Use orig[i].setValue(...) to set the StateInt, not orig[i] = ...");
                    }
                    assertNotSame(origI, circuit.dest[j], "Use orig[i].setValue(...) to set the StateInt, not orig[i] = ...");
                }
            }
            for (int i = 0; i < x.length; ++i) {
                StateInt destI = circuit.dest[i];
                for (int j = 0; j < x.length; ++j) {
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
