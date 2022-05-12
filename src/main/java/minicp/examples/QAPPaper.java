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

import minicp.cp.Factory;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.Objective;
import minicp.search.SearchStatistics;
import minicp.util.io.InputReader;
import minicp.util.Procedure;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static minicp.cp.BranchingScheme.firstFail;
import static minicp.cp.Factory.*;

/**
 * The Quadratic Assignment problem.
 * There are a set of n facilities and a set of n locations.
 * For each pair of locations, a distance is specified and for
 * each pair of facilities a weight or flow is specified
 * (e.g., the amount of supplies transported between the two facilities).
 * The problem is to assign all facilities to different locations
 * with the goal of minimizing the sum of the distances multiplied
 * by the corresponding flows.
 * <a href="https://en.wikipedia.org/wiki/Quadratic_assignment_problem">Wikipedia</a>.
 */
public class QAPPaper {
    public static void main(String[] args) {
        // ---- read the instance -----
        InputReader reader = new InputReader("data/qap.txt");

        int n = reader.getInt();
        // Weights
        int[][] w = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                w[i][j] = reader.getInt();
            }
        }
        // Distance
        int[][] d = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                d[i][j] = reader.getInt();
            }
        }
        // Model creation and resolution
        Solver cp = makeSolver();
        IntVar[] x = makeIntVarArray(cp, n, n);

        cp.post(allDifferent(x));
        IntVar[] weightedDist = new IntVar[n * n];
        int k = 0;
        for (int i = 0; i < n; i++) 
            for (int j = 0; j < n; j++) {
                IntVar dij = element(d, x[i], x[j]);
                weightedDist[k++] = mul(dij,w[i][j]);
            }
        IntVar totCost  = sum(weightedDist);
        Objective obj = cp.minimize(totCost);

        DFSearch dfs = makeDfs(cp,() -> {
                int idx = -1; // index of the first variable that is not fixed
                for (int l = 0; l < x.length; l++)
                    if (x[l].size() > 1) {
                        idx = l;
                        break;
                    }
                if (idx == -1)
                    return new Procedure[0];
                else {
                    IntVar xi = x[idx];
                    int v = xi.min();
                    Procedure left = () -> cp.post(Factory.equal(xi, v));
                    Procedure right = () -> cp.post(Factory.notEqual(xi, v));
                    return new Procedure[]{left, right};
                }
            }
            );

        dfs.onSolution(() -> {
                System.out.println("objective:" + totCost.min());
        });
        dfs.optimize(obj);
    }
}
