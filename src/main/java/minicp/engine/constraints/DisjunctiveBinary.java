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


package minicp.engine.constraints;

import minicp.engine.core.AbstractConstraint;
import minicp.engine.core.BoolVar;
import minicp.engine.core.IntVar;
import minicp.util.exception.NotImplementedException;

import static minicp.cp.Factory.*;

/**
 * Constraint enforcing that two activities cannot overlap in time
 *
 * The implementation of this constraint uses reified constraints.
 */
public class DisjunctiveBinary extends AbstractConstraint implements Comparable<DisjunctiveBinary> {

    // before = true if activity 1 << activity2, after = true if activity 1 >> activity2 (one of the two must be true)
    private BoolVar before, after;
    private IntVar start1, start2;
    private IntVar end1, end2;

    int[] duration;

    /**
     * Constraint enforcing that two activities cannot overlap
     * @param start1 start time of activity 1
     * @param duration1 duration of activity 1
     *
     */
    public DisjunctiveBinary(IntVar start1, int duration1, IntVar start2, int duration2) {
        super(start1.getSolver());
        this.start1 = start1;
        this.start2 = start2;
        this.end1 = plus(start1,duration1);
        this.end2 = plus(start2,duration2);
        this.before = makeBoolVar(getSolver());
        this.after = not(before);
    }

    @Override
    public void post() {
        // TODO : post binary decomposition using IsLessOrEqualVar (reified constraints)
        // one of the two activities must precede the other one
         throw new NotImplementedException("DisjunctiveBinary");
    }

    /**
     * Tells if the decision of which activity should come first as already been fixed
     * @return if the decision of which activity should come first as already been fixed
     */
    public boolean isFixed() {
        return before.isFixed();
    }

    /**
     * The total slack (estimation of degree of freedom)
     * @return the sum of domain sizes of both activities
     */
    public int slack() {
        return start1.size() + start2.size();
    }

    /**
     * The total slack if activity 1 would be placed before activity 2
     * @return slack if activity 1 would be placed before activity 2
     */
    public int slackIfBefore() {
        int slack1 = Math.min(start2.max() - 1, start1.max()) - start1.min();
        int slack2 = start2.max() - Math.max(start2.min(), end1.min());
        return slack1 + slack2;
    }

    /**
     * The total slack if activity 1 would be placed after activity 2
     * @return slack if activity 1 would be placed after activity 2
     */
    public int slackIfAfter() {
        int slack2 = Math.min(start1.max() - 1, start2.max()) - start2.min();
        int slack1 = start1.max() - Math.max(start1.min(), end2.min());
        return slack1 + slack2;
    }

    /**
     * The boolean variable telling if activity 1 comes before activity 2
     * @return the boolean variable telling if activity 1 comes before activity 2
     */
    public BoolVar before() {
        return before;
    }

    /**
     * The boolean variable telling if activity 1 comes after activity 2
     * @return the boolean variable telling if activity 1 comes after activity 2
     */
    public BoolVar after() {
        return after;
    }

    /**
     * A comparator by increasing slack value
     * @param o the object to be compared.
     * @return slack() - o.slack()
     */
    @Override
    public int compareTo(DisjunctiveBinary o) {
        return slack() - o.slack();
    }
}
