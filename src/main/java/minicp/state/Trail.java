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
 * Implementation of {@link State} with trail strategy
 * @see Trailer
 * @see StateManager#makeStateRef(Object)
 */
public class Trail<T> implements State<T> {

    class TrailStateEntry implements StateEntry {
        private final T v;

        TrailStateEntry(T v) {
            this.v = v;
        }

        @Override
        public void restore() {
            Trail.this.v = v;
        }
    }

    private Trailer trail;
    private T v;
    private long lastMagic = -1L;

    protected Trail(Trailer trail, T initial) {
        this.trail = trail;
        v = initial;
        lastMagic = trail.getMagic() - 1;
    }

    private void trail() {
        long trailMagic = trail.getMagic();
        if (lastMagic != trailMagic) {
            lastMagic = trailMagic;
            trail.pushState(new TrailStateEntry(v));
        }
    }

    @Override
    public T setValue(T v) {
        if (v != this.v) {
            trail();
            this.v = v;
        }
        return this.v;
    }

    @Override
    public T value() {
        return this.v;
    }

    @Override
    public String toString() {
        return "" + v;
    }
}
