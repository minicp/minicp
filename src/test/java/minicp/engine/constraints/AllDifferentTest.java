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
import minicp.search.SearchStatistics;
import minicp.util.exception.InconsistencyException;
import org.javagrader.Grade;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static minicp.cp.BranchingScheme.firstFail;
import static minicp.cp.Factory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@Grade(cpuTimeout = 1)
public class AllDifferentTest extends SolverTest {

    @ParameterizedTest
    @MethodSource("getSolver")
    public void allDifferentTest1(Solver cp) {

        IntVar[] x = makeIntVarArray(cp, 5, 5);

        try {
            cp.post(allDifferent(x));
            cp.post(equal(x[0], 0));
            for (int i = 1; i < x.length; i++) {
                assertEquals(4, x[i].size());
                assertEquals(1, x[i].min());
            }

        } catch (InconsistencyException e) {
            fail("should not fail");
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void allDifferentTest2(Solver cp) {

        IntVar[] x = makeIntVarArray(cp, 5, 5);

        try {
            cp.post(allDifferent(x));

            SearchStatistics stats = makeDfs(cp, firstFail(x)).solve();
            assertEquals(120, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            fail("should not fail");
        }
    }

}
