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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StateBoolTest extends StateManagerTest {


    @Test
    public void testStateBool() {
        StateManager sm = stateFactory.get();

        State<Boolean> b1 = sm.makeStateRef(true);
        State<Boolean> b2 = sm.makeStateRef(false);

        sm.saveState();

        b1.setValue(true);
        b1.setValue(false);
        b1.setValue(true);

        b2.setValue(false);
        b2.setValue(true);

        sm.restoreState();

        assertTrue(b1.value());
        assertFalse(b2.value());

    }

    @Test
    public void bugMagicOnRestore() {
        StateManager sm = stateFactory.get();

        State<Boolean> a = sm.makeStateRef(true);
        // level 0, a is true

        sm.saveState(); // level 1, a is true recorded
        sm.saveState(); // level 2, a is true recorded

        a.setValue(false);

        sm.restoreState(); // level 1, a is true

        a.setValue(false); // level 1, a is false

        sm.saveState(); // level 2, a is false recorded

        sm.restoreState(); // level 1 a is false
        sm.restoreState(); // level 0 a is true

        assertEquals(true, a.value());

    }


}
