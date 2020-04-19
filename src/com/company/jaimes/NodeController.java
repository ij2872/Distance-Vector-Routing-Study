package com.company.jaimes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class NodeController {
    private String filename = "";
    private Node[] nodes;
    private Map<Integer, Node> nodeMap = new HashMap<>();
    private int nodeSize = 0;
    private int stateChangeCount = 0;
    private NodeView view = new NodeView();
    private Scanner scan = new Scanner(System.in);
    private List<List<Integer>> connectionList = new ArrayList<>();

    NodeController(String filename){
        this.filename = filename;
        this.nodeSize = getNodeSize();
        buildNodes();
        view.init(nodes, connectionList);
        System.out.println("printing");
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

    private void printNeighbors() {
        System.out.print("------neighbors of " );
        Arrays.stream(nodes).forEach((node) -> {
            System.out.println(node.getId());
            node.getNeighbors().forEach(nei -> System.out.println(nei.lineString()));
        });
    }

    private boolean updateNodes() {
        boolean hasUpdated = false;

        for(Node node : nodes){
            hasUpdated |= node.pingNeighbors();
        }

//        for(Node node : nodes){
//            didUpdate |= node.pingNeighbors();
////            for(Node otherNode : nodes){
////                if(node != otherNode){
////                    didUpdate |= pingNode(node, otherNode); // if any pingNode returns true, then did update is true. inclusive OR used for cases that are false after it has been set to true.
////                }
////            }
//
//            node.save();
//        }
//
//        for(Node node : nodes){
//            if(node.updateSelf() == true){
//                hasUpdated = true;
//            }
//        }

        return hasUpdated;
    }
//    private boolean pingNode(Node node1, Node node2){
//        return node1.update(node2.getId(), node2.getNodeRow());
//    }

//    private void printState(){
//        view.print(nodes);
//    }

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

                // check if node already exists
                if(nodeMap.containsKey(id)){
                    nodeMap.get(id).addCost(destId, cost);
                }else{
                    // create new node
                    Node newNode = new Node(id, nodeSize, destId, cost);
                    nodeMap.put(id, newNode);
                }
                if(nodeMap.containsKey(destId)){
                    nodeMap.get(destId).addCost(id, cost);
                }else{
                    // create new node
                    Node newNode = new Node(destId, nodeSize, id, cost);
                    nodeMap.put(destId, newNode);
                }

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
