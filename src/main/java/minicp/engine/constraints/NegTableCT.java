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
import minicp.engine.core.Solver;
import minicp.util.exception.NotImplementedException;

import java.util.ArrayList;
import java.util.BitSet;

import static minicp.cp.Factory.minus;

/**
 * Negative table constraint
 */
public class NegTableCT extends AbstractConstraint {

    private IntVar[] x; //variables
    private int[][] table; //the table
    //supports[i][v] is the set of tuples supported by x[i]=v
    private BitSet[][] conflicts;

    /**
     * Negative Table constraint.
     * <p>Assignment of {@code x_0=v_0, x_1=v_1,...} only valid if there does not
     * exists a row {@code (v_0, v_1, ...)} in the table.
     * The table represents the infeasible assignments for the variables.
     *
     * @param x the variables to constraint. x is not empty.
     * @param table the array of invalid solutions (second dimension must be of same size as the array x)
     */
    public NegTableCT(IntVar[] x, int[][] table) {
        super(x[0].getSolver());
        this.x = new IntVar[x.length];


        // remove duplicate (the negative ct algo does not support it)
        ArrayList<int[]> tableList = new ArrayList<>();
        boolean[] duplicate = new boolean[table.length];
        for (int i = 0; i < table.length; i++) {
            if (!duplicate[i]) {
                tableList.add(table[i]);
                for (int j = i + 1; j < table.length; j++) {
                    if (i != j && !duplicate[j]) {
                        boolean same = true;
                        for (int k = 0; k < x.length; k++) {
                            same &= table[i][k] == table[j][k];
                        }
                        if (same) {
                            duplicate[j] = true;
                        }
                    }
                }
            }
        }
        this.table = tableList.toArray(new int[0][]);

        // Allocate supportedByVarVal
        conflicts = new BitSet[x.length][];
        for (int i = 0; i < x.length; i++) {
            this.x[i] = minus(x[i], x[i].min()); // map the variables domain to start at 0
            conflicts[i] = new BitSet[x[i].max() - x[i].min() + 1];
            for (int j = 0; j < conflicts[i].length; j++)
                conflicts[i][j] = new BitSet();
        }

        // Set values in supportedByVarVal, which contains all the tuples supported by each var-val pair
        for (int i = 0; i < this.table.length; i++) { //i is the index of the tuple (in table)
            for (int j = 0; j < x.length; j++) { //j is the index of the current variable (in x)
                if (x[j].contains(this.table[i][j])) {
                    conflicts[j][this.table[i][j] - x[j].min()].set(i);
                }
            }
        }
    }

    @Override
    public void post() {
        // TODO
         throw new NotImplementedException("NegTableCT");
    }

    @Override
    public void propagate() {
        // TODO
         throw new NotImplementedException("NegTableCT");
    }
}
