package minicp.search;

import minicp.util.io.cpprofilerbridge.Connector;

import java.io.IOException;
import java.util.function.Supplier;

public class TreeVisualizer {

    public static void cpProfiler(DFSearch dfs, Supplier<String> nodeString) {
        Connector c = new Connector();
        try {
            // Connect to port 6565 (default for cp-profiler)
            c.connect(6565);
            c.start("EXAMPLE", -1);
            // this listens to the branching and send appropriate messages
            dfs.addListener(new DFSListener() {
                @Override
                public void solution(int pId, int id, int position) {
                    c.createNode(id, pId, position, 0, Connector.NodeStatus.SOLVED).setNodeLabel("node: " + id).setNodeInfo(nodeString.get()).send();
                }
                @Override
                public void fail(int pId, int id, int position) {
                    c.createNode(id, pId, position, 0, Connector.NodeStatus.FAILED).setNodeLabel("node: " +id).setNodeInfo("failure").send();
                }
                @Override
                public void branch(int pId, int id, int position, int nChilds) {
                    c.createNode(id, pId, position, nChilds, Connector.NodeStatus.BRANCH).setNodeLabel("node: " + id).setNodeInfo(nodeString.get()).send();
                }

                // disco
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
