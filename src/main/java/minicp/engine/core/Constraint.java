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

import java.util.Queue;

/**
 * Interface implemented by every Constraint
 * @see AbstractConstraint
 */
public interface Constraint {

    /**
     * Initializes the constraint when it is posted to the solver.
     */
    void post();

    /**
     * Propagates the constraint.
     */
    void propagate();

    /**
     * Set the status of the constraint as
     * scheduled to be propagated by the fix-point.
     * This method Called by the solver when the constraint
     * is enqueued in the propagation queue and is not
     * intended to be called by the user.
     *
     * @param scheduled a value that is true when the constraint
     *                  is enqueued in the propagation queue,
     *                  false when dequeued
     * @see Solver#fixPoint()
     */
    void setScheduled(boolean scheduled);

    /**
     * Returns the schedule status in the fix-point.
     * @return the last {@link #setScheduled(boolean)} given to setScheduled
     */
    boolean isScheduled();

    /**
     * Activates or deactivates the constraint such that it is not scheduled any more.
     * <p>Typically called by the Constraint to let the solver know
     * it should not be scheduled any more when it is subsumed.
     * <p>By default the constraint is active.
     * @param active the status to be set,
     *               this state is reversible and unset
     *               on state restoration {@link StateManager#restoreState()}
     *
     */
    void setActive(boolean active);

    /**
     * Returns the active status of the constraint.
     * @return the last setValue passed to {{@link #setActive(boolean)}
     *         in this state frame {@link StateManager#restoreState()}.
     */
    boolean isActive();


}
