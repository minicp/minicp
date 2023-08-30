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
import org.javagrader.ConditionalOrderingExtension;
import org.javagrader.Grade;
import org.javagrader.GraderExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.IntStream;

import static minicp.cp.BranchingScheme.firstFail;
import static minicp.cp.Factory.*;
import static org.junit.jupiter.api.Assertions.*;

@Grade
@ExtendWith(ConditionalOrderingExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CumulativeDecompTest extends SolverTest {

    @Timeout(1)
    @ParameterizedTest
    @MethodSource("getSolver")
    @Order(1)
    public void testAllDiffWithCumulative(Solver cp) {
        try {
            IntVar[] s = makeIntVarArray(cp, 5, 5);
            int[] d = new int[5];
            Arrays.fill(d, 1);
            int[] r = new int[5];
            Arrays.fill(r, 100);

            cp.post(new CumulativeDecomposition(s, d, r, 100));

            SearchStatistics stats = makeDfs(cp, firstFail(s)).solve();
            assertEquals(120, stats.numberOfSolutions(), "cumulative alldiff expect makeIntVarArray permutations");
        } catch (InconsistencyException e) {
            assert (false);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }

    @Timeout(1)
    @ParameterizedTest
    @MethodSource("getSolver")
    @Order(1)
    public void testBasic1(Solver cp) {

        try {

            IntVar[] s = makeIntVarArray(cp, 2, 10);
            int[] d = new int[]{5, 5};
            int[] r = new int[]{1, 1};

            cp.post(new CumulativeDecomposition(s, d, r, 1));
            cp.post(equal(s[0], 0));

            assertEquals(5, s[1].min());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Timeout(1)
    @ParameterizedTest
    @MethodSource("getSolver")
    @Order(1)
    public void testBasic2(Solver cp) {
        try {

            IntVar[] s = makeIntVarArray(cp, 2, 10);
            int[] d = new int[]{5, 5};
            int[] r = new int[]{1, 1};

            cp.post(new CumulativeDecomposition(s, d, r, 1));

            cp.post(equal(s[0], 5));

            assertEquals(0, s[1].max());

        } catch (InconsistencyException e) {
            assert (false);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    @Grade(cpuTimeout = 2)
    @Order(2)
    @Tag("slow")
    public void testCapaOk() {
        Solver cp = makeSolver();
        try {
            IntVar[] s = makeIntVarArray(cp, 5, 10);
            int[] d = new int[]{5, 10, 3, 6, 1};
            int[] r = new int[]{3, 7, 1, 4, 8};

            cp.post(new CumulativeDecomposition(s, d, r, 12));

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
            assert (false);
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
