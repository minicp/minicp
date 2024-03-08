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

package minicp.cp;

import minicp.engine.constraints.*;
import minicp.engine.core.*;
import minicp.search.DFSearch;
import minicp.search.Objective;
import minicp.state.Copier;
import minicp.state.Trailer;
import minicp.util.exception.InconsistencyException;
import minicp.util.Procedure;
import minicp.util.exception.IntOverFlowException;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Factory to create {@link Solver}, {@link IntVar}, {@link Constraint}
 * and some modeling utility methods.
 * Example for the n-queens problem:
 * <pre>
 * {@code
 *  Solver cp = Factory.makeSolver(false);
 *  IntVar[] q = Factory.makeIntVarArray(cp, n, n);
 *  for (int i = 0; i < n; i++)
 *    for (int j = i + 1; j < n; j++) {
 *      cp.post(Factory.notEqual(q[i], q[j]));
 *      cp.post(Factory.notEqual(q[i], q[j], j - i));
 *      cp.post(Factory.notEqual(q[i], q[j], i - j));
 *    }
 *  search.onSolution(() ->
 *    System.out.println("solution:" + Arrays.toString(q))
 *  );
 *  DFSearch search = Factory.makeDfs(cp,firstFail(q));
 *  SearchStatistics stats = search.solve();
 * }
 * </pre>
 */
public final class Factory {

    private Factory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a constraint programming solver
     * @return a constraint programming solver with trail-based memory management
     */
    public static Solver makeSolver() {
        return new MiniCP(new Trailer());
    }
    /**
     * Creates a constraint programming solver
     * @param byCopy a value that should be true to specify
     *               copy-based state management
     *               or falso for a trail-based memory management
     * @return a constraint programming solver
     */
    public static Solver makeSolver(boolean byCopy) {
        return new MiniCP(byCopy ? new Copier() : new Trailer());
    }

    /**
     * Creates a variable with a domain of specified arity.
     *
     * @param cp the solver in which the variable is created
     * @param sz a positive value that is the size of the domain
     * @return a variable with domain equal to the set {0,...,sz-1}
     */
    public static IntVar makeIntVar(Solver cp, int sz) {
        return new IntVarImpl(cp, sz);
    }

    /**
     * Creates a variable with a domain equal to the specified range.
     *
     * @param cp the solver in which the variable is created
     * @param min the lower bound of the domain (included)
     * @param max the upper bound of the domain (included) {@code max > min}
     * @return a variable with domain equal to the set {min,...,max}
     */
    public static IntVar makeIntVar(Solver cp, int min, int max) {
        return new IntVarImpl(cp, min, max);
    }

    /**
     * Creates a variable with a domain equal to the specified set of values.
     *
     * @param cp the solver in which the variable is created
     * @param values a set of values
     * @return a variable with domain equal to the set of values
     */
    public static IntVar makeIntVar(Solver cp, Set<Integer> values) {
        return new IntVarImpl(cp, values);
    }

    /**
     * Creates a boolean variable.
     *
     * @param cp the solver in which the variable is created
     * @return an uninstantiated boolean variable
     */
    public static BoolVar makeBoolVar(Solver cp) {
        return new BoolVarImpl(cp);
    }

    /**
     * Creates an array of variables with specified domain size.
     *
     * @param cp the solver in which the variables are created
     * @param n the number of variables to create
     * @param sz a positive value that is the size of the domain
     * @return an array of n variables, each with domain equal to the set {0,...,sz-1}
     */
    public static IntVar[] makeIntVarArray(Solver cp, int n, int sz) {
        return makeIntVarArray(n, i -> makeIntVar(cp, sz));
    }

    /**
     * Creates an array of variables with specified domain bounds.
     *
     * @param cp the solver in which the variables are created
     * @param n the number of variables to create
     * @param min the lower bound of the domain (included)
     * @param max the upper bound of the domain (included) {@code max > min}
     * @return an array of n variables each with a domain equal to the set {min,...,max}
     */
    public static IntVar[] makeIntVarArray(Solver cp, int n, int min, int max) {
        return makeIntVarArray(n, i -> makeIntVar(cp, min, max));
    }

    /**
     * Creates an array of variables with specified lambda function
     *
     * @param n the number of variables to create
     * @param body the function that given the index i in the array creates/map the corresponding {@link IntVar}
     * @return an array of n variables
     *         with variable at index <i>i</i> generated as {@code body.get(i)}
     */
    public static IntVar[] makeIntVarArray(int n, Function<Integer, IntVar> body) {
        IntVar[] t = new IntVar[n];
        for (int i = 0; i < n; i++)
            t[i] = body.apply(i);
        return t;
    }

    /**
     * Creates a Depth First Search with custom branching heuristic
     * <pre>
     * // Example of binary search: At each node it selects
     * // the first free variable qi from the array q,
     * // and creates two branches qi=v, qi!=v where v is the min value domain
     * {@code
     * DFSearch search = Factory.makeDfs(cp, () -> {
     *     IntVar qi = Arrays.stream(q).reduce(null, (a, b) -> b.size() > 1 && a == null ? b : a);
     *     if (qi == null) {
     *        return return EMPTY;
     *     } else {
     *        int v = qi.min();
     *        Procedure left = () -> cp.post(equal(qi, v)); // left branch
     *        Procedure right = () -> cp.post(notEqual(qi, v)); // right branch
     *        return branch(left, right);
     *     }
     * });
     * }
     * </pre>
     *
     * @param cp the solver that will be used for the search
     * @param branching a generator that is called at each node of the depth first search
     *                 tree to generate an array of {@link Procedure} objects
     *                 that will be used to commit to child nodes.
     *                 It should return {@link BranchingScheme#EMPTY} whenever the current state
     *                  is a solution.
     *
     * @return the depth first search object ready to execute with
     *         {@link DFSearch#solve()} or
     *         {@link DFSearch#optimize(Objective)}
     *         using the given branching scheme
     * @see BranchingScheme#firstFail(IntVar...)
     * @see BranchingScheme#branch(Procedure...)
     */
    public static DFSearch makeDfs(Solver cp, Supplier<Procedure[]> branching) {
        return new DFSearch(cp.getStateManager(), branching);
    }

    // -------------- constraints -----------------------

    /**
     * A variable that is a view of {@code x*a}.
     *
     * @param x a variable
     * @param a a constant to multiply x with
     * @return a variable that is a view of {@code x*a}
     */
    public static IntVar mul(IntVar x, int a) {
        if (a == 0) return makeIntVar(x.getSolver(), 0, 0);
        else if (a == 1) return x;
        else if (a < 0) {
            return minus(new IntVarViewMul(x, -a));
        } else {
            return new IntVarViewMul(x, a);
        }
    }

    /**
     * A variable that is a view of {@code -x}.
     *
     * @param x a variable
     * @return a variable that is a view of {@code -x}
     */
    public static IntVar minus(IntVar x) {
        return new IntVarViewOpposite(x);
    }

    /**
     * A variable that is a view of {@code x+v}.
     *
     * @param x a variable
     * @param v a value
     * @return a variable that is a view of {@code x+v}
     */
    public static IntVar plus(IntVar x, int v) {
        return v == 0 ? x : new IntVarViewOffset(x, v);
    }

    /**
     * A variable that is a view of {@code x-v}.
     *
     * @param x a variable
     * @param v a value
     * @return a variable that is a view of {@code x-v}
     */
    public static IntVar minus(IntVar x, int v) {
        return v == 0 ? x: new IntVarViewOffset(x, -v);
    }


    /**
     *  A boolean variable that is a view of {@code !b}.
     *
     * @param b a boolean variable
     * @return a boolean variable that is a view of {@code !b}
     */
    public static BoolVar not(BoolVar b) {
        return new BoolVarImpl(plus(minus(b),1));
    }

    /**
     * Computes a variable that is the absolute value of the given variable.
     * This relation is enforced by the {@link Absolute} constraint
     * posted by calling this method.
     *
     * @param x a variable
     * @return a variable that represents the absolute value of x
     */
    public static IntVar abs(IntVar x) {
        if (x.min() >= 0) return x;
        else {
            IntVar r = makeIntVar(x.getSolver(), 0, Math.max(-x.min(), x.max()));
            x.getSolver().post(new Absolute(x, r));
            return r;
        }
    }

    /**
     * Computes a variable that is the maximum of a set of variables.
     * This relation is enforced by the {@link Maximum} constraint
     * posted by calling this method.
     *
     * @param x the variables on which to compute the maximum
     * @return a variable that represents the maximum on x
     * @see Factory#minimum(IntVar...)
     */
    public static IntVar maximum(IntVar... x) {
        Solver cp = x[0].getSolver();
        int min = Arrays.stream(x).mapToInt(IntVar::min).min().getAsInt();
        int max = Arrays.stream(x).mapToInt(IntVar::max).max().getAsInt();
        IntVar y = makeIntVar(cp, min, max);
        cp.post(new Maximum(x, y));
        return y;
    }

    /**
     * Computes a variable that is the minimum of a set of variables.
     * This relation is enforced by the {@link Maximum} constraint
     * posted by calling this method.
     *
     * @param x the variables on which to compute the minimum
     * @return a variable that represents the minimum on x
     * @see Factory#maximum(IntVar...) (IntVar...)
     */
    public static IntVar minimum(IntVar... x) {
        IntVar[] minusX = Arrays.stream(x).map(Factory::minus).toArray(IntVar[]::new);
        return minus(maximum(minusX));
    }

    /**
     * Returns a constraint imposing that the variable is
     * equal to some given value.
     *
     * @param x the variable to be assigned to v
     * @param v the value that must be assigned to x
     * @return a constraint so that {@code x = v}
     */
    public static Constraint equal(IntVar x, int v) {
        return new AbstractConstraint(x.getSolver()) {
            @Override
            public void post() {
                x.fix(v);
            }
        };
    }

    /**
     * Returns a constraint imposing that the variable less or
     * equal to some given value.
     *
     * @param x the variable that is constrained bo be less or equal to v
     * @param v the value that must be the upper bound on x
     * @return a constraint so that {@code x <= v}
     */
    public static Constraint lessOrEqual(IntVar x, int v) {
        return new AbstractConstraint(x.getSolver()) {
            @Override
            public void post() {
                x.removeAbove(v);
            }
        };
    }

    /**
     * Returns a constraint imposing that the variable larger or
     * equal to some given value.
     *
     * @param x the variable that is constrained bo be larger or equal to v
     * @param v the value that must be the lower bound on x
     * @return a constraint so that {@code x >= v}
     */
    public static Constraint largerOrEqual(IntVar x, int v) {
        return new AbstractConstraint(x.getSolver()) {
            @Override
            public void post() {
                x.removeBelow(v);
            }
        };
    }

    /**
     * Returns a constraint imposing that the variable is different
     * from some given value.
     *
     * @param x the variable that is constrained bo be different from v
     * @param v the value that must be different from x
     * @return a constraint so that {@code x != y}
     */
    public static Constraint notEqual(IntVar x, int v) {
        return new AbstractConstraint(x.getSolver()) {
            @Override
            public void post() {
                x.remove(v);
            }
        };
    }

    /**
     * Returns a constraint imposing that the two different variables
     * must take different values.
     *
     * @param x a variable
     * @param y a variable
     * @return a constraint so that {@code x != y}
     */
    public static Constraint notEqual(IntVar x, IntVar y) {
        return new NotEqual(x, y);
    }


    /**
     * Returns a constraint imposing that the two different variables
     * must take the value.
     *
     * @param x a variable
     * @param y a variable
     * @return a constraint so that {@code x = y}
     */
    public static Constraint equal(IntVar x, IntVar y) {
        return new Equal(x, y);
    }

    /**
     * Returns a constraint imposing that the
     * the first variable differs from the second
     * one minus a constant value.
     *
     * @param x a variable
     * @param y a variable
     * @param c a constant
     * @return a constraint so that {@code x != y+c}
     */
    public static Constraint notEqual(IntVar x, IntVar y, int c) {
        return new NotEqual(x, y, c);
    }

    /**
     * Returns a boolean variable representing
     * whether one variable is equal to the given constant.
     * This relation is enforced by the {@link IsEqual} constraint
     * posted by calling this method.
     *
     * @param x the variable
     * @param c the constant
     * @return a boolean variable that is true if and only if x takes the value c
     * @see IsEqual
     */
    public static BoolVar isEqual(IntVar x, final int c) {
        BoolVar b = makeBoolVar(x.getSolver());
        Solver cp = x.getSolver();
        try {
            cp.post(new IsEqual(b, x, c));
        } catch (InconsistencyException e) {
            e.printStackTrace();
        }
        return b;
    }

    /**
     * Returns a boolean variable representing
     * whether one variable is less or equal to the given constant.
     * This relation is enforced by the {@link IsLessOrEqual} constraint
     * posted by calling this method.
     *
     * @param x the variable
     * @param c the constant
     * @return a boolean variable that is true if and only if
     *         x takes a value less or equal to c
     */
    public static BoolVar isLessOrEqual(IntVar x, final int c) {
        BoolVar b = makeBoolVar(x.getSolver());
        Solver cp = x.getSolver();
        cp.post(new IsLessOrEqual(b, x, c));
        return b;
    }

    /**
     * Returns a boolean variable representing
     * whether one variable is less than the given constant.
     * This relation is enforced by the {@link IsLessOrEqual} constraint
     * posted by calling this method.
     *
     * @param x the variable
     * @param c the constant
     * @return a boolean variable that is true if and only if
     *         x takes a value less than c
     */
    public static BoolVar isLess(IntVar x, final int c) {
        return isLessOrEqual(x, c - 1);
    }

    /**
     * Returns a boolean variable representing
     * whether one variable is larger or equal to the given constant.
     * This relation is enforced by the {@link IsLessOrEqual} constraint
     * posted by calling this method.
     *
     * @param x the variable
     * @param c the constant
     * @return a boolean variable that is true if and only if
     *         x takes a value larger or equal to c
     */
    public static BoolVar isLargerOrEqual(IntVar x, final int c) {
        return isLessOrEqual(minus(x), -c);
    }

    /**
     * Returns a boolean variable representing
     * whether one variable is larger than the given constant.
     * This relation is enforced by the {@link IsLessOrEqual} constraint
     * posted by calling this method.
     *
     * @param x the variable
     * @param c the constant
     * @return a boolean variable that is true if and only if
     *         x takes a value larger than c
     */
    public static BoolVar isLarger(IntVar x, final int c) {
        return isLargerOrEqual(x, c + 1);
    }

    /**
     * Returns a constraint imposing that the
     * a first variable is less or equal to a second one.
     *
     * @param x a variable
     * @param y a variable
     * @return a constraint so that {@code x <= y}
     */
    public static Constraint lessOrEqual(IntVar x, IntVar y) {
        return new LessOrEqual(x, y);
    }

    /**
     * Returns a constraint imposing that the
     * a first variable is larger or equal to a second one.
     *
     * @param x a variable
     * @param y a variable
     * @return a constraint so that {@code x >= y}
     */
    public static Constraint largerOrEqual(IntVar x, IntVar y) {
        return new LessOrEqual(y, x);
    }

    /**
     * Returns a variable representing
     * the value in an array at the position
     * specified by the given index variable
     * This relation is enforced by the {@link Element1D} constraint
     * posted by calling this method.
     *
     * @param array the array of values
     * @param y the variable
     * @return a variable equal to {@code array[y]}
     */
    public static IntVar element(int[] array, IntVar y) {
        Solver cp = y.getSolver();
        IntVar z = makeIntVar(cp, IntStream.of(array).min().getAsInt(), IntStream.of(array).max().getAsInt());
        cp.post(new Element1D(array, y, z));
        return z;
    }

    /**
     * Returns a variable representing
     * the value in a matrix at the position
     * specified by the two given row and column index variables
     * This relation is enforced by the {@link Element2D} constraint
     * posted by calling this method.
     *
     * @param matrix the n x m 2D array of values
     * @param x the row variable with domain included in 0..n-1
     * @param y the column variable with domain included in 0..m-1
     * @return a variable equal to {@code matrix[x][y]}
     */
    public static IntVar element(int[][] matrix, IntVar x, IntVar y) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                min = Math.min(min, matrix[i][j]);
                max = Math.max(max, matrix[i][j]);
            }
        }
        IntVar z = makeIntVar(x.getSolver(), min, max);
        x.getSolver().post(new Element2D(matrix, x, y, z));
        return z;
    }

    /**
     * Returns a variable representing
     * the sum of a given set of variables.
     * This relation is enforced by the {@link Sum} constraint
     * posted by calling this method.
     *
     * @param x the n variables to sum
     * @return a variable equal to {@code x[0]+x[1]+...+x[n-1]}
     */
    public static IntVar sum(IntVar... x) {
        long sumMin = 0;
        long sumMax = 0;
        for (int i = 0; i < x.length; i++) {
            sumMin += x[i].min();
            sumMax += x[i].max();
        }
        if (sumMin < (long) Integer.MIN_VALUE || sumMax > (long) Integer.MAX_VALUE) {
            throw new IntOverFlowException("domains are too large for sum constraint and would exceed Integer bounds");
        }
        Solver cp = x[0].getSolver();
        IntVar s = makeIntVar(cp, (int) sumMin, (int) sumMax);
        cp.post(new Sum(x, s));
        return s;
    }

    /**
     * Returns a sum constraint.
     *
     * @param x an array of variables
     * @param y a variable
     * @return a constraint so that {@code y = x[0]+x[1]+...+x[n-1]}
     */
    public static Constraint sum(IntVar[] x, IntVar y) {
        return new Sum(x, y);
    }

    /**
     * Returns a sum constraint.
     *
     * @param x an array of variables
     * @param y a constant
     * @return a constraint so that {@code y = x[0]+x[1]+...+x[n-1]}
     */
    public static Constraint sum(IntVar[] x, int y) {
        return new Sum(x, y);
    }

    /**
     * Returns a sum constraint.
     * <p>
     * Uses a _parameter pack_ to automatically bundle a list of IntVar as an array
     *
     * @param y the target value for the sum (a constant)
     * @param x a parameter pack of IntVar representing an array of variables
     * @return a constraint so that {@code y = x[0] + ... + x[n-1]}
     */
    public static Constraint sum(int y, IntVar... x) {
        return new Sum(x, y);
    }

    /**
     * Returns a binary decomposition of the allDifferent constraint.
     *
     * @param x an array of variables
     * @return a constraint so that {@code x[i] != x[j] for all i < j}
     */
    public static Constraint allDifferent(IntVar[] x) {
        return new AllDifferentBinary(x);
    }

    /**
     * Returns an allDifferent constraint that enforces
     * domain consistency.
     *
     * @param x an array of variables
     * @return a constraint so that {@code x[i] != x[j] for all i < j}
     */
    public static Constraint allDifferentDC(IntVar[] x) {
        return new AllDifferentDC(x);
    }
}
