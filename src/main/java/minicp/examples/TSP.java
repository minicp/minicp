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

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Traveling salesman problem.
 * <a href="https://en.wikipedia.org/wiki/Travelling_salesman_problem">Wikipedia</a>.
 */
public class TSP extends OptimizationProblem {

    public final int n;
    public final int[][] distanceMatrix;
    public IntVar[] succ;
    public IntVar totalDist;
    String instance;

    public TSP(String instancePath) {
        InputReader reader = new InputReader(instancePath);
        instance = reader.getFilename();
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

    /**
     * Performs a large neighborhood search
     */
    public void lns(boolean verbose, Predicate<Integer> stopLNS) {
        // starts from a first solution
        // current best solution
        int[] xBest = IntStream.range(0, n).toArray();
        AtomicInteger bestSol = new AtomicInteger(Integer.MAX_VALUE);
        dfs.onSolution(() -> {
            // Update the current best solution
            for (int i = 0; i < n; i++) {
                xBest[i] = succ[i].min();
            }
            bestSol.set(totalDist.min());
        });
        if (verbose)
            dfs.onSolution(() -> System.out.println(objective));
        dfs.optimize(objective, statistics -> statistics.numberOfSolutions() == 1);
        // first solution found and registered, now the LNS can start

        // TODO modify the percentage and/or failureLimit to find better solutions
        //  You should try to interpret what they will do
        //  For instance, about the percentage, setting 5% will do nothing: you almost start from scratch
        //  But setting 95% will not help much as there are not a lot of things decide: almost everything is fixed!
        //  Try to find the sweet spot for this problem
         int failureLimit = 1000;
         int percentage = 5;
        Random rand = new java.util.Random(42);
        Solver cp = totalDist.getSolver();

        while (!stopLNS.test(bestSol.get())) {
            dfs.optimizeSubjectTo(objective,
                    statistics -> statistics.numberOfFailures() >= failureLimit ||  stopLNS.test(bestSol.get()),
                    () -> {
                        // Assign the fragment percentage% of the variables randomly chosen
                        for (int j = 0; j < n; j++) {
                            if (rand.nextInt(100) < percentage) {
                                // after the solveSubjectTo those constraints are removed
                                cp.post(equal(succ[j], xBest[j]));
                            }
                        }
                    }
            );
        }
    }

    public void lns(long maxRunTime) {
        long maxRunTimeMS = maxRunTime * 1000;
        long startTime = System.currentTimeMillis();
        lns(true, i -> System.currentTimeMillis() - startTime > maxRunTimeMS);
    }

    @Override
    public String toString() {
        return "TSP(" + instance + ')';
    }

    public static void main(String[] args) {
        // instance gr17 https://people.sc.fsu.edu/~jburkardt/datasets/tsp/gr17_d.txt
        // instance fri26 https://people.sc.fsu.edu/~jburkardt/datasets/tsp/fri26_d.txt
        // instance p01 (adapted from) https://people.sc.fsu.edu/~jburkardt/datasets/tsp/p01_d.txt
        // the other instances are located at data/tsp/ and adapted from https://lopez-ibanez.eu/tsptw-instances
        String instance = "data/tsp/tsp_61.txt";
        TSP tsp = new TSP(instance);
        tsp.buildModel();
        // stops at the first solutions using the exact search
        SearchStatistics stats = tsp.solve(true);
        System.out.println(stats);

        // same TSP model but uses a lns
        int runTimeSeconds = 2;
        tsp = new TSP(instance);
        tsp.buildModel();
        tsp.lns(runTimeSeconds);
    }
}
