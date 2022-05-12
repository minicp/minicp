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
import minicp.engine.core.BoolVar;
import minicp.engine.core.IntVar;

/**
 * Reified equality constraint
 * @see minicp.cp.Factory#isEqual(IntVar, int)
 */
public class IsEqual extends AbstractConstraint { // b <=> x == v

    private final BoolVar b;
    private final IntVar x;
    private final int v;

    /**
     * Returns a boolean variable representing
     * whether one variable is equal to the given constant.
     * @param x the variable
     * @param v the constant
     * @param b the boolean variable that is set to true
     *          if and only if x takes the value v
     * @see minicp.cp.Factory#isEqual(IntVar, int)
     */
    public IsEqual(BoolVar b, IntVar x, int v) {
        super(b.getSolver());
        this.b = b;
        this.x = x;
        this.v = v;
    }

    @Override
    public void post() {
        propagate();
        if (isActive()) {
            x.propagateOnDomainChange(this);
            b.propagateOnFix(this);
        }
    }

    @Override
    public void propagate() {
        if (b.isTrue()) {
            x.fix(v);
            setActive(false);
        } else if (b.isFalse()) {
            x.remove(v);
            setActive(false);
        } else if (!x.contains(v)) {
            b.fix(false);
            setActive(false);
        } else if (x.isFixed()) {
            b.fix(true);
            setActive(false);
        }
    }
}
