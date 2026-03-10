package minicp.examples;

import minicp.util.io.InputReader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class AircraftLanding {

    /**
     * Main function that provides a solution to an instance
     *
     * @param instance instance to solve
     * @return best solution found to the instance
     */
    public static AircraftLandingSolution solve(AircraftLandingInstance instance) {
         return null;
    }

    /**
     * Function that list all feasible solutions to an instance.
     * <p>
     * This function does not count in the grade, and should only be used for debugging purposes, to verify that you
     * find all solutions to a (small) instance.
     * <p>
     * Even though it is not mandatory, it is STRONGLY ADVISED to implement it and pass the related tests :-) .
     * @param instance instance to solve
     * @return all feasible solutions found to the instance
     */
    public List<AircraftLandingSolution> findAll(AircraftLandingInstance instance) {
         return null;
    }


    /**
     * A plane in the problem
     */
    public static class Plane {
        public int wantedTime;
        public int deadline;
        public int type;

        public Plane(int wantedTime, int deadline, int type) {
            this.type = type;
            this.wantedTime = wantedTime;
            this.deadline = deadline;
        }
    }

    /**
     * An instance of the aircraft landing problem
     */
    public static class AircraftLandingInstance {

        public int nPlanes, nTypes, nLanes;
        public Plane[] planes;
        public int[][] switchDelay;

        public AircraftLandingInstance(String url) {

            InputReader reader = new InputReader(url);

            nPlanes = reader.getInt();
            nTypes = reader.getInt();
            nLanes = reader.getInt();
            planes = new Plane[nPlanes];

            for (int i = 0; i < nPlanes; i++) {
                Plane plane = new Plane(reader.getInt(), reader.getInt(), reader.getInt());
                this.planes[i] = plane;
            }

            switchDelay = new int[nTypes][nTypes];
            for (int i = 0; i < nTypes; i++) {
                for (int j = 0; j < nTypes; j++) {
                    switchDelay[i][j] = reader.getInt();
                }
            }
        }

        public Plane getPlane(int i) {
            return planes[i];
        }

        /**
         * Gives the switch delay between two planes
         *
         * @param p1 first plane
         * @param p2 second plane
         * @return
         */
        public int switchDelay(Plane p1, Plane p2) {
            return switchDelay[p1.type][p2.type];
        }
    }

    /**
     * A solution to an aircraft landing instance.
     * <p>
     * Each time a plane lands, it must be declared using {@link AircraftLandingSolution#landPlane(int, int, int)}.
     * Once all planes are landed, {@link AircraftLandingSolution#compute()} and {@link AircraftLandingSolution#toString()} can be safely used.
     * A solution may be reset using {@link AircraftLandingSolution#clear()}.
     * <p>
     * DO NOT MODIFY THIS CLASS.
     */
    public static class AircraftLandingSolution {
        public AircraftLandingInstance instance;
        public List<Integer>[] lanes; // for each plane, the ids of the planes that have landed there
        public int[] times; // for each plane, the time at which it lands

        public AircraftLandingSolution(AircraftLandingInstance instance) {
            this.instance = instance;
            lanes = new ArrayList[instance.nLanes];
            for (int i = 0; i < lanes.length; i++) {
                lanes[i] = new ArrayList<>();
            }
            times = new int[instance.nPlanes];
        }

        /**
         * Encodes the landing of a plane in a solution.
         * This does not verify any constraint (checking is only done in {@link AircraftLandingSolution#compute()})
         *
         * @param planeId if of the plane to land
         * @param lane    lane on which the plane is landing
         * @param time    time at which the plane is landing
         */
        public void landPlane(int planeId, int lane, int time) {
            lanes[lane].add(planeId);
            times[planeId] = time;
        }

        /**
         * Resets this solution, so that this object can encode a new one
         */
        public void clear() {
            for (int i = 0; i < lanes.length; i++) {
                lanes[i].clear();
            }
        }

        /**
         * Gives the cost of a solution and throws a {@link RuntimeException} if the solution is invalid
         *
         * @return solution cost
         */
        public int compute() {
            int cost = 0;
            // sort each lane content based on the landing time of the planes, to have the planes in order of arrival
            for (List<Integer> lane : lanes) {
                lane.sort(Comparator.comparingInt(plane -> times[plane]));
            }
            // tracks the planes that have been seen
            HashSet<Integer> seen = new HashSet<>();
            for (List<Integer> lane : lanes) {
                int prev = -1;
                for (int current : lane) {
                    Plane plane = instance.getPlane(current);
                    if (times[current] < 0 || times[current] > plane.deadline) {
                        throw new RuntimeException("Time of plane " + current + " is out of the time window : " + times[current] + " not in [0," + plane.deadline + "]");
                    }
                    if (prev != -1) {
                        // check if transition between prev and current is has enough delay
                        Plane previousPlane = instance.getPlane(prev);
                        int switchDelay = instance.switchDelay(previousPlane, plane);
                        if (times[prev] + switchDelay > times[current]) {
                            throw new RuntimeException("Plane " + prev + " and plane " + current + " are too close to one another.\n" + "Expected minimum delay was " + switchDelay + " but got " + (times[prev] - times[current]));
                        }
                    }
                    cost += Math.abs(plane.wantedTime - times[current]);
                    prev = current;
                    if (seen.contains(current)) throw new RuntimeException("Plane " + current + " landed more than once");
                    seen.add(current);
                }
            }
            if (seen.size() != instance.nPlanes) {
                throw new RuntimeException("Some planes did not land");
            }
            return cost;
        }


        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append("Cost: ");
            b.append(compute());
            b.append("\n");
            for (List<Integer> lane : lanes) {
                b.append("- ");
                for (int i = 0; i < lane.size(); i++) {
                    int planeId = lane.get(i);
                    b.append(planeId);
                    b.append("(t=");
                    b.append(times[planeId]);
                    b.append(')');
                    if (i < lane.size() - 1) b.append(", ");
                }
                b.append("\n");
            }
            return b.toString();
        }
    }


    public static void main(String[] args) {
        //TODO change file to test the various instances.
        AircraftLandingInstance instance = new AircraftLandingInstance("data/alp/training");
        AircraftLanding alp = new AircraftLanding();
        AircraftLandingSolution solution = alp.solve(instance);
        System.out.println(solution);
    }
}
