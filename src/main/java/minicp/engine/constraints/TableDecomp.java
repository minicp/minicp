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

import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.IntVar;

public class TableDecomp extends AbstractConstraint {
    private final IntVar[] x;
    private final int[][] table;

    /**
     * Decomposition of a table constraint.
     * <p>The table constraint ensures that
     * {@code x} is a row from the given table.
     * More exactly, there exist some row <i>i</i>
     * such that
     * {@code x[0]==table[i][0], x[1]==table[i][1], etc}.
     * <p>This constraint is sometimes called <i>in extension</i> constraint
     * as the user enumerates the set of solutions that can be taken
     * by the variables.
     *
     * @param x  the non empty set of variables to constraint
     * @param table the possible set of solutions for x.
     *              The second dimension must be of the same size as the array x.
     */
    public TableDecomp(IntVar[] x, int[][] table) {
        super(x[0].getSolver());
        this.x = x;
        this.table = table;
    }

    @Override
    public void post() {
        for (IntVar var : x)
            var.propagateOnDomainChange(this);
        propagate();
    }

    @Override
    public void propagate() {
        for (int i = 0; i < x.length; i++) {
            for (int v = x[i].min(); v <= x[i].max(); v++) {
                if (x[i].contains(v)) {
                    boolean valueIsSupported = false;
                    for (int tupleIdx = 0; tupleIdx < table.length && !valueIsSupported; tupleIdx++) {
                        if (table[tupleIdx][i] == v) {
                            boolean allValueVariableSupported = true;
                            for (int j = 0; j < x.length && allValueVariableSupported; j++) {
                                if (!x[j].contains(table[tupleIdx][j])) {
                                    allValueVariableSupported = false;
                                }
                            }
                            valueIsSupported = allValueVariableSupported;
                        }
                    }
                    if (!valueIsSupported)
                        x[i].remove(v);
                }
            }
        }
    }
}
