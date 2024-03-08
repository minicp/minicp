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

package minicp.engine;


import minicp.engine.core.MiniCP;
import minicp.engine.core.Solver;
import minicp.state.Copier;
import minicp.state.Trailer;
import org.javagrader.Allow;
import org.javagrader.Grade;
import org.junit.jupiter.params.provider.Arguments;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Allow("all")
public abstract class SolverTest {

    public static Stream<Solver> getSolver() {
        return Stream.of(new MiniCP(new Trailer()), new MiniCP(new Copier()));
    }

    public static Stream<Arguments> solverSupplier() {
        return Stream.of(
                arguments(named(
                        new MiniCP(new Trailer()).toString(),
                        (Supplier<Solver>) () -> new MiniCP(new Trailer()))),
                arguments(named(
                        new MiniCP(new Copier()).toString(),
                        (Supplier<Solver>) () -> new MiniCP(new Copier()))));
    }

}
