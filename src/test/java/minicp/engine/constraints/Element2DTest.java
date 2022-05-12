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

package minicp.engine.constraints;

import minicp.engine.SolverTest;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.SearchStatistics;
import minicp.util.exception.InconsistencyException;
import org.junit.Test;

import static minicp.cp.BranchingScheme.firstFail;
import static minicp.cp.Factory.makeDfs;
import static minicp.cp.Factory.makeIntVar;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class Element2DTest extends SolverTest {

    @Test
    public void element2dTest1() {

        try {

            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, -2, 40);
            IntVar y = makeIntVar(cp, -3, 10);
            IntVar z = makeIntVar(cp, 2, 40);

            int[][] T = new int[][]{
                    {9, 8, 7, 5, 6},
                    {9, 1, 5, 2, 8},
                    {8, 3, 1, 4, 9},
                    {9, 1, 2, 8, 6},
            };

            cp.post(new Element2D(T, x, y, z));

            assertEquals(0, x.min());
            assertEquals(0, y.min());
            assertEquals(3, x.max());
            assertEquals(4, y.max());
            assertEquals(2, z.min());
            assertEquals(9, z.max());

            z.removeAbove(7);
            cp.fixPoint();

            assertEquals(1, y.min());

            x.remove(0);
            cp.fixPoint();

            assertEquals(6, z.max());
            assertEquals(3, x.max());

            y.remove(4);
            cp.fixPoint();

            assertEquals(5, z.max());
            assertEquals(2, z.min());

            y.remove(2);
            cp.fixPoint();

            assertEquals(4, z.max());
            assertEquals(2, z.min());


        } catch (InconsistencyException e) {
            fail("should not fail");
        }
    }

    @Test
    public void element2dTest2() {

        try {

            Solver cp = solverFactory.get();
            IntVar x = makeIntVar(cp, -2, 40);
            IntVar y = makeIntVar(cp, -3, 10);
            IntVar z = makeIntVar(cp, -20, 40);

            int[][] T = new int[][]{
                    {9, 8, 7, 5, 6},
                    {9, 1, 5, 2, 8},
                    {8, 3, 1, 4, 9},
                    {9, 1, 2, 8, 6},
            };

            cp.post(new Element2D(T, x, y, z));

            DFSearch dfs = makeDfs(cp, firstFail(x, y, z));
            dfs.onSolution(() ->
                    assertEquals(T[x.min()][y.min()], z.min())
            );
            SearchStatistics stats = dfs.solve();

            assertEquals(20, stats.numberOfSolutions());


        } catch (InconsistencyException e) {
            fail("should not fail");
        }
    }

}
