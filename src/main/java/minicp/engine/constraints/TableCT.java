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
import minicp.state.StateInt;
import minicp.state.StateSparseBitSet;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;

import java.util.Arrays;

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
    protected StateSparseBitSet.SupportBitSet[][] supports;

    protected StateSparseBitSet supportedTuples;
    private StateSparseBitSet.MaskBitSet tmpSupport;

    private StateInt[] lastDomSize; // store the last size of the domain of the variable
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

        supportedTuples = new StateSparseBitSet(this.getSolver().getStateManager(), table.length);

        // Allocate supports
        supports = new StateSparseBitSet.SupportBitSet[x.length][];
        lastDomSize = new StateInt[x.length];
        for (int i = 0; i < x.length; i++) {
            this.x[i] = minus(x[i], x[i].min()); // map the variables domain to start at 0
            supports[i] = new StateSparseBitSet.SupportBitSet[x[i].max() - x[i].min() + 1];
            for (int v = 0; v < supports[i].length; v++) {
                supports[i][v] = supportedTuples.new SupportBitSet();
            }
            lastDomSize[i] = this.getSolver().getStateManager().makeStateInt(-1); // put to -1 to force initial propagation to check all vars
        }

        // Set the supports for each var-va
        for (int t = 0; t < table.length; t++) { // t is the index of the tuple (in table)
            for (int i = 0; i < x.length; i++) { // i is the index of the current variable (in x)
                // TODO 1: fill the support bitset, hint: use {@link StateSparseBitSet.SupportBitSet#set(t}
                 throw new NotImplementedException("TableCT");
            }
        }

        tmpSupport = supportedTuples.new MaskBitSet();
    }

    @Override
    public void post() {
        for (IntVar var : x) {
            var.propagateOnDomainChange(this);
        }
        propagate();
    }

    /**
     * Tells if a variable x[i] has been modified since the last call node in the search tree
     * Uses the value of {@link TableCT#lastDomSize} to verify if the domain size has changed since the last propagation
     *
     * @param i index of the variable in {@link TableCT#x} that must be checked
     * @return true if the domain of x[i] has been changed since the last propagation
     */
    public boolean hasChanged(int i) {
        // TODO 2: use lastDomSize[i] to verify if the domain size of x[i] has changed since last propagation
         throw new NotImplementedException("TableCT");
    }

    @Override
    public void propagate() {
        for (int i = 0; i < x.length; i++) {
            if (hasChanged(i)) {
                // TODO 3: update supportedTuples as
                // supportedTuples &= (supports[i][x[i].min()] | ... | supports[i][x[i].max()] )
                // for all x[i] modified since last call node in the search tree (see TODO 2 )
            }
        }

        // TODO 4 filter impossible values
        for (int i = 0; i < x.length; i++) {
            int nVal = x[i].fillArray(dom);
            for (int v = 0; v < nVal; v++) {
                    // TODO 4 the condition for removing the setValue dom[v] from x[i] is to check if
                    //  there is no intersection between supportedTuples and the support[i][dom[v]]

            }
            lastDomSize[i].setValue(x[i].size()); // store the current domain size to compare during next propagation
        }
         throw new NotImplementedException("TableCT");
    }
}
