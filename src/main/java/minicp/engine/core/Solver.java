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

import minicp.search.Objective;
import minicp.state.StateManager;
import minicp.util.Procedure;

public interface Solver {

    /**
     * Posts the constraint, that is call {@link Constraint#post()} and
     * computes the fix-point.
     * A {@link minicp.util.exception.InconsistencyException} is thrown
     * if by posting the constraint it is proven that there is no solution.
     *
     * @param c the constraint to be posted
     */
    void post(Constraint c);

    /**
     * Schedules the constraint to be propagated by the fix-point.
     *
     * @param c the constraint to be scheduled
     */
    void schedule(Constraint c);

    /**
     * Posts the constraint that is call {@link Constraint#post()}
     * and optionally computes the fix-point.
     * A {@link minicp.util.exception.InconsistencyException} is thrown
     * if by posting the constraint it is proven that there is no solution.
     * @param c the constraint to be posted
     * @param enforceFixPoint is one wants to compute the fix-point after
     */
    void post(Constraint c, boolean enforceFixPoint);

    /**
     * Computes the fix-point with all the scheduled constraints.
     */
    void fixPoint();

    /**
     * Returns the state manager in charge of the global
     * state of the solver.
     *
     * @return the state manager
     */
    StateManager getStateManager();

    /**
     * Adds a listener called whenever the fix-point.
     *
     * @param listener the listener that is called whenever the fix-point is started
     */
    void onFixPoint(Procedure listener);

    /**
     * Creates a minimization objective on the given variable.
     *
     * @param x the variable to minimize
     * @return an objective that can minimize x
     * @see minicp.search.DFSearch#optimize(Objective)
     */
    Objective minimize(IntVar x);

    /**
     * Creates a maximization objective on the given variable.
     *
     * @param x the variable to maximize
     * @return an objective that can maximize x
     * @see minicp.search.DFSearch#optimize(Objective)
     */
    Objective maximize(IntVar x);

    /**
     * Forces the boolean variable to be true and then
     * computes the fix-point.
     *
     * @param b the variable that must be set to true
     */
    void post(BoolVar b);
}

