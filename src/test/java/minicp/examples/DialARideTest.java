package minicp.examples;

import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.NotImplementedException;
import minicp.util.io.InputReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static minicp.examples.DialARide.readRide;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * This test class is not graded
 * It is meant as a helper class to get started with the project located in the {@link DialARide} file
 * Use it to make sure that your model respects the constraints from the problem
 * This supposes that you did not modify the solution class, nor the methods already provided
 */
public class DialARideTest {

    /**
     * Ensures that all stops in the problem are visited exactly once
     *
     * @param solution solution to check
     */
    private static void assertVisitAllStopsExactlyOnce(DialARide.DialARideSolution solution) {
        HashSet<DialARide.RideStop> seenStops = new HashSet<>();
        HashSet<DialARide.RideStop> allStops = new HashSet<>();
        allStops.addAll(solution.pickupRideStops);
        allStops.addAll(solution.dropRideStops);
        for (int vehicleId = 0; vehicleId < solution.stops.length; vehicleId++) {
            for (int next : solution.stops[vehicleId]) {
                DialARide.RideStop stop;
                int rideId = next < solution.pickupRideStops.size() ? next : next - solution.pickupRideStops.size();
                if (next < solution.pickupRideStops.size())
                    stop = solution.pickupRideStops.get(next);
                else
                    stop = solution.dropRideStops.get(next - solution.pickupRideStops.size());
                if (seenStops.contains(stop))
                    fail(String.format("Stop of ride %d (%s) is visited twice", rideId, stop.type == 1 ? "pickup" : "drop"));
                seenStops.add(stop);
            }
        }
        if (seenStops.size() != allStops.size())
            fail("Not all stops are visited");
    }

    /**
     * Ensures that the capacity of the vehicle is respected
     *
     * @param solution solution to check
     */
    private static void assertCapacity(DialARide.DialARideSolution solution) {
        for (int vehicleId = 0; vehicleId < solution.stops.length; vehicleId++) {
            int capa = 0;
            for (int next : solution.stops[vehicleId]) {
                if (next < solution.pickupRideStops.size()) {
                    capa += 1;
                } else {
                    capa -= 1;
                }
                if (capa > solution.capacity)
                    fail(String.format("Vehicle %d exceeded maximum capacity (%d > %d)", vehicleId, capa, solution.capacity));
                if (capa < 0)
                    fail(String.format("Vehicle %d had negative capacity (%d)", vehicleId, capa));
            }
            if (capa != 0)
                fail(String.format("Vehicle %d ended with a capacity != 0 (found %d)", vehicleId, capa));
        }
    }

    /**
     * Ensures that each pickup occurs before its corresponding delivery
     * This also checks that a pickup has had its corresponding delivery visited by the end of the vehicle path
     *
     * @param solution solution to check
     */
    private static void assertPickupBeforeDelivery(DialARide.DialARideSolution solution) {
        for (int vehicleId = 0; vehicleId < solution.stops.length; vehicleId++) {
            HashMap<Integer, Integer> inside = new HashMap<>();
            int currentLength = 0;
            for (int next : solution.stops[vehicleId]) {
                if (next < solution.pickupRideStops.size()) {
                    inside.put(next, currentLength);
                } else {
                    if (!inside.containsKey(next - solution.pickupRideStops.size()))
                        fail(String.format("Drop of ride %d was visited before its corresponding pickup", next - solution.pickupRideStops.size()));
                    inside.remove(next - solution.pickupRideStops.size());
                }
            }
            if (inside.size() > 0)
                fail(String.format("Some rides never ended on vehicle %d", vehicleId));
        }
    }

    /**
     * Ensures that each pickup occurs before its corresponding delivery, and that the capacity of the vehicle is respected
     * This also checks that a pickup has had its corresponding delivery visited by the end of the vehicle path
     *
     * @param solution solution to check
     */
    private static void assertPickupBeforeDeliveryWithCapacity(DialARide.DialARideSolution solution) {
        for (int vehicleId = 0; vehicleId < solution.stops.length; vehicleId++) {
            HashMap<Integer, Integer> inside = new HashMap<>();
            DialARide.RideStop current = solution.depot;
            int currentLength = 0;
            for (int next : solution.stops[vehicleId]) {
                DialARide.RideStop nextStop;
                if (next < solution.pickupRideStops.size())
                    nextStop = solution.pickupRideStops.get(next);
                else
                    nextStop = solution.dropRideStops.get(next - solution.pickupRideStops.size());

                currentLength += DialARide.distance(current, nextStop);

                if (next < solution.pickupRideStops.size()) {
                    inside.put(next, currentLength);
                } else {
                    if (!inside.containsKey(next - solution.pickupRideStops.size()))
                        fail(String.format("Drop of ride %d was visited before its corresponding pickup", next - solution.pickupRideStops.size()));
                    inside.remove(next - solution.pickupRideStops.size());
                }
                if (inside.size() > solution.capacity)
                    fail(String.format("Vehicle %d exceeded maximum capacity (%d > %d)", vehicleId, inside.size(), solution.capacity));

                current = nextStop;
            }

            if (inside.size() != 0)
                fail(String.format("Vehicle %d ended with a capacity != 0 (found %d)", vehicleId, inside.size()));
        }
    }

    /**
     * Ensures that the maximum ride time of each solution is respected
     *
     * @param solution solution to check
     */
    private static void assertRideTime(DialARide.DialARideSolution solution) {
        HashSet<Integer> seenRides = new HashSet<>();

        for (int vehicleId = 0; vehicleId < solution.stops.length; vehicleId++) {
            HashMap<Integer, Integer> inside = new HashMap<>();
            DialARide.RideStop current = solution.depot;
            int currentLength = 0;
            for (int next : solution.stops[vehicleId]) {
                DialARide.RideStop nextStop;
                if (next < solution.pickupRideStops.size())
                    nextStop = solution.pickupRideStops.get(next);
                else
                    nextStop = solution.dropRideStops.get(next - solution.pickupRideStops.size());

                currentLength += DialARide.distance(current, nextStop);

                if (next < solution.pickupRideStops.size()) {
                    if (seenRides.contains(next))
                        throw new RuntimeException(String.format("Pickup of ride %d visited twice", next));
                    seenRides.add(next);
                    inside.put(next, currentLength);
                } else {
                    if (!inside.containsKey(next - solution.pickupRideStops.size()))
                        fail(String.format("Drop of ride %d was visited before its corresponding pickup", next - solution.pickupRideStops.size()));
                    if (inside.get(next - solution.pickupRideStops.size()) + solution.maxRideTime < currentLength)
                        fail(String.format("Ride %d is taking too long (%d > %d)",
                                next - solution.pickupRideStops.size(), currentLength - inside.get(next - solution.pickupRideStops.size()), solution.maxRideTime));
                    inside.remove(next - solution.pickupRideStops.size());
                }

                current = nextStop;
            }

            if (inside.size() > 0)
                fail(String.format("Vehicle %d ended without a pickup having its corresponding drop visited", vehicleId));
        }
    }

    /**
     * Ensures that all the maximum route duration of each vehicle is respected
     *
     * @param solution solution to check
     */
    private static void assertMaxRouteDuration(DialARide.DialARideSolution solution) {
        for (int vehicleId = 0; vehicleId < solution.stops.length; vehicleId++) {
            DialARide.RideStop current = solution.depot;
            int currentLength = 0;
            for (int next : solution.stops[vehicleId]) {
                DialARide.RideStop nextStop;
                if (next < solution.pickupRideStops.size())
                    nextStop = solution.pickupRideStops.get(next);
                else
                    nextStop = solution.dropRideStops.get(next - solution.pickupRideStops.size());

                currentLength += DialARide.distance(current, nextStop);
                current = nextStop;
            }

            currentLength += DialARide.distance(current, solution.depot);

            if (currentLength > solution.maxRouteDuration)
                fail(String.format("Route of vehicle %d is too long (%d > %d)", vehicleId, currentLength, solution.maxRouteDuration));
        }
    }

    /**
     * Ensures that all stops done respect the window end
     *
     * @param solution solution to check
     */
    private static void assertWindowEnd(DialARide.DialARideSolution solution) {
        for (int vehicleId = 0; vehicleId < solution.stops.length; vehicleId++) {
            DialARide.RideStop current = solution.depot;
            int currentLength = 0;
            for (int next : solution.stops[vehicleId]) {
                int rideId = next < solution.pickupRideStops.size() ? next : next - solution.pickupRideStops.size();
                DialARide.RideStop nextStop;
                if (next < solution.pickupRideStops.size())
                    nextStop = solution.pickupRideStops.get(next);
                else
                    nextStop = solution.dropRideStops.get(next - solution.pickupRideStops.size());

                currentLength += DialARide.distance(current, nextStop);

                if (currentLength > nextStop.window_end)
                    fail(String.format("Ride %d: %s visited too late ()", rideId, nextStop.type == 1 ? "pickup" : "drop"));

                current = nextStop;
            }
        }
    }

    private class Instance {
        int nVehicles;
        int maxRouteDuration;
        int vehicleCapacity;
        int maxRideTime;
        ArrayList<DialARide.RideStop> pickupRideStops;
        ArrayList<DialARide.RideStop> dropRideStops;
        DialARide.RideStop depot;

        public Instance(String filename) {
            InputReader reader = new InputReader(filename);

            nVehicles = reader.getInt();
            reader.getInt(); //ignore
            maxRouteDuration = reader.getInt() * 100;
            vehicleCapacity = reader.getInt();
            maxRideTime = reader.getInt() * 100;

            depot = null;
            pickupRideStops = new ArrayList<>();
            dropRideStops = new ArrayList<>();
            boolean lastWasNotDrop = true;
            while (true) {
                DialARide.RideStop r = readRide(reader);
                if (r == null)
                    break;
                if (r.type == 0) {
                    assert depot == null;
                    depot = r;
                } else if (r.type == 1) {
                    assert lastWasNotDrop;
                    pickupRideStops.add(r);
                } else { //r.type == -1
                    lastWasNotDrop = false;
                    dropRideStops.add(r);
                }
            }
            assert depot != null;
            assert pickupRideStops.size() == dropRideStops.size();
        }

    }

    @ParameterizedTest
    @CsvSource({
            "data/dialaride/training",
            "data/dialaride/custom0",
            "data/dialaride/custom1",
            "data/dialaride/custom2",
            "data/dialaride/custom3",
    })
    public void testFirstSolutionFound(String instanceFile) {
        Instance instance = new Instance(instanceFile);
        DialARide.DialARideSolution solution = DialARide.solve(instance.nVehicles,
                instance.maxRouteDuration, instance.vehicleCapacity, instance.maxRideTime, instance.pickupRideStops, instance.dropRideStops, instance.depot);
        if (solution == null) {
            NotImplementedExceptionAssume.fail(new NotImplementedException("not implemented"));
        } else {
            // these are the checks done for every part of the problem
            // feel free to comment one of the assertions in case you only implemented a part of the constraints from the problem,
            // and want to check your solutions at this points
            assertVisitAllStopsExactlyOnce(solution);
            assertCapacity(solution);
            assertWindowEnd(solution);
            assertRideTime(solution);
            assertMaxRouteDuration(solution);
            assertPickupBeforeDelivery(solution);
            assertPickupBeforeDeliveryWithCapacity(solution);
        }
    }

    @Test
    public void testFindAllSolutions() {
        String instanceFile = "data/dialaride/training";
        Instance instance = new Instance(instanceFile);
        List<DialARide.DialARideSolution> solutions = DialARide.findAll(instance.nVehicles,
                instance.maxRouteDuration, instance.vehicleCapacity, instance.maxRideTime, instance.pickupRideStops, instance.dropRideStops, instance.depot);
        if (solutions == null) {
            NotImplementedExceptionAssume.fail(new NotImplementedException("not implemented"));
        } else {
            for (DialARide.DialARideSolution solution: solutions) {
                // these are the checks done for every part of the problem
                // feel free to comment one of the assertions in case you only implemented a part of the constraints from the problem,
                // and want to check your solutions at this points
                assertVisitAllStopsExactlyOnce(solution);
                assertCapacity(solution);
                assertWindowEnd(solution);
                assertRideTime(solution);
                assertMaxRouteDuration(solution);
                assertPickupBeforeDelivery(solution);
                assertPickupBeforeDeliveryWithCapacity(solution);
            }
            // check that all solutions are found on the training instance
            assertEquals(564, solutions.size(), "You did not find all solutions on the training instance");
        }

    }

}
