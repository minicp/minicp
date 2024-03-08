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
import static minicp.cp.Factory.allDifferent;

/**
 * Hamiltonian Circuit Constraint with a successor model
 */
public class Circuit extends AbstractConstraint {

    private final IntVar[] x;
    protected final StateInt[] dest;
    protected final StateInt[] orig;
    protected final StateInt[] lengthToDest;

    /**
     * Creates an Hamiltonian Circuit Constraint
     * with a successor model.
     *
     * @param x the variables representing the successor array that is
     *          {@code x[i]} is the city visited after city i
     */
    public Circuit(IntVar[] x) {
        super(x[0].getSolver());
        this.x = x;
        dest = new StateInt[x.length];
        orig = new StateInt[x.length];
        lengthToDest = new StateInt[x.length];
        for (int i = 0; i < x.length; i++) {
            dest[i] = getSolver().getStateManager().makeStateInt(i);
            orig[i] = getSolver().getStateManager().makeStateInt(i);
            lengthToDest[i] = getSolver().getStateManager().makeStateInt(0);
        }
    }


    @Override
    public void post() {
        getSolver().post(allDifferent(x));
        // TODO
        // Hint: use x[i].whenFixed(...) to call the fix
         throw new NotImplementedException("Circuit");
    }


    protected void fix(int i) {
        // TODO
         throw new NotImplementedException("Circuit");
    }
}
