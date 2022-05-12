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

import static minicp.cp.BranchingScheme.*;
import static minicp.examples.SMoney.Letters.*;
import static minicp.cp.Factory.*;

/**
 * The Send-More-Money problem.
 *    S E N D
 * +  M O R E
 * ----------
 *  M O N E Y
 * All digits values are different.
 * Leading digits can't be zero
 */
public class SMoney {

    enum Letters {
        S(0), E(1), N(2), D(3), M(4), O(5), R(6), Y(7);
        public final int val;

        Letters(int v) {
            val = v;
        }
    };

    public static void main(String[] args) {
        Solver cp = Factory.makeSolver(false);
        IntVar[] values = Factory.makeIntVarArray(cp, Y.val + 1, 0, 9);
        IntVar[] carry = Factory.makeIntVarArray(cp, 4, 0, 1);

        cp.post(allDifferent(values));
        cp.post(notEqual(values[S.val], 0));
        cp.post(notEqual(values[M.val], 0));
        cp.post(equal(values[M.val], carry[3]));
        cp.post(equal(sum(carry[2], values[S.val], values[M.val], minus(values[O.val]), mul(carry[3], -10)), 0));
        cp.post(equal(sum(carry[1], values[E.val], values[O.val], minus(values[N.val]), mul(carry[2], -10)), 0));
        cp.post(equal(sum(carry[0], values[N.val], values[R.val], minus(values[E.val]), mul(carry[1], -10)), 0));
        cp.post(equal(sum(values[D.val], values[E.val], minus(values[Y.val]), mul(carry[0], -10)), 0));


        DFSearch search = Factory.makeDfs(cp, firstFail(values));

        search.onSolution(() ->
                System.out.println("solution:" + Arrays.toString(values))
        );
        SearchStatistics stats = search.solve();
        System.out.format("#Solutions: %s\n", stats.numberOfSolutions());
        System.out.format("Statistics: %s\n", stats);
    }
}
