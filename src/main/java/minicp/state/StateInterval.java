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
 * Implementation of an interval that can saved and restored through
 * the {@link StateManager#saveState()} / {@link StateManager#restoreState()}
 * methods.
 */
public class StateInterval {
    private StateManager sm;

    private StateInt min;
    private StateInt max;

    /**
     * Creates an interval that can be saved and restored
     * with the {@link StateManager#saveState()} / {@link StateManager#restoreState()}
     * methods.
     *
     * @param sm the state-manager that save and restore the state of this interval
     * @param min the minimum value of the interval
     * @param max the maximum value of the interval {@code max >= min}
     */
    public StateInterval(StateManager sm, int min, int max) {
        this.min = sm.makeStateInt(min);
        this.max = sm.makeStateInt(max);
    }

    /**
     * Checks if the interval is empty.
     *
     * @return true if the set is empty
     */
    public boolean isEmpty() {
        return min.value() > max.value();
    }

    /**
     * Returns the number of integer values in the interval.
     *
     * @return the size of the set
     */
    public int size() {
        return Math.max(max.value() - min.value() + 1, 0);
    }

    /**
     * Returns the minimum value in the interval.
     *
     * @return the minimum value in the set
     */
    public int min() {
        return min.value();
    }

    /**
     * Returns the maximum value in the interval.
     *
     * @return the maximum value in the set
     */
    public int max() {
        return max.value();
    }

    /**
     * Checks if the a given value in the interval
     *
     * @param val the value to check check.
     * @return true if the value is in the interval
     */
    public boolean contains(int val) {
        return min.value() <= val && val <= max.value();
    }

    /**
     * Sets the first values of <code>dest</code> to the ones
     * present in the interval.
     *
     * @param dest, an array large enough {@code dest.length >= size()}
     * @return the size of the set
     */
    public int fillArray(int[] dest) {
        int s = size();
        int from = min();
        for (int i = 0; i < s; i++)
            dest[i] = from + i;
        return s;
    }


    /**
     * Reduces the interval to a single value.
     *
     * @param v is an element in the set
     */
    public void removeAllBut(int v) {
        assert (contains(v));
        min.setValue(v);
        max.setValue(v);

    }

    /**
     * Empties the interval.
     */
    public void removeAll() {
        min.setValue(max.value() + 1);
    }

    /**
     * Updates the minimum value of the interval
     * to the given one if it is larger than the
     * current {@link #min()}.
     *
     * @param value the minimum to set
     */
    public void removeBelow(int value) {
        min.setValue(value);
    }

    /**
     * Updates the maximum value of the interval
     * to the given one if it is less than the
     * current {@link #max()}.
     *
     * @param value the maximum to set
     */
    public void removeAbove(int value) {
        max.setValue(value);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("{").append(min());
        b.append("..").append(max()).append("}");
        return b.toString();
    }
}
