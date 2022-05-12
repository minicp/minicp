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

package minicp.engine.core;

import com.github.guillaumederval.javagrading.Grade;
import minicp.engine.SolverTest;
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static minicp.cp.Factory.*;
import static org.junit.Assert.*;


public class BoolVarTest extends SolverTest {

    public boolean propagateCalled = false;

    @Test
    public void testBoolVar() {
        Solver cp = solverFactory.get();

        BoolVar b = makeBoolVar(cp);

        BoolVar notB = new BoolVarImpl(new IntVarViewOffset(new IntVarViewOpposite(b), 1)); // 1 - b

        assertTrue(!b.isFixed());
        assertTrue(!notB.isFixed());

        b.fix(false);

        assertTrue(b.isFalse());
        assertTrue(notB.isTrue());


    }


}
