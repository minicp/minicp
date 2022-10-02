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

/**
 * Boolean variable, that can be used as a 0-1 IntVar
 * <p>0 corresponds to false, and 1 corresponds to true
 */
public interface BoolVar extends IntVar {

    /**
     * Tests if the variable is fixed to true.
     * @return true if the variable is fixed to true (value 1)
     */
    boolean isTrue();

    /**
     * Tests if the variable is fixed to false.
     * @return true if the variable is fixed to false (value 0)
     */
    boolean isFalse();

    /**
     * Assigns the variable.
     * @param b the value to assign to this boolean variable
     * @exception InconsistencyException
     *            is thrown if the value is not in the domain
     */
    void fix(boolean b);


}
