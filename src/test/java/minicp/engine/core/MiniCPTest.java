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

package minicp.engine.core;

import minicp.engine.SolverTest;
import minicp.search.DFSearch;
import minicp.search.SearchStatistics;
import org.junit.Test;

import static minicp.cp.BranchingScheme.*;
import static minicp.cp.Factory.*;
import static org.junit.Assert.assertEquals;


public class MiniCPTest extends SolverTest {


    @Test
    public void testSolveSubjectTo() {
        Solver cp = makeSolver();
        IntVar[] x = makeIntVarArray(cp, 3, 2);

        DFSearch dfs = makeDfs(cp, firstFail(x));


        SearchStatistics stats1 = dfs.solveSubjectTo(l -> false, () -> {
            cp.post(equal(x[0], 0));
        });

        assertEquals(4, stats1.numberOfSolutions());

        SearchStatistics stats2 = dfs.solve(l -> false);

        assertEquals(8, stats2.numberOfSolutions());


    }

    @Test
    public void testDFS() {
        Solver cp = solverFactory.get();
        IntVar[] values = makeIntVarArray(cp, 3, 2);

        DFSearch dfs = makeDfs(cp, () -> {
            int sel = -1;
            for (int i = 0; i < values.length; i++)
                if (values[i].size() > 1 && sel == -1)
                    sel = i;
            final int i = sel;
            if (i == -1)
                return EMPTY;
            else return branch(() -> cp.post(equal(values[i], 0)),
                    () -> cp.post(equal(values[i], 1)));
        });


        SearchStatistics stats = dfs.solve();

        assertEquals (8,stats.numberOfSolutions());
        assertEquals (0,stats.numberOfFailures());
        assertEquals ((8 + 4 + 2),stats.numberOfNodes());
    }


}
