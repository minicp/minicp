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

package minicp.examples;

import minicp.engine.core.IntVar;
import minicp.search.SearchStatistics;
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import org.javagrader.ConditionalOrderingExtension;
import org.javagrader.Grade;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Named.named;

@Grade(value = 2)
@ExtendWith(ConditionalOrderingExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JobShopTest {

    public static int assertValidSolutionFast(JobShop jobShop) {
        for (int i = 0 ; i < jobShop.start.length ; ++i) {
            for (int j = 0; j < jobShop.start[i].length ; ++j) {
                IntVar start = jobShop.start[i][j];
                IntVar end = jobShop.end[i][j];
                int duration = jobShop.instance.duration[i][j];
                assertEquals(start.min() + duration, end.min(), "Your end variable is not correctly computed");
                assertEquals(start.max() + duration, end.max(), "Your end variable is not correctly computed");
            }
        }
        // job precedence
        int maxEnd = Integer.MIN_VALUE;
        String msg = "The tasks within a job must be scheduled one after another";
        for (int j = 0 ; j < jobShop.instance.nJobs; ++j) {
            for (int m = 1 ; m < jobShop.instance.nMachines ; ++m) {
                assertTrue(jobShop.end[j][m-1].min() <= jobShop.start[j][m].min(), msg);
                assertTrue(jobShop.end[j][m-1].max() <= jobShop.start[j][m].max(), msg);
            }
            maxEnd = Math.max(maxEnd, jobShop.end[j][jobShop.instance.nMachines -1].max());
        }
        assertTrue(jobShop.makespan.isFixed(), "The makespan is not fixed");
        assertEquals(maxEnd, jobShop.makespan.min(), "The makespan is not correctly computed");
        return jobShop.makespan.min();
    }

    public static int assertValidSolution(JobShop jobShop) {
        int val = assertValidSolutionFast(jobShop);
        // tasks on a machine cannot overlap
        int[][] startMin = new int[jobShop.instance.nMachines][jobShop.instance.nJobs];
        int[][] endMin = new int[jobShop.instance.nMachines][jobShop.instance.nJobs];
        int[] idx = new int[jobShop.instance.nJobs];
        for (int i = 0; i < jobShop.instance.nJobs; i++) {
            for (int j = 0; j < jobShop.instance.nMachines; j++) {
                int m = jobShop.instance.machine[i][j];
                startMin[m][idx[m]] = jobShop.start[i][j].min();
                endMin[m][idx[m]++] = jobShop.end[i][j].min();
            }
        }
        for (int m = 0; m < jobShop.instance.nMachines; m++) {
            Arrays.sort(endMin[m]);
            Arrays.sort(startMin[m]);
            for (int i = 1; i < endMin.length; ++i) {
                if (endMin[m][i - 1] > startMin[m][i]) {
                    String message = String.format("Tasks cannot overlap but they did for machine %d\n" +
                                    "%s == end[%d] > start[%d] == %d" +
                                    "\nstart: %s\n  end: %s",
                            m, endMin[m][i - 1], i - 1, i, startMin[m][i],
                            Arrays.toString(startMin), Arrays.toString(endMin));
                    fail(message);
                }
            }
        }
        return val;
    }

    @Grade(value = 1, cpuTimeout=1500, unit = TimeUnit.MILLISECONDS)
    @ParameterizedTest
    @MethodSource("getTinyInstances")
    @Order(1)
    public void testModel(String instance, int objective) {
        try {
            JobShop jb = new JobShop(instance);
            jb.buildModel();
            AtomicBoolean seen = new AtomicBoolean(false);
            jb.dfs.onSolution(() -> {
                assertValidSolution(jb);
                seen.set(true);
            });
            SearchStatistics statistics = jb.solve(false, searchStatistics -> searchStatistics.numberOfSolutions() >= 1);
            assertTrue(statistics.numberOfSolutions() > 0);
            assertTrue(seen.get());
        } catch (InconsistencyException e) {
            fail("You have thrown an inconsistency although the instance given as input is valid");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Grade(value = 3, cpuTimeout = 4500, unit = TimeUnit.MILLISECONDS,threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    @ParameterizedTest
    @MethodSource("getSmallAndBigInstances")
    @Order(2)
    @Tag("slow")
    public void testFindOptimality(String instance, int objective) {
        try {
            JobShop jb = new JobShop(instance);
            jb.buildModel();
            AtomicInteger solution = new AtomicInteger(Integer.MAX_VALUE);
            jb.dfs.onSolution(() -> {
                int value = assertValidSolutionFast(jb);
                assertTrue(value < solution.get());
                solution.set(value);
            });
            // cutoff if best solution is encountered, to stop the search before proving that it is optimal
            // this is to save some time as this test is already long
            SearchStatistics statistics = jb.solve(false, searchStatistics -> solution.get() <= objective);
            assertTrue(statistics.numberOfSolutions() > 0);
            assertEquals(objective, solution.get(), "You did find the optimal solution");
        } catch (InconsistencyException e) {
            fail("You have thrown an inconsistency although the instance given as input is valid");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    public static Stream<Arguments> getTinyInstances() {
        return Arrays.stream((new Object[][]{
                {"data/jobshop/tiny/jobshop-2-2-0", 13},
                {"data/jobshop/tiny/jobshop-2-2-1", 18},
                {"data/jobshop/tiny/jobshop-2-2-2", 17},
                {"data/jobshop/tiny/jobshop-2-2-3", 23},
                {"data/jobshop/tiny/jobshop-2-2-4", 8},
                {"data/jobshop/tiny/jobshop-4-4-0", 67},
                {"data/jobshop/tiny/jobshop-4-4-1", 63},
                {"data/jobshop/tiny/jobshop-4-4-2", 63},
                {"data/jobshop/tiny/jobshop-4-4-3", 57},
                {"data/jobshop/tiny/jobshop-4-4-4", 49},
        })).map(data -> Arguments.arguments(named(new File((String) data[0]).getName(), data[0]), data[1]));
    }

    public static Stream<Arguments> getSmallAndBigInstances() {
        return Arrays.stream((new Object[][]{
                {"data/jobshop/small/jobshop-7-7-0", 99},
                {"data/jobshop/small/jobshop-7-7-1", 108},
                {"data/jobshop/small/jobshop-7-7-2", 96},
                {"data/jobshop/small/jobshop-7-7-3", 103},
                {"data/jobshop/small/jobshop-7-7-4", 101},
                {"data/jobshop/medium/jobshop-8-8-0", 117},
                {"data/jobshop/medium/jobshop-8-8-1", 114},
                {"data/jobshop/medium/jobshop-8-8-2", 118},
                {"data/jobshop/medium/jobshop-8-8-3", 120},
                {"data/jobshop/medium/jobshop-8-8-4", 115},
                {"data/jobshop/medium/jobshop-9-9-0", 140},
                {"data/jobshop/medium/jobshop-9-9-1", 135},
                {"data/jobshop/medium/jobshop-9-9-2", 134},
                {"data/jobshop/medium/jobshop-9-9-4", 130},
                {"data/jobshop/big/jobshop-15-5-0", 926},
                {"data/jobshop/uclouvain.txt", 808},
        })).map(data -> Arguments.arguments(named(new File((String) data[0]).getName(), data[0]), data[1]));
    }

}