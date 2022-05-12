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

package minicp.engine.core;


import minicp.state.StateManager;
import minicp.state.StateSparseSet;
import minicp.util.exception.NotImplementedException;

/**
 * Implementation of a domain with a sparse-set
 */
public class SparseSetDomain implements IntDomain {
    private StateSparseSet domain;


    public SparseSetDomain(StateManager sm, int min, int max) {
        domain = new StateSparseSet(sm, max - min + 1, min);
    }

    @Override
    public int fillArray(int[] dest) {
        return domain.fillArray(dest);
    }

    @Override
    public int min() {
        return domain.min();
    }

    @Override
    public int max() {
        return domain.max();
    }

    @Override
    public int size() {
        return domain.size();
    }

    @Override
    public boolean contains(int v) {
        return domain.contains(v);
    }

    @Override
    public boolean isSingleton() {
        return domain.size() == 1;
    }

    @Override
    public void remove(int v, DomainListener l) {
        if (domain.contains(v)) {
            boolean maxChanged = max() == v;
            boolean minChanged = min() == v;
            domain.remove(v);
            if (domain.size() == 0)
                l.empty();
            l.change();
            if (maxChanged) l.changeMax();
            if (minChanged) l.changeMin();
            if (domain.size() == 1) l.fix();
        }
    }

    @Override
    public void removeAllBut(int v, DomainListener l) {
        if (domain.contains(v)) {
            if (domain.size() != 1) {
                boolean maxChanged = max() != v;
                boolean minChanged = min() != v;
                domain.removeAllBut(v);
                if (domain.size() == 0)
                    l.empty();
                l.fix();
                l.change();
                if (maxChanged) l.changeMax();
                if (minChanged) l.changeMin();
            }
        } else {
            domain.removeAll();
            l.empty();
        }
    }

    @Override
    public void removeBelow(int value, DomainListener l) {
        if (domain.min() < value) {
            domain.removeBelow(value);
            switch (domain.size()) {
                case 0:
                    l.empty();
                    break;
                case 1:
                    l.fix();
                default:
                    l.changeMin();
                    l.change();
                    break;
            }
        }
    }

    @Override
    public void removeAbove(int value, DomainListener l) {
        if (domain.max() > value) {
            domain.removeAbove(value);
            switch (domain.size()) {
                case 0:
                    l.empty();
                    break;
                case 1:
                    l.fix();
                default:
                    l.changeMax();
                    l.change();
                    break;
            }
        }
    }

    @Override
    public String toString() {
        if (size() == 0) return "{}";
        StringBuilder b = new StringBuilder();
        b.append("{");
        for (int i = min(); i < max(); i++)
            if (contains((i)))
                b.append(i).append(',');
        b.append(max());
        b.append("}");
        return b.toString();
    }

}
