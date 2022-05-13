package minicp.util.io.cpprofilerbridge;

import java.io.IOException;

public class Example {
    public static void main (String args[]) throws IOException, InterruptedException {
        try {

            Connector c = new Connector();

            // Connect to port 6565 (default for cp-profiler)
            c.connect(6565);

            // Initiate restarts
            c.start("premier_test", -1);

            // Let build treed
            c.createNode(0, -1, -1, 4, Connector.NodeStatus.BRANCH).setNodeLabel("0-root").send();
            c.createNode(1, 0, 0, 0, Connector.NodeStatus.FAILED).setNodeLabel("1-failed").send();
            c.createNode(2, 0, 1, 3, Connector.NodeStatus.BRANCH).setNodeLabel("2").setNodeInfo("some info").send();

            c.createNode(3, 2, 0, 0, Connector.NodeStatus.SOLVED).setNodeLabel("3-solution").send();
            c.createNode(4, 2, 1, 0, Connector.NodeStatus.FAILED).setNodeLabel("4-failed").send();
            c.createNode(5, 2, 2, 0, Connector.NodeStatus.FAILED).setNodeLabel("5-failed").send();

            c.createNode(6, 0, 2, 0, Connector.NodeStatus.FAILED).setNodeLabel("6-failed").send();
            c.createNode(7, 0, 3, 0, Connector.NodeStatus.FAILED).setNodeLabel("7-failed").send();


            // Logout message
            c.disconnect();
        }
        catch (Exception e){
            System.out.println("\n‼‼ Please launch CPProfiler first ‼‼");
        }
    }
}
