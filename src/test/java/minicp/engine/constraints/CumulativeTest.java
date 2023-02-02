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
import minicp.engine.constraints.Profile.Rectangle;
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
import java.util.stream.IntStream;

import static minicp.cp.BranchingScheme.firstFail;
import static minicp.cp.Factory.*;
import static org.junit.jupiter.api.Assertions.*;

@Grade(cpuTimeout = 2)
public class CumulativeTest extends SolverTest {

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testBuildProfile1(Solver cp) {
        try {
            IntVar[] s = new IntVar[3];
            s[0] = makeIntVar(cp, 1, 1);
            s[1] = makeIntVar(cp, 2, 2);
            s[2] = makeIntVar(cp, 3, 3);
            int[] d = new int[] {8, 3, 3};
            int[] r = new int[] {1, 1, 2};
            Cumulative cumulative = new Cumulative(s, d, r, 4);
            Profile profile = cumulative.buildProfile();
            assertEquals(7, profile.size(), "There are 7 rectangles composing the profile");
            int[] starts = new int[] {1, 2, 3, 5, 6};
            int[] duration = new int[] {1, 1, 2, 1, 3};
            int[] height = new int[] {1, 2, 4, 3, 1};
            for (int i = 0 ; i < starts.length ; ++i) {
                Rectangle rect = profile.get(i+1);
                assertEquals(starts[i], rect.start());
                assertEquals(duration[i], rect.dur());
                assertEquals(height[i], rect.height());
            }
        } catch (InconsistencyException e) {
            fail("There is no inconsistency with the provided arguments to the cumulative constraint");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testBuildProfile2(Solver cp) {
        try {
            IntVar[] s = new IntVar[3];
            s[0] = makeIntVar(cp, 1, 5); // not fixed but has a mandatory part
            s[1] = makeIntVar(cp, 2, 2);
            s[2] = makeIntVar(cp, 3, 3);
            int[] d = new int[] {8, 3, 3};
            int[] r = new int[] {1, 1, 2};
            Cumulative cumulative = new Cumulative(s, d, r, 4);
            Profile profile = cumulative.buildProfile();
            assertEquals(6, profile.size(), "There are 6 rectangles composing the profile");
            int[] starts = new int[] {2, 3, 5, 6};
            int[] duration = new int[] {1, 2, 1, 3};
            int[] height = new int[] {1, 3, 3, 1};
            for (int i = 0 ; i < starts.length ; ++i) {
                Rectangle rect = profile.get(i+1);
                assertEquals(starts[i], rect.start());
                assertEquals(duration[i], rect.dur());
                assertEquals(height[i], rect.height());
            }
        } catch (InconsistencyException e) {
            fail("There is no inconsistency with the provided arguments to the cumulative constraint");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testAllDiffWithCumulative(Solver cp) {
        try {

            IntVar[] s = makeIntVarArray(cp, 5, 5);
            int[] d = new int[5];
            Arrays.fill(d, 1);
            int[] r = new int[5];
            Arrays.fill(r, 100);

            cp.post(new Cumulative(s, d, r, 100));

            SearchStatistics stats = makeDfs(cp, firstFail(s)).solve();
            assertEquals(120, stats.numberOfSolutions(), "cumulative alldiff expect makeIntVarArray permutations");

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testBasic1(Solver cp) {
        try {

            IntVar[] s = makeIntVarArray(cp, 2, 10);
            int[] d = new int[]{5, 5};
            int[] r = new int[]{1, 1};

            cp.post(new Cumulative(s, d, r, 1));
            cp.post(equal(s[0], 0));

            assertEquals(5, s[1].min());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testBasic2(Solver cp) {
        try {

            IntVar[] s = makeIntVarArray(cp, 2, 10);
            int[] d = new int[]{5, 5};
            int[] r = new int[]{1, 1};

            cp.post(new Cumulative(s, d, r, 1));

            cp.post(equal(s[0], 5));

            assertEquals(0, s[1].max());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testCapaOk(Solver cp) {
        try {

            IntVar[] s = makeIntVarArray(cp, 5, 10);
            int[] d = new int[]{5, 10, 3, 6, 1};
            int[] r = new int[]{3, 7, 1, 4, 8};

            cp.post(new Cumulative(s, d, r, 12));

            DFSearch search = makeDfs(cp, firstFail(s));

            search.onSolution(() -> {
                Rectangle[] rects = IntStream.range(0, s.length).mapToObj(i -> {
                    int start = s[i].min();
                    int end = start + d[i];
                    int height = r[i];
                    return new Rectangle(start, end, height);
                }).toArray(Rectangle[]::new);
                int[] discreteProfile = discreteProfile(rects);
                for (int h : discreteProfile) {
                    assertTrue(h <= 12, "capa exceeded in cumulative constraint");
                }
            });

            SearchStatistics stats = search.solve();
            assertEquals(15649, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testSameNumberOfSolutionsAsDecomp(Solver cp) {
        try {

            IntVar[] s = makeIntVarArray(cp, 5, 7);
            int[] d = new int[]{5, 10, 3, 6, 1};
            int[] r = new int[]{3, 7, 1, 4, 8};

            DFSearch search = makeDfs(cp, firstFail(s));

            cp.getStateManager().saveState();

            cp.post(new Cumulative(s, d, r, 12));
            SearchStatistics stats1 = search.solve();

            cp.getStateManager().restoreState();

            cp.post(new CumulativeDecomposition(s, d, r, 12));
            SearchStatistics stats2 = search.solve();


            assertEquals(stats1.numberOfSolutions(), stats2.numberOfSolutions());
            assertEquals(stats1.numberOfFailures(), stats2.numberOfFailures());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testStartFiltering1(Solver cp) {
        try {
            IntVar[] s = makeIntVarArray(cp, 4, 20);
            int[] d = new int[]{4, 2, 5, 3};
            int[] r = new int[]{1, 1, 1, 1};

            cp.post(equal(s[0], 2));
            cp.post(equal(s[1], 9));
            cp.post(equal(s[2], 12));

            cp.post(new Cumulative(s, d, r, 1));

            assertEquals(6, s[3].min());
            assertEquals(19, s[3].max());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testStartFiltering2(Solver cp) {
        try {

            IntVar[] s = makeIntVarArray(cp, 6, 30);
            int[] d = new int[]{4, 2, 4, 3, 5, 3};
            int[] r = new int[]{1, 1, 1, 1, 1, 1};

            cp.post(equal(s[0], 2));
            cp.post(equal(s[1], 8));
            cp.post(equal(s[2], 11));
            cp.post(equal(s[3], 18));
            cp.post(equal(s[4], 22));

            cp.post(new Cumulative(s, d, r, 1));

            assertEquals(15, s[5].min());
            assertEquals(29, s[5].max());
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testStartFiltering3(Solver cp) {
        try {

            IntVar[] s = makeIntVarArray(cp, 8, 30);
            int[] d = new int[]{4, 3, 2, 4, 3, 3, 3, 3};
            int[] r = new int[]{3, 2, 1, 1, 1, 1, 2, 2};

            cp.post(equal(s[0], 2));
            cp.post(equal(s[1], 8));
            cp.post(equal(s[2], 9));
            cp.post(equal(s[3], 12));
            cp.post(equal(s[4], 13));
            cp.post(equal(s[5], 13));
            cp.post(equal(s[6], 19));

            cp.post(new Cumulative(s, d, r, 3));

            assertEquals(16, s[7].min());
            assertEquals(29, s[7].max());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testFixInMiddleOfProfile(Solver cp) {
        try {

            IntVar[] s = makeIntVarArray(cp, 5, 10);
            int[] d = new int[]{5, 5, 1, 2, 2};
            int[] r = new int[]{1, 1, 1, 2, 2};

            cp.post(equal(s[0], 0));
            cp.post(equal(s[1], 1));
            cp.post(equal(s[2], 5));
            cp.post(equal(s[3], 8));

            cp.post(new Cumulative(s, d, r, 2));

            assertEquals(6, s[4].min()); // s[4] needs to be fixed at the middle of the profile
            assertTrue(s[4].isFixed());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    private static int[] discreteProfile(Rectangle... rectangles) {
        int min = Arrays.stream(rectangles).filter(r -> r.height() > 0).map(Rectangle::start).min(Integer::compare).get();
        int max = Arrays.stream(rectangles).filter(r -> r.height() > 0).map(Rectangle::end).max(Integer::compare).get();
        int[] heights = new int[max - min];
        // discrete profileRectangles of rectangles
        for (Rectangle r : rectangles) {
            if (r.height() > 0) {
                for (int i = r.start(); i < r.end(); i++) {
                    heights[i - min] += r.height();
                }
            }
        }
        return heights;
    }


}
