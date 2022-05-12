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
 * Implementation of {@link State} with copy strategy
 * @see Copier
 * @see StateManager#makeStateRef(Object)
 */
public class Copy<T> implements Storage, State<T> {

    class CopyStateEntry implements StateEntry {
        private final T v;

        CopyStateEntry(T v) {
            this.v = v;
        }
        @Override public void restore() {
            Copy.this.v = v;
        }
    }

    private T v;

    protected Copy(T initial) {
        v = initial;
    }

    @Override
    public T setValue(T v) {
        this.v = v;
        return v;
    }

    @Override
    public T value() {
        return v;
    }


    @Override
    public String toString() {
        return String.valueOf(v);
    }

    @Override
    public StateEntry save() {
        return new CopyStateEntry(v);
    }
}
