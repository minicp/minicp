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

import minicp.util.Procedure;

/**
 * A view on a variable of type {@code -x}
 */
public class IntVarViewOpposite implements IntVar {

    private final IntVar x;

    public IntVarViewOpposite(IntVar x) {
        this.x = x;
    }

    @Override
    public Solver getSolver() {
        return x.getSolver();
    }

    @Override
    public void whenFixed(Procedure f) {
        x.whenFixed(f);
    }

    @Override
    public void whenBoundChange(Procedure f) {
        x.whenBoundChange(f);
    }

    @Override
    public void whenDomainChange(Procedure f) {
        x.whenDomainChange(f);
    }

    @Override
    public void propagateOnDomainChange(Constraint c) {
        x.propagateOnDomainChange(c);
    }

    @Override
    public void propagateOnFix(Constraint c) {
        x.propagateOnFix(c);
    }

    @Override
    public void propagateOnBoundChange(Constraint c) {
        x.propagateOnBoundChange(c);
    }

    @Override
    public int min() {
        return -x.max();
    }

    @Override
    public int max() {
        return -x.min();
    }

    @Override
    public int size() {
        return x.size();
    }

    @Override
    public int fillArray(int[] dest) {
        int s = x.fillArray(dest);
        for (int i = 0; i < s; i++) {
            dest[i] = -dest[i];
        }
        return s;
    }

    @Override
    public boolean isFixed() {
        return x.isFixed();
    }

    @Override
    public boolean contains(int v) {
        return x.contains(-v);
    }

    @Override
    public void remove(int v) {
        x.remove(-v);
    }

    @Override
    public void fix(int v) {
        x.fix(-v);
    }

    @Override
    public void removeBelow(int v) {
        x.removeAbove(-v);
    }

    @Override
    public void removeAbove(int v) {
        x.removeBelow(-v);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("{");
        for (int i = min(); i <= max() - 1; i++) {
            if (contains((i))) {
                b.append(i);
                b.append(',');
            }
        }
        if (size() > 0) b.append(max());
        b.append("}");
        return b.toString();
    }
}
