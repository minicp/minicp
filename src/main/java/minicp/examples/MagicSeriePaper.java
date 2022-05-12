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
import minicp.util.Procedure;

import static minicp.cp.BranchingScheme.*;
import static minicp.cp.Factory.*;

/**
 * The Magic Series problem.
 * <a href="http://csplib.org/Problems/prob019/">CSPLib</a>.
 */
public class MagicSeriePaper {
    public static void main(String[] args) {
        int n = 8;
        Solver cp = makeSolver(false);
        IntVar[] s = makeIntVarArray(cp, n, n);

        for (int i = 0; i < n; i++) {
            final int fi = i;
            cp.post(sum(makeIntVarArray(n, j -> isEqual(s[j], fi)), s[i]));
        }
        cp.post(sum(s, n));
        cp.post(sum(makeIntVarArray(n, i -> mul(s[i], i)), n));

        DFSearch dfs = makeDfs(cp, () -> {
            int idx = -1; // index of the first variable that is not fixed
            for (int k = 0; k < s.length; k++)
                if (s[k].size() > 1) {
                    idx = k;
                    break;
                }
            if (idx == -1)
                return new Procedure[0];
            else {
                IntVar si = s[idx];
                int v = si.min();
                Procedure left = () -> cp.post(Factory.equal(si, v));
                Procedure right = () -> cp.post(Factory.notEqual(si, v));
                return new Procedure[]{left, right};
            }                
        });

        dfs.onSolution(() ->
                System.out.println("solution:" + Arrays.toString(s))
        );
        dfs.solve();
    }
}
