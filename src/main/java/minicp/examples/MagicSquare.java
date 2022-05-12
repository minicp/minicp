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
import minicp.search.SearchStatistics;

import java.util.Arrays;

import static minicp.cp.BranchingScheme.firstFail;
import static minicp.cp.Factory.*;

/**
 * The Magic Square problem.
 * <a href="http://csplib.org/Problems/prob019/">CSPLib</a>.
 */
public class MagicSquare {

    //
    public static void main(String[] args) {

        int n = 6;
        int sumResult = n * (n * n + 1) / 2;

        Solver cp = Factory.makeSolver();
        IntVar[][] x = new IntVar[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                x[i][j] = makeIntVar(cp, 1, n * n);
            }
        }


        IntVar[] xFlat = new IntVar[x.length * x.length];
        for (int i = 0; i < x.length; i++) {
            System.arraycopy(x[i], 0, xFlat, i * x.length, x.length);
        }


        // AllDifferent
        cp.post(allDifferent(xFlat));

        // Sum on lines
        for (int i = 0; i < n; i++) {
            cp.post(sum(x[i], sumResult));
        }

        // Sum on columns
        for (int j = 0; j < x.length; j++) {
            IntVar[] column = new IntVar[n];
            for (int i = 0; i < x.length; i++)
                column[i] = x[i][j];
            cp.post(sum(column, sumResult));
        }

        // Sum on diagonals
        IntVar[] diagonalLeft = new IntVar[n];
        IntVar[] diagonalRight = new IntVar[n];
        for (int i = 0; i < x.length; i++) {
            diagonalLeft[i] = x[i][i];
            diagonalRight[i] = x[n - i - 1][i];
        }
        cp.post(sum(diagonalLeft, sumResult));
        cp.post(sum(diagonalRight, sumResult));

        DFSearch dfs = makeDfs(cp, firstFail(xFlat));

        dfs.onSolution(() -> {
                    for (int i = 0; i < n; i++) {
                        System.out.println(Arrays.toString(x[i]));
                    }
                }
        );

        SearchStatistics stats = dfs.solve(stat -> stat.numberOfSolutions() >= 1); // stop on first solution

        System.out.println(stats);
    }

}
