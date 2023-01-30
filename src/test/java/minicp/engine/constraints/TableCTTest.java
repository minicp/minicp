package minicp.engine.constraints;

import minicp.cp.BranchingScheme;
import minicp.cp.Factory;
import minicp.engine.SolverTest;
import minicp.engine.core.Constraint;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.SearchStatistics;
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import org.javagrader.Grade;
import org.javagrader.GradeFeedback;
import org.javagrader.TestResultStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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
    public void simpleTestAlwaysTrue(Solver cp) {
        try {
            IntVar[] x = makeIntVarArray(cp, 2, 1);
            int[][] table = new int[][]{{0, 0}};
            cp.post(new TableCT(x, table));
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
            cp.post(new TableCT(x, table));
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
            cp.post(new TableCT(x, table));

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
            cp.post(new TableCT(x, table));
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
