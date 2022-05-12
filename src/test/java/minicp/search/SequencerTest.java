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

package minicp.search;

import com.github.guillaumederval.javagrading.Grade;
import com.github.guillaumederval.javagrading.GradeClass;
import minicp.state.StateInt;
import minicp.state.StateManager;
import minicp.state.StateManagerTest;
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.Procedure;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static minicp.cp.BranchingScheme.EMPTY;
import static minicp.cp.BranchingScheme.branch;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


@GradeClass(totalValue=1, defaultValue=1, allCorrect=true)
public class SequencerTest extends StateManagerTest {

    /**
     * exert the Sequencer in a BFS-way
     */
    @Test
    @Grade
    public void testExample1() {
        try {
            Sequencer seq = new Sequencer(SequencerTest::fakeSequencer0, SequencerTest::fakeSequencer1, SequencerTest::fakeSequencer2);

            state = 0;
            Procedure[] branches = seq.get();
            assertEquals(2, branches.length);
            branches[0].call();
            assertEquals(1, state);
            branches[1].call();
            assertEquals(2, state);

            state = 1;
            branches = seq.get();
            assertEquals(2, branches.length);
            branches[0].call();
            assertEquals(3, state);
            branches[1].call();
            assertEquals(4, state);

            state = 2;
            branches = seq.get();
            assertEquals(2, branches.length);
            branches[0].call();
            assertEquals(5, state);
            branches[1].call();
            assertEquals(6, state);

            state = 4;
            branches = seq.get();
            assertEquals(3, branches.length);
            branches[0].call();
            assertEquals(7, state);
            branches[1].call();
            assertEquals(8, state);
            branches[2].call();
            assertEquals(9, state);

            for(int s: new int[]{3, 5, 6, 7, 8, 9}) {
                state = s;
                branches = seq.get();
                assertEquals(0, branches.length);
            }
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    /**
     * exert the Sequencer in a DFS-way
     */
    @Test
    @Grade
    public void testExample2() {
        try {
            Sequencer seq = new Sequencer(SequencerTest::fakeSequencer0, SequencerTest::fakeSequencer1, SequencerTest::fakeSequencer2);

            state = 0;
            Procedure[] branches = seq.get();
            assertEquals(2, branches.length);
            branches[0].call();
            assertEquals(1, state);
            branches[1].call();
            assertEquals(2, state);

            state = 1;
            branches = seq.get();
            assertEquals(2, branches.length);
            branches[0].call();
            assertEquals(3, state);
            branches[1].call();
            assertEquals(4, state);

            state = 4;
            branches = seq.get();
            assertEquals(3, branches.length);
            branches[0].call();
            assertEquals(7, state);
            branches[1].call();
            assertEquals(8, state);
            branches[2].call();
            assertEquals(9, state);

            state = 2;
            branches = seq.get();
            assertEquals(2, branches.length);
            branches[0].call();
            assertEquals(5, state);
            branches[1].call();
            assertEquals(6, state);

            for(int s: new int[]{3, 5, 6, 7, 8, 9}) {
                state = s;
                branches = seq.get();
                assertEquals(0, branches.length);
            }
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    private static int state = 0;
    private static Procedure[] fakeSequencer0() {
        if(state == 0)
            return new Procedure[]{() -> {state = 1;}, () -> {state = 2;}};
        return EMPTY;
    }

    private static Procedure[] fakeSequencer1() {
        if(state == 1)
            return new Procedure[]{() -> {state = 3;}, () -> {state = 4;}};
        else if(state == 2)
            return new Procedure[]{() -> {state = 5;}, () -> {state = 6;}};
        return EMPTY;
    }

    private static Procedure[] fakeSequencer2() {
        if(state == 2)
            return new Procedure[]{() -> {state = 10;}};
        if(state == 4)
            return new Procedure[]{() -> {state = 7;}, () -> {state = 8;}, () -> {state = 9;}};
        return EMPTY;
    }
}
