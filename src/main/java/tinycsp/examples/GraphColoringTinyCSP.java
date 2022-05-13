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

package tinycsp.examples;



import tinycsp.TinyCSP;
import tinycsp.Variable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Example that illustrates how TinyCSP can be used to model
 * and solve the graph-coloring problem.
 */
public class GraphColoringTinyCSP {

    public static class GraphColoringInstance {

        public final int n;
        public final List<int []> edges;
        public final int maxColor;

        public GraphColoringInstance(int n, List<int []> edges, int maxColor) {
            this.n = n;
            this.edges = edges;
            this.maxColor = maxColor;
        }
    }


    public static void main(String[] args) {
        String path = "data/GC/easy/gc_15_30_9";
        GraphColoringInstance instance = readInstance(path);
        int [] solution= solve(instance);
        // writeSol(path+".sol",solution,instance.maxColor);
    }

    /**
     * Useful if you want to visualize your solution
     * @param file where you want to store the solution
     * @param sol the color of each vertex
     * @param nCol the number of colors used
     */
    public static void writeSol(String file, int [] sol, int nCol) {
        try {
            FileWriter fw = new FileWriter(file+".sol");
            fw.write(nCol+" "+1+"\n");

            for (int i = 0; i < sol.length; i++) {
                fw.write(sol[i]+" ");
            }

            fw.write("\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read the instance at the specified path
     * @param file the path to the instance file
     * @return the instance
     */
    public static GraphColoringInstance  readInstance(String file) {

        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int n = scanner.nextInt();
        int e = scanner.nextInt();
        int nCol = scanner.nextInt();

        List<int []> edges = new LinkedList<>();

        for (int i = 0; i < e; i++) {
            int source = scanner.nextInt();
            int dest = scanner.nextInt();
            edges.add(new int[] {source, dest});
        }
        return new GraphColoringInstance(n,edges,nCol);

    }

    /**
     * Solve the graph coloring problem
     * @param instance a graph coloring instance
     * @return the color of each node such that no two adjacent node receive a same color,
     *         or null if the problem is unfeasible
     */
    public static int[] solve(GraphColoringInstance instance) {
        // TODO: solve the graph coloring problem using TinyCSP and return a solution
        // Hint: you can stop the search on first solution throwing and catching a exception
        //       in the onSolution closure or you can modify the dfs search
         return null;
    }


}
