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
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.NotImplementedException;
import org.javagrader.Grade;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static minicp.cp.Factory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Grade(cpuTimeout = 1)
public class LastConflictSearchTest {

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
                    () -> { //select first unbound variable in x
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
