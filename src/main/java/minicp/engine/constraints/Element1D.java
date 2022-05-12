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

import minicp.cp.Factory;
import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.Constraint;
import minicp.engine.core.IntVar;
import minicp.state.StateInt;
import minicp.state.StateManager;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;

import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * Element Constraint modeling {@code array[y] = z}
 *
 */
public class Element1D extends AbstractConstraint {

    private final int[] t;


    private final Integer[] sortedPerm;
    private final StateInt low;
    private final StateInt up;

    private final IntVar y;
    private final IntVar z;


    /**
     * Creates an element constraint {@code array[y] = z}
     *
     * @param array the array to index
     * @param y the index variable
     * @param z the result variable
     */
    public Element1D(int[] array, IntVar y, IntVar z) {
        super(y.getSolver());
        this.t = array;

        sortedPerm = new Integer[t.length];
        for (int i = 0; i < t.length; i++) {
            sortedPerm[i] = i;
        }
        Arrays.sort(sortedPerm,Comparator.comparingInt(i -> t[i]));

        StateManager sm = getSolver().getStateManager();
        low = sm.makeStateInt(0);
        up = sm.makeStateInt(t.length - 1);

        this.y = y;
        this.z = z;
    }

    @Override
    public void post() {
         throw new NotImplementedException("Element1D");
    }

    @Override
    public void propagate() {
         throw new NotImplementedException("Element1D");
    }
}
