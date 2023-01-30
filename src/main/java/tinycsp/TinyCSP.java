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

package tinycsp;

import java.util.*;
import java.util.function.Consumer;

public class TinyCSP {

    public static int nRecur = 0;

    /* constraints of the CSP */
    List<Constraint> constraints = new LinkedList<>();
    /* variables of the CSP */
    List<Variable> variables = new LinkedList<>();

    /**
     * Create a variable
     *
     * @param domSize the number of values in the domain
     * @return a variable with domain {0..domSize-1}
     */
    public Variable makeVariable(int domSize) {
        Variable x = new Variable(domSize);
        variables.add(x);
        return x;
    }

    public void notEqual(Variable x, Variable y, int offset) {
        constraints.add(new NotEqual(x, y, offset));
        fixPoint();
    }

    public void fixPoint() {
        boolean fix = false;
        while (!fix) {
            fix = true;
            for (Constraint c : constraints) {
                fix &= !c.propagate();
            }
        }
    }

    private ArrayList<Domain> backupDomains() {
        ArrayList<Domain> backup = new ArrayList<>();
        for (Variable x : variables) {
            backup.add(x.dom.clone());
        }
        return backup;
    }

    private void restoreDomains(ArrayList<Domain> backup) {
        for (int i = 0; i < variables.size(); i++) {
            variables.get(i).dom = backup.get(i);
        }
    }

    Optional<Variable> firstNotFixed() {
        return variables.stream().filter(x -> !x.dom.isFixed()).findFirst();
    }

    Optional<Variable> smallestNotFixed() {
        int min = Integer.MAX_VALUE;
        Variable y = null;
        for (Variable x : variables) {
            if (!x.dom.isFixed() && x.dom.size() < min) {
                y = x;
                min = y.dom.size();
            }
        }
        return y == null ? Optional.empty() : Optional.of(y);
    }

    public void dfs(Consumer<int[]> onSolution) {

        nRecur += 1;

        // pickup a variable that is not yet fixed if any
        Optional<Variable> notFixed = firstNotFixed();
        //Optional<Variable> notFixed = smallestNotFixed();
        if (!notFixed.isPresent()) { // all variables fixed, a solution is found
            int[] solution = variables.stream().mapToInt(x -> x.dom.min()).toArray();
            onSolution.accept(solution);
        } else {
            Variable y = notFixed.get(); // take the unfixed variable

            int v = y.dom.min();
            ArrayList<Domain> backup = backupDomains();
            // left branch x = v
            try {
                y.dom.fix(v);
                fixPoint();
                dfs(onSolution);
            } catch (Inconsistency i) {
            }

            restoreDomains(backup);

            // right branch x != v
            try {
                y.dom.remove(v);
                fixPoint();
                dfs(onSolution);
            } catch (Inconsistency i) {
            }
        }
    }

    static class Inconsistency extends RuntimeException {

    }


}
