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
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import org.javagrader.Grade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;

import static minicp.cp.BranchingScheme.*;
import static minicp.cp.Factory.*;
import static org.junit.jupiter.api.Assertions.*;

@Grade(cpuTimeout = 1)
public class AllDifferentFWCTest extends SolverTest {

    @ParameterizedTest
    @MethodSource("getSolver")
    public void allDifferentTest1(Solver cp) {

        IntVar[] x = makeIntVarArray(cp, 5, 5);

        try {
            AllDifferentFWC allDiff = new AllDifferentFWC(x);
            allDiff.post();
            allDiff.setActive(false);
            cp.post(equal(x[0], 0));
            for (int i = 1; i < x.length; i++) {
                assertEquals(5, x[i].size());
                assertEquals(0, x[i].min());
            }
            allDiff.propagate();
            for (int i = 1; i < x.length; i++) {
                assertEquals(4, x[i].size());
                assertEquals(1, x[i].min());
            }

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void allDifferentTest2(Solver cp) {

        IntVar[] x = makeIntVarArray(cp, 5, 5);

        try {
            cp.post(new AllDifferentFWC(x));

            SearchStatistics stats = makeDfs(cp, firstFail(x)).solve();
            assertEquals(120, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void allDifferentTest3(Solver cp) {

        IntVar[] x = makeIntVarArray(cp, 5, 5);
        try {
            AllDifferentFWC allDiff = new AllDifferentFWC(x);
            allDiff.post();
            allDiff.setActive(false);

            cp.post(equal(x[2], 3));
            cp.post(equal(x[1], 3));
            try {
                allDiff.propagate();
                fail();
            } catch (InconsistencyException e) {

            }
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void allDifferentTest4(Solver cp) {

        IntVar[] x = makeIntVarArray(cp, 7, 7);

        try {
            cp.post(new AllDifferentFWC(x));

            DFSearch search = makeDfs(cp, () -> {
                // selects the unfixed variable with the smallest domain
                IntVar xs = selectMin(x, xi -> !xi.isFixed(), xi -> xi.size());
                if (xs == null)
                    return EMPTY; // solution found
                // check all diff
                for (int i = 0 ; i < x.length ; i++) {
                    if (x[i].isFixed()) {
                        for (int j = 0 ; j < x.length ; j++) {
                            if (i != j) {
                                assertFalse(x[j].contains(x[i].min()),
                                        String.format("x[%d] = {%d} but x[%d] contains the value (its domain is %s)", i, x[i].min(), j, x[j].toString()));
                            }
                        }
                    }
                }
                // assign the variable to a value
                int v = xs.min();
                return branch(() -> cp.post(equal(xs, v)),
                        () -> cp.post(notEqual(xs, v)));
            });

            SearchStatistics stats = search.solve();
            assertEquals(5040, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    @Grade(cpuTimeout = 20)
    public void allDifferentTest5() {
        Solver cp = makeSolver();
        int nVariables = 42; // number of variables for the test
        int domainSize = 2; // domain size for each variable
        IntVar[] x = new IntVar[nVariables];
        for (int split = 0 ; split < x.length ; split += domainSize)
            for (int i = split ; i < Math.min(split + domainSize, x.length) ; i++)
                x[i] = makeIntVar(cp, split, split + domainSize - 1);
        // x0, x1 = {0, 1}
        // x2, x3 = {2, 3}
        // x4, x5 = {4, 5}
        // ...
        try {
            // solve problem with forward checking
            cp.getStateManager().saveState();
            cp.post(new AllDifferentFWC(x));
            DFSearch search = makeDfs(cp, firstFail(x));
            long init = System.currentTimeMillis();
            SearchStatistics statsFWC = search.solve();
            long allDiffFWCElapsed = System.currentTimeMillis() - init;

            cp.getStateManager().restoreState();

            // solve problem with binary decomposition
            cp.post(new AllDifferentBinary(x));
            search = makeDfs(cp, firstFail(x));
            init = System.currentTimeMillis();
            SearchStatistics statsBinary = search.solve();
            long allDiffBinaryElapsed = System.currentTimeMillis() - init;
            //System.out.println("binary: " + (((double) (allDiffBinaryElapsed)) / 1000));
            //System.out.println("fwc   : " + (((double) (allDiffFWCElapsed)) / 1000));
            assertTrue(allDiffFWCElapsed * 1.4 <=  allDiffBinaryElapsed,
                    "All different with forward checking should be faster than using binary decomposition");
            assertEquals(statsFWC.numberOfSolutions(), statsBinary.numberOfSolutions());
            assertEquals(statsFWC.numberOfFailures(), statsBinary.numberOfFailures());
            assertEquals(statsFWC.numberOfNodes(), statsBinary.numberOfNodes());
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

}
