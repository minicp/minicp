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

import java.util.ArrayList;

/**
 * Generic Stack that can be saved and restored through
 * the {@link StateManager#saveState()} / {@link StateManager#restoreState()}
 * methods.
 */
public class StateStack<E> {

    private StateInt size;
    private ArrayList<E> stack;

    /**
     * Creates a restorable stack.
     * @param sm the state manager that saves/restores the stack
     *         when {@link StateManager#saveState()} / {@link StateManager#restoreState()}
     *         methods are called.
     */
    public StateStack(StateManager sm) {
        size = sm.makeStateInt(0);
        stack = new ArrayList<E>();
    }

    public void push(E elem) {
        int s = size.value();
        if (stack.size() > s) stack.set(s, elem);
        else stack.add(elem);
        size.increment();
    }

    public int size() {
        return size.value();
    }

    public E get(int index) {
        return stack.get(index);
    }
}
