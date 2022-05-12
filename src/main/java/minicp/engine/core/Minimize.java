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

import minicp.search.Objective;
import minicp.util.exception.InconsistencyException;

/**
 * Minimization objective function
 */
public class Minimize implements Objective {
    private int bound = Integer.MAX_VALUE;
    private final IntVar x;

    public Minimize(IntVar x) {
        this.x = x;
        x.getSolver().onFixPoint(() -> x.removeAbove(bound));
    }

    public void tighten() {
        if (!x.isFixed()) throw new RuntimeException("objective not fixed");
        this.bound = x.max() - 1;
        throw InconsistencyException.INCONSISTENCY;
    }
}
