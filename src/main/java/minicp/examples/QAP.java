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

import java.util.function.Predicate;
import java.util.stream.IntStream;

import static minicp.cp.BranchingScheme.*;
import static minicp.cp.Factory.*;
import minicp.util.exception.NotImplementedException;

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
public class QAP extends OptimizationProblem {

    /**
     * Utility class to store pairs of integers
     */
    static class Pair {
        final int first;
        final int second;
        Pair(int first, int second) {
            this.first = first;
            this.second = second;
        }
    }

    public final int n;
    public final int[][] weights;
    public final int[][] distances;
    public IntVar[] x;
    public IntVar[] weightedDistances;
    public IntVar totCost;
    String instance;

    public QAP(String instanceFilePath) {
        InputReader reader = new InputReader(instanceFilePath);
        instance = reader.getFilename();

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
        this.n = n;
        this.weights = w;
        this.distances = d;
    }

    @Override
    public void buildModel() {
        Solver cp = makeSolver();
        x = makeIntVarArray(cp, n, n);

        cp.post(allDifferent(x));

        // build the objective function
        weightedDistances = new IntVar[n * n];
        for (int k = 0, i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                weightedDistances[k] = mul(element(distances, x[i], x[j]), weights[i][j]);
                k++;
            }
        }
        totCost = sum(weightedDistances);
        objective = cp.minimize(totCost);

        Pair[] pairs = IntStream.range(0, n)
                .mapToObj(i -> IntStream.range(0, n).mapToObj(j -> new Pair(i,j)))
                .flatMap(s -> s)
                .filter(p -> p.first != p.second)
                .toArray(Pair[]::new);

        dfs = makeDfs(cp, () -> {
            // TODO modify the default variable selector
            IntVar sel = selectMin(x,
                    vari -> vari.size() > 1, // filter
                    vari -> vari.size()      // variable selector
            );
            if (sel == null)
                return EMPTY;
            int v = sel.min(); // TODO modify the default value selector
            return branch(
                    () -> cp.post(equal(sel,v)),
                    () -> cp.post(notEqual(sel,v))
            );
        });

        /*
        // TODO: discrepancy search (to be implemented as an exercise)
        for (int dL = 0; dL < x.length; dL++) {
            dfs = makeDfs(cp, limitedDiscrepancy(firstFail(x), dL));

        }
        */
        // TODO implement the search and remove the NotImplementedException
         throw new NotImplementedException("QAP");
    }

    @Override
    public String toString() {
        return "QAP(" + instance + ')';
    }

    public static void main(String[] args) {
        // ---- read the instance -----
        QAP qap = new QAP("data/qap.txt");
        // ----- build the model ---
        qap.buildModel();
        // ----- solve the model ---
        SearchStatistics statistics = qap.solve(true, stats -> false);
        System.out.println(statistics);
    }

}