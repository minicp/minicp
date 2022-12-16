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

import org.javagrader.GraderExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(GraderExtension.class)
public class StateSparseBitSetTest extends StateManagerTest {

    @ParameterizedTest
    @MethodSource("getStateManager")
    public void testExample(StateManager sm) {
        StateSparseBitSet set = new StateSparseBitSet(sm, 256);

        StateSparseBitSet.BitSet b1 = set.new BitSet(); // [0..59] U [130..255]
        StateSparseBitSet.BitSet b2 = set.new BitSet(); // [60..129]
        StateSparseBitSet.BitSet b3 = set.new BitSet(); // empty

        for (int i = 0; i < 256; i++) {
            if (i < 60 || i >= 130) {
                b1.set(i);
            } else {
                b2.set(i);
            }
        }

        set.intersect(b1); // set is now [0..59] U [130..255]

        assertFalse(set.hasEmptyIntersection(b1));
        assertTrue(set.hasEmptyIntersection(b2));

        sm.saveState();

        set.intersect(b3); // set is now empty

        assertTrue(set.hasEmptyIntersection(b1));
        assertTrue(set.hasEmptyIntersection(b2));
        assertTrue(set.hasEmptyIntersection(b3));

        sm.restoreState();  // set is now [0..59] U [130..255]

        assertFalse(set.hasEmptyIntersection(b1));

        assertTrue(set.hasEmptyIntersection(b2));
    }


}