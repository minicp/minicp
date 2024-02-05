package minicp.engine.constraints;

import minicp.engine.SolverTest;
import minicp.engine.core.Constraint;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.SearchStatistics;
import minicp.state.StateSparseBitSet;
import minicp.state.StateSparseBitSet.SupportBitSet;
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import org.javagrader.Grade;
import org.javagrader.GradeFeedback;
import org.javagrader.TestResultStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.HashSet;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static minicp.cp.BranchingScheme.firstFail;
import static minicp.cp.Factory.*;
import static minicp.cp.Factory.makeDfs;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Grade(cpuTimeout = 1)
public class TableCTTest extends SolverTest {

    private int[][] randomTuples(Random rand, int arity, int nTuples, int minvalue, int maxvalue) {
        int[][] r = new int[nTuples][arity];
        for (int i = 0; i < nTuples; i++)
            for (int j = 0; j < arity; j++)
                r[i][j] = rand.nextInt(maxvalue - minvalue) + minvalue;
        return r;
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testInitSupports1(Solver cp) {
        try {
            IntVar[] x = makeIntVarArray(cp, 3, 12);
            int[][] table = new int[][]{
                    {0, 0, 2},
                    {3, 5, 7},
                    {6, 9, 10},
                    {1, 2, 3},
                    {0, 0, 3},
            };
            TableCT tableCT = new TableCT(x, table);
            SupportBitSet[][] supports = tableCT.supports;
            //supports[i][v] is the set of tuples supported by x[i]=v
            HashSet<Integer> valid = new HashSet<>();
            for (int i = 0 ; i < 3 ; ++i) {
                valid.clear(); // values that are valid for x[i]
                for (int[] ints : table) {
                    valid.add(ints[i]);
                }
                for (int v = 0 ; v < 12 ; ++v) {
                    if (valid.contains(v)) {
                        // the value is contained within some row of the table
                        for (int row = 0 ; row < table.length ; ++row) {
                            if (table[row][i] == v) {
                                assertTrue(supports[i][v].get(row),
                                        String.format("The value %d is valid for x[%d] at row %d but you did not set support[%d][%d] at bit %d",
                                                v, i, row, i, v, row));
                            } else {
                                assertFalse(supports[i][v].get(row),
                                        String.format("The value %d is not valid for x[%d] at row %d but you set support[%d][%d] at bit %d",
                                                v, i, row, i, v, row));
                            }
                        }
                        for (int bit = table.length ; bit < 64 ; ++bit) {
                            assertFalse(supports[i][v].get(bit),
                                    String.format("The row %d exceeds the table length, support[%d][%d] should not be set at bit %d",
                                            bit, i, v, bit));
                        }
                    } else {
                        for (int bit = 0 ; bit < 64 ; ++bit) {
                            assertFalse(supports[i][v].get(bit),
                                    String.format("The value %d does not appear in the table for x[%d] but you set support[%d][%d] at bit %d",
                                            v, i, i, v, bit));
                        }
                    }
                }
            }
        } catch (InconsistencyException e) {
            fail("Your constraint should not throw any inconsistency outside of .post() or .propagate()");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    @GradeFeedback(message = "How should you handle x = {10_000..10_100}? Do you need more than 10 000 entries?",
            on = TestResultStatus.FAIL)
    public void testInitSupports2(Solver cp) {
        try {
            IntVar[] x = makeIntVarArray(cp, 3, 12);
            for (int i = 0 ; i < 3 ; ++i) {
                x[i].removeBelow(i+1);
            }
            int[][] table = new int[][]{
                    {0, 0, 2},
                    {3, 5, 7},
                    {6, 9, 10},
                    {1, 2, 3},
                    {0, 0, 3},
            };
            TableCT tableCT = new TableCT(x, table);
            SupportBitSet[][] supports = tableCT.supports;
            //supports[i][v] is the set of tuples supported by x[i]=v
            HashSet<Integer> valid = new HashSet<>();
            for (int i = 0 ; i < 3 ; ++i) {
                valid.clear(); // values that are valid for x[i]
                for (int[] ints : table) {
                    if (x[i].contains(ints[i]))
                        valid.add(ints[i]);
                }
                for (int v = 0 ; v < 12 ; ++v) {
                    int ofs = x[i].min();
                    if (valid.contains(v)) {
                        // the value is contained within some row of the table
                        for (int row = 0 ; row < table.length ; ++row) {
                            if (table[row][i] == v) {
                                assertTrue(supports[i][v - ofs].get(row),
                                        String.format("The value %d is valid for x[%d] at row %d but you did not set support[%d][%d] at bit %d",
                                                v, i, row, i, v, row));
                            } else {
                                assertFalse(supports[i][v - ofs].get(row),
                                        String.format("The value %d is not valid for x[%d] at row %d but you set support[%d][%d] at bit %d",
                                                v, i, row, i, v, row));
                            }
                        }
                        for (int bit = table.length ; bit < 64 ; ++bit) {
                            assertFalse(supports[i][v - ofs].get(bit),
                                    String.format("The row %d exceeds the table length, support[%d][%d] should not be set at bit %d",
                                            bit, i, v, bit));
                        }
                    }
                }
            }
        } catch (InconsistencyException e) {
            fail("Your constraint should not throw any inconsistency outside of .post() or .propagate()");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    private void assertSupportedTuples(TableCT tableCT, IntVar[] x, int[][] table) {
        StateSparseBitSet supportedTuples = tableCT.supportedTuples;
        for (int row = 0 ; row < table.length ; ++row) {
            boolean valid = true;
            for (int i = 0 ; i < table[row].length ; ++i) {
                valid = valid && x[i].contains(table[row][i]);
            }
            assertEquals(valid, supportedTuples.get(row),
                    String.format("The row %d of the table is %svalid but you set supportedTuples[%d] to %b",
                            row, valid ? "" : "in", row, valid));
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testSetSupportedTuples1(Solver cp) {
        try {
            IntVar[] x = makeIntVarArray(cp, 3, 12);
            int[][] table = new int[][]{
                    {-1, 0, 5},
                    {0, 0, 2},
                    {2, 0, -9},
                    {3, 5, 7},
                    {15, 0, -6},
                    {6, 9, 10},
                    {5, -2, 0},
                    {-1, 0, 7},
                    {1, 2, 3},
                    {0, 0, 12},
            };
            TableCT tableCT = new TableCT(x, table);
            tableCT.post();
            // propagation has been called and supportedTuples should be set
            assertSupportedTuples(tableCT, x, table);
        } catch (InconsistencyException e) {
            fail("Your constraint should not throw any inconsistency outside of .post() or .propagate()");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testSetSupportedTuples2(Solver cp) {
        try {
            IntVar[] x = makeIntVarArray(cp, 3, 12);
            int[][] table = new int[][]{
                    {-1, 0, 5},
                    {0, 0, 2},
                    {2, 0, -9},
                    {3, 5, 7},
                    {15, 0, -6},
                    {6, 9, 10},
                    {5, -2, 0},
                    {-1, 0, 7},
                    {1, 2, 3},
                    {0, 0, 12},
            };
            TableCT tableCT = new TableCT(x, table);
            tableCT.post();
            // propagation has been called and supportedTuples should be set
            assertSupportedTuples(tableCT, x, table);
            HashSet<Integer> invalid = new HashSet<>();
            invalid.add(0);
            invalid.add(5);
            invalid.add(7);
            for (int i = 0 ; i < 3 ; ++i) {
                for (int v: invalid) {
                    x[i].remove(v);
                }
            }
            cp.fixPoint();
            // the supported tables have changed, as some rows are not valid anymore
            assertSupportedTuples(tableCT, x, table);
        } catch (InconsistencyException e) {
            fail("Your constraint should not throw any inconsistency outside of .post() or .propagate()");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void simpleTestAlwaysTrue(Solver cp) {
        try {
            IntVar[] x = makeIntVarArray(cp, 2, 1);
            int[][] table = new int[][]{{0, 0}};
            new TableCT(x, table).post();
        } catch (InconsistencyException e) {
            fail("Your implementation does finds inconsistent the simple CSP with D(x)={0}, D(y)={0} " +
                    "and a simple table with scope (x,y) and composed of unique tuple (0,0)");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void simpleTestEmpty(Solver cp) {
        try {
            IntVar[] x = makeIntVarArray(cp, 2, 1);
            int[][] table = new int[][]{};
            new TableCT(x, table).post();
            fail("Your implementation doesn't filter well invalid tuples");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        } catch (InconsistencyException ignored) {

        }
    }

    private boolean checkDomain(IntVar x, int v){
        int[] temp = new int[x.size()];
        x.fillArray(temp);
        for (int i = 0; i < x.size(); i++) {
            if (temp[i] == v)
                return true;
        }
        return false;
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void simpleTestSimpleTable(Solver cp) {
        try {
            IntVar[] x = makeIntVarArray(cp, 3, 12);
            int[][] table = new int[][]{{0, 0, 2},
                    {3, 5, 7},
                    {6, 9, 10},
                    {1, 2, 3}};
            new TableCT(x, table).post();

            String str0 = (x[0].size() > 4? "not enough" : "too much");
            assertEquals(4, x[0].size(), "Your filtering seems to filter " + str0 + " values");
            String str1 = (x[1].size() > 4? "not enough" : "too much");
            assertEquals(4, x[1].size(), "Your filtering seems to filter " + str1 + " values");
            String str2 = (x[2].size() > 4? "not enough" : "too much");
            assertEquals(4, x[2].size(), "Your filtering seems to filter " + str2 + " values");

            for (int i = 0 ; i < x.length;i++){
                for (int j = 0; j < table.length; j++)
                    assertTrue(checkDomain(x[i],table[j][i]), "Your filtering did not keep one of the valid value");
            }

        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void simpleTestOffset(Solver cp) {
        try {
            IntVar[] x = makeIntVarArray(cp, 3, 12);
            int[][] table = new int[][]{{0, 0, 2},
                    {3, 5, 7},
                    {6, 9, 10},
                    {1, 2, 3}};
            new TableCT(x, table).post();
            String warn = "(this test declares variable where min(D(x)) > 0, " +
                    "if this is the only filtering test failing, you may not be handling the offset of variable well)";

            String str0 = (x[0].size() > 4? "not enough" : "too much");
            assertEquals(4, x[0].size(),
                    "Your filtering seems to filter " + str0 + " values "+warn);
            String str1 = (x[1].size() > 4? "not enough" : "too much");
            assertEquals(4, x[1].size(),
                    "Your filtering seems to filter " + str1 + " values "+warn);
            String str2 = (x[2].size() > 4? "not enough" : "too much");
            assertEquals(4, x[2].size(),
                    "Your filtering seems to filter " + str2 + " values "+warn);

            for (int i = 0; i < x.length;i++){
                for (int j = 0; j < table.length;j++){
                    assertTrue(checkDomain(x[i],table[j][i]),
                            "Your filtering does not work, it removes values that are still supported "+warn);
                }
            }
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("solverSupplier")
    public void randomTest(Supplier<Solver> cp) {
        Random rand = new Random(67292);

        for (int i = 0; i < 100; i++) {
            int[][] tuples1 = randomTuples(rand, 3, 50, 2, 8);
            int[][] tuples2 = randomTuples(rand, 3, 50, 1, 7);
            int[][] tuples3 = randomTuples(rand, 3, 50, 0, 6);
            try {
                testTable(cp, TableCT::new, tuples1, tuples2, tuples3);
            } catch (NotImplementedException e) {
                NotImplementedExceptionAssume.fail(e);
            }
        }
    }

    public void testTable(Supplier<Solver> CPSupplier, BiFunction<IntVar[], int[][], Constraint> tc, int[][] t1, int[][] t2, int[][] t3) {
        SearchStatistics statsDecomp;
        SearchStatistics statsAlgo;

        try {
            Solver cp = CPSupplier.get();
            IntVar[] x = makeIntVarArray(cp, 5, 9);
            cp.post(allDifferent(x));
            cp.post(new TableDecomp(new IntVar[]{x[0], x[1], x[2]}, t1));
            cp.post(new TableDecomp(new IntVar[]{x[2], x[3], x[4]}, t2));
            cp.post(new TableDecomp(new IntVar[]{x[0], x[2], x[4]}, t3));
            statsDecomp = makeDfs(cp, firstFail(x)).solve();
        } catch (InconsistencyException e) {
            statsDecomp = null;
        }

        try {
            Solver cp = CPSupplier.get();
            IntVar[] x = makeIntVarArray(cp, 5, 9);
            cp.post(allDifferent(x));
            cp.post(tc.apply(new IntVar[]{x[0], x[1], x[2]}, t1));
            cp.post(tc.apply(new IntVar[]{x[2], x[3], x[4]}, t2));
            cp.post(tc.apply(new IntVar[]{x[0], x[2], x[4]}, t3));
            statsAlgo = makeDfs(cp, firstFail(x)).solve();
        } catch (InconsistencyException e) {
            statsAlgo = null;
        }

        assertTrue((statsDecomp == null && statsAlgo == null) || (statsDecomp != null && statsAlgo != null),
                "Your propagator is not correct");
        if (statsDecomp != null) {
            String strsol = (statsAlgo.numberOfSolutions() > statsDecomp.numberOfSolutions()? "too much": "too few");
            assertEquals(statsDecomp.numberOfSolutions(), statsAlgo.numberOfSolutions(),
                    "Your propagator does not found the right number of solution for a problem. " +
                            "It finds "+strsol+ " solutions than expected");
            assertEquals(statsDecomp.numberOfFailures(), statsAlgo.numberOfFailures(),
                    "Your method is not filtering as it should");
            assertEquals(statsDecomp.numberOfNodes(), statsAlgo.numberOfNodes());
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void testIncrementalUpdates(Solver cp) {
        try {
            IntVar[] x = makeIntVarArray(cp, 3, 12);
            int[][] table = new int[][]{
                    {0, 0, 2},
                    {1, 1, 7},
                    {2, 2, 10},
                    {3, 3, 3}
            };
            // creates the constraint but does not propagate yet
            TableCT tableCT = new TableCT(x, table);
            // no propagation has been done yet => every variable must be updated
            for (int i = 0 ; i < x.length ; ++i) {
                assertTrue(tableCT.hasChanged(i),
                        "Every variable induces a change at the very first propagation call");
            }
            // execute the constraint
            cp.post(tableCT);
            // after the fixpoint, everything is stable and the variables must not be updated
            for (int i = 0 ; i < x.length ; ++i) {
                assertFalse(tableCT.hasChanged(i),
                        String.format("The domain of x[%d] has not changed since the last propagation", i));
            }
            // propagate once the constraint. This should do nothing and no variable should induce a change
            tableCT.propagate();
            for (int i = 0 ; i < x.length ; ++i) {
                assertFalse(tableCT.hasChanged(i),
                        String.format("The domain of x[%d] has not changed since the last propagation", i));
            }
            cp.getStateManager().saveState();
            // change variables one by one
            for (int i = 0 ; i < x.length ; ++i) {
                x[i].remove(x[i].min());
                assertTrue(tableCT.hasChanged(i),
                        String.format("The domain of x[%d] has changed since the last propagation", i));
                for (int j = i+1 ; j < x.length ; ++j) {
                    assertFalse(tableCT.hasChanged(j),
                            String.format("The domain of x[%d] has not changed since the last propagation", j));
                }
                cp.fixPoint();
                // after the fixpoint no changes should be detected
                for (int j = 0 ; j < x.length ; ++j) {
                    assertFalse(tableCT.hasChanged(j),
                            String.format("The domain of x[%d] has not changed since the last propagation", j));
                }
            }
            cp.getStateManager().restoreState();
        } catch (InconsistencyException e) {
            fail("should not fail");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

}
