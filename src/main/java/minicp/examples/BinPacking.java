package minicp.examples;

import minicp.cp.BranchingScheme;
import minicp.cp.Factory;
import minicp.engine.core.BoolVar;
import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.DFSearch;
import minicp.search.SearchStatistics;
import minicp.util.Procedure;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static minicp.cp.Factory.*;

public class BinPacking {

    // return the maximum value among the fixed variables of x
    public static Optional<Integer> maxBound(IntVar [] x) {
        return Stream.of(x).filter(var -> var.isFixed()).map(var -> var.min()).max(Integer::compareTo);
    }

    // return the first unfixed variable of x
    public static OptionalInt firstIndexNotBound(IntVar [] x) {
        return IntStream.range(0, x.length).filter(i -> !x[i].isFixed()).findFirst();
    }

    public static void main(String[] args) {

        int capa = 9;
        //int [] items = new int[] {2,2,4,4,5,5,5,5,6,7}; // infeasible
        int [] items = new int[] {2,2,2,2,4,4,5,5,5,6,7};
        int nBins = 5;
        int nItems = items.length;

        Solver cp = makeSolver();
        IntVar []  x = makeIntVarArray(cp, nItems,nBins);
        IntVar []  l = makeIntVarArray(cp, nBins,capa+1);


        BoolVar [][] inBin = new BoolVar[nBins][nItems]; // inBin[j][i] = 1 if item i is placed in bin j

        for (int j = 0; j < nBins; j++) {
            for (int i = 0; i < nItems; i++) {
                inBin[j][i] = isEqual(x[i], j);
            }
        }

        // bin packing constraint
        for (int j = 0; j < nBins; j++) {
            IntVar[] wj = new IntVar[nItems];
            for (int i = 0; i < nItems; i++) {
                wj[i] = mul(inBin[j][i], items[i]);
            }
            cp.post(sum(wj, l[j]));
        }

        // redundant constraint : sum of bin load = sum of item weights
        cp.post(sum(l, IntStream.of(items).sum()));

        // break symmetries imposing increasing loads
        for (int j = 0; j < nBins - 1; j++) {
            cp.post(lessOrEqual(l[j],l[j+1]));
        }

        int [] values = new int[nBins];

        DFSearch dfs = makeDfs(cp, () -> {
            final int item = firstIndexNotBound(x).orElse(-1);
            if (item == -1) {
                return new Procedure[0];
            }
            else {
                int maxUsedBin = maxBound(x).orElse(-1); // index max used bin
                List<Procedure> branches = new LinkedList<>();
                // dynamic symmetry breaking: branch on at most one empty bin
                for (int j = 0; j <= Math.max(maxUsedBin + 1, nBins - 1); j++) {
                    if (x[item].contains(j)) {
                        final int bin = j;
                        branches.add(() -> cp.post(equal(x[item],bin)));
                    }
                }
                return branches.toArray(new Procedure[]{});
            }
        });

        dfs.onSolution(() -> {
            System.out.println("---"+firstIndexNotBound(x));
            System.out.println(Arrays.toString(x));
            System.out.println(Arrays.toString(l));
        });
        SearchStatistics stats = dfs.solve(s -> s.numberOfSolutions() >= 1);
        System.out.println(stats);


    }
}
