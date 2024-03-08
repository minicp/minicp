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
import minicp.engine.core.BoolVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.SearchStatistics;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import minicp.util.NotImplementedExceptionAssume;
import org.javagrader.Grade;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static minicp.cp.BranchingScheme.firstFail;
import static minicp.cp.Factory.*;
import static org.junit.jupiter.api.Assertions.*;

@Grade(cpuTimeout = 1)
public class IsOrTest extends SolverTest {

    @ParameterizedTest
    @MethodSource("getSolver")
    public void isOr1(Solver cp) {
        try {
            BoolVar[] x = new BoolVar[]{makeBoolVar(cp), makeBoolVar(cp), makeBoolVar(cp), makeBoolVar(cp)};
            BoolVar b = makeBoolVar(cp);
            cp.post(new IsOr(b, x));

            for (BoolVar xi : x)
                assertFalse(xi.isFixed());

            cp.getStateManager().saveState();
            cp.post(equal(x[1], 0));
            cp.post(equal(x[2], 0));
            cp.post(equal(x[3], 0));
            assertFalse(b.isFixed());
            cp.post(equal(x[0], 0));
            assertTrue(b.isFalse());
            cp.getStateManager().restoreState();

            cp.getStateManager().saveState();
            cp.post(equal(x[1], 0));
            cp.post(equal(x[2], 1));
            assertTrue(b.isTrue());
            cp.getStateManager().restoreState();

            cp.getStateManager().saveState();
            cp.post(equal(b, 1));
            cp.post(equal(x[1], 0));
            cp.post(equal(x[2], 0));
            assertFalse(x[0].isFixed());
            cp.post(equal(x[3], 0));
            assertTrue(x[0].isTrue());
            cp.getStateManager().restoreState();


            cp.getStateManager().saveState();
            cp.post(equal(b, 0));
            assertTrue(x[0].isFalse());
            assertTrue(x[1].isFalse());
            assertTrue(x[2].isFalse());
            assertTrue(x[3].isFalse());
            cp.getStateManager().restoreState();


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void isOr2(Solver cp) {
        try {
            BoolVar[] x = new BoolVar[]{makeBoolVar(cp), makeBoolVar(cp), makeBoolVar(cp), makeBoolVar(cp)};
            BoolVar b = makeBoolVar(cp);
            cp.post(new IsOr(b, x));

            DFSearch dfs = makeDfs(cp, firstFail(x));

            dfs.onSolution(() -> {
                        int nTrue = 0;
                        for (BoolVar xi : x) {
                            if (xi.isTrue()) nTrue++;
                        }
                        assertTrue((nTrue > 0 && b.isTrue()) || (nTrue == 0 && b.isFalse()));
                    }
            );

            SearchStatistics stats = dfs.solve();
            assertEquals(16, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }
}
