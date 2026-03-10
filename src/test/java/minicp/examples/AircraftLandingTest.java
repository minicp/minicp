package minicp.examples;

import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.NotImplementedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.opentest4j.AssertionFailedError;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AircraftLandingTest {

    private static void assertAllPlanesPlaced(AircraftLanding.AircraftLandingSolution solution) {
        HashSet<Integer> seen = new HashSet<>();
        for (List<Integer> lane: solution.lanes) {
            for (int plane: lane) {
                assertFalse(seen.contains(plane), "Plane " + plane + " landed more than once");
                seen.add(plane);
            }
        }
        assertEquals(solution.instance.nPlanes, seen.size(), "Some planes did not land");
    }

    private static void assertCorrectTimes(AircraftLanding.AircraftLandingSolution solution) {
        for (List<Integer> lane: solution.lanes) {
            for (int plane: lane) {
                int time = solution.times[plane];
                int deadline = solution.instance.getPlane(plane).deadline;
                assertFalse(time < 0 || time > deadline, "Time of plane " + plane + " is out of the time window : " + time + " not in [0," + deadline + "]");
            }
        }
    }

    private static void assertCorrectSwitchDelay(AircraftLanding.AircraftLandingSolution solution) {
        for (int lane = 0 ; lane < solution.lanes.length ; lane++) {
            for (int i = 0; i < solution.lanes[lane].size(); i++) {
                int planeI = solution.lanes[lane].get(i);
                int closest = -1;
                for (int j = 0; j < solution.lanes[lane].size(); j++) {
                    if (i != j) {
                        int planeJ = solution.lanes[lane].get(j);
                        if (solution.times[planeJ] >= solution.times[planeI]) {
                            if (closest == -1 || solution.times[planeJ] < solution.times[closest]) {
                                closest = planeJ;
                            }
                        }
                    }
                }
                if (closest != -1) {
                    AircraftLanding.Plane current = solution.instance.getPlane(i);
                    AircraftLanding.Plane next = solution.instance.getPlane(closest);
                    int delay = solution.instance.switchDelay(current, next);
                    try {
                        assertFalse(solution.times[planeI] + delay > solution.times[closest], "Plane " + planeI + " and plane " + closest + " are too close to one another.\n" +
                                "Expected minimum delay was " + delay + " but got " + (solution.times[closest] - solution.times[planeI]));
                    } catch (AssertionFailedError e) {
                        solution.compute();
                        int a = 0;
                    }
                }
            }
        }
    }

    @ParameterizedTest
    @CsvSource({
            "data/alp/training",
            "data/alp/custom0",
            "data/alp/custom1",
            "data/alp/custom2",
            "data/alp/custom3",
    })
    public void testFirstSolutionFound(String instanceFile) {
        AircraftLanding.AircraftLandingInstance instance = new AircraftLanding.AircraftLandingInstance(instanceFile);
        AircraftLanding.AircraftLandingSolution solution = AircraftLanding.solve(instance);
        if (solution == null) {
            NotImplementedExceptionAssume.fail(new NotImplementedException("not implemented"));
        } else {
            // these are the checks done for every part of the problem
            // feel free to comment one of the assertions in case you only implemented a part of the constraints from the problem,
            // and want to check your solutions at this points
            assertAllPlanesPlaced(solution);
            assertCorrectTimes(solution);
            assertCorrectSwitchDelay(solution);
        }
    }

    @Test
    public void testFindAllSolutions() {
        String instanceFile = "data/alp/training";
        AircraftLanding.AircraftLandingInstance instance = new AircraftLanding.AircraftLandingInstance(instanceFile);
        List<AircraftLanding.AircraftLandingSolution> solutions = new AircraftLanding().findAll(instance);
        if (solutions == null) {
            NotImplementedExceptionAssume.fail(new NotImplementedException("not implemented"));
        } else {
            for (AircraftLanding.AircraftLandingSolution solution: solutions) {
                // these are the checks done for every part of the problem
                // feel free to comment one of the assertions in case you only implemented a part of the constraints from the problem,
                // and want to check your solutions at this points
                assertAllPlanesPlaced(solution);
                assertCorrectTimes(solution);
                assertCorrectSwitchDelay(solution);
            }
            assertEquals(1816, solutions.size());
        }
    }
}
