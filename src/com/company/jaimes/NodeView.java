package com.company.jaimes;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class NodeView {
    private NodeGraph nodeGraph;

    public void init(List<Node> nodes) {
        nodeGraph = new NodeGraph(nodes);
    }

    public void print(List<Node> nodes) {
        System.out.println("--------------NodeView.print()---------------");
        nodes.forEach(node -> System.out.println(node.lineString()));
        System.out.println("--------------------------------------");
    }

    // listens to gui for any button responses. Goes back to NodeController completed
    public NodeViewStatus listen(int stateChangeCount) {
        CountDownLatch latch = new CountDownLatch(1); // pauses thread until other threads lower the countdown to 0
        nodeGraph.addLatch(latch);
        nodeGraph.setStateChangeCount(stateChangeCount);

        try {
            latch.await(); // wait until all threads are finished
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("NodeView button pressed");
        return nodeGraph.getStatus();
    }

    // graph is finished for updating
    public void close(){
        nodeGraph.close();
    }

    // update the graph
    public boolean update() {
        return nodeGraph.update();
    }

    // reload the graph buttons
    public void reload(boolean hasUpdated) {
        nodeGraph.reload(hasUpdated);
    }

    // update the counter
    public void validateStateChangeCount(int stateChangeCount) {
        nodeGraph.setStateChangeCount(stateChangeCount);
    }

    // used for simulation option to show steps with time passed
    public void overwriteLabel(String text){
        nodeGraph.overwriteLabel(text);
    }
}
