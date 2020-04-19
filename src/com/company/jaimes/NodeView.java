package com.company.jaimes;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class NodeView {
    Scanner in;
    NodeGraph nodeGraph;

    NodeView(){
        initView();
    }

    private void initView(){
        in = new Scanner(System.in);
    }

    public void print(Node[] nodes) {
        System.out.println("--------------NodeView.print()---------------");
        Arrays.stream(nodes).forEach(System.out::println);
        System.out.println("--------------------------------------");

    }

    public void listen() {
        in.nextLine();
    }

    public void close(){
        in.close();
    }

    public void init(Node[] nodes, List<List<Integer>> connectionList) {
        nodeGraph = new NodeGraph(nodes, connectionList);
    }

    public void update() {
        nodeGraph.update();
    }
}
