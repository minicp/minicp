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

import minicp.engine.constraints.Element1D;
import minicp.engine.constraints.Element1DVar;
import minicp.engine.core.BoolVar;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.SearchStatistics;
import minicp.util.io.InputReader;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static minicp.cp.BranchingScheme.and;
import static minicp.cp.BranchingScheme.firstFail;
import static minicp.cp.Factory.*;
import minicp.util.exception.NotImplementedException;

/**
 * Stable Matching problem:
 * Given n students and n companies, where each student (resp. company) has
 * ranked each company (resp. student) with a unique number between 1 and n
 * in order of preference (the lower the number, the higher the preference),
 * say for summer internships, match the students and companies such that
 * there is no pair of a student and a company who would both prefer to be
 * matched with each other than with their actually matched ones.
 * If there are no such pairs, then the matching is said to be stable.
 * <a href="https://en.wikipedia.org/wiki/Stable_matching_problem">Wikipedia</a>.
 */
public class StableMatching extends SatisfactionProblem {
    public final int n;
    public final int[][] rankCompanies;
    public final int[][] rankStudents;
    public IntVar[] student;
    public IntVar[] company;
    public IntVar[] studentPref;
    public IntVar[] companyPref;
    String instance;
    public StableMatching(String instanceFilePath) {
        InputReader reader = new InputReader(instanceFilePath);
        instance = reader.getFilename();

        n = reader.getInt();
        rankCompanies = reader.getMatrix(n, n);
        rankStudents = reader.getMatrix(n, n);
    }


    @Override
    public void buildModel() {
        Solver cp = makeSolver();

        // company[s] is the company chosen for student s
        company = makeIntVarArray(cp, n, n);
        // student[c] is the student chosen for company c
        student = makeIntVarArray(cp, n, n);

        // companyPref[s] is the preference of student s for the company chosen for s
        companyPref = makeIntVarArray(cp, n, 1, n);
        // studentPref[c] is the preference of company c for the student chosen for c
        studentPref = makeIntVarArray(cp, n, 1, n);


        for (int s = 0; s < n; s++) {
            // TODO: model this with Element1DVar: the student of the company of student s is s
            
            // TODO: model this with Element1D: rankCompanies[s][company[s]] = companyPref[s]
            

        }

        for (int c = 0; c < n; c++) {
            // TODO: model this with Element1DVar: the company of the student of company c is c
            
            // TODO: model this with Element1D: rankStudents[c][student[c]] = studentPref[c]
            
        }

        for (int s = 0; s < n; s++) {
            for (int c = 0; c < n; c++) {
                // if student s prefers company c over the chosen company, then the opposite is not true: c prefers their chosen student over s
                // (companyPref[s] > rankCompanies[s][c]) => (studentPref[c] < rankStudents[c][s])

                BoolVar sPrefersC = isLarger(companyPref[s], rankCompanies[s][c]);
                BoolVar cDoesnot = isLess(studentPref[c], rankStudents[c][s]);
                cp.post(implies(sPrefersC, cDoesnot));

                // if company c prefers student s over their chosen student, then the opposite is not true: s prefers the chosen company over c
                // (studentPref[c] > rankStudents[c][s]) => (companyPref[s] < rankCompanies[s][c])
                // TODO: model this constraint
                

            }
        }

        dfs = makeDfs(cp, and(firstFail(company), firstFail(student)));
        // TODO add the constraints to the model and remove the NotImplementedException
         throw new NotImplementedException("StableMatching");
    }

    public SearchStatistics solve(boolean verbose, Predicate<SearchStatistics> limit) {
        SearchStatistics stats = dfs.solve(limit);
        if (verbose) {
            System.out.println(stats);
        }
        return stats;
    }

    @Override
    public String toString() {
        return "StableMatching(" + instance + ')';
    }

    /**
     * Model the reified logical implication constraint
     * @param b1 left-hand side of the implication
     * @param b2 right-hand side of the implication
     * @return a boolean variable that is true if and only if
     *         the relation "b1 implies b2" is true, and false otherwise.
     */
    private static BoolVar implies(BoolVar b1, BoolVar b2) {
        return isLargerOrEqual(sum(not(b1), b2), 1);
    }

    public static void main(String[] args) {

        // also use the instances at data/stable_matching/
        StableMatching stableMatching = new StableMatching("data/stable_matching.txt");

        // you should get six solutions:
        /*
        company: 5,3,8,7,2,6,0,4,1
        student: 6,8,4,1,7,0,5,3,2

        company: 5,4,8,7,2,6,0,3,1
        student: 6,8,4,7,1,0,5,3,2

        company: 5,0,3,7,4,8,2,1,6
        student: 1,7,6,2,4,0,8,3,5

        company: 5,0,3,7,4,6,2,1,8
        student: 1,7,6,2,4,0,5,3,8

        company: 5,3,0,7,4,6,2,1,8
        student: 2,7,6,1,4,0,5,3,8

        company: 6,4,8,7,2,5,0,3,1
        student: 6,8,4,7,1,5,0,3,2
        */
        stableMatching.buildModel();
        // print the layout of the solutions
        stableMatching.dfs.onSolution(() -> {
            String companyString = Arrays.stream(stableMatching.company).map(v -> Integer.toString(v.min())).collect(Collectors.joining(","));
            System.out.println("company: " + companyString);
            String studentString = Arrays.stream(stableMatching.student).map(v -> Integer.toString(v.min())).collect(Collectors.joining(","));
            System.out.println("student: " + studentString + "\n");
        });
        stableMatching.solve(true,  s -> false);
    }
}

