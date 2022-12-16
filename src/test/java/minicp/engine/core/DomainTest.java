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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class DomainTest extends SolverTest {

    private static class MyDomainListener implements DomainListener {

        int nFix = 0;
        int nChange = 0;
        int nRemoveBelow = 0;
        int nRemoveAbove = 0;

        @Override
        public void empty() {
        }

        @Override
        public void fix() {
            nFix++;
        }

        @Override
        public void change() {
            nChange++;
        }

        @Override
        public void changeMin() {
            nRemoveBelow++;
        }

        @Override
        public void changeMax() {
            nRemoveAbove++;
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testDomain1(Solver cp) {
        MyDomainListener dlistener = new MyDomainListener();
        IntDomain dom = new SparseSetDomain(cp.getStateManager(), 5, 10);

        dom.removeAbove(8, dlistener);

        assertEquals(1, dlistener.nChange);
        assertEquals(0, dlistener.nFix);
        assertEquals(1, dlistener.nRemoveAbove);
        assertEquals(0, dlistener.nRemoveBelow);

        dom.remove(6, dlistener);

        assertEquals(2, dlistener.nChange);
        assertEquals(0, dlistener.nFix);
        assertEquals(1, dlistener.nRemoveAbove);
        assertEquals(0, dlistener.nRemoveBelow);

        dom.remove(5, dlistener);

        assertEquals(3, dlistener.nChange);
        assertEquals(0, dlistener.nFix);
        assertEquals(1, dlistener.nRemoveAbove);
        assertEquals(1, dlistener.nRemoveBelow);

        dom.remove(7, dlistener);

        assertEquals(4, dlistener.nChange);
        assertEquals(1, dlistener.nFix);
        assertEquals(1, dlistener.nRemoveAbove);
        assertEquals(2, dlistener.nRemoveBelow);

    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testDomain2(Solver cp) {
        MyDomainListener dlistener = new MyDomainListener();
        IntDomain dom = new SparseSetDomain(cp.getStateManager(), 5, 10);

        dom.removeAllBut(7, dlistener);

        assertEquals(1, dlistener.nChange);
        assertEquals(1, dlistener.nFix);
        assertEquals(1, dlistener.nRemoveAbove);
        assertEquals(1, dlistener.nRemoveBelow);

    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testDomain3(Solver cp) {
        MyDomainListener dlistener = new MyDomainListener();
        IntDomain dom = new SparseSetDomain(cp.getStateManager(), 5, 10);

        dom.removeAbove(5, dlistener);

        assertEquals(1, dlistener.nChange);
        assertEquals(1, dlistener.nFix);
        assertEquals(1, dlistener.nRemoveAbove);
        assertEquals(0, dlistener.nRemoveBelow);

    }


}
