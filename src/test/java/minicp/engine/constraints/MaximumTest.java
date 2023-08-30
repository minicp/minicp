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
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import minicp.util.NotImplementedExceptionAssume;
import org.javagrader.Allow;
import org.javagrader.Grade;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static minicp.cp.BranchingScheme.*;
import static minicp.cp.Factory.*;
import static org.junit.jupiter.api.Assertions.*;

@Grade(cpuTimeout = 1)
public class MaximumTest extends SolverTest {

    @ParameterizedTest
    @MethodSource("getSolver")
    public void maximumTest1(Solver cp) {
        try {

            IntVar[] x = makeIntVarArray(cp, 3, 10);
            IntVar y = makeIntVar(cp, -5, 20);
            cp.post(new Maximum(x, y));

            // x[i] = 0..9
            // y = 0..9
            assertEquals(9, y.max());
            assertEquals(0, y.min());

            y.removeAbove(8);
            cp.fixPoint();

            // x[i] = 0..8
            // y = 0..8
            assertEquals(8, x[0].max());
            assertEquals(8, x[1].max());
            assertEquals(8, x[2].max());

            y.removeBelow(5);
            x[0].removeAbove(2);
            x[1].removeBelow(6);
            x[2].removeBelow(6);
            cp.fixPoint();

            // x0 = 0..1
            // x1 = 6..8
            // x2 = 6..8
            // y = 6..8  (the maximum is either x1 or x2)

            assertEquals(8, y.max());
            assertEquals(6, y.min());

            y.removeBelow(7);
            x[1].removeAbove(6);
            // x0 = 0..2
            // x1 = 6
            // x2 = 6..8
            // y = 7..8
            cp.fixPoint(); // propagate the maximum constraint
            assertEquals(7, x[2].min(), "In some cases, y can also change the minimum value of one of the x's");


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void maximumTest2(Solver cp) {
        try {

            IntVar x1 = makeIntVar(cp, 0, 0);
            IntVar x2 = makeIntVar(cp, 1, 1);
            IntVar x3 = makeIntVar(cp, 2, 2);
            IntVar y = maximum(x1, x2, x3);


            assertEquals(2, y.max());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void maximumTest3(Solver cp) {
        try {

            IntVar x1 = makeIntVar(cp, 0, 10);
            IntVar x2 = makeIntVar(cp, 0, 10);
            IntVar x3 = makeIntVar(cp, -5, 50);
            IntVar y = maximum(x1, x2, x3);

            y.removeAbove(5);
            cp.fixPoint();

            assertEquals(5, x1.max());
            assertEquals(5, x2.max());
            assertEquals(5, x3.max());


        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void maximumTest4(Solver cp) {
        try {

            IntVar[] x = makeIntVarArray(cp, 4, 5);
            IntVar y = makeIntVar(cp, -5, 20);

            IntVar[] allIntVars = new IntVar[x.length+1];
            System.arraycopy(x, 0, allIntVars, 0, x.length);
            allIntVars[x.length] = y;

            DFSearch dfs = makeDfs(cp, firstFail(allIntVars));

            cp.post(new Maximum(x, y));
            // 5*5*5*5 // 625

            dfs.onSolution(() -> {
                int max = Arrays.stream(x).mapToInt(xi -> xi.max()).max().getAsInt();
                String variables = IntStream.range(0, 4).mapToObj(i -> String.format("x[%d] = %s", i, x[i])).collect(Collectors.joining(" ; "));
                assertEquals(y.min(), max, String.format("The variables are \n%s\nbut you set y to %s", variables, y));
                assertEquals(y.max(), max, String.format("The variables are \n%s\nbut you set y to %s", variables, y));
                assertTrue(y.isFixed(), "If all the x[i]'s are fixed, y should be fixed as well");
            });

            SearchStatistics stats = dfs.solve();

            assertEquals(625, stats.numberOfSolutions());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void maximumTest5(Solver cp) {
        try {

            IntVar[] x = makeIntVarArray(cp, 3, 10);
            IntVar y = makeIntVar(cp, -5, 20);
            cp.post(new Maximum(x, y));

            assertEquals(9, y.max());
            assertEquals(0, y.min());

            x[0].removeAbove(3);
            y.removeBelow(6);
            cp.fixPoint();

            assertEquals(6, y.min());
            assertEquals(9, y.max());
            assertEquals(0, x[1].min());
            assertEquals(0, x[2].min());

        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void maximumTest6(Solver cp) {
        try {

            IntVar[] x = makeIntVarArray(cp, 3, 15);
            IntVar y = makeIntVar(cp, 4, 10);

            cp.post(new Maximum(x, y));
            for (IntVar xi: x) {
                assertEquals(10, xi.max(), "x[i].max() cannot exceed y.max()");
            }
            x[0].removeAbove(3);
            x[2].removeAbove(3);
            // x0 = 0..3
            // x1 = 0..10
            // x2 = 0..3
            // y = 4..10
            // only x1 can be a valid candidate for the maximum value => x[1] = 4..10
            cp.fixPoint();
            assertEquals(4, x[1].min());
            assertEquals(10, x[1].max());
            assertEquals(4, y.min());
            assertEquals(10, y.max());

            x[1].removeBelow(5);
            cp.fixPoint();
            assertEquals(5, x[1].min());
            assertEquals(10, x[1].max());
            assertEquals(5, y.min());
            assertEquals(10, y.max());

            x[1].removeAbove(9);
            cp.fixPoint();
            assertEquals(5, x[1].min());
            assertEquals(9, x[1].max());
            assertEquals(5, y.min());
            assertEquals(9, y.max());

            y.removeBelow(6);
            cp.fixPoint();
            assertEquals(6, x[1].min());
            assertEquals(9, x[1].max());
            assertEquals(6, y.min());
            assertEquals(9, y.max());

            y.removeAbove(8);
            cp.fixPoint();
            assertEquals(6, x[1].min());
            assertEquals(8, x[1].max());
            assertEquals(6, y.min());
            assertEquals(8, y.max());
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void maximumTest7(Solver cp) {
        try {

            IntVar[] x = makeIntVarArray(cp, 4, 6);
            IntVar y = makeIntVar(cp, 0, 6);

            IntVar[] allIntVars = new IntVar[x.length+1];
            System.arraycopy(x, 0, allIntVars, 0, x.length);
            allIntVars[x.length] = y;

            Random random = new Random(42);
            DFSearch dfs = makeDfs(cp, () -> {
                // choose a variable randomly
                IntVar xs = selectMin(x, xi -> !xi.isFixed(), xi -> random.nextInt(100));
                if (xs == null)
                    return EMPTY;
                // verify that the constraint is respected (note that this condition is not enough, more checks should actually be done!)
                int max = y.max();
                for (IntVar xi: x) {
                    assertTrue(xi.max() <= max, "No x[i] can be larger than y");
                }

                // choose a point in the middle of the domain
                int v = (xs.max() - xs.min()) / 2 + xs.min();
                return branch(
                        () -> cp.post(largerOrEqual(xs, v+1)),
                        () -> cp.post(lessOrEqual(xs, v))
                );
            });

            cp.post(new Maximum(x, y));
            // 6*6*6*6 // 1296

            dfs.onSolution(() -> {
                int max = Arrays.stream(x).mapToInt(xi -> xi.max()).max().getAsInt();
                assertTrue(y.isFixed(), "If all the x[i]'s are fixed, y should be fixed as well");
                String variables = IntStream.range(0, 4).mapToObj(i -> String.format("x[%d] = %s", i, x[i])).collect(Collectors.joining(" ; "));
                assertEquals(y.min(), max, String.format("The variables are \n%s\nbut you set y to %s", variables, y));
                assertEquals(y.max(), max, String.format("The variables are \n%s\nbut you set y to %s", variables, y));
            });

            SearchStatistics stats = dfs.solve();
            assertEquals(1296, stats.numberOfSolutions());
            assertEquals(0, stats.numberOfFailures());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void maximumTest8(Solver cp) {
        try {

            IntVar[] x = new IntVar[] {
                    makeIntVar(cp, 0, 5),
                    makeIntVar(cp, -1, 4),
                    makeIntVar(cp, 2, 7),
                    makeIntVar(cp, 3, 8),
            };
            IntVar y = makeIntVar(cp, -10, 13);

            cp.post(new Maximum(x, y));
            assertEquals(8, y.max());
            assertEquals(3, y.min(), "What happens if all x's are fixed to their min value?");

            DFSearch dfs = makeDfs(cp, firstFail(x));
            dfs.onSolution(() -> {
                int max = Arrays.stream(x).mapToInt(xi -> xi.max()).max().getAsInt();
                String variables = IntStream.range(0, 4).mapToObj(i -> String.format("x[%d] = %s", i, x[i])).collect(Collectors.joining(" ; "));
                assertEquals(y.min(), max, String.format("The variables are \n%s\nbut you set y to %s", variables, y));
                assertEquals(y.max(), max, String.format("The variables are \n%s\nbut you set y to %s", variables, y));
                assertTrue(y.isFixed(), "If all the x[i]'s are fixed, y should be fixed as well");
            });

            SearchStatistics stats = dfs.solve();
            assertTrue(stats.numberOfSolutions() > 0);
            assertEquals(0, stats.numberOfFailures());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void maximumTest9(Solver cp) {
        try {

            IntVar[] x = makeIntVarArray(cp, 4, 8);
            IntVar y = makeIntVar(cp, 0, 8);
            x[0].remove(0);
            x[1].remove(1);
            x[2].remove(2);
            x[3].remove(3);
            x[0].remove(4);
            x[1].remove(5);
            x[2].remove(6);
            x[3].remove(7);

            DFSearch dfs = makeDfs(cp, () -> {
                // choose the variable with the smallest domain size
                IntVar xs = selectMin(x, xi -> !xi.isFixed(), IntVar::size);
                if (xs == null)
                    return EMPTY; // solution found
                // verify that the constraint is respected (note that this condition is not enough, more checks should actually be done!)
                int max = y.max();
                for (IntVar xi: x) {
                    assertTrue(xi.max() <= max, "No x[i] can be larger than y");
                }
                // choose the largest value within the domain
                int v = xs.max();
                return branch(
                        () -> cp.post(equal(xs, v)),
                        () -> cp.post(notEqual(xs, v))
                );
            });

            cp.post(new Maximum(x, y));
            // 6*6*6*6 // 1296

            dfs.onSolution(() -> {
                int max = Arrays.stream(x).mapToInt(xi -> xi.max()).max().getAsInt();
                assertTrue(y.isFixed(), "If all the x[i]'s are fixed, y should be fixed as well");
                String variables = IntStream.range(0, 4).mapToObj(i -> String.format("x[%d] = %s", i, x[i])).collect(Collectors.joining(" ; "));
                assertEquals(y.min(), max, String.format("The variables are \n%s\nbut you set y to %s", variables, y));
                assertEquals(y.max(), max, String.format("The variables are \n%s\nbut you set y to %s", variables, y));
            });

            SearchStatistics stats = dfs.solve();
            assertEquals(1296, stats.numberOfSolutions());
            assertEquals(0, stats.numberOfFailures());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void maximumTest10(Solver cp) {
        try {

            IntVar[] x = makeIntVarArray(cp, 4, 8);
            IntVar y = makeIntVar(cp, 0, 8);
            x[0].remove(0);
            x[1].remove(1);
            x[2].remove(2);
            x[3].remove(3);
            x[0].remove(4);
            x[1].remove(5);
            x[2].remove(6);
            x[3].remove(7);

            DFSearch dfs = makeDfs(cp, () -> {
                // choose the variable with the largest domain size
                IntVar xs = selectMin(x, xi -> !xi.isFixed(), xi -> -xi.size());
                if (xs == null)
                    return EMPTY; // solution found
                // verify that the constraint is respected (note that this condition is not enough, more checks should actually be done!)
                int max = y.max();
                for (IntVar xi: x) {
                    assertTrue(xi.max() <= max, "No x[i] can be larger than y");
                }
                // choose the tiniest value within the domain
                int v = xs.min();
                return branch(
                        () -> cp.post(equal(xs, v)),
                        () -> cp.post(notEqual(xs, v))
                );
            });

            cp.post(new Maximum(x, y));
            // 6*6*6*6 // 1296

            dfs.onSolution(() -> {
                int max = Arrays.stream(x).mapToInt(xi -> xi.max()).max().getAsInt();
                assertTrue(y.isFixed(), "If all the x[i]'s are fixed, y should be fixed as well");
                String variables = IntStream.range(0, 4).mapToObj(i -> String.format("x[%d] = %s", i, x[i])).collect(Collectors.joining(" ; "));
                assertEquals(y.min(), max, String.format("The variables are \n%s\nbut you set y to %s", variables, y));
                assertEquals(y.max(), max, String.format("The variables are \n%s\nbut you set y to %s", variables, y));
            });

            SearchStatistics stats = dfs.solve();
            assertEquals(1296, stats.numberOfSolutions());
            assertEquals(0, stats.numberOfFailures());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    @Allow("java.lang.Thread")
    public void maximumTest11(Solver cp) {
        try {

            IntVar[] x = makeIntVarArray(cp, 3, 28);
            IntVar y = makeIntVar(cp, 0, 28);

            DFSearch dfs = makeDfs(cp, firstFail(x));

            cp.post(new Maximum(x, y));

            dfs.onSolution(() -> {
                int max = Arrays.stream(x).mapToInt(xi -> xi.max()).max().getAsInt();
                assertTrue(y.isFixed(), "If all the x[i]'s are fixed, y should be fixed as well");
                String variables = IntStream.range(0, x.length).mapToObj(i -> String.format("x[%d] = %s", i, x[i])).collect(Collectors.joining(" ; "));
                assertEquals(y.min(), max, String.format("The variables are \n%s\nbut you set y to %s", variables, y));
                assertEquals(y.max(), max, String.format("The variables are \n%s\nbut you set y to %s", variables, y));
            });

            AtomicReference<SearchStatistics> stats = new AtomicReference<>();
            assertTimeoutPreemptively(Duration.ofSeconds(1), () -> stats.set(dfs.solve()));
            assertEquals(0, stats.get().numberOfFailures());
            assertEquals(21_952, stats.get().numberOfSolutions());

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testInvalidMaximum1(Solver cp) {
        try {

            IntVar[] x = new IntVar[] {
                    makeIntVar(cp, 0, 5),
                    makeIntVar(cp, -1, 4),
                    makeIntVar(cp, 2, 7),
                    makeIntVar(cp, 3, 8),
            };
            IntVar y = makeIntVar(cp, -10, -2);
            try {
                cp.post(new Maximum(x, y));
                fail();
            } catch (InconsistencyException e) {

            }
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testInvalidMaximum2(Solver cp) {
        try {
            IntVar[] x = new IntVar[] {
                    makeIntVar(cp, 0, 5),
                    makeIntVar(cp, -1, 4),
                    makeIntVar(cp, 2, 7),
                    makeIntVar(cp, 3, 8),
            };
            IntVar y = makeIntVar(cp, 10, 20);
            try {
                cp.post(new Maximum(x, y));
                fail();
            } catch (InconsistencyException e) {

            }
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

}
