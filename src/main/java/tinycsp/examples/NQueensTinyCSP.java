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

package tinycsp.examples;

import tinycsp.TinyCSP;
import tinycsp.Variable;

import java.util.ArrayList;

/**
 * Example that illustrates how TinyCSP can be used to model
 * and solve the n-queens problem.
 */
public class NQueensTinyCSP {

    public static void main(String[] args) {

        int n = 15;
        TinyCSP csp = new TinyCSP();
        Variable[] q = new Variable[n];

        for (int i = 0; i < n; i++) {
            q[i] = csp.makeVariable(n);
        }

        for (int i = 0; i < n; i++) {
            for (int j = i+1; j < n; j++) {
                // queens q[i] and q[i] not on ...
                csp.notEqual(q[i],q[j],0); // ... the same line
                csp.notEqual(q[i],q[j],i-j); // ... the same left diagonal
                csp.notEqual(q[i],q[j],j-i); // ... the same right diagonal
            }
        }

        ArrayList<int []> solutions = new ArrayList<>();

        // collect all the solutions
        long t0 = System.currentTimeMillis();

        csp.dfs(solution -> {
            solutions.add(solution);
        });

        long t1 = System.currentTimeMillis();

        System.out.println("# solutions: " + solutions.size());
        System.out.println("# recurs: " + csp.nRecur);
        System.out.println("# time(ms): " + (t1-t0));


    }
}
