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

import minicp.util.exception.NotImplementedException;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Implementation of {@link StateMap} with copy strategy
 * @see Copier
 * @see StateManager#makeStateMap()
 */
public class CopyMap<K, V> implements Storage, StateMap<K, V> {

    

    private Map<K, V> map;

    protected CopyMap() {
         throw new NotImplementedException("CopyMap");
    }

    protected CopyMap(Map<K, V> m) {
         throw new NotImplementedException("CopyMap");
    }

    @Override
    public void put(K k, V v) {
         throw new NotImplementedException("CopyMap");
    }

    @Override
    public V get(K k) {
         throw new NotImplementedException("CopyMap");
    }

    @Override
    public StateEntry save() {
         throw new NotImplementedException("CopyMap");
    }

}
