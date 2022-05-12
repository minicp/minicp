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
 * A generic map that can revert its state
 * with {@link StateManager#saveState()} / {@link StateManager#restoreState()}
 * methods.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @see StateManager#makeStateMap() for the creation.
 */
public interface StateMap<K, V> {

    /**
     * Inserts the key-value pair.
     * It erases the existing ones
     * if the map already contains an entry
     * with the given key.
     *
     * @param k the key
     * @param v the value
     */
    void put(K k, V v);

    /**
     * Retrieves the value for a given key.
     *
     * @param k the key
     * @return the value v if the entry (k,v) was previously put, null otherwise
     */
    V get(K k);
}
