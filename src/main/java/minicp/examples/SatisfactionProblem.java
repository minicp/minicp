package minicp.examples;

import minicp.search.DFSearch;
import minicp.search.SearchStatistics;

import java.util.function.Predicate;

/**
 * A constraint problem with no associated objective
 */
public abstract class SatisfactionProblem {

    public DFSearch dfs;
    private int nSolutions = 0;

    public int getNSolutions() {
        return nSolutions;
    }

    public void resetNSolutions() {
        this.nSolutions = 0;
    }

    /**
     * Creates a model for the optimization
     * A CP model is composed of the variables + a search procedure
     * This method should set the values for {@link SatisfactionProblem#dfs}
     * such that {@link SatisfactionProblem#solve()} can be called afterwards
     */
    public abstract void buildModel();

    /**
     * Runs the full search defined in the {@link SatisfactionProblem#dfs}
     */
    public void solve() {
        solve(false, statistics -> false);
    }

    /**
     * Runs the full search defined in the {@link SatisfactionProblem#dfs} and possibly prints each found solution
     *
     * @param verbose if true, prints the number of found solution each time a new one is found
     */
    public SearchStatistics solve(boolean verbose) {
        return solve(verbose, statistics -> false);
    }


    /**
     * Runs the search defined in the {@link SatisfactionProblem#dfs} and possibly prints each found solution
     *
     * @param limit a predicate called at each node that stops the search when it becomes true
     */
    public SearchStatistics solve(Predicate<SearchStatistics> limit) {
        return solve(false, limit);
    }

    /**
     * Runs the search defined in the {@link SatisfactionProblem#dfs} and possibly prints each found solution
     *
     * @param verbose if true, prints the number of found solutions each time a new one is found
     * @param limit a predicate called at each node that stops the search when it becomes true
     * @return statistics related to the search
     */
    public SearchStatistics solve(boolean verbose, Predicate<SearchStatistics> limit) {
        dfs.onSolution(() -> nSolutions += 1);
        if (verbose)
            dfs.onSolution(() -> System.out.println("Solutions found: " + nSolutions));
        return dfs.solve(limit);
    }

}
