package com.company.jaimes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class NodeController {
    private String filename = "";
    private Node[] nodes;
    private int nodeSize = 0;
    private int stateChangeCount = 0;
    private NodeView view = new NodeView();
    private List<List<Integer>> connectionList = new ArrayList<>();

    NodeController(String filename){
        this.filename = filename;
        this.nodeSize = getNodeSize();
        buildNodes();
        view.init(nodes, connectionList);
        run();
    }

    // Listen for a space press to update state. Stop updating at final state.
    private void run() {
        System.out.println("init node load");
        view.print(nodes);

        while (updateNodes()){
            view.print(nodes);
            view.listen();
            ++stateChangeCount;
            view.update();
        }

        view.close();
        System.out.println("Stable State reached. " + stateChangeCount + " state changes happened.");

    }

    private boolean updateNodes() {
        boolean hasUpdated = false;

        for(Node node : nodes){
            hasUpdated |= node.pingNeighbors();
        }

        return hasUpdated;
    }

    private boolean buildNodes() {
        try {
            List<List<Integer>> dataInfo = Files.lines(Paths.get(filename))
                    .map(this::parseString)
                    .map((el) -> {
                        addConnectionList(el);
                        return el;
                    }).collect(Collectors.toList());

            nodes = new Node[nodeSize];
            Arrays.fill(nodes, null);
            for(List<Integer> data : dataInfo){
                int id = data.get(0);
                int destId = data.get(1);
                int cost = data.get(2);

                // arr list
                if(nodes[id-1] == null) {
                    nodes[id-1] = new Node(id, nodeSize, destId, cost);
                }else{
                    nodes[id-1].addCost(destId, cost);
                }

                if(nodes[destId-1] == null){
                    nodes[destId-1] = new Node(destId, nodeSize, id, cost);
                }else{
                    nodes[destId-1].addCost(id, cost);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        linkNeighbors();
        return true;
    }

    private void linkNeighbors() {
        connectionList.stream()
                .map((info) -> new int[]{info.get(0), info.get(1)}) // [id1, id2]
                .forEach((pair) -> {
                    // make neighbors with each other


                    nodes[pair[0]-1].addNeighbor(nodes[pair[1]-1]);
                    nodes[pair[1]-1].addNeighbor(nodes[pair[0]-1]);
                });
    }

    public Node[] getNodes(){
        return this.nodes;
    }

    private List<Integer> parseString(String inputLine){
        return Arrays.stream(inputLine.split(" "))
                .map(str -> Integer.parseInt(str))
                .collect(Collectors.toList());
    }


    private int getNodeSize(){
        int distinctNodeSize = 0;
        int maxValue = 0;

        try {
            List<Integer> nodeList = Files.lines(Paths.get(filename))
                    .map(this::parseString)
                    .map(el ->el.subList(0, el.size()-1))
                    .flatMap(num -> num.stream())
                    .distinct()
                    .collect(Collectors.toList());

            distinctNodeSize = nodeList.size();
            maxValue = nodeList.stream().max(Integer::compare).get();

            System.out.println("size of " + distinctNodeSize + " max :" + maxValue);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return maxValue;
    }

    // used for the view to connect
    private void addConnectionList(List<Integer> line) {
        this.connectionList.add(line);
    }
}
