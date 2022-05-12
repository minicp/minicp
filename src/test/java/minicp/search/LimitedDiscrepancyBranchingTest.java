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

import com.github.guillaumederval.javagrading.GradeClass;
import com.github.guillaumederval.javagrading.GradingRunner;
import minicp.cp.BranchingScheme;
import minicp.state.StateInt;
import minicp.state.StateManager;
import minicp.state.Trailer;
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.Procedure;
import minicp.util.exception.NotImplementedException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

@RunWith(GradingRunner.class)
@GradeClass(totalValue = 1, defaultCpuTimeout = 1000)
public class LimitedDiscrepancyBranchingTest {


    @Test
    public void testExample1() {
        try {
            StateManager sm = new Trailer();
            StateInt i = sm.makeStateInt(0);
            int[] values = new int[4];

            Supplier<Procedure[]> bs = () -> {
                if (i.value() >= values.length)
                    return BranchingScheme.EMPTY;
                else return BranchingScheme.branch(
                        () -> { // left branch
                            values[i.value()] = 0;
                            i.increment();
                        },
                        () -> { // right branch
                            values[i.value()] = 1;
                            i.increment();
                        });
            };

            LimitedDiscrepancyBranching bsDiscrepancy =
                    new LimitedDiscrepancyBranching(bs, 2);

            DFSearch dfs = new DFSearch(sm, bsDiscrepancy);

            dfs.onSolution(() -> {
                int n1 = 0;
                for (int k = 0; k < values.length; k++) {
                    n1 += values[k];
                }
                Assert.assertTrue(n1 <= 2);
            });

            SearchStatistics stats = dfs.solve();

            assertEquals(11, stats.numberOfSolutions());
            assertEquals(0, stats.numberOfFailures());
            assertEquals(24, stats.numberOfNodes()); // root node does not count
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


}
