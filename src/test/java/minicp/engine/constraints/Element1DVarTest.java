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
import minicp.state.StateManager;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import minicp.util.NotImplementedExceptionAssume;
import org.javagrader.Grade;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.HashSet;

import static minicp.cp.BranchingScheme.firstFail;
import static minicp.cp.Factory.makeDfs;
import static minicp.cp.Factory.makeIntVar;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@Grade(cpuTimeout = 1)
public class Element1DVarTest extends SolverTest {

    private static IntVar makeIVar(Solver cp, Integer... values) {
        return makeIntVar(cp, new HashSet<>(Arrays.asList(values)));
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void element1dVarTest1(Solver cp) {
        try {

            IntVar y = makeIntVar(cp, -3, 10);
            IntVar z = makeIntVar(cp, -20, 20);

            IntVar[] T = new IntVar[]{
                    makeIntVar(cp, 3, 3),
                    makeIntVar(cp, 2, 2),
                    makeIntVar(cp, 1, 1),
                    makeIntVar(cp, -1, -1),
                    makeIntVar(cp, 0, 0)};

            Element1DVar element1DVar = new Element1DVar(T, y, z);
            element1DVar.post();

            assertEquals(0, y.min());
            assertEquals(4, y.max());


            assertEquals(-1, z.min());
            assertEquals(3, z.max());

            z.removeAbove(1);
            element1DVar.propagate();

            assertEquals(2, y.min());


            y.remove(3);
            cp.fixPoint();

            assertEquals(1, z.max());
            assertEquals(0, z.min());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void element1dVarTest2(Solver cp) {
        try {

            IntVar y = makeIntVar(cp, -3, 10);
            IntVar z = makeIntVar(cp, -4, 40);

            IntVar[] T = new IntVar[]{makeIntVar(cp, 1, 2),
                    makeIntVar(cp, -3, -2),
                    makeIntVar(cp, -1, 0),
                    makeIntVar(cp, 1, 2),
                    makeIntVar(cp, 3, 4)};

            new Element1DVar(T, y, z).post();

            assertEquals(0, y.min());
            assertEquals(4, y.max());

            assertEquals(-3, z.min());
            assertEquals(4, z.max());

            y.removeAbove(2);
            cp.fixPoint();

            assertEquals(2, z.max());

            y.fix(2);
            cp.fixPoint();

            assertEquals(-1, z.min());
            assertEquals(0, z.max());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void element1dVarTest3(Solver cp) {
        try {

            IntVar y = makeIntVar(cp, -3, 10);
            IntVar z = makeIntVar(cp, -20, 40);

            IntVar[] T = new IntVar[]{
                    makeIntVar(cp, 9, 9),
                    makeIntVar(cp, 8, 8),
                    makeIntVar(cp, 7, 7),
                    makeIntVar(cp, 5, 5),
                    makeIntVar(cp, 6, 6)};

            cp.post(new Element1DVar(T, y, z));

            DFSearch dfs = makeDfs(cp, firstFail(y, z));
            dfs.onSolution(() ->
                    assertEquals(T[y.min()].min(), z.min(),
                            String.format("T[y] != z : T[%d] == %d, which is different from %d", y.min(), T[y.min()].min(), z.min()))
            );
            SearchStatistics stats = dfs.solve();

            assertEquals(5, stats.numberOfSolutions());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void element1dVarTest4(Solver cp) {

        try {
            IntVar x0 = makeIVar(cp, 0, 1, 5);
            IntVar x1 = makeIVar(cp, -5, -4, -3, -2, 0, 1, 5);
            IntVar x2 = makeIVar(cp, -2, 0);


            new Element1DVar(new IntVar[]{x0}, x1, x2).post();

            assertEquals(0, x0.min());
            assertEquals(0, x1.min());
            assertEquals(0, x2.min());
            assertEquals(0, x0.max());
            assertEquals(0, x1.max());
            assertEquals(0, x2.max());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void element1dVarTest5(Solver cp) {

        try {
            StateManager sm = cp.getStateManager();
            IntVar x0 = makeIVar(cp, 1, 5);
            IntVar x1 = makeIVar(cp, 0, 5);
            IntVar x2 = makeIVar(cp, 0, 5, 6);

            IntVar y = makeIVar(cp, -1, 0, 1, 2, 3);
            IntVar z = makeIVar(cp, -2, 0, 1, 5, 6, 9);

            new Element1DVar(new IntVar[]{x0, x1, x2}, y, z).post();

            assertEquals(1, x0.min());
            assertEquals(5, x0.max());
            assertEquals(0, x1.min());
            assertEquals(5, x1.max());
            assertEquals(0, x2.min());
            assertEquals(6, x2.max());

            assertEquals(0, y.min());
            assertEquals(2, y.max());
            assertEquals(0, z.min());
            assertEquals(6, z.max());

            sm.saveState();

            y.fix(0);
            cp.fixPoint();

            assertEquals(1, x0.min());
            assertEquals(5, x0.max());
            assertEquals(0, x1.min());
            assertEquals(5, x1.max());
            assertEquals(0, x2.min());
            assertEquals(6, x2.max());

            assertEquals(1, z.min());
            assertEquals(5, z.max());

            sm.restoreState();
            sm.saveState();

            z.remove(0);
            cp.fixPoint();

            assertEquals(1, x0.min());
            assertEquals(5, x0.max());
            assertEquals(0, x1.min());
            assertEquals(5, x1.max());
            assertEquals(0, x2.min());
            assertEquals(6, x2.max());

            assertEquals(0, y.min());
            assertEquals(2, y.max());
            assertEquals(1, z.min());
            assertEquals(6, z.max());

            z.remove(5);
            z.remove(6);
            x1.remove(0);
            x2.remove(0);
            cp.fixPoint();
            cp.fixPoint();

            assertEquals(1, x0.min());
            assertEquals(1, x0.max());
            assertEquals(5, x1.min());
            assertEquals(5, x1.max());
            assertEquals(5, x2.min());
            assertEquals(6, x2.max());

            assertEquals(0, y.min());
            assertEquals(0, y.max());
            assertEquals(1, z.min());
            assertEquals(1, z.max());

            sm.restoreState();

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void element1dVarTest6(Solver cp) {
        try {

            IntVar x0 = makeIVar(cp, 1, 2);
            IntVar x1 = makeIVar(cp, 0, 5);
            IntVar x2 = makeIVar(cp, 10, 11);

            IntVar y = makeIntVar(cp, 0, 2);
            IntVar z = makeIVar(cp,5, 6, 9);

            new Element1DVar(new IntVar[]{x0, x1, x2}, y, z).post();

            assertEquals(1, y.min());
            assertEquals(1, y.max());
            assertEquals(5, z.min());
            assertEquals(5, z.max());
            assertEquals(5, x1.min());
            assertEquals(5, x1.max());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

}
