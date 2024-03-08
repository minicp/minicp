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

package minicp.state;

import minicp.util.exception.NotImplementedException;
import org.javagrader.GraderExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(GraderExtension.class)
public class StateSparseSetTest extends StateManagerTest {

    @ParameterizedTest
    @MethodSource("getStateManager")
    public void testExample(StateManager sm) {
        StateSparseSet set = new StateSparseSet(sm, 9, 0);

        sm.saveState();

        set.remove(4);
        set.remove(6);

        assertFalse(set.contains(4));
        assertFalse(set.contains(6));

        sm.restoreState();

        assertTrue(set.contains(4));
        assertTrue(set.contains(6));

    }

    @ParameterizedTest
    @MethodSource("getStateManager")
    public void testReversibleSparseSet(StateManager sm) {
        StateSparseSet set = new StateSparseSet(sm, 10, 0);

        assertEquals(toSet(set.toArray()), toSet(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));

        sm.saveState();

        set.remove(1);
        set.remove(0);

        assertEquals(2, set.min());

        set.remove(8);
        set.remove(9);

        assertEquals(toSet(set.toArray()), toSet(2, 3, 4, 5, 6, 7));
        assertEquals(7, set.max());

        sm.restoreState();
        sm.saveState();

        assertEquals(10, set.size());

        for (int i = 0; i < 10; i++) {
            assertTrue(set.contains(i));
        }
        assertFalse(set.contains(10));

        assertEquals(0, set.min());
        assertEquals(9, set.max());

        set.removeAllBut(2);

        for (int i = 0; i < 10; i++) {
            if (i != 2) assertFalse(set.contains(i));
        }
        assertTrue(set.contains(2));
        assertEquals(toSet(set.toArray()), toSet(2));


        sm.restoreState();
        sm.saveState();

        assertEquals(10, set.size());

    }

    private Set<Integer> toSet(int... values) {
        Set<Integer> set = new java.util.HashSet<>();
        for (int v : values) {
            set.add(v);
        }
        return set;
    }

    @ParameterizedTest
    @MethodSource("getStateManager")
    public void testRangeConstructor(StateManager sm) {
        try {
            StateSparseSet set = new StateSparseSet(sm, 10, 0);

            for (int i = 0; i < 10; i++) {
                assertTrue(set.contains(i));
            }

            sm.saveState();

            set.remove(4);
            set.remove(5);
            set.remove(0);
            set.remove(1);

            assertEquals(2, set.min());
            assertEquals(9, set.max());

            sm.saveState();

            set.removeAllBut(7);
            assertEquals(7, set.min());
            assertEquals(7, set.max());


            sm.restoreState();
            sm.restoreState();

            for (int i = 0; i < 10; i++) {
                assertTrue(set.contains(i));
            }

        } catch (NotImplementedException e) {
            e.print();
        }
    }

    @ParameterizedTest
    @MethodSource("getStateManager")
    public void testRemoveBelow(StateManager sm) {
        StateSparseSet set = new StateSparseSet(sm, 10, 0);

        for (int i = 0; i < 10; i++) {
            assertTrue(set.contains(i));
        }

        sm.saveState();

        set.removeBelow(5);

        assertEquals(5, set.min());
        assertEquals(9, set.max());

        sm.saveState();

        set.remove(7);
        set.removeBelow(7);

        assertEquals(8, set.min());

        sm.restoreState();
        sm.restoreState();

        for (int i = 0; i < 10; i++) {
            assertTrue(set.contains(i));
        }
    }

    @ParameterizedTest
    @MethodSource("getStateManager")
    public void testRemoveAbove(StateManager sm) {
        try {
            StateSparseSet set = new StateSparseSet(sm, 10, 0);

            for (int i = 0; i < 10; i++) {
                assertTrue(set.contains(i));
            }

            sm.saveState();

            set.remove(1);
            set.remove(2);

            set.removeAbove(7);

            assertEquals(0, set.min());
            assertEquals(7, set.max());

            sm.saveState();

            set.removeAbove(2);

            assertEquals(0, set.max());

            sm.restoreState();
            sm.restoreState();

            for (int i = 0; i < 10; i++) {
                assertTrue(set.contains(i));
            }

        } catch (NotImplementedException e) {
            e.print();
        }
    }
}
