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

package minicp.search;


import minicp.cp.BranchingScheme;
import minicp.util.Procedure;
import minicp.util.exception.NotImplementedException;

import java.util.function.Supplier;

/**
 * Branching combinator
 * that ensures that that the alternatives created are always within the
 * discrepancy limit.
 * The discrepancy of an alternative generated
 * for a given node is the distance from the left most alternative.
 * The discrepancy of a node is the sum of the discrepancy of its ancestors.
 */
public class LimitedDiscrepancyBranching implements Supplier<Procedure[]> {

    private int curD;
    private final int maxD;
    private final Supplier<Procedure[]> bs;

    /**
     * Creates a discprepancy combinator on a given branching.
     *
     * @param branching the branching on which to apply the discrepancy combinator
     * @param maxDiscrepancy the maximum discrepancy limit. Any node exceeding
     *                       that limit is pruned.
     */
    public LimitedDiscrepancyBranching(Supplier<Procedure[]> branching, int maxDiscrepancy) {
        if (maxDiscrepancy < 0) throw new IllegalArgumentException("max discrepancy should be >= 0");
        this.bs = branching;
        this.maxD = maxDiscrepancy;
    }

    @Override
    public Procedure[] get() {
        // Hint:
        // Filter-out alternatives from that would exceed maxD
        // Therefore wrap each alternative
        // such that the call method of the wrapped alternatives
        // augment the curD depending on its position
        // +0 for alts[0], ..., +i for alts[i]

         throw new NotImplementedException();
    }
}
