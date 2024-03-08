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
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import org.javagrader.Grade;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.SearchStatistics;
import minicp.util.Procedure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static minicp.cp.BranchingScheme.EMPTY;
import static minicp.cp.BranchingScheme.branch;
import static minicp.cp.Factory.*;
import static minicp.cp.Factory.notEqual;
import static org.junit.jupiter.api.Assertions.*;

@Grade(cpuTimeout = 1)
public class Element1DDCTest extends SolverTest {

    @ParameterizedTest
    @MethodSource("getSolver")
    public void element1dTest1(Solver cp) {
        try {

            Random rand = new Random(678);
            IntVar y = makeIntVar(cp, -1, 100);
            IntVar z = makeIntVar(cp, -50, 50);


            int[] T = new int[70];
            HashSet<Integer> uniqueValues = new HashSet<>(T.length);
            for (int i = 0; i < T.length; i++) {
                T[i] = rand.nextInt(100) - 50;
                uniqueValues.add(T[i]);
            }

            cp.post(new Element1DDomainConsistent(T, y, z));

            assertEquals(T.length, y.size());
            assertEquals(uniqueValues.size(), z.size());

            assertTrue(y.max() < T.length);

            Supplier<Procedure[]> branching = () -> {
                if (y.isFixed() && z.isFixed()) {
                    assertEquals(T[y.min()], z.min());
                    return EMPTY;
                }
                int[] possibleY = new int[y.size()];
                y.fillArray(possibleY);

                int[] possibleZ = new int[z.size()];
                z.fillArray(possibleZ);

                HashSet<Integer> possibleValues = new HashSet<>();
                HashSet<Integer> possibleValues2 = new HashSet<>();
                for (int j : possibleZ)
                    possibleValues.add(j);

                for (int j : possibleY) {
                    assertTrue(possibleValues.contains(T[j]));
                    possibleValues2.add(T[j]);
                }
                assertEquals(possibleValues.size(), possibleValues2.size());

                if (!y.isFixed() && (z.isFixed() || rand.nextBoolean())) {
                    //select a random y
                    int val = possibleY[rand.nextInt(possibleY.length)];
                    return branch(() -> cp.post(equal(y, val)),
                            () -> cp.post(notEqual(y, val)));
                } else {
                    int val = possibleZ[rand.nextInt(possibleZ.length)];
                    return branch(() -> cp.post(equal(z, val)),
                            () -> cp.post(notEqual(z, val)));
                }
            };

            DFSearch dfs = makeDfs(cp, branching);

            SearchStatistics stats = dfs.solve();
            assertTrue(stats.numberOfSolutions() >= uniqueValues.size());
            assertTrue(stats.numberOfSolutions() <= T.length);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void element1dTest2(Solver cp) {
        try {
            IntVar y = makeIntVar(cp, 0, 100);
            IntVar z = makeIntVar(cp, -50, 100);

            int[] T = new int[15];
            Arrays.fill(T, 5);
            T[T.length - 1] = 0;

            new Element1DDomainConsistent(T, y, z).post();

            assertEquals(y.min(), 0);
            assertEquals(y.size(), T.length);
            assertEquals(z.size(), 2);
            assertEquals(z.min(), 0);
            assertEquals(z.max(), 5);

            cp.getStateManager().saveState();
            y.remove(T.length - 1);
            z.remove(5);
            try {
                cp.fixPoint();
                fail("An inconsistency needs to be" +
                        "thrown when no solution where the constraint holds exists");
            } catch (InconsistencyException ignored) {

            }
            cp.getStateManager().restoreState();

            // indices is a permutation of {1, 2, ..., 13 }
            // the final index, 14, is not present.
            int[] indices = { 9, 1, 2, 4, 11, 6, 13, 0, 12, 10, 3, 7, 5, 8 };

            for (int i = 0; i < indices.length; i++) {
                assertEquals(y.size(), T.length - i);
                assertTrue(y.contains(indices[i]));
                y.remove(indices[i]);
                cp.fixPoint();
                assertEquals(z.min(), 0);
                if (i == indices.length - 1) {
                    assertEquals(z.size(), 1);
                } else {
                    assertEquals(z.max(), 5);
                    assertEquals(z.size(), 2);
                }
            }
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }
}
