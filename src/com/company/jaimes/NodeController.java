package com.company.jaimes;

import com.sun.deploy.uitoolkit.impl.awt.ui.SwingConsoleWindow;

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
//        view.print(nodes);
        view.update();
        System.out.println("-------------");

        while (updateNodes()  ){
            view.listen();

            ++stateChangeCount;
//            view.print(nodes);
            view.update();

        }

        view.close();
        System.out.println("Stable State reached. " + stateChangeCount + " state changes happened.");

    }

    private boolean updateNodes() {
        boolean didUpdate = false;
        for(Node node : nodes){

            for(Node otherNode : nodes){
                if(node != otherNode){
                    didUpdate |= pingNode(node, otherNode); // if any pingNode returns true, then did update is true. inclusive OR used for cases that are false after it has been set to true.
                }
            }

            node.save();
        }

        for(Node node : nodes){
            if(node.updateSelf() == true){
                didUpdate = true;
            }
        }

        return didUpdate;
    }
    private boolean pingNode(Node node1, Node node2){
        return node1.update(node2.getId(), node2.getNodeRow());
    }

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

        return true;
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
