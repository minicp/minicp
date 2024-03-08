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
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import org.javagrader.Grade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;

import static minicp.cp.BranchingScheme.*;
import static minicp.cp.Factory.*;
import static org.junit.jupiter.api.Assertions.*;

@Grade(cpuTimeout = 1)
public class AllDifferentFWCTest extends SolverTest {

    @ParameterizedTest
    @MethodSource("getSolver")
    public void allDifferentTest1(Solver cp) {

        IntVar[] x = makeIntVarArray(cp, 5, 5);

        try {
            AllDifferentFWC allDiff = new AllDifferentFWC(x);
            allDiff.post();
            allDiff.setActive(false);
            cp.post(equal(x[0], 0));
            for (int i = 1; i < x.length; i++) {
                assertEquals(5, x[i].size());
                assertEquals(0, x[i].min());
            }
            allDiff.propagate();
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
    public void allDifferentTest2(Solver cp) {

        IntVar[] x = makeIntVarArray(cp, 5, 5);

        try {
            cp.post(new AllDifferentFWC(x));

            SearchStatistics stats = makeDfs(cp, firstFail(x)).solve();
            assertEquals(120, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void allDifferentTest3(Solver cp) {

        IntVar[] x = makeIntVarArray(cp, 5, 5);
        try {
            AllDifferentFWC allDiff = new AllDifferentFWC(x);
            allDiff.post();
            allDiff.setActive(false);

            cp.post(equal(x[2], 3));
            cp.post(equal(x[1], 3));
            try {
                allDiff.propagate();
                fail();
            } catch (InconsistencyException e) {

            }
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void allDifferentTest4(Solver cp) {

        IntVar[] x = makeIntVarArray(cp, 7, 7);

        try {
            cp.post(new AllDifferentFWC(x));

            DFSearch search = makeDfs(cp, () -> {
                // selects the unfixed variable with the smallest domain
                IntVar xs = selectMin(x, xi -> !xi.isFixed(), xi -> xi.size());
                if (xs == null)
                    return EMPTY; // solution found
                // check all diff
                for (int i = 0 ; i < x.length ; i++) {
                    if (x[i].isFixed()) {
                        for (int j = 0 ; j < x.length ; j++) {
                            if (i != j) {
                                assertFalse(x[j].contains(x[i].min()),
                                        String.format("x[%d] = {%d} but x[%d] contains the value (its domain is %s)", i, x[i].min(), j, x[j].toString()));
                            }
                        }
                    }
                }
                // assign the variable to a value
                int v = xs.min();
                return branch(() -> cp.post(equal(xs, v)),
                        () -> cp.post(notEqual(xs, v)));
            });

            SearchStatistics stats = search.solve();
            assertEquals(5040, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void allDifferentTest5(Solver cp) {

        IntVar[] x = makeIntVarArray(cp, 8, 8);
        try {
            cp.post(new AllDifferentFWC(x));
            try {
                cp.post(equal(x[1], 2));
                cp.post(equal(x[2], 4));

                cp.post(equal(x[6], 1));
                cp.post(equal(x[3], 1));
                fail();
            } catch (InconsistencyException e) {

            }
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void allDifferentTimeComplexity(Solver cp) {
        int n = 1000;
        IntVar[] x = makeIntVarArray(cp, n, n);
        try {
            cp.post(new AllDifferentFWC(x));
            for (int i = 0 ; i < n-1 ; i++) {
                cp.post(equal(x[i], i));
            }
            for (int i = 0 ; i < n ; i++) {
                cp.getStateManager().saveState();
                cp.post(equal(x[n-1], n-1));
                cp.getStateManager().restoreState();
            }
            try {
            } catch (InconsistencyException e) {
                fail();
            }
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

}
