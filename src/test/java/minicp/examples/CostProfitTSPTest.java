package minicp.examples;

import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.NotImplementedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CostProfitTSPTest {

    @BeforeEach
    public void disableVerbose() {
        CostProfitTSP.verbose = false;
    }

    private static void assertStartEnd(CostProfitTSP.CostProfitTSPSolution solution) {
        assertEquals(solution.instance.start, solution.seq.get(0), "The tour must start at " + solution.instance.start);
        assertEquals(solution.instance.end, solution.seq.get(solution.seq.size() - 1), "The tour must end at " + solution.instance.end);
    }

    private static void assertAllMandatory(CostProfitTSP.CostProfitTSPSolution solution) {
        for (int m : solution.instance.mandatory) {
            assertTrue(solution.seq.contains(m), "Your tour is missing the mandatory visit " + m);
        }
    }

    private static void assertTimeBudgetNotExceeded(CostProfitTSP.CostProfitTSPSolution solution) {
        int time = 0;
        for (int i = 0; i < solution.seq.size() - 1; i++) {
            time += solution.instance.serviceTime[solution.seq.get(i)];
            time += solution.instance.travelTime[solution.seq.get(i)][solution.seq.get(i + 1)];
        }
        time += solution.instance.serviceTime[solution.instance.end];
        assertTrue(time <= solution.instance.timeBudget, "Your tour is exceeding the time budget of the instance, time budget " + solution.instance.timeBudget + ", your tour " + time);
    }

    private static void assertNoTwoVisits(CostProfitTSP.CostProfitTSPSolution solution) {
        HashSet<Integer> set = new HashSet<>(solution.seq);
        assertEquals(set.size(), solution.seq.size(), "You cannot visit multiple times the same visit point.");
    }

    @ParameterizedTest
    @CsvSource({
            "data/CostProfitTSP/training1.json",
            "data/CostProfitTSP/training2.json"
    })
    public void testFirstSolutionFound(String instanceFile) {
        CostProfitTSP.CostProfitTSPInstance instance = new CostProfitTSP.CostProfitTSPInstance(instanceFile);
        CostProfitTSP.CostProfitTSPSolution solution = CostProfitTSP.solve(instance);
        if (solution == null) {
            NotImplementedExceptionAssume.fail(new NotImplementedException("not implemented"));
        } else {
            // these are the checks done for every part of the problem
            // feel free to comment one of the assertions in case you only implemented a part of the constraints from the problem,
            // and want to check your solutions at this points
            assertStartEnd(solution);
            assertAllMandatory(solution);
            assertNoTwoVisits(solution);
            assertTimeBudgetNotExceeded(solution);
        }
    }

    @Test
    public void testFindAllSolutions() {
        String instanceFile = "data/CostProfitTSP/training1.json";
        CostProfitTSP.CostProfitTSPInstance instance = new CostProfitTSP.CostProfitTSPInstance(instanceFile);
        List<CostProfitTSP.CostProfitTSPSolution> solutions = new CostProfitTSP().findAll(instance);
        if (solutions == null) {
            NotImplementedExceptionAssume.fail(new NotImplementedException("not implemented"));
        } else {
            for (CostProfitTSP.CostProfitTSPSolution solution : solutions) {
                // these are the checks done for every part of the problem
                // feel free to comment one of the assertions in case you only implemented a part of the constraints from the problem,
                // and want to check your solutions at this points
                assertStartEnd(solution);
                assertAllMandatory(solution);
                assertNoTwoVisits(solution);
                assertTimeBudgetNotExceeded(solution);
            }
            assertEquals(36134, solutions.size());
        }
    }
}
