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
 * Sequential Search combinator that linearly
 * considers a list of branching generator.
 * One branching of this list is executed
 * when all the previous ones are exhausted, that is
 * they return an empty array.
 */
public class Sequencer implements Supplier<Procedure[]> {
    private Supplier<Procedure[]>[] branching;

    /**
     * Creates a sequential search combinator.
     *
     * @param branching the sequence of branching
     */
    public Sequencer(Supplier<Procedure[]>... branching) {
        this.branching = branching;
    }

    @Override
    public Procedure[] get() {
        for (int i = 0; i < branching.length; i++) {
            Procedure[] alts = branching[i].get();
            if (alts.length != 0)
                return alts;
        }
        return BranchingScheme.EMPTY;
    }
}
