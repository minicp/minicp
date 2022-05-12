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


package minicp.search;

/**
 * Objective object to be used
 * in the {@link DFSearch#optimize(Objective)}
 * for implementing the branch and bound depth first search.
 */
public interface Objective {

    /**
     * Method called each time a solution is found
     * during the search to let the tightening
     * of the primal bound occurs such that
     * the next found solution is better.
     */
    void tighten();
}
