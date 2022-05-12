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

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * StateManager that will store
 * the state of every created elements
 * at each {@link #saveState()} call.
 */
public class Copier implements StateManager {

    class Backup extends Stack<StateEntry> {
        private int sz;

        Backup() {
            sz = store.size();
            for (Storage s : store)
                add(s.save());
        }

        void restore() {
            store.setSize(sz);
            for (StateEntry se : this)
                se.restore();
        }
    }

    private Stack<Storage> store;
    private Stack<Backup> prior;
    private List<Procedure> onRestoreListeners;

    public Copier() {
        store = new Stack<Storage>();
        prior = new Stack<Backup>();
        onRestoreListeners = new LinkedList<Procedure>();
    }

    private void notifyRestore() {
        for (Procedure l: onRestoreListeners) {
            l.call();
        }
    }

    @Override
    public void onRestore(Procedure listener) {
        onRestoreListeners.add(listener);
    }

    public int getLevel() {
        return prior.size() - 1;
    }


    public int storeSize() {
        return store.size();
    }

    @Override
    public void saveState() {
        prior.add(new Backup());
    }

    @Override
    public void restoreState() {
        prior.pop().restore();
        notifyRestore();
    }

    @Override
    public void withNewState(Procedure body) {
        final int level = getLevel();
        saveState();
        body.call();
        restoreStateUntil(level);
    }

    @Override
    public void restoreStateUntil(int level) {
        while (getLevel() > level)
            restoreState();
    }

    @Override
    public <T> State<T> makeStateRef(T initValue) {
        Copy r = new Copy(initValue);
        store.add(r);
        return r;
    }

    @Override
    public StateInt makeStateInt(int initValue) {
        CopyInt s = new CopyInt(initValue);
        store.add(s);
        return s;
    }

    @Override
    public StateMap makeStateMap() {
        CopyMap s = new CopyMap<>();
        store.add(s);
        return s;
    }


}
