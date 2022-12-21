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

package minicp.search;

import minicp.cp.BranchingScheme;
import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.Constraint;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.Procedure;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import org.javagrader.Grade;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static minicp.cp.Factory.*;
import static minicp.util.exception.InconsistencyException.INCONSISTENCY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Grade(cpuTimeout = 1)
public class LastConflictSearchTest {

    /**
     * Example given on the slide of the course
     * Nodes 3, 5 and 6 results in a failure
     * On node 8, x should be chosen instead of y as it resulted in a conflict
     *
     *                        +------------1------------+
     *                    y=0 |                         | y!=0
     *                  +----2----+                +----7
     *              x=0 |         | x!=0       x=0 |
     *                  3    +----4----+           8
     *                   x=1 |         | x!=1
     *                       5         6
     *
     */
    @Test
    public void testSlideExample() {
        try {
            Solver cp = makeSolver();
            IntVar x = makeIntVar(cp, 5); // x: {0..4}
            IntVar y = makeIntVar(cp, 5); // y: {0..4}
            IntVar[] variables = new IntVar[] {y, x};

            Constraint c = new AbstractConstraint(cp) {

                final Set<Integer> throwInconsistency = Arrays.stream(new int[] {3, 5, 6}).boxed().collect(Collectors.toSet());
                final AtomicInteger node = new AtomicInteger(1);

                @Override
                public void post() {
                    x.propagateOnDomainChange(this);
                    y.propagateOnDomainChange(this);
                }

                @Override
                public void propagate() {
                    int n = node.incrementAndGet();
                    if (throwInconsistency.contains(n)) {
                        throw INCONSISTENCY;
                    }
                    if (n == 8) {
                        assertTrue(x.isFixed());
                        assertEquals(0, x.min());
                    }
                }
            };
            cp.post(c);

            DFSearch dfs = new DFSearch(cp.getStateManager(), BranchingScheme.lastConflict(
                    () -> { //select first unfixed variable
                        for(IntVar z: variables)
                            if(!z.isFixed())
                                return z;
                        return null;
                    },
                    IntVar::min //select smallest value
            ));
            dfs.solve(searchStatistics -> searchStatistics.numberOfNodes() >= 7);
        }
        catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    public void testExample1() {
        try {
            Solver cp = makeSolver();
            IntVar[] x = makeIntVarArray(cp, 8, 8);
            for(int i = 4; i < 8; i++)
                x[i].removeAbove(2);

            // apply alldifferent on the four last variables.
            // of course, this cannot work!
            IntVar[] fourLast = Arrays.stream(x).skip(4).toArray(IntVar[]::new);
            cp.post(allDifferent(fourLast));

            DFSearch dfs = new DFSearch(cp.getStateManager(), BranchingScheme.lastConflict(
                    () -> { //select first unfixed variable in x
                        for(IntVar z: x)
                            if(!z.isFixed())
                                return z;
                        return null;
                    },
                    IntVar::min //select smallest value
            ));

            SearchStatistics stats = dfs.solve();
            assertEquals(0, stats.numberOfSolutions());
            assertEquals(70, stats.numberOfFailures());
            assertEquals(138, stats.numberOfNodes());
        }
        catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


}
