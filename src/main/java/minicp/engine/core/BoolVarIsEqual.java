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

import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;

public class BoolVarIsEqual extends IntVarImpl implements BoolVar {

    public BoolVarIsEqual(IntVar x, int v) {
        super(x.getSolver(), 0, 1);

        if (!x.contains(v)) {
            fix(false);
        } else if (x.isFixed() && x.min() == v) {
            fix(true);
        } else {

            this.whenFixed(() -> {
                if (isTrue()) x.fix(v);
                else x.remove(v);
            });

            x.whenDomainChange(() -> {
                if (!x.contains(v)) {
                    this.fix(false);
                }
            });

            x.whenFixed(() -> {
                if (x.min() == v) {
                    fix(true);
                } else {
                    fix(false);
                }
            });

        }

    }

    @Override
    public boolean isTrue() {
        return min() == 1;
    }

    @Override
    public boolean isFalse() {
        return max() == 0;
    }

    @Override
    public void fix(boolean b) throws InconsistencyException {
        fix(b ? 1 : 0);
    }
}
