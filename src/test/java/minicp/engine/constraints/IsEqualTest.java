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
import org.junit.Test;

import static minicp.cp.BranchingScheme.firstFail;
import static minicp.cp.Factory.*;
import static org.junit.Assert.*;


public class IsEqualTest extends SolverTest {

    @Test
    public void test1() {
        try {

            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, -4, 7);

            BoolVar b = isEqual(x, -2);

            DFSearch search = makeDfs(cp, firstFail(x));

            SearchStatistics stats = search.solve();

            search.onSolution(() ->
                    assertEquals(-2 == x.min(), b.isTrue())
            );

            assertEquals(12, stats.numberOfSolutions());


        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void test2() {
        try {

            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, -4, 7);

            BoolVar b = isEqual(x, -2);

            cp.getStateManager().saveState();
            cp.post(equal(b, 1));
            assertEquals(-2, x.min());
            cp.getStateManager().restoreState();

            cp.getStateManager().saveState();
            cp.post(equal(b, 0));
            assertFalse(x.contains(-2));
            cp.getStateManager().restoreState();

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void test3() {

        try {
            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, -4, 7);
            cp.post(equal(x, -2));

            {
                BoolVar b = makeBoolVar(cp);
                cp.post(new IsEqual(b, x, -2));
                assertTrue(b.isTrue());
            }
            {
                BoolVar b = makeBoolVar(cp);
                cp.post(new IsEqual(b, x, -3));
                assertTrue(b.isFalse());
            }

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void test4() {
        try {

            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, -4, 7);
            BoolVar b = makeBoolVar(cp);

            cp.getStateManager().saveState();
            cp.post(equal(b, 1));
            cp.post(new IsEqual(b, x, -2));
            assertEquals(-2, x.min());
            cp.getStateManager().restoreState();

            cp.getStateManager().saveState();
            cp.post(equal(b, 0));
            cp.post(new IsEqual(b, x, -2));
            assertFalse(x.contains(-2));
            cp.getStateManager().restoreState();


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


}
