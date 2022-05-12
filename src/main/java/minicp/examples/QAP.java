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

import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.Objective;
import minicp.search.SearchStatistics;
import minicp.util.io.InputReader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static minicp.cp.BranchingScheme.firstFail;
import static minicp.cp.BranchingScheme.limitedDiscrepancy;
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
public class QAP {

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

        // ----- build the model ---
        solve(n, w, d, true, stats -> false);
    }

    /**
     * @param n       size of the problem
     * @param w       weights
     * @param d       distances
     * @param verbose indicates if the solver should indicates on stdout its progression
     * @param limit   allow to interrupt the solver faster if needed. See dfs.solve().
     * @return list of solutions encountered
     */
    public static List<Integer> solve(int n, int[][] w, int[][] d, boolean verbose, Predicate<SearchStatistics> limit) {
        Solver cp = makeSolver();
        IntVar[] x = makeIntVarArray(cp, n, n);

        cp.post(allDifferent(x));


        // build the objective function
        IntVar[] weightedDist = new IntVar[n * n];
        for (int k = 0, i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                weightedDist[k] = mul(element(d, x[i], x[j]), w[i][j]);
                k++;
            }
        }
        IntVar totCost = sum(weightedDist);
        Objective obj = cp.minimize(totCost);

        /*
        // TODO: discrepancy search (to be implemented as an exercise)
        for (int dL = 0; dL < x.length; dL++) {
            DFSearch dfs = makeDfs(cp, limitedDiscrepancy(firstFail(x), dL));
            dfs.optimize(obj);
        }
        */

        DFSearch dfs = makeDfs(cp, firstFail(x));

        ArrayList<Integer> solutions = new ArrayList<>();
        dfs.onSolution(() -> {
            solutions.add(totCost.min());

            if (verbose)
                System.out.println("objective:" + totCost.min());
        });

        SearchStatistics stats = dfs.optimize(obj, limit);
        if (verbose)
            System.out.println(stats);

        return solutions;
    }
}
