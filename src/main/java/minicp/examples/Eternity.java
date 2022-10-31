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
import minicp.engine.constraints.TableCT;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.util.io.InputReader;

import java.util.Arrays;

import static minicp.cp.BranchingScheme.and;
import static minicp.cp.BranchingScheme.firstFail;
import static minicp.cp.Factory.*;
import minicp.util.exception.NotImplementedException;

/**
 *
 *  The Eternity II puzzle is an edge-matching puzzle which
 *  involves placing 256 square puzzle pieces into a 16 by 16 grid,
 *  constrained by the requirement to match adjacent edges.
 *  <a href="https://en.wikipedia.org/wiki/Eternity_II_puzzle">Wikipedia.</a>
 */
public class Eternity extends SatisfactionProblem {
    public final int n;
    public final int m;
    public final int max;
    public final int[][] pieces;

    public IntVar[][] id;
    public IntVar[][] u;
    public IntVar[][] d;
    public IntVar[][] l;
    public IntVar[][] r;

    public Eternity(String instanceFilePath) {
        this(false, instanceFilePath);
    }

    public Eternity(boolean verbose, String instanceFilePath) {
        InputReader reader = new InputReader(instanceFilePath); // Reading the data
        n = reader.getInt();
        m = reader.getInt();

        int[][] pieces = new int[n * m][4];
        int maxTmp = 0;

        for (int i = 0; i < n * m; i++) {
            for (int j = 0; j < 4; j++) {
                pieces[i][j] = reader.getInt();
                if (pieces[i][j] > maxTmp)
                    maxTmp = pieces[i][j];
            }
            if (verbose) { // print the pieces from the instance
                System.out.println(Arrays.toString(pieces[i]));
            }
        }
        this.max = maxTmp;
        this.pieces = pieces;
    }

    public static IntVar[] flatten(IntVar[][] x) {
        return Arrays.stream(x).flatMap(Arrays::stream).toArray(IntVar[]::new);
    }

    @Override
    public void buildModel() {
        // ------------------------

        // TODO: create the table where each line correspond to one possible rotation of a piece
        // For instance if the line piece[6] = [2,3,5,1]
        // the four lines created in the table are
        // [6,2,3,5,1] // rotation of 0°
        // [6,3,5,1,2] // rotation of 90°
        // [6,5,1,2,3] // rotation of 180°
        // [6,1,2,3,5] // rotation of 270°

        // Table with makeIntVarArray pieces and for each their 4 possible rotations

        int[][] table = new int[4 * n * m][5];
        

        Solver cp = makeSolver();

        //   |         |
        // - +---------+- -
        //   |    u    |
        //   | l  i  r |
        //   |    d    |
        // - +---------+- -
        //   |         |


        id = new IntVar[n][m]; // id
        u = new IntVar[n][m];  // up
        r = new IntVar[n][m];  // right
        d = new IntVar[n][m];  // down
        l = new IntVar[n][m];  // left

        for (int i = 0; i < n; i++) {
            u[i] = Factory.makeIntVarArray(m, j -> makeIntVar(cp, 0, max));
            id[i] = makeIntVarArray(cp, m, n * m);
        }
        for (int k = 0; k < n; k++) {
            final int i = k;
            if (i < n - 1) {
                d[i] = u[i + 1];
            } else {
                d[i] = Factory.makeIntVarArray(m, j -> makeIntVar(cp, 0, max));
            }
        }
        for (int j = 0; j < m; j++) {
            for (int i = 0; i < n; i++) {
                l[i][j] = makeIntVar(cp, 0, max);
            }
        }
        for (int j = 0; j < m; j++) {
            for (int i = 0; i < n; i++) {
                if (j < m - 1) {
                    r[i][j] = l[i][j + 1];
                } else {
                    r[i][j] = makeIntVar(cp, 0, max);
                }
            }
        }

        // The constraints of the problem

        // TODO: State the constraints of the problem

        // Constraint1: all the pieces placed are different

        // Constraint2: all the pieces placed are valid ones i.e. one of the given mxn pieces possibly rotated

        // Constraint3: place "0" one all external side of the border (gray color)

        


        // The search using the and combinator

        dfs = makeDfs(cp,
                /* TODO: continue, are you branching on all the variables ? */
                 and(firstFail(flatten((id))), firstFail(flatten(u)))
        );
        // TODO add the constraints and remove the NotImplementedException
         throw new NotImplementedException("Eternity");
    }

    /**
     * Prints a solution when it is found
     */
    public void printSolutionFound() {
        dfs.onSolution(() -> {
            System.out.println("----------------");
            // Pretty Print
            for (int i = 0; i < n; i++) {
                StringBuilder line = new StringBuilder("   ");
                for (int j = 0; j < m; j++) {
                    line.append(u[i][j].min()).append("   ");
                }
                System.out.println(line);
                line = new StringBuilder(" ");
                for (int j = 0; j < m; j++) {
                    line.append(l[i][j].min()).append("   ");
                }
                line.append(r[i][m - 1].min());
                System.out.println(line);
            }
            StringBuilder line = new StringBuilder("   ");
            for (int j = 0; j < m; j++) {
                line.append(d[n - 1][j].min()).append("   ");
            }
            System.out.println(line);
        });
    }

    public static void main(String[] args) {
        // also use instances at data/eternity/brendan
        // verbose = true ==> print the pieces from the instance
        Eternity eternity = new Eternity(true, "data/eternity/brendan/pieces_03x03.txt");
        eternity.buildModel();
        eternity.printSolutionFound();
        //eternity.solve( true, stats -> stats.numberOfSolutions() == 5);
        eternity.solve( true);
    }

}
