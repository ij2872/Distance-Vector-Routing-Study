package com.company.jaimes;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class NodeView {
    private Scanner in;
    private NodeGraph nodeGraph;


    NodeView(){
        initView();
    }

    private void initView(){
        in = new Scanner(System.in);
    }

    public void print(Node[] nodes) {
        System.out.println("--------------NodeView.print()---------------");
        Arrays.stream(nodes).forEach((node)-> System.out.println(node.lineString()));
        System.out.println("--------------------------------------");

    }



    public void listen() {
//        in.nextLine();
        CountDownLatch latch = new CountDownLatch(1);
        nodeGraph.addStateCount();
        nodeGraph.addLatch(latch);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("NodeView button pressed");
    }

    public void close(){
        in.close();
        nodeGraph.close();
    }

    public void init(Node[] nodes, List<List<Integer>> connectionList) {
        nodeGraph = new NodeGraph(nodes, connectionList);
    }

    public void update() {
        nodeGraph.update();
    }
}
