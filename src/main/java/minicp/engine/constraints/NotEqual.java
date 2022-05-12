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
 * Copyright (v)  2018. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */

package minicp.engine.constraints;

import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.IntVar;
import minicp.util.exception.NotImplementedException;

/**
 * Not Equal constraint between two variables
 */
public class NotEqual extends AbstractConstraint {
    private final IntVar x, y;
    private final int v;

    /**
     * Creates a constraint such
     * that {@code x != y + v}
     * @param x the left member
     * @param y the right memer
     * @param v the offset value on y
     * @see minicp.cp.Factory#notEqual(IntVar, IntVar, int)
     */
    public NotEqual(IntVar x, IntVar y, int v) { // x != y + v
        super(x.getSolver());
        this.x = x;
        this.y = y;
        this.v = v;
    }

    /**
     * Creates a constraint such
     * that {@code x != y}
     * @param x the left member
     * @param y the right memer
     * @see minicp.cp.Factory#notEqual(IntVar, IntVar)
     */
    public NotEqual(IntVar x, IntVar y) { // x != y
        this(x, y, 0);
    }

    @Override
    public void post() {
        if (y.isFixed())
            x.remove(y.min() + v);
        else if (x.isFixed())
            y.remove(x.min() - v);
        else {
            x.propagateOnFix(this);
            y.propagateOnFix(this);
        }
    }

    @Override
    public void propagate() {
        if (y.isFixed())
            x.remove(y.min() + v);
        else y.remove(x.min() - v);
        setActive(false);
    }
}
