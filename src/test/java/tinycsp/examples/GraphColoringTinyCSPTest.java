package tinycsp.examples;

import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.NotImplementedException;
import org.javagrader.Grade;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Grade(cpuTimeout = 1)
public class GraphColoringTinyCSPTest {

    @ParameterizedTest
    @MethodSource("getInstancePaths")
    public void testSolve(String path) {
        GraphColoringTinyCSP.GraphColoringInstance instance = readInstance(path);
        try {
            int[] solution = GraphColoringTinyCSP.solve(instance);
            assertNotNull(solution);
            for (int[] edge : instance.edges) {
                int i = edge[0];
                int j = edge[1];
                assertNotEquals(solution[i], solution[j]);
            }
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }

    /**
     * Read the instance at the specified path
     * @param file the path to the instance file
     * @return the instance
     */
    public static GraphColoringTinyCSP.GraphColoringInstance readInstance(String file) {

        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int n = scanner.nextInt();
        int e = scanner.nextInt();
        int nCol = scanner.nextInt();

        List<int []> edges = new LinkedList<>();

        for (int i = 0; i < e; i++) {
            int source = scanner.nextInt();
            int dest = scanner.nextInt();
            edges.add(new int[] {source, dest});
        }
        return new GraphColoringTinyCSP.GraphColoringInstance(n,edges,nCol);

    }

    public static Stream<Arguments> getInstancePaths() {
        //I want my code to read input.txt line by line and feed the input in an arraylist so that it returns an equivalent of the code below
        List<String> files = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            files.add("data/graph_coloring/gc_15_30_"+i);
        }
        return files.stream().map(file -> arguments(named(new File(file).getName(), file)));
    }
}