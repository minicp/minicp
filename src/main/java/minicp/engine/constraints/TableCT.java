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
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;

import java.util.Arrays;
import java.util.BitSet;

import static minicp.cp.Factory.minus;

/**
 * Implementation of Compact Table algorithm described in
 * <p><i>Compact-Table: Efficiently Filtering Table Constraints with Reversible Sparse Bit-Sets</i>
 * Jordan Demeulenaere, Renaud Hartert, Christophe Lecoutre, Guillaume Perez, Laurent Perron, Jean-Charles Régin, Pierre Schaus
 * <p>See <a href="https://www.info.ucl.ac.be/~pschaus/assets/publi/cp2016-compacttable.pdf">The article.</a>
 */
public class TableCT extends AbstractConstraint {
    private IntVar[] x; //variables
    private int[][] table; //the table
    //supports[i][v] is the set of tuples supported by x[i]=v
    private BitSet[][] supports;

    private BitSet supportedTuples;
    private BitSet tmpSupport;

    private int[] dom; // domain iterator

    /**
     * Table constraint.
     * <p>The table constraint ensures that
     * {@code x} is a row from the given table.
     * More exactly, there exist some row <i>i</i>
     * such that
     * {@code x[0]==table[i][0], x[1]==table[i][1], etc}.
     *
     * <p>This constraint is sometimes called <i>in extension</i> constraint
     * as the user enumerates the set of solutions that can be taken
     * by the variables.
     *
     * @param x  the non empty set of variables to constraint
     * @param table the possible set of solutions for x.
     *              The second dimension must be of the same size as the array x.
     */
    public TableCT(IntVar[] x, int[][] table) {
        super(x[0].getSolver());
        this.x = new IntVar[x.length];
        this.table = table;
        dom = new int[Arrays.stream(x).map(var -> var.size()).max(Integer::compare).get()];

        // Allocate supportedByVarVal
        supports = new BitSet[x.length][];
        for (int i = 0; i < x.length; i++) {
            this.x[i] = minus(x[i], x[i].min()); // map the variables domain to start at 0
            supports[i] = new BitSet[x[i].max() - x[i].min() + 1];
            for (int j = 0; j < supports[i].length; j++)
                supports[i][j] = new BitSet();
        }

        // Set values in supportedByVarVal, which contains all the tuples supported by each var-val pair
        for (int t = 0; t < table.length; t++) { //i is the index of the tuple (in table)
            for (int i = 0; i < x.length; i++) { //j is the index of the current variable (in x)
                if (x[i].contains(table[t][i])) {
                    supports[i][table[t][i] - x[i].min()].set(t);
                }
            }
        }

        supportedTuples = new BitSet(table.length);
        tmpSupport = new BitSet(table.length);
    }

    @Override
    public void post() {
        for (IntVar var : x)
            var.propagateOnDomainChange(this);
        propagate();
    }



    @Override
    public void propagate() {


        // Bit-set of tuple indices all set to 1
        supportedTuples.set(0, table.length);

        // TODO 1: compute supportedTuples as
        // supportedTuples = (supports[0][x[0].min()] | ... | supports[0][x[0].max()] ) & ... &
        //                   (supports[x.length][x[0].min()] | ... | supports[x.length][x[0].max()] )
        //

         // This should be displayed instead of the actual code

        // TODO 2
        for (int i = 0; i < x.length; i++) {
            int nVal = x[i].fillArray(dom);
            for (int v = 0; v < nVal; v++) {
                    // TODO 2 the condition for removing the setValue dom[v] from x[i] is to check if
                    // there is no intersection between supportedTuples and the support[i][dom[v]]
                     throw new NotImplementedException();

            }
        }


        //throw new NotImplementedException("TableCT");
    }
}
