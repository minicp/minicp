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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ThetaTreeTest {

    @Test
    public void simpleTest0() {
        ThetaTree thetaTree = new ThetaTree(4);
        thetaTree.insert(0, 5, 5);
        assertEquals(5, thetaTree.getECT());
        thetaTree.insert(1, 31, 6);
        assertEquals(31, thetaTree.getECT());
        thetaTree.insert(2, 30, 4);
        assertEquals(35, thetaTree.getECT());
        thetaTree.insert(3, 42, 10);
        assertEquals(45, thetaTree.getECT());
        thetaTree.remove(3);
        assertEquals(35, thetaTree.getECT());
        thetaTree.reset();
        assertEquals(Integer.MIN_VALUE, thetaTree.getECT());
    }


}
