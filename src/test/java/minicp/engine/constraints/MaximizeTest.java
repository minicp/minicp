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

import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.Objective;
import minicp.search.SearchStatistics;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import minicp.util.NotImplementedExceptionAssume;
import org.junit.Test;

import static minicp.cp.BranchingScheme.EMPTY;
import static minicp.cp.BranchingScheme.branch;
import static minicp.cp.Factory.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MaximizeTest {

    @Test
    public void maximizeTest() {
        try {
            try {

                Solver cp = makeSolver();
                IntVar y = makeIntVar(cp, 10, 20);

                IntVar[] x = new IntVar[]{y};
                DFSearch dfs = makeDfs(cp, () -> y.isFixed() ? EMPTY :
                        branch(() -> cp.post(equal(y, y.min())),
                                () -> cp.post(notEqual(y, y.min()))));
                Objective obj = cp.maximize(y);

                SearchStatistics stats = dfs.solve();

                assertEquals(11, stats.numberOfSolutions());


            } catch (InconsistencyException e) {
                fail("should not fail");
            }
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }


}
