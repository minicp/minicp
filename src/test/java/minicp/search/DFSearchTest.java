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

import com.github.guillaumederval.javagrading.Grade;
import com.github.guillaumederval.javagrading.GradeClass;
import minicp.state.StateInt;
import minicp.state.StateManager;
import minicp.state.StateManagerTest;
import minicp.state.Trailer;
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.Procedure;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static minicp.cp.BranchingScheme.EMPTY;
import static minicp.cp.BranchingScheme.branch;
import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;


@GradeClass(totalValue=1, allCorrect=true)
public class DFSearchTest extends StateManagerTest {
    @Test
    public void testExample0() {
        final int save = -1;
        final int restore = -2;

        class TempStateManager extends Trailer {
            final List<Integer> operations = new LinkedList<>();
            private int lastSaveLevel = -1;
            private int startLevel = -1;

            public void performOp(int character) {
                if (startLevel < 0) {
                    startLevel = getLevel();
                    if (lastSaveLevel == startLevel) {
                        this.operations.add(save);
                    }
                }
                this.operations.add(character);

            }
            @Override
            public void saveState() {
                super.saveState();
                if (startLevel >= 0) {
                    this.operations.add(save);
                } else {
                    lastSaveLevel = getLevel();
                }
            }
            @Override
            public void restoreState() {
                if (startLevel >= 0 && getLevel() >= startLevel) this.operations.add(restore);
                super.restoreState();
            }
        }
        TempStateManager sm = new TempStateManager();
        StateInt alternative = sm.makeStateInt(0);

        Supplier<Procedure[]> supplier = () -> {
            if (alternative.value() == 0) {
                return branch(
                        () -> sm.performOp(alternative.setValue((int) 'A')),
                        () -> sm.performOp(alternative.setValue((int) 'B')),
                        () -> sm.performOp(alternative.setValue((int) 'C'))
                );
            } else if (alternative.value() == (int) 'A') {
                return branch(
                        () -> sm.performOp(alternative.setValue((int) 'D')),
                        () -> sm.performOp(alternative.setValue((int) 'E'))
                );
            } else if (alternative.value() == (int) 'C') {
                return branch(
                        () -> sm.performOp(alternative.setValue((int) 'F')),
                        () -> sm.performOp(alternative.setValue((int) 'G'))
                );
            }
            return EMPTY;
        };

        DFSearch dfs = new DFSearch(sm, supplier);

        SearchStatistics stats = dfs.solve();
        assertEquals(7, stats.numberOfNodes());
        assertEquals(5, stats.numberOfSolutions());
        assertEquals(0, stats.numberOfFailures());

        int[] actuals = sm.operations.stream().mapToInt(i -> i).toArray();
        if (actuals.length == 15) {
            assertArrayEquals(
                    new int[] {
                            save, 'A', save, 'D', restore,
                            'E', restore, save, 'B', restore,
                            'C', save, 'F', restore, 'G'
                    },
                    actuals
            );
        } else {
            assertEquals(21, actuals.length);
            assertArrayEquals(
                    new int[] {
                            save, 'A', save, 'D', restore,
                            save, 'E', restore, restore, save,
                            'B', restore, save, 'C', save,
                            'F', restore, save, 'G', restore,
                            restore
                    },
                    actuals
            );
        }
    }

    @Test
    public void testExample1() {
        StateManager sm = stateFactory.get();
        StateInt i = sm.makeStateInt(0);
        int[] values = new int[3];

        DFSearch dfs = new DFSearch(sm, () -> {
            if (i.value() >= values.length)
                return EMPTY;
            else {
                int val = i.value();
                return branch(
                        () -> { // left branch
                            values[val] = 0;
                            i.setValue(val + 1);
                        },
                        () -> { // right branch
                            values[val] = 1;
                            i.setValue(val + 1);
                        }
                );
            }
        });

        SearchStatistics stats = dfs.solve();

        assertEquals(8, stats.numberOfSolutions());
        assertEquals (0, stats.numberOfFailures());
        assertEquals (8 + 4 + 2, stats.numberOfNodes());
    }


    @Test
    public void testExample3() {
        StateManager sm = stateFactory.get();
        StateInt i = sm.makeStateInt(0);
        int[] values = new int[3];

        DFSearch dfs = new DFSearch(sm, () -> {
            if (i.value() >= values.length)
                return EMPTY;
            else {
                int val = i.value();
                return branch(
                        () -> { // left branch
                            values[i.value()] = 1;
                            i.setValue(val + 1);
                        },
                        () -> { // right branch
                            values[i.value()] = 0;
                            i.setValue(val + 1);
                        }
                );
            }
        });

        SearchStatistics stats = dfs.solve(stat -> stat.numberOfSolutions() >= 1);

        assertEquals (1,stats.numberOfSolutions());
    }


    @Test
    public void testDFS() {
        StateManager sm = stateFactory.get();
        StateInt i = sm.makeStateInt(0);
        boolean[] values = new boolean[4];

        AtomicInteger nSols = new AtomicInteger(0);


        DFSearch dfs = new DFSearch(sm, () -> {
            if (i.value() >= values.length)
                return EMPTY;
            else {
                int val = i.value();
                return branch(
                        () -> {
                            // left branch
                            values[i.value()] = false;
                            i.setValue(val + 1);
                        },
                        () -> {
                            // right branch
                            values[i.value()] = true;
                            i.setValue(val + 1);
                        }
                );
            }
        });

        dfs.onSolution(() -> {
            nSols.incrementAndGet();
        });


        SearchStatistics stats = dfs.solve();


        assertEquals(16, nSols.get());
        assertEquals(16, stats.numberOfSolutions());
        assertEquals(0, stats.numberOfFailures());
        assertEquals((16 + 8 + 4 + 2), stats.numberOfNodes());

    }

    @Test
    public void testDFSSearchLimit() {
        StateManager sm = stateFactory.get();

        StateInt i = sm.makeStateInt(0);
        boolean[] values = new boolean[4];

        DFSearch dfs = new DFSearch(sm, () -> {
            if (i.value() >= values.length) {
                return branch(() -> {
                    throw new InconsistencyException();
                });
            } else return branch(
                    () -> {
                        // left branch
                        values[i.value()] = false;
                        i.increment();
                    },
                    () -> {
                        // right branch
                        values[i.value()] = true;
                        i.increment();
                    }
            );
        });

        // stop search after 2 solutions
        SearchStatistics stats = dfs.solve(stat -> stat.numberOfFailures() >= 3);

        assertEquals (0,stats.numberOfSolutions());
        assertEquals (3,stats.numberOfFailures());

    }


    @Test
    @Grade(value = 0.5, cpuTimeout = 2000)
    public void testDeepDFS() {
        testExample1();
        testDFS();
        testDFSSearchLimit();

        StateManager sm = stateFactory.get();
        StateInt i = sm.makeStateInt(0);
        boolean[] values = new boolean[10000];

        DFSearch dfs = new DFSearch(sm, () -> {
            if (i.value() >= values.length) {
                return EMPTY;
            } else return branch(
                    () -> {
                        // left branch
                        values[i.value()] = false;
                        i.increment();
                    },
                    () -> {
                        // right branch
                        values[i.value()] = true;
                        i.increment();
                    }
            );
        });
        try {
            // stop search after 1 solutions (only left most branch)
            SearchStatistics stats = dfs.solve(stat -> stat.numberOfSolutions() >= 1);
            assertEquals (1,stats.numberOfSolutions());
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Test
    @Grade(value = 0.5, cpuTimeout = 2000)
    public void checkInconsistenciesManagedCorrectly() {
        StateManager sm = stateFactory.get();
        int[] values = new int[3]; //init to 0

        DFSearch dfs = new DFSearch(sm, () -> {
            if(values[0] >= 100)
                return EMPTY;

            return branch(
                    () -> {
                        values[0] += 1;
                        if(values[0] == 1)
                            throw new InconsistencyException();
                        //this should never happen in a left branch!
                        assertNotEquals(2, values[0]);
                    },
                    () -> {
                        values[0] += 1;
                    });
        });

        dfs.solve();
    }
}
