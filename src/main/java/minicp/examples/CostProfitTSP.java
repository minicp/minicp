package minicp.examples;

import minicp.engine.constraints.AllDifferentFWC;
import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.BoolVar;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.Objective;
import minicp.search.SearchStatistics;
import minicp.state.StateInt;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static minicp.cp.BranchingScheme.*;
import static minicp.cp.Factory.*;
import static minicp.cp.Factory.makeDfs;

public class CostProfitTSP {

    public static boolean verbose = true;


    /**
     * Main function that provides a solution to an instance
     *
     * @param instance instance to solve
     * @return best solution found to the instance
     */
    public static CostProfitTSPSolution solve(CostProfitTSPInstance instance) {
         return null;
    }

    /**
     * Function that list all feasible solutions to an instance.
     * <p>
     * This function does not count in the grade, and should only be used for debugging purposes, to verify that you
     * find all solutions to a (small) instance.
     * <p>
     * Even though it is not mandatory, it is STRONGLY ADVISED to implement it and pass the related tests :-) .
     *
     * @param instance instance to solve
     * @return all feasible solutions found to the instance
     */
    public List<CostProfitTSPSolution> findAll(CostProfitTSPInstance instance) {
         return null;
    }


    public static class CostProfitTSPInstance {
        int nNodes;
        int start;
        int end;
        int[] mandatory;
        int[] rewards;
        int[] serviceTime;
        int[][] travelTime;
        int timeBudget;

        public CostProfitTSPInstance(String filename) {
            try {
                String content = new String(Files.readAllBytes(Paths.get(filename)));

                // remove all spaces and line breaks
                content = content.replaceAll("\\s", "");

                nNodes = extractInt(content, "n_nodes");
                start = extractInt(content, "start");
                end = extractInt(content, "end");
                timeBudget = extractInt(content, "time_budget");
                mandatory = extractIntArray(content, "mandatory");

                rewards = new int[nNodes];
                serviceTime = new int[nNodes];
                int[][] coords = new int[nNodes][2];
                travelTime = new int[nNodes][nNodes];

                String visitsBlock = extractVisitsBlock(content);

                if (visitsBlock.startsWith("{")) {
                    visitsBlock = visitsBlock.substring(1);
                }
                if (visitsBlock.endsWith("}")) {
                    visitsBlock = visitsBlock.substring(0, visitsBlock.length() - 1);
                }

                String[] visitStrings = visitsBlock.split("\\},\\{");

                for (String visit : visitStrings) {

                    // Ensure valid object format for extractInt
                    if (!visit.startsWith("{")) visit = "{" + visit;
                    if (!visit.endsWith("}")) visit = visit + "}";

                    int id = extractInt(visit, "id");
                    int x = extractInt(visit, "x");
                    int y = extractInt(visit, "y");
                    int s = extractInt(visit, "service_time");
                    int r = extractInt(visit, "reward");

                    coords[id][0] = x;
                    coords[id][1] = y;
                    serviceTime[id] = s;
                    rewards[id] = r;
                }

                for (int i = 0; i < nNodes; i++) {
                    for (int j = 0; j < nNodes; j++) {
                        int dx = coords[i][0] - coords[j][0];
                        int dy = coords[i][1] - coords[j][1];
                        travelTime[i][j] =
                                (int) Math.round(Math.sqrt(dx * dx + dy * dy));
                    }
                }


            } catch (IOException e) {
                throw new RuntimeException("Error reading instance file: " + filename, e);
            }
        }

        private int extractInt(String json, String key) {
            String pattern = "\"" + key + "\":";
            int start = json.indexOf(pattern) + pattern.length();
            int end = json.indexOf(",", start);
            if (end == -1) {
                end = json.indexOf("}", start);
            }
            return Integer.parseInt(json.substring(start, end));
        }

        private int[] extractIntArray(String json, String key) {
            String pattern = "\"" + key + "\":[";
            int start = json.indexOf(pattern) + pattern.length();
            int end = json.indexOf("]", start);

            String[] tokens = json.substring(start, end).split(",");
            int[] array = new int[tokens.length];

            for (int i = 0; i < tokens.length; i++) {
                array[i] = Integer.parseInt(tokens[i]);
            }

            return array;
        }

        private String extractVisitsBlock(String json) {
            String pattern = "\"visits\":[";
            int start = json.indexOf(pattern) + pattern.length();

            int bracketCount = 1;
            int i = start;

            while (bracketCount > 0 && i < json.length()) {
                if (json.charAt(i) == '[') bracketCount++;
                if (json.charAt(i) == ']') bracketCount--;
                i++;
            }

            return json.substring(start, i - 1);
        }


    }

    /**
     * A solution to an CostProfitTSP instance
     * <p>
     * Each time a visit occurs, it must be declared using {@link CostProfitTSPSolution#addVisit(int)}.
     * Once all visits are planned, {@link CostProfitTSPSolution#compute()} and {@link CostProfitTSPSolution#toString()} can be safely used.
     * A solution may be reset using {@link CostProfitTSPSolution#clear()}.
     * <p>
     * DO NOT MODIFY THIS CLASS.
     */
    public static class CostProfitTSPSolution {
        ArrayList<Integer> seq = new ArrayList<>();
        CostProfitTSPInstance instance;

        public CostProfitTSPSolution(CostProfitTSPInstance instance) {
            this.instance = instance;
        }

        public void addVisit(int visit) {
            seq.add(visit);
        }

        public void clear() {
            seq.clear();
        }

        public int compute() {
            if (seq.get(0) != instance.start || seq.get(seq.size() - 1) != instance.end) {
                throw new RuntimeException("The tour must start at 'start' and end at 'end'");
            }
            for (int m : instance.mandatory) {
                if (!seq.contains(m)) {
                    throw new RuntimeException("Missing mandatory visits");
                }
            }
            HashSet<Integer> set = new HashSet<>(seq);
            if (set.size() != seq.size()) {
                throw new RuntimeException("Duplicate visits");
            }
            int time = 0;
            int reward = 0;
            for (int i = 0; i < seq.size() - 1; i++) {
                time += instance.serviceTime[seq.get(i)];
                time += instance.travelTime[seq.get(i)][seq.get(i + 1)];
                reward += instance.rewards[seq.get(i)];
            }
            time += instance.serviceTime[instance.end];
            reward += instance.rewards[instance.end];

            if (time > instance.timeBudget) {
                throw new RuntimeException("Time budget exceeded (" + time + " > " + instance.timeBudget + ")");
            }

            return reward;
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append("Cost: ");
            b.append(compute());
            b.append("\n");
            b.append(seq);
            return b.toString();
        }

    }

}
