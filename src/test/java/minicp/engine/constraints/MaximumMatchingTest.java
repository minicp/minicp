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

import com.github.guillaumederval.javagrading.GradeClass;
import minicp.engine.SolverTest;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static minicp.cp.Factory.makeIntVar;
import static org.junit.Assert.*;

@GradeClass(totalValue = 1, defaultCpuTimeout = 1000)
public class MaximumMatchingTest extends SolverTest {

    private static IntVar makeIVar(Solver cp, Integer... values) {
        return makeIntVar(cp, new HashSet<>(Arrays.asList(values)));
    }

    private void check(IntVar[] x, int[] matching, int size, int expectedSize) {
        Set<Integer> values = new HashSet<>();
        for (int i = 0; i < x.length; i++) {
            if (matching[i] != MaximumMatching.NONE) {
                assertTrue(x[i].contains(matching[i]));
                values.add(matching[i]);
            }

        }
        assertEquals(size, values.size());
        assertEquals(expectedSize, size);
    }

    @Test
    public void test1() {
        try {
            Solver cp = solverFactory.get();
            IntVar[] x = new IntVar[]{
                    makeIVar(cp, 1, 2),
                    makeIVar(cp, 1, 2),
                    makeIVar(cp, 1, 2, 3, 4)};
            int[] matching = new int[x.length];
            MaximumMatching maximumMatching = new MaximumMatching(x);


            check(x, matching, maximumMatching.compute(matching), 3);


            x[2].remove(3);
            check(x, matching, maximumMatching.compute(matching), 3);


            x[2].remove(4);
            check(x, matching, maximumMatching.compute(matching), 2);


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }


    @Test
    public void test2() {
        try {
            Solver cp = solverFactory.get();
            IntVar[] x = new IntVar[]{
                    makeIVar(cp, 1, 4, 5),
                    makeIVar(cp, 9, 10), // will be 10
                    makeIVar(cp, 1, 4, 5, 8, 9), // will be 8 or 9
                    makeIVar(cp, 1, 4, 5), //
                    makeIVar(cp, 1, 4, 5, 8, 9), // will be 8 or 9
                    makeIVar(cp, 1, 4, 5)
            };
            MaximumMatching maximumMatching = new MaximumMatching(x);
            int[] matching = new int[x.length];

            check(x, matching, maximumMatching.compute(matching), 6);

            x[5].remove(5);

            check(x, matching, maximumMatching.compute(matching), 6);

            x[0].remove(5);
            x[3].remove(5);

            check(x, matching, maximumMatching.compute(matching), 5);


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }

    }


}
