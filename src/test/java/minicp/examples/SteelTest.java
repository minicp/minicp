package minicp.examples;

import minicp.engine.core.IntVar;
import minicp.search.SearchStatistics;
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import org.javagrader.ConditionalOrderingExtension;
import org.javagrader.Grade;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static minicp.cp.BranchingScheme.firstFail;
import static minicp.cp.Factory.makeDfs;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Grade
@ExtendWith(ConditionalOrderingExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SteelTest {

    public int assertValidSolution(Steel steel) {
        for (int i = 0 ; i < steel.x.length; ++i)
            assertTrue(steel.x[i].isFixed(), String.format("Order %d is not fixed (it has no slab assigned to it)", i));
        for (int i = 0 ; i < steel.l.length; ++i)
            assertTrue(steel.l[i].isFixed(), String.format("The loss of slab %d is not fixed", i));
        assertTrue(steel.totLoss.isFixed(), "The total loss is not fixed");
        for (int order = 0 ; order < steel.nOrder ; ++order) {
            int slabAssigned = -1;
            for (int slab = 0 ; slab < steel.nSlab ; ++slab) {
                assertTrue(steel.inSlab[slab][order].isFixed(), String.format("inSlab[%d][%d] is not fixed in your solution", slab, order));
                if (steel.inSlab[slab][order].isTrue()) {
                    assertEquals(-1, slabAssigned, String.format("inSlab[..][%d] can only be true at one index but was true at inSlab[..][%d] and inSlab[..][%d]", order, slabAssigned, slab));
                    slabAssigned = slab;
                }
            }
            assertNotEquals(-1, slabAssigned, String.format("inSlab[..][%d] has no element assigned to true", order));
        }
        int sumElements = IntStream.of(steel.w).sum();
        int sumLoad = 0;
        for (IntVar l: steel.l) {
            sumLoad += l.min();
        }
        assertEquals(sumLoad, sumElements, "The sum of load should be equal to the sum of elements");
        int sumLoss = 0;
        for (int i = 0 ; i < steel.l.length; ++i) {
            sumLoss += steel.loss[steel.l[i].min()];
        }
        assertEquals(sumLoss, steel.totLoss.min(), "Your total loss does not correspond to the sum of the losses");
        return sumLoss;
    }

    public void assertNColorsInSlabIsMax2(Steel steel) {
        HashSet<Integer> colors = new HashSet<>();
        for (int slab = 0 ; slab < steel.nSlab ; ++slab) {
            colors.clear();
            for (int order = 0; order < steel.nOrder; ++order) {
                assertTrue(steel.inSlab[slab][order].isFixed(), String.format("inSlab[%d][%d] is not fixed in your solution", slab, order));
                if (steel.inSlab[slab][order].isTrue()) {
                    colors.add(steel.c[order]);
                    assertTrue(colors.size() <= 2, String.format("There can be at most 2 colors within a slab but slab %d contained more than 2", slab));
                }
            }
        }
    }

    @Grade(value = 1, cpuTimeout = 2)
    @ParameterizedTest
    @MethodSource("getInstances")
    @Order(1)
    public void testValidModel1(String instance) {
        try {
            Steel steel = new Steel(instance);
            steel.buildModel();
            steel.dfs = makeDfs(steel.cp, firstFail(steel.x));
            AtomicBoolean seen = new AtomicBoolean(false);
            steel.dfs.onSolution(() -> {
                seen.set(true);
                assertNColorsInSlabIsMax2(steel);
            });
            SearchStatistics statistics = steel.dfs.solve(s -> s.numberOfSolutions() == 1);
            assertTrue(statistics.numberOfSolutions() > 0, "You did not find a solution");
            assertTrue(seen.get(), "You did not find a solution");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        } catch (InconsistencyException e) {
            fail("You have thrown an inconsistency although the instance given as input is valid");
        }
    }

    @Grade(value = 1, cpuTimeout = 2)
    @ParameterizedTest
    @MethodSource("getInstances")
    @Order(2)
    public void testValidModel2(String instance) {
        try {
            Steel steel = new Steel(instance);
            steel.buildModel();
            steel.dfs = makeDfs(steel.cp,firstFail(steel.x));
            AtomicBoolean seen = new AtomicBoolean(false);
            steel.dfs.onSolution(() -> {
                seen.set(true);
                assertNColorsInSlabIsMax2(steel);
                assertValidSolution(steel);
            });
            SearchStatistics statistics = steel.solve(false, s -> s.numberOfSolutions() == 1);
            assertTrue(statistics.numberOfSolutions() > 0, "You did not find a solution");
            assertTrue(seen.get(), "You did not find a solution");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        } catch (InconsistencyException e) {
            fail("You have thrown an inconsistency although the instance given as input is valid");
        }
    }

    @Grade(value = 3, cpuTimeout = 20)
    @ParameterizedTest
    @MethodSource("getInstances")
    @Order(3)
    @Tag("slow")
    public void testSymmetryBreaking(String instance) {
        try {
            Steel steel = new Steel(instance);
            steel.buildModel();
            AtomicInteger solution = new AtomicInteger(Integer.MAX_VALUE);
            steel.dfs.onSolution(() -> {
                assertNColorsInSlabIsMax2(steel);
                int value = assertValidSolution(steel);
                assertTrue(value < solution.get());
                solution.set(value);
            });
            SearchStatistics statistics = steel.solve(false);
            assertTrue(statistics.isCompleted());
            assertTrue(statistics.numberOfSolutions() > 0);
            assertEquals(0, solution.get(), "You did not a solution with a loss of 0");
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        } catch (InconsistencyException e) {
            fail("You have thrown an inconsistency although the instance given as input is valid");
        }
    }

    public static Stream<Arguments> getInstances() {
        return Stream.of("data/steel/bench_19_10")
                .map(f -> arguments(named(new File(f).getName(), f)));
    }
}
