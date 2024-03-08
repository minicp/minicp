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

package minicp.examples;

import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.util.exception.InconsistencyException;

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

import static minicp.cp.Factory.makeSolver;

public class JobShopInstance {

    int nJobs;
    int nMachines;
    String name;

    int [][] duration;
    int [][] machine;
    int horizon;

    /**
     * Read the job-shop instance from the specified file
     * @param file
     */
    public JobShopInstance(String file) {
        try {
            FileInputStream istream = new FileInputStream(file);
            name = new File(file).getName();
            BufferedReader in = new BufferedReader(new InputStreamReader(istream));
            in.readLine();
            in.readLine();
            in.readLine();
            StringTokenizer tokenizer = new StringTokenizer(in.readLine());
            nJobs = Integer.parseInt(tokenizer.nextToken());
            nMachines = Integer.parseInt(tokenizer.nextToken());

            duration = new int[nJobs][nMachines];
            machine = new int[nJobs][nMachines];
            horizon = 0;
            for (int i = 0; i < nJobs; i++) {
                tokenizer = new StringTokenizer(in.readLine());
                for (int j = 0; j < nMachines; j++) {
                    machine[i][j] = Integer.parseInt(tokenizer.nextToken());
                    duration[i][j] = Integer.parseInt(tokenizer.nextToken());
                    horizon += duration[i][j];
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InconsistencyException e) {
            e.printStackTrace();
        }
    }

    /**
     * Collect the variables related to the specified machine
     *
     * @param variables is nJobs x nMachine matrix
     * @param m         a machine index
     * @return an array containing all the variables[i][j] such that machine[i][j] == m
     */
    public IntVar[] collect(IntVar[][] variables, int m) {
        ArrayList<IntVar> res = new ArrayList<IntVar>();
        for (int i = 0; i < nJobs; i++) {
            for (int j = 0; j < nMachines; j++) {
                if (machine[i][j] == m) {
                    res.add(variables[i][j]);
                }
            }
        }
        return res.toArray(new IntVar[]{});
    }

    public int[] collect(int[][] data, int m) {
        ArrayList<Integer> res = new ArrayList<Integer>();
        for (int i = 0; i < nJobs; i++) {
            for (int j = 0; j < nMachines; j++) {
                if (machine[i][j] == m) {
                    res.add(data[i][j]);
                }
            }
        }
        return res.stream().mapToInt(i -> i).toArray();
    }


}
