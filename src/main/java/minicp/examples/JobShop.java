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

import minicp.engine.constraints.Disjunctive;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.Objective;
import minicp.search.SearchStatistics;
import minicp.util.exception.InconsistencyException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import static minicp.cp.BranchingScheme.firstFail;
import static minicp.cp.Factory.*;

/**
 * The JobShop Problem.
 * <a href="https://en.wikipedia.org/wiki/Job_shop_scheduling">Wikipedia.</a>
 */
public class JobShop {

    public static IntVar[] flatten(IntVar[][] x) {
        return Arrays.stream(x).flatMap(Arrays::stream).toArray(IntVar[]::new);
    }

    public static void main(String[] args) {

        // Reading the data

        try {
            FileInputStream istream = new FileInputStream("data/jobshop/sascha/jobshop-7-7-4");
            BufferedReader in = new BufferedReader(new InputStreamReader(istream));
            in.readLine();
            in.readLine();
            in.readLine();
            StringTokenizer tokenizer = new StringTokenizer(in.readLine());
            int nJobs = Integer.parseInt(tokenizer.nextToken());
            int nMachines = Integer.parseInt(tokenizer.nextToken());

            System.out.println(nJobs + " " + nMachines);
            int[][] duration = new int[nJobs][nMachines];
            int[][] machine = new int[nJobs][nMachines];
            int horizon = 0;
            for (int i = 0; i < nJobs; i++) {
                tokenizer = new StringTokenizer(in.readLine());
                for (int j = 0; j < nMachines; j++) {
                    machine[i][j] = Integer.parseInt(tokenizer.nextToken());
                    duration[i][j] = Integer.parseInt(tokenizer.nextToken());
                    horizon += duration[i][j];
                }
            }

            Solver cp = makeSolver();

            IntVar[][] start = new IntVar[nJobs][nMachines];
            IntVar[][] end = new IntVar[nJobs][nMachines];
            ArrayList<IntVar>[] startOnMachine = new ArrayList[nMachines];
            ArrayList<Integer>[] durationsOnMachine = new ArrayList[nMachines];
            for (int m = 0; m < nMachines; m++) {
                startOnMachine[m] = new ArrayList<IntVar>();
                durationsOnMachine[m] = new ArrayList<Integer>();
            }

            IntVar[] endLast = new IntVar[nJobs];
            for (int i = 0; i < nJobs; i++) {

                for (int j = 0; j < nMachines; j++) {

                    start[i][j] = makeIntVar(cp, 0, horizon);
                    end[i][j] = plus(start[i][j], duration[i][j]);
                    int m = machine[i][j];
                    startOnMachine[m].add(start[i][j]);
                    durationsOnMachine[m].add(duration[i][j]);

                    if (j > 0) {
                        // precedence constraint
                        cp.post(lessOrEqual(end[i][j - 1], start[i][j]));
                    }
                }
                endLast[i] = end[i][nMachines - 1];
            }


            for (int m = 0; m < nMachines; m++) {

                int[] durations = new int[nJobs];
                for (int i = 0; i < nJobs; i++) {
                    durations[i] = durationsOnMachine[m].get(i);
                }
                IntVar[] starts = startOnMachine[m].toArray(new IntVar[]{});
                cp.post(new Disjunctive(starts, durations));
            }

            IntVar makespan = maximum(endLast);


            Objective obj = cp.minimize(makespan);

            DFSearch dfs = makeDfs(cp, firstFail(flatten(start)));


            dfs.onSolution(() ->
                    System.out.println("makespan:" + makespan)
            );

            SearchStatistics stats = dfs.optimize(obj);

            System.out.format("Statistics: %s\n", stats);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InconsistencyException e) {
            e.printStackTrace();
        }


    }
}
