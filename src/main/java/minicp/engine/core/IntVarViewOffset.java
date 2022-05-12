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
import minicp.util.exception.IntOverFlowException;

/**
 * A view on a variable of type {@code x+o}
 */
public class IntVarViewOffset implements IntVar {

    private final IntVar x;
    private final int o;

    public IntVarViewOffset(IntVar x, int offset) { // y = x + o
        if (0L + x.min() + offset <= (long) Integer.MIN_VALUE)
            throw new IntOverFlowException("consider applying a smaller offset as the min domain on this view is <= Integer.MIN _VALUE");
        if (0L + x.max() + offset >= (long) Integer.MAX_VALUE)
            throw new IntOverFlowException("consider applying a smaller offset as the max domain on this view is >= Integer.MAX _VALUE");
        this.x = x;
        this.o = offset;

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
        return x.min() + o;
    }

    @Override
    public int max() {
        return x.max() + o;
    }

    @Override
    public int size() {
        return x.size();
    }

    @Override
    public int fillArray(int[] dest) {
        int s = x.fillArray(dest);
        for (int i = 0; i < s; i++) {
            dest[i] += o;
        }
        return s;
    }

    @Override
    public boolean isFixed() {
        return x.isFixed();
    }

    @Override
    public boolean contains(int v) {
        return x.contains(v - o);
    }

    @Override
    public void remove(int v) {
        x.remove(v - o);
    }

    @Override
    public void fix(int v) {
        x.fix(v - o);
    }

    @Override
    public void removeBelow(int v) {
        x.removeBelow(v - o);
    }

    @Override
    public void removeAbove(int v) {
        x.removeAbove(v - o);
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
