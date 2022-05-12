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

package minicp.state;

/**
 * Object that wraps an integer value
 * that can be saved and restored through
 * the {@link StateManager#saveState()} / {@link StateManager#restoreState()}
 * methods.
 *
 * @see StateManager#makeStateInt(int) for the creation.
 */
public interface StateInt extends State<Integer> {

    /**
     * Increments the value
     * @return the new value
     */
    default int increment() {
        return setValue(value() + 1);
    }

    /**
     * Decrements the value
     * @return the new value
     */
    default int decrement() {
        return setValue(value() - 1);
    }

}
