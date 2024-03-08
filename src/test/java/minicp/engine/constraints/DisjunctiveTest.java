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
import minicp.engine.core.BoolVar;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.SearchStatistics;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import minicp.util.NotImplementedExceptionAssume;
import org.javagrader.Grade;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.Arrays;

import static minicp.cp.BranchingScheme.firstFail;
import static minicp.cp.Factory.*;
import static org.junit.jupiter.api.Assertions.*;

@Grade(value = 2, cpuTimeout = 1)
public class DisjunctiveTest extends SolverTest {

    private static void decomposeDisjunctive(IntVar[] start, int[] duration) {
        Solver cp = start[0].getSolver();
        IntVar[] end = makeIntVarArray(start.length, i -> plus(start[i], duration[i]));
        for (int i = 0; i < start.length; i++) {
            for (int j = i + 1; j < start.length; j++) {
                // i before j or j before i:
                BoolVar iBeforej = makeBoolVar(cp);
                BoolVar jBeforei = not(iBeforej);

                cp.post(new IsLessOrEqualVar(iBeforej, end[i], start[j]));
                cp.post(new IsLessOrEqualVar(jBeforei, end[j], start[i]));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testAllDiffDisjunctive(Solver cp) {
        try {
            IntVar[] s = makeIntVarArray(cp, 5, 5);

            int[] d = new int[5];
            Arrays.fill(d, 1);
            cp.post(new Disjunctive(s, d));
            SearchStatistics stats = makeDfs(cp, firstFail(s)).solve();
            assertEquals( 120, stats.numberOfSolutions());
        } catch (InconsistencyException e) {
            fail();
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testNotRemovingSolutions(Solver cp) {
        try {

            IntVar[] s = makeIntVarArray(cp, 4, 20);
            int[] d = new int[]{5, 4, 6, 7};
            DFSearch dfs = makeDfs(cp, firstFail(s));

            cp.getStateManager().saveState();

            cp.post(new Disjunctive(s, d));

            SearchStatistics stat1 = dfs.solve();

            cp.getStateManager().restoreState();

            decomposeDisjunctive(s, d);

            SearchStatistics stat2 = dfs.solve();

            assertEquals(stat1.numberOfSolutions(), stat2.numberOfSolutions());

        } catch (InconsistencyException e) {
            fail();
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testBinaryDecomposition(Solver cp) {
        IntVar s1 = makeIntVar(cp, 0, 10);
        int d1 = 10;
        IntVar s2 = makeIntVar(cp, 6, 15);
        int d2 = 6;

        try {
            cp.post(new Disjunctive(new IntVar[]{s1, s2}, new int[]{d1, d2}));
            assertEquals(10, s2.min());
        } catch (InconsistencyException e) {
            fail();
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testOverloadChecker(Solver cp) {
        IntVar sA = makeIntVar(cp, 0, 9);
        int d1 = 5;
        IntVar sB = makeIntVar(cp, 1, 10);
        int d2 = 5;
        IntVar sC = makeIntVar(cp, 3, 7);
        int d3 = 6;

        Disjunctive disjunctive = new Disjunctive(new IntVar[]{sA, sB, sC}, new int[]{d1, d2, d3});

        // Test the method by itself:
        cp.getStateManager().withNewState(() -> {
            try {
                disjunctive.overLoadChecker();
                fail();
            } catch (InconsistencyException ignored) {
                ;
            } catch (NotImplementedException e) {
                NotImplementedExceptionAssume.fail(e);
            }
        });

        // Integration test by posting the constraint:
        try {
            disjunctive.post();
            fail();
        } catch (InconsistencyException e) {
            assert (true);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testDetectablePrecedence(Solver cp) {
        IntVar sA = makeIntVar(cp, 0, 9);
        int d1 = 5;
        IntVar sB = makeIntVar(cp, 1, 10);
        int d2 = 5;
        IntVar sC = makeIntVar(cp, 8, 15);
        int d3 = 3;

        Disjunctive disjunctive = new Disjunctive(new IntVar[]{sA, sB, sC}, new int[]{d1, d2, d3});

        // Test the method by itself:
        cp.getStateManager().withNewState(() -> {
            try {
                assertTrue(disjunctive.detectablePrecedence());
                assertEquals(10, sC.min(), "detectable precedence should set sC.min() to 10");
                assertFalse(disjunctive.detectablePrecedence());
            } catch (InconsistencyException e) {
                fail();
            } catch (NotImplementedException e) {
                NotImplementedExceptionAssume.fail(e);
            }
        });

        // Integration test by posting the constraint:
        try {
            disjunctive.post();
            assertEquals(10, sC.min(), "detectable precedence should set sC.min() to 10");
        } catch (InconsistencyException e) {
            fail();
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testNotLast(Solver cp) {
        IntVar sA = makeIntVar(cp, 0, 9);
        int d1 = 5;
        IntVar sB = makeIntVar(cp, 1, 10);
        int d2 = 5;
        IntVar sC = makeIntVar(cp, 3, 9);
        int d3 = 4;

        Disjunctive disjunctive = new Disjunctive(new IntVar[]{sA, sB, sC}, new int[]{d1, d2, d3});

        // Test the method by itself:
        cp.getStateManager().withNewState(() -> {
            try {
                assertTrue(disjunctive.notLast());
                assertEquals(6, sC.max(), "not last should set sC.max() to 6");
                assertTrue(disjunctive.notLast());
                assertEquals(1, sA.max(), "not last should set sA.max() to 1");
                assertFalse(disjunctive.notLast(), "the second not last iteration should not detect any changes");
            } catch (InconsistencyException e) {
                fail();
            } catch (NotImplementedException e) {
                NotImplementedExceptionAssume.fail(e);
            }
        });

        // Integration test by posting the constraint:
        try {
            disjunctive.post();
            assertEquals(6, sC.max(), "not last should set sC.max() to 6");
            assertEquals(1, sA.max(), "not last should set sA.max() to 1");
        } catch (InconsistencyException e) {
            fail();
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testUsingManualLoop(Solver cp) {
        IntVar[] s = new IntVar[] {
                makeIntVar(cp, 0, 19),
                makeIntVar(cp, 9, 17),
                makeIntVar(cp, 0, 14),
                makeIntVar(cp, 8, 16)
        };
        int[] d = new int[]{4, 5, 6, 7};
        Disjunctive disjunctive = new Disjunctive(s, d);

        // performDetPrec[i] = true iff the detectable precedence pruning method returns true at iteration i,
        //                     false otherwise:
        boolean[] performDetPrec = new boolean[]{false, false, true, false};
        // If performDetPrec[i] is false, then the not last pruning method is to be called at iteration i and
        // return true.

        // minDetPrec[i][j] = s[j].min() after running the detectable precedence pruning method at iteration i:
        int[][] minDetPrec = new int[][]{
                {0, 9, 0, 8},
                {0, 9, 0, 8},
                {0, 10, 0, 10},
                {0, 10, 0, 10}
        };
        // maxDetPrec[i][j] = s[j].max() after running the detectable precedence pruning method at iteration i:
        int[][] maxDetPrec = new int[][]{
                {19, 17, 14, 16},
                {15, 17, 13, 16},
                {13, 17, 11, 16},
                {13, 17, 11, 16}
        };

        // minNotLast[i][j] = s[j].min() after possibly running the not last pruning method at iteration i:
        int[][] minNotLast = new int[][]{
                {0, 9, 0, 8},
                {0, 9, 0, 8},
                {0, 10, 0, 10},
                {0, 10, 0, 10}
        };
        // maxNotLast[i][j] = s[j].max() after possibly running the not last pruning method at iteration i:
        int[][] maxNotLast = new int[][]{
                {15, 17, 13, 16},
                {13, 17, 11, 16},
                {13, 17, 11, 16},
                {12, 17, 10, 16}
        };

        try {
            for (int i = 0; i < performDetPrec.length; i++) {
                disjunctive.overLoadChecker();

                String message = String.format("iteration: %d", i);
                assertEquals(performDetPrec[i], disjunctive.detectablePrecedence(), message);
                for (int j = 0; j < s.length; j++) {
                    assertEquals(minDetPrec[i][j], s[j].min(), message);
                    assertEquals(maxDetPrec[i][j], s[j].max(), message);
                    assertEquals(1 + maxDetPrec[i][j] - minDetPrec[i][j], s[j].size(), message);
                }
                if (!performDetPrec[i]) {
                    assertEquals(!performDetPrec[i], disjunctive.notLast());
                }
                for (int j = 0; j < s.length; j++) {
                    assertEquals(minNotLast[i][j], s[j].min(), message);
                    assertEquals(maxNotLast[i][j], s[j].max(), message);
                    assertEquals(1 + maxNotLast[i][j] - minNotLast[i][j], s[j].size(), message);
                }
            }
            disjunctive.overLoadChecker();
            assertFalse(disjunctive.detectablePrecedence());
            assertFalse(disjunctive.notLast());
            for (int j = 0; j < s.length; j++) {
                int lastIndex = performDetPrec.length - 1;
                assertEquals(minNotLast[lastIndex][j], s[j].min());
                assertEquals(maxNotLast[lastIndex][j], s[j].max());
                assertEquals(1 + maxNotLast[lastIndex][j] - minNotLast[lastIndex][j], s[j].size());
            }
        } catch (InconsistencyException e) {
            fail();
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testDisjunctive1(Solver cp) {
        IntVar sA = makeIntVar(cp, 0, 9);
        int d1 = 5;
        IntVar sB = makeIntVar(cp, 1, 10);
        int d2 = 5;
        IntVar sC = makeIntVar(cp, 8, 15);
        int d3 = 3;

        try {
            new Disjunctive(new IntVar[]{sA, sB, sC}, new int[]{d1, d2, d3}).post();
            assertEquals(10, sA.size());
            assertEquals(10, sB.size());
            assertEquals(6, sC.size());
            assertEquals(10, sC.min());
            assertEquals(15, sC.max());
        } catch (InconsistencyException e) {
            fail();
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testDisjunctive2(Solver cp) {
        IntVar sA = makeIntVar(cp, 0, 9);
        int d1 = 5;
        IntVar sB = makeIntVar(cp, 1, 10);
        int d2 = 5;
        IntVar sC = makeIntVar(cp, 3, 9);
        int d3 = 4;

        try {
            new Disjunctive(new IntVar[]{sA, sB, sC}, new int[]{d1, d2, d3}).post();
            assertEquals(0, sA.min());
            assertEquals(1, sA.max());
            assertEquals(9, sB.min());
            assertEquals(10, sB.max());
            assertEquals(5, sC.min());
            assertEquals(6, sC.max());
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }
}
