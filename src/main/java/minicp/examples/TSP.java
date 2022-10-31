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

package minicp.examples;

import minicp.engine.constraints.Circuit;
import minicp.engine.constraints.Element1D;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.util.io.InputReader;
import minicp.search.SearchStatistics;

import static minicp.cp.BranchingScheme.*;
import static minicp.cp.Factory.*;
import minicp.util.exception.NotImplementedException;

/**
 * Traveling salesman problem.
 * <a href="https://en.wikipedia.org/wiki/Travelling_salesman_problem">Wikipedia</a>.
 */
public class TSP extends OptimizationProblem {

    public final int n;
    public final int[][] distanceMatrix;
    public IntVar[] succ;
    public IntVar totalDist;

    public TSP(String instancePath) {
        InputReader reader = new InputReader(instancePath);
        n = reader.getInt();
        distanceMatrix = reader.getMatrix(n, n);
    }

    public void buildModel() {
        Solver cp = makeSolver(false);
        succ = makeIntVarArray(cp, n, n);
        IntVar[] distSucc = makeIntVarArray(cp, n, 1000);
        cp.post(new Circuit(succ));
        for (int i = 0; i < n; i++) {
            cp.post(new Element1D(distanceMatrix[i], succ[i], distSucc[i]));
        }

        totalDist = sum(distSucc);
        objective = cp.minimize(totalDist);

        // simple first-fail strategy
        dfs = makeDfs(cp, () -> {
            IntVar xs = selectMin(succ,
                  xi -> xi.size() > 1,
                  xi -> xi.size());
            if (xs == null)
                return EMPTY;
            else {
                // TODO modify the value selector to get a better solution
                int v = xs.min();
                return branch(() -> xs.getSolver().post(equal(xs, v)),
                        () -> xs.getSolver().post(notEqual(xs, v)));
            }
        });

        // TODO implement the search and remove the NotImplementedException
         throw new NotImplementedException("TSP");
    }

    public static void main(String[] args) {
        // instance gr17 https://people.sc.fsu.edu/~jburkardt/datasets/tsp/gr17_d.txt
        // instance fri26 https://people.sc.fsu.edu/~jburkardt/datasets/tsp/fri26_d.txt
        // instance p01 (adapted from) https://people.sc.fsu.edu/~jburkardt/datasets/tsp/p01_d.txt
        // the other instances are located at data/tsp/
        TSP tsp = new TSP("data/tsp/tsp_15.txt");
        tsp.buildModel();
        SearchStatistics stats = tsp.solve(true, s -> s.numberOfSolutions() == 1);
        System.out.println(stats);
    }
}
