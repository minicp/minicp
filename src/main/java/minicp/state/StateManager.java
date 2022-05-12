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

import minicp.util.Procedure;

/**
 * The StateManager exposes
 * all the mechanisms and data-structures
 * needed to implement a depth-first-search
 * with reversible states.
 *
 */
public interface StateManager {

    /**
     * Stores the current state
     * such that it can be recovered using restoreState()
     * Increase the level by 1
     */
    void saveState();


    /**
     * Restores state as it was at getLevel()-1
     * Decrease the level by 1
     */
    void restoreState();

    /**
     * Restores the state up the the given level.
     *
     * @param level the level, a non negative number between 0 and {@link #getLevel()}
     */
    void restoreStateUntil(int level);

    /**
     * Add a listener that is notified each time the {@link #restoreState()}
     * is called.
     *
     * @param listener the listener to be notified
     */
    void onRestore(Procedure listener);

    /**
     * Returns the current level.
     * It is increased at each {@link #saveState()}
     * and decreased at each {@link #restoreState()}.
     * It is initially equal to -1.
     * @return the level
     */
    int getLevel();

    /**
     * Creates a Stateful reference (restorable)
     *
     * @param initValue the initial setValue
     * @return a State object wrapping the initValue
     */
    <T> State<T> makeStateRef(T initValue);

    /**
     * Creates a Stateful integer (restorable)
     *
     * @param initValue the initial setValue
     * @return a StateInt object wrapping the initValue
     */
    StateInt makeStateInt(int initValue);

    /**
     * Creates a Stateful map (restorable)
     *
     * @return a reference to the map.
     */
    StateMap makeStateMap();

    /**
     * Higher-order function that preserves the state prior to calling body and restores it after.
     *
     * @param body the first-order function to execute.
     */
    void withNewState(Procedure body);
}

