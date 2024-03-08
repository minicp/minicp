package minicp.examples;

import minicp.search.SearchStatistics;
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import org.javagrader.ConditionalOrderingExtension;
import org.javagrader.Grade;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;


@Grade
@ExtendWith(ConditionalOrderingExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StableMatchingTest {

    private static class Solution {
        int[] company;
        int[] student;
        Solution(StableMatching sm) {
            this.company = new int[sm.company.length];
            this.student = new int[sm.student.length];
            for (int i = 0 ; i < company.length ; ++i) {
                assertTrue(sm.company[i].isFixed(), "One variable is not fixed");
                this.company[i] = sm.company[i].min();
            }
            for (int i = 0 ; i < student.length ; ++i) {
                assertTrue(sm.company[i].isFixed(), "One variable is not fixed");
                this.student[i] = sm.student[i].min();
            }
            for (int c = 0; c < sm.n; c++) {
                for (int s = 0; s < sm.n; s++) {
                    if (sm.rankStudents[c][this.student[c]] > sm.rankStudents[c][s]) {
                        assertTrue(sm.rankCompanies[s][this.company[s]] < sm.rankCompanies[s][c],
                                String.format("Company %s with student %d prefers student %d.", c, this.student[c], s));
                    }
                }
            }
            for (int s = 0; s < sm.n; s++) {
                for (int c = 0; c < sm.n; c++) {
                    if (sm.rankCompanies[s][this.company[s]] > sm.rankCompanies[s][c]) {
                        assertTrue(sm.rankStudents[c][this.student[c]] < sm.rankStudents[c][s],
                                String.format("Student %s at company %d prefers company %d.", s, this.company[s], c));
                    }
                }
            }
        }

        @Override
        public String toString() {
            String companyString = Arrays.stream(company).mapToObj(Integer::toString).collect(Collectors.joining(","));
            String studentString = Arrays.stream(student).mapToObj(Integer::toString).collect(Collectors.joining(","));
            return String.format("company: %s\nstudent: %s\n", companyString, studentString);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Solution solution = (Solution) o;
            return Arrays.equals(company, solution.company) && Arrays.equals(student, solution.student);
        }

        @Override
        public int hashCode() {
            int result = Arrays.hashCode(company);
            result = 31 * result + Arrays.hashCode(student);
            return result;
        }
    }

    @Grade(cpuTimeout = 500, unit = TimeUnit.MILLISECONDS)
    @Test
    @Order(1)
    @Tag("slow")
    public void testAllSolutionsFound() {
        try {
            String instance = "data/stable_matching.txt";
            StableMatching model = new StableMatching(instance);
            model.buildModel();
            HashSet<Solution> sols = new HashSet<>();
            model.dfs.onSolution(() -> {
                Solution sol = new Solution(model);
                assertFalse(sols.contains(sol), "You found 2 times the solution \n" + sol);
                sols.add(sol);
            });
            SearchStatistics searchStatistics = model.solve(false);
            assertTrue(searchStatistics.isCompleted(), "You need to explore the entire search space");
            assertEquals(6, searchStatistics.numberOfSolutions(), "You did not find all solutions");
            assertEquals(6, sols.size(), "You did not find all solutions");
        } catch (InconsistencyException | NullPointerException e) {
            fail("No inconsistency should happen when creating the constraints and performing the search " + e);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    @Grade(cpuTimeout = 500, unit = TimeUnit.MILLISECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    @ParameterizedTest
    @MethodSource("getSmallInstances")
    @Order(2)
    public void testValidFirstFewSolutions(String instance) {
        try {
            int nSolutions = 4;
            StableMatching model = new StableMatching(instance);
            model.buildModel();
            HashSet<Solution> sols = new HashSet<>();
            model.dfs.onSolution(() -> {
                Solution sol = new Solution(model);
                assertFalse(sols.contains(sol), "You found 2 times the solution \n" + sol);
                sols.add(sol);
            });
            SearchStatistics searchStatistics = model.solve(false, s -> s.numberOfSolutions() >= nSolutions);
            assertTrue(searchStatistics.numberOfSolutions() >= 2, "You did not give enough solutions");
            assertTrue(searchStatistics.numberOfSolutions() <= nSolutions, "You should give at most " + nSolutions + " solutions");
            assertEquals(searchStatistics.numberOfSolutions(), sols.size(), "You did not find all solutions");
            // check that all solutions are different
        } catch (InconsistencyException | NullPointerException e) {
            fail("No inconsistency should happen when creating the constraints and performing the search " + e);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }
    
    public static Stream<Arguments> getSmallInstances() {
        return Stream.of(Objects.requireNonNull(new File("data/stable_matching/").listFiles()))
                .filter(file -> !file.isDirectory() && file.getName().startsWith("smp_01"))
                .map(s -> arguments(named(s.getName(), s.getPath())));
    }


}
