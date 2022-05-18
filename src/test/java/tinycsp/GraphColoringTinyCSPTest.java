package tinycsp;

import junit.framework.TestCase;
import minicp.util.NotImplementedExceptionAssume;
import minicp.util.exception.NotImplementedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tinycsp.examples.GraphColoringTinyCSP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

@RunWith(Parameterized.class)
public class GraphColoringTinyCSPTest extends TestCase {

    String path;

    public GraphColoringTinyCSPTest(String path){
        this.path = path;
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

    @Test
    public void testSolve() {
        GraphColoringTinyCSP.GraphColoringInstance instance = readInstance(path);
        try {
            int[] solution = GraphColoringTinyCSP.solve(instance);
            for (int[] edge : instance.edges) {
                int i = edge[0];
                int j = edge[1];
                assertTrue(solution[i] != solution[j]);
            }
        } catch (NotImplementedException e) {
            NotImplementedExceptionAssume.fail(e);
        }
    }


    @Parameterized.Parameters
    public static Collection<String> getNum() throws IOException {
        //I want my code to read input.txt line by line and feed the input in an arraylist so that it returns an equivalent of the code below
        List<String> files = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            files.add("data/graph_coloring/gc_15_30_"+i);
        }
        return files;
    }
}