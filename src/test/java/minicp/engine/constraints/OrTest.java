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
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.SearchStatistics;
import minicp.state.StateSparseSet;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import minicp.util.NotImplementedExceptionAssume;
import org.javagrader.Grade;
import org.javagrader.GradeFeedback;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static minicp.cp.BranchingScheme.*;
import static minicp.cp.Factory.*;
import static org.javagrader.TestResultStatus.TIMEOUT;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;

@Grade(cpuTimeout = 1)
public class OrTest extends SolverTest {

    @ParameterizedTest
    @MethodSource("getSolver")
    public void or1(Solver cp) {
        try {
            BoolVar[] x = new BoolVar[]{makeBoolVar(cp), makeBoolVar(cp), makeBoolVar(cp), makeBoolVar(cp)};
            cp.post(new Or(x));

            for (BoolVar xi : x) {
                assertTrue(!xi.isFixed());
            }

            cp.post(equal(x[1], 0));
            cp.post(equal(x[2], 0));
            cp.post(equal(x[3], 0));
            assertTrue(x[0].isTrue());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void or2(Solver cp) {
        try {
            BoolVar[] x = new BoolVar[]{makeBoolVar(cp), makeBoolVar(cp), makeBoolVar(cp), makeBoolVar(cp)};
            cp.post(new Or(x));


            DFSearch dfs = makeDfs(cp, firstFail(x));

            dfs.onSolution(() -> {
                        int nTrue = 0;
                        for (BoolVar xi : x) {
                            if (xi.isTrue()) nTrue++;
                        }
                        assertTrue(nTrue > 0);

                    }
            );

            SearchStatistics stats = dfs.solve();

            assertEquals(15, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            fail("should not fail");

        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void or3(Solver cp) {
        try {
            BoolVar[] x = new BoolVar[]{makeBoolVar(cp), makeBoolVar(cp), makeBoolVar(cp), makeBoolVar(cp)};
            
            for (BoolVar xi : x) {
                xi.fix(false);
            }
            
            cp.post(new Or(x));
            fail("should fail");
            
        } catch (InconsistencyException e) {
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Grade(cpuTimeout = 4)
    @GradeFeedback(message = "Are you using watched literals in your constraint?", on = TIMEOUT)
    @Test
    public void or4() {
        try {
            // create an array of variables, with a lot fixed to false and only a few unfixed at the center of the array
            Solver cp = makeSolver();
            int nVars = 500_000;
            int nFixed = 17;
            int firstFixed = (nVars - nFixed) / 2;
            int lastFixed = (nVars + nFixed) / 2;
            BoolVar[] x = new BoolVar[nVars];
            for (int i = 0 ; i < x.length ; i++) {
                x[i] = makeBoolVar(cp);
                if (i < firstFixed || i >= lastFixed) {
                    x[i].fix(false); // only variables at the center of the array are unfixed, the other are false
                }
            }

            cp.post(new Or(x));
            // search for a solution
            DFSearch search = makeDfs(cp, () -> {
                IntVar xs = null;
                for (int i = firstFixed ; i < lastFixed ; i++) {
                    if (!x[i].isFixed()) {
                        xs = x[i];
                    }
                }
                if (xs == null)
                    return EMPTY;
                IntVar branchingVar = xs;
                return branch(() -> cp.post(equal(branchingVar, 1)),
                        () -> cp.post(equal(branchingVar, 0)));
            });
            // checks that at least one variable is set to true
            search.onSolution(() -> {
                boolean valid = false;
                for (int i = firstFixed; i < lastFixed ; i++) {
                    if (x[i].isTrue()) {
                        valid = true;
                        break;
                    }
                }
                assertTrue(valid, "One variable needs to be fixed when using the or constraint");
            });
            for (int i = 0 ; i < 20 ; i++) {
                SearchStatistics statistics = search.solve();
                assertEquals(131_071, statistics.numberOfSolutions()); // 2^17 - 1
            }
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

}
