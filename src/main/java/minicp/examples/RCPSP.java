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

import minicp.engine.constraints.Cumulative;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.Objective;
import minicp.search.SearchStatistics;
import minicp.util.exception.NotImplementedException;
import minicp.util.io.InputReader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static minicp.cp.BranchingScheme.firstFail;
import static minicp.cp.Factory.*;
import minicp.util.exception.NotImplementedException;


/**
 * Resource Constrained Project Scheduling Problem.
 * <a href="http://www.om-db.wi.tum.de/psplib/library.html">PSPLIB</a>.
 */
public class RCPSP extends OptimizationProblem {

    public final int nActivities;
    public final int nResources;
    public final int horizon;
    public final int[] capa;
    public final int[] duration;
    public final int[][] consumption;
    public final int[][] successors;
    String instance;

    public IntVar[] start;
    public IntVar[] end;
    public RCPSP(String instanceFilePath) {
        // Reading the data
        InputReader reader = new InputReader(instanceFilePath);
        instance = reader.getFilename();
        // use all instances at data/rcpsp

        nActivities = reader.getInt();
        nResources = reader.getInt();

        int[] capa = new int[nResources];
        for (int i = 0; i < nResources; i++) {
            capa[i] = reader.getInt();
        }

        int[] duration = new int[nActivities];
        int[][] consumption = new int[nResources][nActivities];
        int[][] successors = new int[nActivities][];


        int horizon = 0;
        for (int i = 0; i < nActivities; i++) {
            // durations, demand for each resource, successors
            duration[i] = reader.getInt();
            horizon += duration[i];
            for (int r = 0; r < nResources; r++) {
                consumption[r][i] = reader.getInt();
            }
            successors[i] = new int[reader.getInt()];
            for (int k = 0; k < successors[i].length; k++) {
                successors[i][k] = reader.getInt() - 1;
            }
        }

        this.horizon = horizon;
        this.capa = capa;
        this.duration = duration;
        this.consumption = consumption;
        this.successors = successors;
    }

    @Override
    public void buildModel() {
        // The Model
        Solver cp = makeSolver();
        start = makeIntVarArray(cp, nActivities, horizon);
        end = new IntVar[nActivities];


        for (int i = 0; i < nActivities; i++) {
            end[i] = plus(start[i], duration[i]);
        }

        // TODO 1: add the cumulative constraint to model the resource
        // capa[r] is the capacity of resource r
        // consumption[r] is the consumption for each activity on the resource [r]
        // duration is the duration of each activity

        // TODO 2: add the precedence constraints
        // successors[i] is the sucessors of activity i

        // TODO 3: minimize the makespan

        // TODO 4: implement the search

        
        // TODO add the constraints and remove the NotImplementedException
         throw new NotImplementedException("RCPSP");
    }

    @Override
    public String toString() {
        return "RCPSP(" + instance + ')';
    }

    public static void main(String[] args) {
        RCPSP rcpsp = new RCPSP("data/rcpsp/j30_1_3.rcp");
        rcpsp.buildModel();
        rcpsp.solve( true);
    }
}
