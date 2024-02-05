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

import java.util.concurrent.atomic.AtomicInteger;

import static minicp.cp.BranchingScheme.*;
import static minicp.cp.Factory.*;
import static org.junit.jupiter.api.Assertions.*;

public class IsLessOrEqualVarTest extends SolverTest {

    @ParameterizedTest
    @MethodSource("getSolver")
    public void test1(Solver cp) {
        try {
            IntVar x = makeIntVar(cp, 0, 5);
            IntVar y = makeIntVar(cp, 0, 5);

            BoolVar b = makeBoolVar(cp);

            cp.post(new IsLessOrEqualVar(b, x, y));

            DFSearch search = makeDfs(cp, firstFail(x, y));

            SearchStatistics stats = search.solve();

            search.onSolution(() ->
                    assertTrue(x.min() <= y.min() && b.isTrue() || x.min() > y.min() && b.isFalse())
            );

            assertEquals(36, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void test2(Solver cp) {
        try {
            IntVar x = makeIntVar(cp, -8, 7);
            IntVar y = makeIntVar(cp, -4, 3);

            BoolVar b = makeBoolVar(cp);

            cp.post(new IsLessOrEqualVar(b, x, y));

            cp.getStateManager().saveState();
            cp.post(equal(b, 1));
            assertEquals(3, x.max());
            cp.getStateManager().restoreState();

            cp.getStateManager().saveState();
            cp.post(equal(b, 0));
            assertEquals(-3, x.min());
            cp.getStateManager().restoreState();

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void test3(Solver cp) {
        try {
            IntVar x = makeIntVar(cp, -4, 7);
            IntVar y = makeIntVar(cp, 0, 7);
            cp.post(equal(x, -2));

            BoolVar b = makeBoolVar(cp);
            cp.post(new IsLessOrEqualVar(b, x, y));
            assertTrue(b.isTrue());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testReactChanges(Solver cp) {
        try {
            IntVar x = makeIntVar(cp, 0, 10);
            IntVar y = makeIntVar(cp, 0, 10);
            BoolVar b = makeBoolVar(cp);
            cp.post(new IsLessOrEqualVar(b, x, y));

            cp.getStateManager().saveState();
            cp.post(equal(b, 1));
            assertEquals(10, x.max());
            assertEquals(0, x.min());
            assertEquals(10, y.max());
            assertEquals(0, y.min());
            cp.post(equal(y, 5));
            assertEquals(5, x.max());
            assertEquals(0, x.min());

            cp.getStateManager().restoreState();
            cp.post(equal(b, 0));
            cp.post(equal(y, 5));
            assertEquals(10, x.max());
            assertEquals(6, x.min());
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void test4(Solver cp) {
        try {
            IntVar x = makeIntVar(cp, 0, 10);
            IntVar y = makeIntVar(cp, 0, 10);
            BoolVar b = makeBoolVar(cp);
            cp.post(new IsLessOrEqualVar(b, x, y));

            cp.post(equal(b, 0));

            DFSearch search;
            SearchStatistics stats;
            AtomicInteger expectedVal = new AtomicInteger(10);

            search = makeDfs(cp, () -> {
                assertEquals(expectedVal.get(), y.size());
                assertEquals(expectedVal.decrementAndGet(), y.max());
                if (x.isFixed()) return EMPTY;
                assertTrue(x.min() > y.min());
                assertTrue(x.max() > y.max());
                return branch(() -> cp.post(notEqual(x, x.max())));
            });
            stats = search.solve();
            assertEquals(0, stats.numberOfFailures());
            assertEquals(1, stats.numberOfSolutions());
            assertEquals(9, stats.numberOfNodes());
            assertEquals(0, expectedVal.get());

            expectedVal.set(0);
            search = makeDfs(cp, () -> {
                assertEquals(10 - expectedVal.get(), x.size());
                assertEquals(expectedVal.incrementAndGet(), x.min());
                if (x.isFixed()) return EMPTY;
                assertTrue(x.min() > y.min());
                assertTrue(x.max() > y.max());
                return branch(() -> cp.post(notEqual(y, y.min())));
            });
            stats = search.solve();
            assertEquals(0, stats.numberOfFailures());
            assertEquals(1, stats.numberOfSolutions());
            assertEquals(9, stats.numberOfNodes());
            assertEquals(10, expectedVal.get());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }
}
