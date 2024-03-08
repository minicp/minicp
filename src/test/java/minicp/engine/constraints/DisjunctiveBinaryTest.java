package minicp.engine.constraints;

import minicp.engine.SolverTest;
import minicp.engine.core.BoolVar;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import org.javagrader.Grade;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.TimeUnit;

import static minicp.cp.BranchingScheme.firstFail;
import static minicp.cp.Factory.*;
import static org.junit.jupiter.api.Assertions.*;

@Grade(cpuTimeout = 200, unit = TimeUnit.MILLISECONDS)
public class DisjunctiveBinaryTest extends SolverTest {

    @ParameterizedTest
    @MethodSource("getSolver")
    public void basicTest(Solver cp) {
        try {
            // The situation is the following:
            // A = [ ---    ]    est = 0, lst = 5, dur = 3
            // B = [ -----  ]    est = 0, lst = 3, dur = 5
            // clearly impossible given the time windows that B comes before A
            // therefore the constraint should detect that B is after A
            IntVar startA = makeIntVar(cp, 0, 5);
            IntVar startB = makeIntVar(cp, 0, 3);
            int durA = 3;
            int durB = 5;

            DisjunctiveBinary binary = new DisjunctiveBinary(startA, durA, startB, durB);

            // posting the disjunction and triggering the fix-point
            cp.post(binary);

            assertEquals(0, startA.min());
            assertEquals(5, startA.max());
            assertEquals(0, startB.min());
            assertEquals(3, startB.max());
            assertFalse(binary.before().isFixed()); // we don't know which one will come first
            assertFalse(binary.after().isTrue());
            assertEquals(10, binary.slack()); // slack =  6+3

            // tighten of the lst of A
            cp.post(lessOrEqual(startA, 2));

            // The situation is the following:
            // A = [ --- ]    est = 0, lst = 2, dur = 3
            // B = [ -----  ] est = 0, lst = 3, dur = 5
            // It is clearly impossible given the time windows that B comes before A
            // therefore the constraint should detect that B should come after A

            // verify that B starts after A and the filtering is correct
            assertEquals(0, startA.min());
            assertEquals(3, startB.min());
            assertTrue(binary.before().isTrue()); // A should be fixed before
            assertFalse(binary.after().isTrue());  // B should be fixed before
            assertTrue(binary.isFixed());
            assertEquals(2, binary.slack()); // slack = 2 since variables are fixed
        } catch (InconsistencyException e) {
            fail("You have thrown an inconsistency although the problem is valid");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @ParameterizedTest
    @MethodSource("getSolver")
    public void allDiffTest(Solver cp) {
        try {
            int[] dur = new int[]{1, 3, 2, 4};
            int n = dur.length;
            BoolVar[] before = new BoolVar[n * (n - 1) / 2];
            IntVar[] start = makeIntVarArray(cp, n, 10);
            IntVar[] end = makeIntVarArray(n, i -> plus(start[i], dur[i]));
            for (int i = 0; i < n; i++) {
                cp.post(lessOrEqual(end[i], 10));
            }
            int k = 0;
            for (int i = 0; i < dur.length; i++) {
                for (int j = i+1; j < dur.length; j++) {
                    DisjunctiveBinary db = new DisjunctiveBinary(start[i], dur[i], start[j], dur[j]);
                    cp.post(db);
                    before[k++] = db.before();
                }
            }
            DFSearch dfs = makeDfs(cp, firstFail(before));
            int[] cpt = new int[1];
            dfs.onSolution(() -> {
                cpt[0]++;
            });
            dfs.solve();
            assertEquals(24, cpt[0], "There are 24 ways of arranging differently 4 variables but you did not find them all");
        } catch (InconsistencyException e) {
            fail("You have thrown an inconsistency although the problem is valid");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }
}