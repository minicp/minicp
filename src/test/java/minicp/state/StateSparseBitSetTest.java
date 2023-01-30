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

import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.NotImplementedException;
import org.javagrader.ConditionalOrderingExtension;
import org.javagrader.Grade;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Grade(cpuTimeout = 300, unit = TimeUnit.MILLISECONDS)
@ExtendWith(ConditionalOrderingExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StateSparseBitSetTest extends StateManagerTest {

    @Grade(0)
    @ParameterizedTest
    @MethodSource("getStateManager")
    @Order(1)
    public void testIntersects(StateManager sm) {
        try {
            StateSparseBitSet set = new StateSparseBitSet(sm, 256);

            StateSparseBitSet.SupportBitSet b1 = set.new SupportBitSet(); // [0..59] U [130..255]
            StateSparseBitSet.SupportBitSet b2 = set.new SupportBitSet(); // [60..129]
            StateSparseBitSet.SupportBitSet b3 = set.new SupportBitSet(); // empty

            String b1s = "[0..59] U [130..255]";
            String b2s = "[60..129]";
            String b3s = "empty bitset";

            for (int i = 0; i < 256; i++) {
                if (i < 60 || i >= 130) {
                    b1.set(i);
                } else {
                    b2.set(i);
                }
            }

            set.and(b1); // set is now [0..59] U [130..255]
            String sets = b1s;

            assertTrue(set.intersects(b1), "You did not detect an intersection between " + sets + " and " + b1s);
            assertFalse(set.intersects(b2), "You said that there is an intersection between " + sets + " and " + b2s);

            sm.saveState();

            set.and(b3); // set is now empty
            sets = b3s;

            assertFalse(set.intersects(b1), "You said that there is an intersection between " + sets + " and " + b1s);
            assertFalse(set.intersects(b2), "You said that there is an intersection between " + sets + " and " + b2s);
            assertFalse(set.intersects(b3), "You said that there is an intersection between " + sets + " and " + b3s);

            sm.restoreState();  // set is now [0..59] U [130..255]
            sets = b1s;

            assertTrue(set.intersects(b1), "You did not detect an intersection between " + sets + " and " + b1s);
            assertFalse(set.intersects(b2), "You said that there is an intersection between " + sets + " and " + b2s);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Grade(0)
    @ParameterizedTest
    @MethodSource("getStateManager")
    @Order(1)
    public void testEmptyStateSparseBitSet(StateManager sm) {
        try {
            StateSparseBitSet set = new StateSparseBitSet(sm, 0);
            StateSparseBitSet.SupportBitSet b1 = set.new SupportBitSet();
            assertFalse(set.intersects(b1));
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            fail("Your implementation isn't resilient to StateSparseBitSet defined with n=0\n" + e);
        }
    }

    @ParameterizedTest
    @MethodSource("getStateManager")
    @Order(2)
    public void testIntersectsResidueOnly1(StateManager sm) {
        try {
            StateSparseBitSet set = new StateSparseBitSet(sm, 256);
            StateSparseBitSet.SupportBitSet b1 = set.new SupportBitSet(); // [128..255]
            StateSparseBitSet.SupportBitSet b2 = set.new SupportBitSet(); // [0..255]
            String b1s = "[128..255]";
            String b2s = "[0..255]";
            String between = "between " + b1s + " and " + b2s;

            for (int i = 0; i < 256; i++) {
                b2.set(i);
                if (i >= 128)
                    b1.set(i);
            }
            set.and(b2); // set is now [0..255]


            // manually set the residue to 0: nothing is selected;
            b1.residue = 0;
            assertFalse(set.intersectsResidueOnly(b1), "There is no intersection " + between + " at word " + 0);

            // residue select the indices [64..127]
            b1.residue = 1;
            assertFalse(set.intersectsResidueOnly(b1), "There is no intersection " + between + " at word " + 1);

            // residue select the indices [128..191]
            b1.residue = 2;
            assertTrue(set.intersectsResidueOnly(b1), "There is an intersection " + between + " at word " + 2);

            // residue select the indices [192..255]
            b1.residue = 3;
            assertTrue(set.intersectsResidueOnly(b1), "There is an intersection " + between + " at word " + 3);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getStateManager")
    @Order(2)
    public void testIntersectsResidueOnly2(StateManager sm) {
        try {
            StateSparseBitSet set = new StateSparseBitSet(sm, 256);
            StateSparseBitSet.SupportBitSet bSet = set.new SupportBitSet(); // [123..194]
            StateSparseBitSet.SupportBitSet b1 = set.new SupportBitSet();   // [110..180]
            StateSparseBitSet.SupportBitSet b2 = set.new SupportBitSet();   // [181..200]
            String bs = "[123..194]";
            String b1s = "[110..180]";
            String b2s = "[181..200]";
            String between1 = "between " + bs + " and " + b1s;
            String between2 = "between " + bs + " and " + b2s;

            for (int i = 0; i < 256; i++) {
                if (i >= 123 && i <= 194)
                    bSet.set(i);
                if (i >= 110 && i <= 180)
                    b1.set(i);
                if (i >= 181 && i <= 200)
                    b2.set(i);
            }
            set.and(bSet); // set is now [123..194]

            // manually set the residue to 0: nothing is selected;
            b1.residue = 0;
            assertFalse(set.intersectsResidueOnly(b1), "There is no intersection " + between1 + " at word " + 0);
            b2.residue = 0;
            assertFalse(set.intersectsResidueOnly(b2), "There is no intersection " + between2 + " at word " + 0);

            // residue selects the indices [64..127]
            b1.residue = 1;
            assertTrue(set.intersectsResidueOnly(b1), "There is an intersection " + between1 + " at word " + 1);
            b2.residue = 1;
            assertFalse(set.intersectsResidueOnly(b2), "There is no intersection " + between2 + " at word " + 1);

            // residue selects the indices [128..191]
            b1.residue = 2;
            assertTrue(set.intersectsResidueOnly(b1), "There is an intersection " + between1 + " at word " + 2);
            b2.residue = 2;
            assertTrue(set.intersectsResidueOnly(b2), "There is an intersection " + between2 + " at word " + 2);

            // residue selects the indices [192..255]
            b1.residue = 3;
            assertFalse(set.intersectsResidueOnly(b1), "There is no intersection " + between1 + " at word " + 3);
            b2.residue = 3;
            assertTrue(set.intersectsResidueOnly(b2), "There is an intersection " + between2 + " at word " + 3);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getStateManager")
    @Order(3)
    public void testResidue(StateManager sm) {
        try {
            StateSparseBitSet set = new StateSparseBitSet(sm, 256);
            StateSparseBitSet.SupportBitSet b1 = set.new SupportBitSet(); // [64..255]

            for (int i = 0; i < 256; i++) {
                if (i >= 64)
                    b1.set(i);
            }

            set.and(b1); // set is now [0..59] U [130..255]
            // residue has by default value (i.e. 0)
            assertEquals(b1.residue, 0,
                    "You shouldn't be modifying the initial value of the residue");

            sm.saveState();

            set.intersects(b1);

            int r = b1.residue;
            // word 0 is empty, new residue should have been found
            assertNotEquals(b1.residue, 0,
                    "You are not updating the value of the residue");

            sm.restoreState();  // set is now [0..59] U [130..255]

            set.intersects(b1); // residue still leading to an intersection

            // no change should have been done to the residue
            assertEquals(b1.residue, r,
                    "The value of the residue should not be restored");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

}