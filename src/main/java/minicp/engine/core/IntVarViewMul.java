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
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.IntOverFlowException;

/**
 * A view on a variable of type {@code a*x}
 */
public class IntVarViewMul implements IntVar {

    private final int a;
    private final IntVar x;

    public IntVarViewMul(IntVar x, int a) {
        if ((1L + x.min()) * a <= (long) Integer.MIN_VALUE)
            throw new IntOverFlowException("consider applying a smaller mul cte as the min domain on this view is <= Integer.MIN _VALUE");
        if ((1L + x.max()) * a >= (long) Integer.MAX_VALUE)
            throw new IntOverFlowException("consider applying a smaller mul cte as the max domain on this view is >= Integer.MAX _VALUE");
        assert (a > 0);
        this.a = a;
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
        if (a >= 0)
            return a * x.min();
        else return a * x.max();
    }

    @Override
    public int max() {
        if (a >= 0)
            return a * x.max();
        else return a * x.min();
    }

    @Override
    public int size() {
        return x.size();
    }

    @Override
    public int fillArray(int[] dest) {
        int s = x.fillArray(dest);
        for (int i = 0; i < s; i++) {
            dest[i] *= a;
        }
        return s;
    }

    @Override
    public boolean isFixed() {
        return x.isFixed();
    }

    @Override
    public boolean contains(int v) {
        return (v % a != 0) ? false : x.contains(v / a);
    }

    @Override
    public void remove(int v) {
        if (v % a == 0) {
            x.remove(v / a);
        }
    }

    @Override
    public void fix(int v) {
        if (v % a == 0) {
            x.fix(v / a);
        } else {
            throw new InconsistencyException();
        }
    }

    @Override
    public void removeBelow(int v) {
        x.removeBelow(ceilDiv(v, a));
    }

    @Override
    public void removeAbove(int v) {
        x.removeAbove(floorDiv(v, a));
    }

    // Java's division always rounds to the integer closest to zero, but we need flooring/ceiling versions.
    private int floorDiv(int a, int b) {
        int q = a / b;
        return (a < 0 && q * b != a) ? q - 1 : q;
    }

    private int ceilDiv(int a, int b) {
        int q = a / b;
        return (a > 0 && q * b != a) ? q + 1 : q;
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
