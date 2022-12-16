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
import minicp.util.exception.NotImplementedException;
import org.javagrader.Grade;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.SearchStatistics;
import minicp.util.Procedure;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.Random;
import java.util.function.Supplier;

import static minicp.cp.BranchingScheme.EMPTY;
import static minicp.cp.BranchingScheme.branch;
import static minicp.cp.Factory.*;
import static minicp.cp.Factory.notEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Grade(cpuTimeout = 1)
public class Element1DDCTest extends SolverTest {

    @ParameterizedTest
    @MethodSource("getSolver")
    public void element1dTest1(Solver cp) {
        try {

            Random rand = new Random(678);
            IntVar y = makeIntVar(cp, 0, 100);
            IntVar z = makeIntVar(cp, 0, 100);


            int[] T = new int[70];
            HashSet<Integer> uniqueValues = new HashSet<>(T.length);
            for (int i = 0; i < T.length; i++) {
                T[i] = rand.nextInt(100);
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
}
