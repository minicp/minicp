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
import minicp.util.exception.NotImplementedException;

import java.util.stream.IntStream;

/**
 * Forward Checking filtering AllDifferent Constraint
 *
 * Whenever one variable is fixed, this value
 * is removed from the domain of other variables.
 * This filtering is weaker than the {@link AllDifferentDC}
 * but executes faster.
 */
public class AllDifferentFWC extends AbstractConstraint {

    private IntVar[] x;

    

    public AllDifferentFWC(IntVar... x) {
        super(x[0].getSolver());
        this.x = x;
        
    }

    @Override
    public void post() {
         throw new NotImplementedException("AllDifferentFWC");
    }

    @Override
    public void propagate() {
        // TODO use the sparse-set trick as seen in Sum.java
         throw new NotImplementedException("AllDifferentFWC");
    }
}
