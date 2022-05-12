package minicp.examples;

import minicp.util.io.InputReader;

import java.util.HashSet;

public class EBRP {

    public static final int DEPOT = 0; // the depot is always located at index 0

    /**
     * TODO
     * given an instance {@link EBRPInstance} to solve (a number of  nodes, distance between them and their time windows {@link TimeWindow}),
     * give the order of visit of each nodes minimizing the traveled distance of the Bell
     * use {@link EBRPInstance#distances} or {@link EBRPInstance#distance(int, int)} to compute distances
     * the Bell can arrive at a node n before the beginning of its time window {@link TimeWindow#getEarliest()}
     *  if this is the case, the departure occurs at the beginning of its time window
     * the depot is always located at node 0 {@link EBRP#DEPOT} and has the longest time window
     * @param instance instance to solve
     * @return valid solution minimizing the traveled distance of the Bell
     */
    public static EBRPSolution solve(EBRPInstance instance) {
        return null;
    }


    /**
     * A solution. To create one, first do new EBRPSolution, then
     * add, in order, the id of the node being visited {@link EBRPSolution#addVisit(int)}
     * You can also add the whole ordering in one go {@link EBRPSolution#setVisitOrder(int[])}
     * Do not add the first nor the last stop to the depot, it is implicit
     * <p>
     * You can check the validity of your solution with {@link EBRPSolution#compute()}, which returns the total distance
     * and throws a {@link RuntimeException} if something is invalid or {@link EBRPSolution#isValid()}, which only tells
     * if the solution is valid or not
     * <p>
     * DO NOT MODIFY THIS CLASS.
     */
    public static class EBRPSolution {

        private final int[] ordering; // inner ordering of the solution
        private final EBRPInstance instance; // instance related to the solution
        private int nVisitedNodes = 0; // number of visited nodes in the provided solution

        /**
         * output the solution on two lines: its length (objective value) and its ordering
         * @return solution as string
         */
        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append("Length: ");
            b.append(compute());
            b.append('\n');
            b.append(0);
            b.append(' ');
            for (int node: ordering) {
                b.append(node);
                b.append(' ');
            }
            b.append('\n');
            return b.toString();
        }

        /**
         * create a solution to a EBRP instance
         * the visit order is added through {@link EBRPSolution#addVisit(int)}
         * or {@link EBRPSolution#setVisitOrder(int[])}
         * @param instance instance whose solution will be represented
         */
        public EBRPSolution(EBRPInstance instance) {
            this.instance = instance;
            ordering = new int[instance.nNodes - 1]; // depot is not encoded
        }

        /**
         * set the last visited node in the current travel
         * @param node last node being visited (do not include the depot)
         */
        public void addVisit(int node) {
            if (node <= 0 || node >= instance.nNodes)
                throw new RuntimeException(String.format("Node %3d is invalid for the instance", node));
            ordering[nVisitedNodes++] = node;
        }

        /**
         * set the order of visits of nodes
         * @param ordering order of nodes, not containing the depot
         */
        public void setVisitOrder(int[] ordering) {
            if (ordering.length != instance.nNodes - 1) // too many / not enough nodes in the visit
                return;
            System.arraycopy(ordering, 0, this.ordering, 0, ordering.length);
            nVisitedNodes = ordering.length;
        }

        /**
         * compute the value of the solution
         * throws a {@link RuntimeException} if the solution is invalid
         * @return objective value
         */
        public int compute() {
            if (nVisitedNodes < ordering.length)
                throw new RuntimeException("Not all nodes have been visited");
            HashSet<Integer> seenStops = new HashSet<>();
            int distance = 0;
            int pred = DEPOT;
            int currentTime = instance.timeWindows[DEPOT].getEarliest();
            int current;
            for (int i = 0 ; i < nVisitedNodes ; ++i) {
                current = ordering[i];
                if (current <= 0 || current >= instance.nNodes)
                    throw new RuntimeException(String.format("Node %3d cannot be specified in the solution", current));
                if (seenStops.contains(current))
                    throw new RuntimeException(String.format("Node %3d visited twice", current));
                seenStops.add(current);

                int edgeDist = instance.distance(pred, current);
                currentTime += edgeDist;
                // waiting at a node is allowed but the node is then processed at its earliest time
                if (currentTime < instance.timeWindows[current].getEarliest())
                    currentTime = instance.timeWindows[current].getEarliest();

                if (currentTime > instance.timeWindows[current].getLatest())
                    throw new RuntimeException(String.format("Node %3d visited too late (transition %3d -> %3d)", current, pred, current));
                distance += edgeDist;

                // goes to the next node
                pred = current;
            }
            if (seenStops.size() != nVisitedNodes)
                throw new RuntimeException("Not all nodes have been visited");
            if (seenStops.contains(0))
                throw new RuntimeException("Do not specify the depot in your solution, it is implicit at both " +
                        "the beginning and end of the travel");

            int edgeDist = instance.distance(pred, DEPOT);
            currentTime += edgeDist;
            if (currentTime > instance.timeWindows[DEPOT].getLatest())
                throw new RuntimeException("Route ended too late");
            distance += edgeDist;
            return distance;
        }

        /**
         * tell if the solution is valid or not
         * @return true if the solution is valid
         */
        public boolean isValid() {
            try {
                compute();
                return true;
            } catch (RuntimeException exception) {
                return false;
            }
        }

        /**
         * clear the solution, forgetting the order of visit that was given previously
         */
        public void clear() {
            nVisitedNodes = 0;
        }

    }

    /**
     *  An EBRP instance, with its distance matrix and time windows {@link TimeWindow}
     */
    public static class EBRPInstance {
        public final int nNodes; // number of nodes in the problem (including depot)
        public final int[][] distances; // distance matrix
        public final TimeWindow[] timeWindows; // time window of each node

        /**
         * An EBRP instance
         * @param nNodes number of nodes in the instance
         * @param distances distance between nodes
         * @param tw time windows of each node
         */
        public EBRPInstance(final int nNodes, final int[][] distances, final TimeWindow[] tw) {
            this.nNodes = nNodes;
            this.distances = distances;
            this.timeWindows = tw;
        }

        /**
         * create an instance from a file
         * @param filePath file where the instance is written
         * @return instance related to the file
         */
        public static EBRPInstance fromFile(String filePath) {
            InputReader reader = new InputReader(filePath);
            int nNodes = reader.getInt();
            int[][] distMatrix = reader.getMatrix(nNodes, nNodes);
            TimeWindow[] tw = new TimeWindow[nNodes];

            for (int i = 0 ; i < nNodes ; ++i) {
                tw[i] = new TimeWindow(reader.getInt(), reader.getInt());
            }

            EBRPInstance instance = new EBRPInstance(nNodes, distMatrix, tw);
            return instance;
        }

        /**
         * gives the cost associated to a visit ordering
         *
         * @param ordering order of visit for the nodes. First node == 0 == begin depot
         * @return routing cost associated with the visit of the nodes
         */
        public int cost(int[] ordering) {
            int cost = 0;
            int pred = 0;
            int current = -1;
            for (int i = 1; i < nNodes; ++i) {
                current = ordering[i];
                cost += distances[pred][current];
                pred = current;
            }
            cost += distances[current][0]; // closes the route
            return cost;
        }

        /**
         * give the distance between two nodes
         * @param from origin node
         * @param to destination node
         * @return distance to travel from node "from" until node "to"
         */
        public int distance(int from, int to) {
            return distances[from][to];
        }

        @Override
        public String toString() {
            return "EBRPInstance{" +
                    nNodes + " nodes, " +
                    "depot window = [" + timeWindows[DEPOT].getEarliest() + "..." + timeWindows[DEPOT].getLatest() + "]" +
                    '}';
        }

    }

    /**
     *  A time window, represented by the earliest visit time and latest visit time
     */
    public static class TimeWindow {
        final int earliest; // earliest visit time
        final int latest; // latest visit time

        /**
         * a time window, containing the earliest visit time allowed and the latest visit time allowed
         * @param earliest earliest visit time
         * @param latest latest visit time
         */
        public TimeWindow(final int earliest, final int latest) {
            this.earliest = earliest;
            this.latest   = latest;
        }

        /**
         * @return earliest visit time of the node
         */
        public int getEarliest() {
            return earliest;
        }

        /**
         * @return latest visit time of the node
         */
        public int getLatest() {
            return latest;
        }

    }

    /**
     * solve an EBRP instance using the time allowed
     * @param args contains the path to the instance (one parameter only)
     */
    public static void main(String[] args) {
        // Reading the data

        //TODO change file to train the various instances.
        // EBRPInstance instance = EBRPInstance.fromFile(args[0]);
        EBRPInstance instance = EBRPInstance.fromFile("data/ebrp/train1");
        EBRPSolution sol = solve(instance);
    }

}
