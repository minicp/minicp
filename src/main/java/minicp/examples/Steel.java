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
import minicp.engine.constraints.IsOr;
import minicp.engine.constraints.LessOrEqual;
import minicp.engine.core.BoolVar;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.Objective;
import minicp.search.SearchStatistics;
import minicp.util.exception.InconsistencyException;
import minicp.util.io.InputReader;
import minicp.util.Procedure;
import minicp.util.exception.NotImplementedException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static minicp.cp.BranchingScheme.*;
import static minicp.cp.Factory.*;

/**
 * Steel is produced by casting molten iron into slabs.
 * A steel mill can produce a finite number of slab sizes.
 * An order has two properties, a colour corresponding to the route required through the steel mill and a weight.
 * Given d input orders, the problem is to assign the orders to slabs, the number and size of which are also to be determined,
 * such that the total weight of steel produced is minimised.
 * This assignment is subject to two further constraints:
 * - Capacity constraints: The total weight of orders assigned to a slab cannot exceed the slab capacity.
 * - Colour constraints: Each slab can contain at most p of k total colours (p is usually 2).
 * <a href="http://www.csplib.org/Problems/prob038/">CSPLib</a>
 */
public class Steel extends OptimizationProblem {

    public final String instance;
    public final int nCapa;
    public final int[] capa;
    public final int maxCapa;
    public final int[] loss;
    public final int nCol;
    public final int nSlab;
    public final int nOrder;
    public final int[] w;
    public final int[] c;

    public Solver cp;
    public IntVar[] x;          // x[i] = j if order i is placed in slab j
    public IntVar[] l;          // l[j] = load within slab j
    public BoolVar[][] inSlab;  // inSlab[j][i] = 1 if order i is placed in slab j
    public IntVar totLoss;      // sum of the losses: the objective to minimize

    public Steel(String instancePath) {
        InputReader reader = new InputReader(instancePath);
        instance = reader.getFilename();
        // Reading the data
        nCapa = reader.getInt();
        capa = new int[nCapa];
        for (int i = 0; i < nCapa; i++) {
            capa[i] = reader.getInt();
        }
        maxCapa = capa[capa.length - 1];
        loss = new int[maxCapa + 1];
        int capaIdx = 0;
        for (int i = 0; i < maxCapa; i++) {
            loss[i] = capa[capaIdx] - i;
            if (loss[i] == 0) capaIdx++;
        }
        loss[0] = 0;

        nCol = reader.getInt();
        nSlab = reader.getInt();
        nOrder = nSlab;
        w = new int[nSlab];
        c = new int[nSlab];
        for (int i = 0; i < nSlab; i++) {
            w[i] = reader.getInt();
            c[i] = reader.getInt() - 1;
        }
    }

    @Override
    public void buildModel() {
        // ---------------------------
        try {
            cp = makeSolver();
            x = makeIntVarArray(cp, nOrder, nSlab); // x[i] = j if order i is placed in slab j
            l = makeIntVarArray(cp, nSlab, maxCapa + 1); // loss of each slab

            inSlab = new BoolVar[nSlab][nOrder]; // inSlab[j][i] = 1 if order i is placed in slab j

            for (int j = 0; j < nSlab; j++) {
                for (int i = 0; i < nOrder; i++) {
                    inSlab[j][i] = isEqual(x[i], j);
                }
            }

            for (int j = 0; j < nSlab; j++) {
                // for each color, is it present in the slab
                IntVar[] presence = new IntVar[nCol];

                for (int col = 0; col < nCol; col++) {
                    presence[col] = makeBoolVar(cp);

                    ArrayList<BoolVar> inSlabWithColor = new ArrayList<>();
                    for (int i = 0; i < nOrder; i++) {
                        if (c[i] == col) inSlabWithColor.add(inSlab[j][i]);
                    }

                    // TODO 2: model that presence[col] is true iff at least one order with color col is placed in slab j
                    
                }
                // TODO 3: restrict the number of colors present in slab j to be <= 2
                
            }

            // bin packing constraint
            for (int j = 0; j < nSlab; j++) {
                IntVar[] wj = new IntVar[nSlab];
                for (int i = 0; i < nOrder; i++) {
                    wj[i] = mul(inSlab[j][i], w[i]);
                }
                cp.post(sum(wj, l[j]));
            }

            // TODO 4: add the redundant constraint that the sum of the loads is equal to the sum of elements
            

            // TODO 1: model the objective function using element constraint + a sum constraint
            totLoss = null;
            
            if (totLoss == null)
                throw new NotImplementedException("Steel");

            objective = cp.minimize(totLoss);

            // TODO 5 add static symmetry breaking constraint

            // TODO 6 implement a dynamic symmetry breaking during search
            dfs = makeDfs(cp,firstFail(x));
        } catch (InconsistencyException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Steel(" + instance + ")";
    }

    public SearchStatistics solve(boolean verbose, Predicate<SearchStatistics> limit) {
        if (verbose) {
            dfs.onSolution(() -> {
                System.out.println("---");
                Set<Integer>[] colorsInSlab = new Set[nSlab];
                for (int j = 0; j < nSlab; j++) {
                    colorsInSlab[j] = new HashSet<>();
                }
                for (int i = 0; i < nOrder; i++) {
                    colorsInSlab[x[i].min()].add(c[i]);
                }
                for (int j = 0; j < nSlab; j++) {
                    if (colorsInSlab[j].size() > 2) {
                        System.out.println("problem, " + colorsInSlab[j].size() + " colors in slab " + j + " should be <= 2");
                    }
                }
            });
        }
        return super.solve(verbose, limit);
    }

    public static void main(String[] args) {
        Steel steel = new Steel("data/steel/bench_19_10");
        steel.buildModel();
        SearchStatistics statistics = steel.solve(true);
        System.out.println(statistics);
    }

}
