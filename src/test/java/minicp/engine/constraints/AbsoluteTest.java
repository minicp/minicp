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
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import minicp.util.NotImplementedExceptionAssume;
import org.junit.Test;

import static minicp.cp.Factory.*;
import static org.junit.Assert.*;

@GradeClass(totalValue = 1, defaultCpuTimeout = 1000)
public class AbsoluteTest extends SolverTest {
    @Test
    public void simpleTest0() {

        try {
            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, -5, 5);
            IntVar y = makeIntVar(cp, -10, 10);

            cp.post(new Absolute(x, y));

            assertEquals(0, y.min());
            assertEquals(5, y.max());
            assertEquals(11, x.size());

            x.removeAbove(-2);
            cp.fixPoint();

            assertEquals(2, y.min());

            x.removeBelow(-4);
            cp.fixPoint();

            assertEquals(4, y.max());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void simpleTest1() {
        try {
            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, -5, 5);
            IntVar y = makeIntVar(cp, -10, 10);

            cp.post(notEqual(x, 0));
            cp.post(notEqual(x, 5));
            cp.post(notEqual(x, -5));

            cp.post(new Absolute(x, y));


            assertEquals(1, y.min());
            assertEquals(4, y.max());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Test
    public void simpleTest2() {
        try {
            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, -5, 0);
            IntVar y = makeIntVar(cp, 4, 4);

            cp.post(new Absolute(x, y));

            assertTrue(x.isFixed());
            assertTrue(y.isFixed());
            assertEquals(-4, x.max());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void simpleTest3() {
        try {
            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, 7, 7);
            IntVar y = makeIntVar(cp, -1000, 12);

            cp.post(new Absolute(x, y));

            assertTrue(x.isFixed());
            assertTrue(y.isFixed());
            assertEquals(7, y.max());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void simpleTest4() {
        try {
            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, -5, 10);
            IntVar y = makeIntVar(cp, -6, 7);

            cp.post(new Absolute(x, y));

            assertEquals(7, x.max());
            assertEquals(-5, x.min());

            cp.post(notEqual(y, 0));

            cp.post(lessOrEqual(x,4));

            assertEquals(5, y.max());

            cp.post(lessOrEqual(x,-2));

            assertEquals(2, y.min());

            y.removeBelow(5);
            cp.fixPoint();

            assertTrue(x.isFixed());
            assertTrue(y.isFixed());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


}
