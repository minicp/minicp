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
 * StateManager that will lazily store
 * the state of state object
 * at each {@link #saveState()} call.
 * Only the one that effectively change are stored
 * and at most once between any to call to {@link #saveState()}.
 * This can be seen as an optimized version of {@link Copier}.
 */
public class Trailer implements StateManager {

    static class Backup extends Stack<StateEntry> {
        Backup() {
        }

        void restore() {
            //note that using for on a Stack gives the wrong order.
            while (!isEmpty())
                pop().restore();
        }
    }

    private Stack<Backup> prior;
    private Backup current;
    private long magic = 0L;

    private List<Procedure> onRestoreListeners;

    public Trailer() {
        prior = new Stack<Backup>();
        current = new Backup();
        onRestoreListeners = new LinkedList<Procedure>();
    }

    private void notifyRestore() {
        for (Procedure l : onRestoreListeners) {
            l.call();
        }
    }

    @Override
    public void onRestore(Procedure listener) {
        onRestoreListeners.add(listener);
    }

    public long getMagic() {
        return magic;
    }

    public void pushState(StateEntry entry) {
        current.push(entry);
    }

    @Override
    public int getLevel() {
        return prior.size() - 1;
    }

    @Override
    public void saveState() {
        prior.add(current);
        current = new Backup();
        magic++;
    }


    @Override
    public void restoreState() {
        current.restore();
        current = prior.pop();
        magic++;
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
        return new Trail<>(this,initValue);
    }

    @Override
    public StateInt makeStateInt(int initValue) {
        return new TrailInt(this,initValue);
    }

    @Override
    public StateMap makeStateMap() {
        return new TrailMap(this);
    }

}
