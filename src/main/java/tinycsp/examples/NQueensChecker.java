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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

public class NQueensChecker {

    int [] q;
    int n = 0;

    static int nRecur = 0;

    public NQueensChecker(int n) {
        this.n = n;
        q = new int[n];
    }

    public void dfs(Consumer<int []> onSolution) {
        dfs(0,onSolution);
    }

    private void dfs(int idx, Consumer<int []> onSolution) {
        nRecur++;
        if (idx == n) {
            if (constraintsSatisfied()) {
                onSolution.accept(Arrays.copyOf(q,n));
            }
        } else {
            for (int i = 0; i < n; i++) {
                q[idx] = i;
                dfs(idx+1, onSolution);
            }
        }
    }

    public boolean constraintsSatisfied() {
        for (int i = 0; i < n; i++) {
            for (int j = i+1; j < n; j++) {
                // no two queens on the same row
                if (q[i] == q[j]) return false;
                // no two queens on the diagonal
                if (Math.abs(q[j] - q[i]) == j-i) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        NQueensChecker q = new NQueensChecker(8);
        ArrayList<int []> solutions = new ArrayList<>();
        q.dfs(0, solution -> solutions.add(solution));
        System.out.println("# solutions: " + solutions.size());
        System.out.println("# recurs: " + nRecur);
    }
}
