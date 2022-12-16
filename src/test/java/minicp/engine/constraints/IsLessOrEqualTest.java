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

import static minicp.cp.BranchingScheme.firstFail;
import static minicp.cp.Factory.*;
import static org.junit.jupiter.api.Assertions.*;

@Grade(cpuTimeout = 1)
public class IsLessOrEqualTest extends SolverTest {

    @ParameterizedTest
    @MethodSource("getSolver")
    public void test1(Solver cp) {
        try {

            IntVar x = makeIntVar(cp, -4, 7);

            BoolVar b = makeBoolVar(cp);

            cp.post(new IsLessOrEqual(b, x, 3));

            DFSearch search = makeDfs(cp, firstFail(x));

            search.onSolution(() ->
                    assertTrue(x.min() <= 3 && b.isTrue() || x.min() > 3 && b.isFalse())
            );

            SearchStatistics stats = search.solve();


            assertEquals(12, stats.numberOfSolutions());

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

            IntVar x = makeIntVar(cp, -4, 7);

            BoolVar b = makeBoolVar(cp);

            cp.post(new IsLessOrEqual(b, x, -2));

            cp.getStateManager().saveState();
            cp.post(equal(b, 1));
            assertEquals(-2, x.max());
            cp.getStateManager().restoreState();

            cp.getStateManager().saveState();
            cp.post(equal(b, 0));
            assertEquals(-1, x.min());
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
            cp.post(equal(x, -2));
            {
                BoolVar b = makeBoolVar(cp);
                cp.post(new IsLessOrEqual(b, x, -2));
                assertTrue(b.isTrue());
            }
            {
                BoolVar b = makeBoolVar(cp);
                cp.post(new IsLessOrEqual(b, x, -3));
                assertTrue(b.isFalse());
            }

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

            IntVar x = makeIntVar(cp, -4, 7);
            BoolVar b = makeBoolVar(cp);

            cp.getStateManager().saveState();
            cp.post(equal(b, 1));
            cp.post(new IsLessOrEqual(b, x, -2));
            assertEquals(-2, x.max());
            cp.getStateManager().restoreState();

            cp.getStateManager().saveState();
            cp.post(equal(b, 0));
            cp.post(new IsLessOrEqual(b, x, -2));
            assertEquals(-1, x.min());
            cp.getStateManager().restoreState();


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void test5(Solver cp) {
        try {

            IntVar x = makeIntVar(cp, -5, 10);
            BoolVar b = makeBoolVar(cp);

            cp.getStateManager().saveState();
            cp.post(new IsLessOrEqual(b, x, -6));
            assertTrue(b.isFixed());
            assertTrue(b.isFalse());
            cp.getStateManager().restoreState();

            cp.getStateManager().saveState();
            cp.post(new IsLessOrEqual(b, x, 11));
            assertTrue(b.isFixed());
            assertTrue(b.isTrue());
            cp.getStateManager().restoreState();

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void test6(Solver cp) {
        try {

            IntVar x = makeIntVar(cp, -5, -3);
            BoolVar b = makeBoolVar(cp);

            cp.getStateManager().saveState();
            cp.post(new IsLessOrEqual(b, x, -3));
            assertTrue(b.isTrue());
            cp.getStateManager().restoreState();


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }



}
