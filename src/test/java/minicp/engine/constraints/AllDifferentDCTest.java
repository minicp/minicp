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
import minicp.util.GraphUtil;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import minicp.util.NotImplementedExceptionAssume;
import org.javagrader.Grade;
import org.javagrader.GradeFeedback;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static minicp.cp.BranchingScheme.*;
import static minicp.cp.Factory.*;
import static org.junit.jupiter.api.Assertions.*;

@Grade(cpuTimeout = 1)
public class AllDifferentDCTest extends SolverTest {

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testOneFixedVariable(Solver cp) {

        IntVar[] x = makeIntVarArray(cp, 5, 5);

        try {
            AllDifferentDC allDifferentDC = new AllDifferentDC(x);
            allDifferentDC.post();
            x[0].fix(0);
            allDifferentDC.propagate();
            for (int i = 1; i < x.length; i++) {
                assertEquals(4, x[i].size());
                assertEquals(1, x[i].min());
            }
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testNoSolutionRemoved(Solver cp) {

        IntVar[] x = makeIntVarArray(cp, 5, 5);

        try {
            cp.post(new AllDifferentDC(x));

            SearchStatistics stats = makeDfs(cp, firstFail(x)).solve();
            assertEquals(120, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    private static IntVar makeIVar(Solver cp, Integer... values) {
        return makeIntVar(cp, new HashSet<>(Arrays.asList(values)));
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testUpdateGraph1(Solver cp) {
        try {
            Set<Integer>[] domainsBefore = new Set[] {
                    new HashSet(Arrays.asList(3, 4)),
                    new HashSet(Arrays.asList(1)),
                    new HashSet(Arrays.asList(3, 4)),
                    new HashSet(Arrays.asList(0)),
                    new HashSet(Arrays.asList(3, 4, 5)),
                    new HashSet(Arrays.asList(5, 6, 7)),
                    new HashSet(Arrays.asList(2, 9, 10)),
                    new HashSet(Arrays.asList(5, 6, 7, 8)),
                    new HashSet(Arrays.asList(5, 6, 7)),
            };
            IntVar[] x = new IntVar[domainsBefore.length];
            for (int i = 0 ; i < x.length ; ++i) {
                x[i] = makeIntVar(cp, domainsBefore[i]);
            }
            AllDifferentDC allDiff = new AllDifferentDC(x);
            allDiff.post(); // no fixpoint to be sure that the propagation is done only once
            assertValidGraph(allDiff.g, x);
            DFSearch search = searchWithCheckingGraph(allDiff, x);
            search.solve();
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testUpdateGraph2(Solver cp) {
        try {
            IntVar[] x = new IntVar[]{
                    makeIVar(cp, 0, 3, 4, 7),
                    makeIVar(cp, 1, 10),
                    makeIVar(cp, 3, 4),
                    makeIVar(cp, 0, 6),
                    makeIVar(cp, 3, 4, 5, 2),
                    makeIVar(cp, 5, 6, 7),
                    makeIVar(cp, 2, 9, 10),
                    makeIVar(cp, 4, 5, 6, 7, 8),
                    makeIVar(cp, 5, 7)};
            AllDifferentDC allDiff = new AllDifferentDC(x);
            cp.post(allDiff);
            assertValidGraph(allDiff.g, x);
            DFSearch search = searchWithCheckingGraph(allDiff, x);
            search.solve();
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    @GradeFeedback(message = "How are you handling the case when the values do not start from 0?")
    public void testUpdateGraph3(Solver cp) {
        try {
            IntVar[] x = new IntVar[]{
                    makeIVar(cp, 2, 3, 4, 5, 6, 7, 8, 9),
                    makeIVar(cp, 2, 4, 7),
                    makeIVar(cp, 2, 3, 7),
                    makeIVar(cp, 1, 10),
            };
            AllDifferentDC allDiff = new AllDifferentDC(x);
            allDiff.post();
            assertEquals(8, x[0].size());
            assertEquals(3, x[1].size());
            assertEquals(3, x[2].size());
            assertEquals(2, x[3].size());
            assertValidGraph(allDiff.g, x);
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    @GradeFeedback(message = "How are you handling the case when the values do not start from 0?")
    public void testUpdateGraph4(Solver cp) {
        try {
            IntVar[] x = new IntVar[]{
                    makeIVar(cp, 2, 3, 4, 5, 6, 7, 8, 9),
                    makeIVar(cp, 2, 4, 7),
                    makeIVar(cp, 2, 3, 7),
                    makeIVar(cp, 1, 10),
            };
            AllDifferentDC allDiff = new AllDifferentDC(x);
            cp.post(allDiff);
            assertEquals(8, x[0].size());
            assertEquals(3, x[1].size());
            assertEquals(3, x[2].size());
            assertEquals(2, x[3].size());
            //assertValidGraph(allDiff.g, x);
            DFSearch search = searchWithCheckingGraph(allDiff, x);
            SearchStatistics stats = search.solve();
            assertTrue(stats.numberOfSolutions() >= 1);
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    private static DFSearch searchWithCheckingGraph(AllDifferentDC allDiff, IntVar[] x) {
        Solver cp = x[0].getSolver();
        return makeDfs(cp, () -> {
            try {
                assertValidGraph(allDiff.g, x);
            } catch (AssertionFailedError e) {
                throw new AssertionFailedError("Your graph was not correctly updated after one propagation\n" + e.getMessage(), e);
            }
            IntVar xs = selectMin(x,
                    xi -> xi.size() > 1,
                    xi -> xi.size());
            if (xs == null)
                return EMPTY;
            else {
                int v = xs.min();
                return branch(() -> xs.getSolver().post(equal(xs, v)),
                        () -> xs.getSolver().post(notEqual(xs, v)));
            }
        });
    }

    /**
     * Asserts that a variable-value graph respects some basic properties:
     * - the sink has a number of outgoing links equal to the ingoing links
     * - the
     * @param g
     * @param x
     */
    private static void assertValidGraph(GraphUtil.Graph g, IntVar[] x) {
        int n = g.n();
        Set<Integer> [] in = new Set[n];
        Set<Integer> [] out = new Set[n];
        for (int i = 0 ; i < n ; ++i) {
            in[i] = new HashSet<>();
            for (int j: g.in(i))
                in[i].add(j);
            out[i] = new HashSet<>();
            for (int j: g.out(i))
                out[i].add(j);
        }
        int sink = -1;
        for (int i = 0 ; i < n ; ++i) {
            for (int ingoing: in[i]) {
                assertTrue(out[ingoing].contains(i),
                        String.format("%d belongs to in[%d] <=> %d belongs to out[%d]", ingoing, i, i, ingoing));
            }
            for (int outgoing: out[i]) {
                assertTrue(in[outgoing].contains(i),
                        String.format("%d belongs to out[%d] <=> %d belongs to in[%d]", outgoing, i, i, outgoing));
            }
            if (out[i].size() == x.length) {
                assertNotEquals(sink, x.length, "There is only one sink");
                sink = i;
            }
        }
        // test the sink
        assertNotEquals(-1, sink, "You should have a sink with " + x.length + " outgoing links in your graph");
        // from the sink, retrieve the values
        for (int value: out[sink]) {
            assertEquals(1, out[value].size(), "Values to which the sink points towards have only one outgoing link");
            for (int var: out[value]) {
                assertEquals(1, in[var].size(), "Variables have only one ingoing link");
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testRemoveStronglyConnected(Solver cp) {
        try {
            IntVar[] x = new IntVar[]{
                    makeIVar(cp, 1, 2),
                    makeIVar(cp, 1, 2),
                    makeIVar(cp, 1, 2, 3, 4)};

            new AllDifferentDC(x).post();

            assertEquals(x[2].min(), 3);
            assertEquals(x[2].size(), 2);

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void allDifferentTest1(Solver cp) {
        try {
            IntVar[] x = new IntVar[]{
                    makeIVar(cp, 1, 2, 3, 4, 5),
                    makeIVar(cp, 2),
                    makeIVar(cp, 1, 2, 3, 4, 5),
                    makeIVar(cp, 1),
                    makeIVar(cp, 1, 2, 3, 4, 5, 6),
                    makeIVar(cp, 6, 7, 8),
                    makeIVar(cp, 3),
                    makeIVar(cp, 6, 7, 8, 9),
                    makeIVar(cp, 6, 7, 8)};

            new AllDifferentDC(x).post();

            assertEquals(x[0].size(), 2);
            assertEquals(x[2].size(), 2);
            assertEquals(x[4].min(), 6);
            assertEquals(x[7].min(), 9);
            assertEquals(x[8].min(), 7);
            assertEquals(x[8].max(), 8);

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testNoFailuresWithDFS(Solver cp) {
        try {
            IntVar[] x = new IntVar[]{
                    makeIVar(cp, 1, 2, 3, 4, 5),
                    makeIVar(cp, 2, 7),
                    makeIVar(cp, 1, 2, 3, 4, 5),
                    makeIVar(cp, 1, 3),
                    makeIVar(cp, 1, 2, 3, 4, 5, 6),
                    makeIVar(cp, 6, 7, 8),
                    makeIVar(cp, 3, 4, 5),
                    makeIVar(cp, 6, 7, 8, 9),
                    makeIVar(cp, 6, 7, 8)};
            AllDifferentDC allDiff = new AllDifferentDC(x);
            cp.post(allDiff);

            DFSearch dfs = searchWithCheckingGraph(allDiff, x);
            dfs.solve();

            SearchStatistics stats = dfs.solve();
            // GAC filter with a single constraint should have no fail
            assertEquals(0, stats.numberOfFailures());
            assertEquals(80, stats.numberOfSolutions());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testExample(Solver cp) {
        try {
            IntVar[] x = new IntVar[]{
                    makeIVar(cp, 3, 4),
                    makeIVar(cp, 1),
                    makeIVar(cp, 3, 4),
                    makeIVar(cp, 0),
                    makeIVar(cp, 3, 4, 5),
                    makeIVar(cp, 5, 6, 7),
                    makeIVar(cp, 2, 9, 10),
                    makeIVar(cp, 5, 6, 7, 8),
                    makeIVar(cp, 5, 6, 7)};

            new AllDifferentDC(x).post();

            assertFalse(x[4].contains(3));
            assertFalse(x[4].contains(4));
            assertFalse(x[5].contains(5));
            assertFalse(x[7].contains(5));
            assertFalse(x[7].contains(6));
            assertFalse(x[8].contains(5));
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testHandleNegativeValues(Solver cp) {
        try {
            IntVar[] x = new IntVar[]{
                    makeIVar(cp, 0,2,3,5),
                    makeIVar(cp, 4),
                    makeIVar(cp, -1,1),
                    makeIVar(cp, -4,-2,0,2,3),
                    makeIVar(cp, -1)};

            new AllDifferentDC(x).post();

            assertFalse(x[2].contains(-1));

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


}
