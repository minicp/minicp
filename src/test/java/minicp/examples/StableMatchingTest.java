package minicp.examples;

import com.github.guillaumederval.javagrading.Grade;
import com.github.guillaumederval.javagrading.GradeClass;
import minicp.search.SearchStatistics;
import minicp.util.DataPermissionFactory;
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.InconsistencyException;
import minicp.util.exception.NotImplementedException;
import org.junit.Test;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@GradeClass(totalValue = 1)
public class StableMatchingTest {

    private static class Solution {
        int[] company;
        int[] student;
        Solution(StableMatching sm) {
            this.company = new int[sm.company.length];
            this.student = new int[sm.student.length];
            for (int i = 0 ; i < company.length ; ++i) {
                assertTrue("One variable is not fixed", sm.company[i].isFixed());
                this.company[i] = sm.company[i].min();
            }
            for (int i = 0 ; i < student.length ; ++i) {
                assertTrue("One variable is not fixed", sm.company[i].isFixed());
                this.student[i] = sm.student[i].min();
            }
            for (int c = 0; c < sm.n; c++) {
                for (int s = 0; s < sm.n; s++) {
                    if (sm.rankStudents[c][this.student[c]] > sm.rankStudents[c][s]) {
                        assertTrue(String.format("Company %s with student %d prefers student %d.", c, this.student[c], s),
                                sm.rankCompanies[s][this.company[s]] < sm.rankCompanies[s][c]);
                    }
                }
            }
            for (int s = 0; s < sm.n; s++) {
                for (int c = 0; c < sm.n; c++) {
                    if (sm.rankCompanies[s][this.company[s]] > sm.rankCompanies[s][c]) {
                        assertTrue(String.format("Student %s at company %d prefers company %d.", s, this.company[s], c),
                                sm.rankStudents[c][this.student[c]] < sm.rankStudents[c][s]);
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

    @Test(timeout = 1500)
    @Grade(cpuTimeout = 500, customPermissions = DataPermissionFactory.class)
    public void testAllSolutionsFound() {
        try {
            String instance = "data/stable_matching.txt";
            StableMatching model = new StableMatching(instance);
            model.buildModel();
            HashSet<Solution> sols = new HashSet<>();
            model.dfs.onSolution(() -> {
                Solution sol = new Solution(model);
                assertFalse("You found 2 times the solution \n" + sol, sols.contains(sol));
                sols.add(sol);
            });
            SearchStatistics searchStatistics = model.solve(false);
            assertTrue("You need to explore the entire search space", searchStatistics.isCompleted());
            assertEquals("You did not find all solutions", 6, searchStatistics.numberOfSolutions());
            assertEquals("You did not find all solutions", 6, sols.size());
            // check that all solutions are different
        } catch (InconsistencyException | NullPointerException e) {
            fail("No inconsistency should happen when creating the constraints and performing the search " + e);
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


}
