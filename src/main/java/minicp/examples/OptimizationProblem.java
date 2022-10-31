package minicp.examples;

import minicp.search.DFSearch;
import minicp.search.Objective;
import minicp.search.SearchStatistics;

import java.util.function.Predicate;

public abstract class OptimizationProblem {

    public Objective objective;
    public DFSearch dfs;

    /**
     * Creates a model for the optimization
     * A CP model is composed of the variables + a search procedure
     * This method should set the values for {@link OptimizationProblem#objective} and {@link OptimizationProblem#dfs}
     * such that {@link OptimizationProblem#solve()} can be called afterwards
     */
    public abstract void buildModel();

    /**
     * Runs the full search defined in the {@link OptimizationProblem#dfs}
     */
    public void solve() {
        solve(false, statistics -> false);
    }

    /**
     * Runs the full search defined in the {@link OptimizationProblem#dfs} and possibly prints each found solution
     *
     * @param verbose if true, prints the objective value each time a solution is found
     */
    public SearchStatistics solve(boolean verbose) {
        return solve(verbose, statistics -> false);
    }

    /**
     * Runs the search defined in the {@link OptimizationProblem#dfs}
     *
     * @param limit a predicate called at each node that stops the search when it becomes true
     */
    public SearchStatistics solve(Predicate<SearchStatistics> limit) {
        return solve(false, limit);
    }

    /**
     * Runs the search defined in the {@link OptimizationProblem#dfs} and possibly prints each found solution
     *
     * @param verbose if true, prints the objective value each time a solution is found
     * @param limit a predicate called at each node that stops the search when it becomes true
     * @return statistics related to the search
     */
    public SearchStatistics solve(boolean verbose, Predicate<SearchStatistics> limit) {
        if (verbose)
            dfs.onSolution(() -> System.out.println(objective));
        return dfs.optimize(objective, limit);
    }

}
