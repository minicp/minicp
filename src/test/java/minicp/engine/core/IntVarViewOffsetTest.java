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

package minicp.engine.core;

import minicp.engine.SolverTest;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.IntOverFlowException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static minicp.cp.Factory.makeIntVar;
import static minicp.cp.Factory.plus;
import static org.junit.jupiter.api.Assertions.*;


public class IntVarViewOffsetTest extends SolverTest {

    public boolean propagateCalled = false;

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testIntVar(Solver cp) {

        IntVar x = plus(makeIntVar(cp, -3, 4), 3); // domain is {0,1,2,3,4,5,6,7}

        assertEquals(0, x.min());
        assertEquals(7, x.max());
        assertEquals(8, x.size());

        cp.getStateManager().saveState();


        try {

            assertFalse(x.isFixed());

            x.remove(0);
            assertFalse(x.contains(0));
            x.remove(3);
            assertTrue(x.contains(1));
            assertTrue(x.contains(2));
            assertEquals(6, x.size());
            x.removeAbove(6);
            assertEquals(6, x.max());
            x.removeBelow(3);
            assertEquals(4, x.min());
            x.fix(5);
            assertTrue(x.isFixed());
            assertEquals(5, x.max());


        } catch (InconsistencyException e) {
            e.printStackTrace();
            fail("should not fail here");
        }

        try {
            x.fix(4);
            fail("should have failed");
        } catch (InconsistencyException expectedException) {
        }

        cp.getStateManager().restoreState();

        assertEquals(8, x.size());
        assertFalse(x.contains(-1));

    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void onDomainChangeOnFix(Solver cp) {
        propagateCalled = false;

        IntVar x = plus(makeIntVar(cp, 10), 1); // 1..11
        IntVar y = plus(makeIntVar(cp, 10), 1); // 1..11

        Constraint cons = new AbstractConstraint(cp) {

            @Override
            public void post() {
                x.whenFixed(() -> propagateCalled = true);
                y.whenDomainChange(() -> propagateCalled = true);
            }
        };

        try {
            cp.post(cons);
            x.remove(9);
            cp.fixPoint();
            assertFalse(propagateCalled);
            x.fix(5);
            cp.fixPoint();
            assertTrue(propagateCalled);
            propagateCalled = false;
            y.remove(11);
            cp.fixPoint();
            assertFalse(propagateCalled);
            y.remove(10);
            cp.fixPoint();
            assertTrue(propagateCalled);

        } catch (InconsistencyException inconsistency) {
            fail("should not fail");
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void onBoundChange(Solver cp) {

        IntVar x = plus(makeIntVar(cp, 10), 1);
        IntVar y = plus(makeIntVar(cp, 10), 1);

        Constraint cons = new AbstractConstraint(cp) {

            @Override
            public void post() {
                x.whenFixed(() -> propagateCalled = true);
                y.whenDomainChange(() -> propagateCalled = true);
            }
        };

        try {
            cp.post(cons);
            x.remove(9);
            cp.fixPoint();
            assertFalse(propagateCalled);
            x.remove(10);
            cp.fixPoint();
            assertFalse(propagateCalled);
            x.fix(5);
            cp.fixPoint();
            assertTrue(propagateCalled);
            propagateCalled = false;
            assertFalse(y.contains(11));
            y.remove(11);
            cp.fixPoint();
            assertFalse(propagateCalled);
            y.remove(3);
            cp.fixPoint();
            assertTrue(propagateCalled);

        } catch (InconsistencyException inconsistency) {
            fail("should not fail");
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testOverFlow(Solver cp) {
        assertThrowsExactly(IntOverFlowException.class,
                () -> plus(makeIntVar(cp, Integer.MAX_VALUE - 5, Integer.MAX_VALUE - 2), 3));
    }


}
