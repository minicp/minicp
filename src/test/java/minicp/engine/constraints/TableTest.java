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
import minicp.util.exception.NotImplementedException;
import minicp.util.NotImplementedExceptionAssume;
import org.javagrader.Grade;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import static minicp.cp.BranchingScheme.firstFail;
import static minicp.cp.Factory.*;
import static org.junit.jupiter.api.Assertions.*;

@Grade(cpuTimeout = 1)
public class TableTest extends SolverTest {

    private int[][] randomTuples(Random rand, int arity, int nTuples, int minvalue, int maxvalue) {
        int[][] r = new int[nTuples][arity];
        for (int i = 0; i < nTuples; i++)
            for (int j = 0; j < arity; j++)
                r[i][j] = rand.nextInt(maxvalue - minvalue) + minvalue;
        return r;
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void simpleTest0(Solver cp) {
        try {
            IntVar[] x = makeIntVarArray(cp, 2, 1);
            int[][] table = new int[][]{{0, 0}};
            cp.post(new TableCT(x, table));

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @ParameterizedTest
    @MethodSource("getSolver")
    public void simpleTest1(Solver cp) {
        try {
            IntVar[] x = makeIntVarArray(cp, 3, 12);
            int[][] table = new int[][]{{0, 0, 2},
                    {3, 5, 7},
                    {6, 9, 10},
                    {1, 2, 3}};
            cp.post(new TableCT(x, table));

            assertEquals(4, x[0].size());
            assertEquals(4, x[1].size());
            assertEquals(4, x[2].size());

            assertEquals(0, x[0].min());
            assertEquals(6, x[0].max());
            assertEquals(0, x[1].min());
            assertEquals(9, x[1].max());
            assertEquals(2, x[2].min());
            assertEquals(10, x[2].max());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void randomTest(Solver cp) {
        Random rand = new Random(67292);

        for (int i = 0; i < 100; i++) {
            int[][] tuples1 = randomTuples(rand, 3, 50, 2, 8);
            int[][] tuples2 = randomTuples(rand, 3, 50, 1, 7);
            int[][] tuples3 = randomTuples(rand, 3, 50, 0, 6);

            try {
                compareDecompWithTableCT(cp, tuples1, tuples2, tuples3);
            } catch (NotImplementedException e) {
                NotImplementedExceptionAssume.fail(e);
            }
        }
    }


    public void compareDecompWithTableCT(Solver cp, int[][] t1, int[][] t2, int[][] t3) {

        AtomicReference<SearchStatistics> statsDecomp = new AtomicReference<>(null);
        AtomicReference<SearchStatistics> statsAlgo = new AtomicReference<>(null);

        cp.getStateManager().withNewState(() -> {
            try {
                IntVar[] x = makeIntVarArray(cp, 5, 9);
                cp.post(allDifferent(x));
                cp.post(new TableDecomp(new IntVar[]{x[0], x[1], x[2]}, t1));
                cp.post(new TableDecomp(new IntVar[]{x[2], x[3], x[4]}, t2));
                cp.post(new TableDecomp(new IntVar[]{x[0], x[2], x[4]}, t3));
                statsDecomp.set(makeDfs(cp, firstFail(x)).solve());
            } catch (InconsistencyException ignored) {

            }
        });

        cp.getStateManager().withNewState(() -> {
            try {
                IntVar[] x = makeIntVarArray(cp, 5, 9);
                cp.post(allDifferent(x));
                cp.post(new TableCT(new IntVar[]{x[0], x[1], x[2]}, t1));
                cp.post(new TableCT(new IntVar[]{x[2], x[3], x[4]}, t2));
                cp.post(new TableCT(new IntVar[]{x[0], x[2], x[4]}, t3));
                statsAlgo.set(makeDfs(cp, firstFail(x)).solve());
            } catch (InconsistencyException ignored) {

            }
        });

        assertTrue((statsDecomp.get() == null && statsAlgo.get() == null) || (statsDecomp.get() != null && statsAlgo.get() != null));
        if (statsDecomp.get() != null) {
            assertEquals(statsDecomp.get().numberOfSolutions(), statsAlgo.get().numberOfSolutions());
            assertEquals(statsDecomp.get().numberOfFailures(), statsAlgo.get().numberOfFailures());
            assertEquals(statsDecomp.get().numberOfNodes(), statsAlgo.get().numberOfNodes());
        }
    }
}
