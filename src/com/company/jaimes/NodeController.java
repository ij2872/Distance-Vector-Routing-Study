package com.company.jaimes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


public class NodeController {
    private String filename = "";
    private List<Node> nodes;
    private int nodeSize = 0;
    private int stateChangeCount = 0;
    private NodeView view = new NodeView();

    NodeController(String filename){
        this.filename = filename;
        this.nodeSize = getNodeSize();

        buildNodes();
        view.init(nodes); // GUI setup

        run(); // run state changes
    }

    private void run() {
        System.out.println("init node load");
        view.print(nodes);
        boolean hasUpdated;
        NodeViewStatus listenStatus = view.listen(stateChangeCount);

        // simulate if user wants to
        if(listenStatus == NodeViewStatus.SIMULATE){
            System.out.println("runnin sim.");
            // run simulation if user clicks button, otherwise run the program with steps
            long startTime = System.currentTimeMillis();
            runSimulation();
            long endTime = System.currentTimeMillis();

            String status = "State Changes: " + stateChangeCount + " Time: " + (endTime - startTime) + "ms";
            view.update();
            view.overwriteLabel(status);
            return;
        }
        do{
            hasUpdated = false;
            switch (listenStatus){
                case UPDATED:{
                    hasUpdated |= true;// safety case
                }
                case NEXT:  {
                    // user pressed button for next state
                    System.out.println("NEXT");
                    hasUpdated |= processRequest();
                    view.reload(hasUpdated);
                    break;
                }
                case RELOAD_PRESSED: {
                    // user pressed the reload button
                    hasUpdated |= view.update();
                    view.reload(hasUpdated);

                    System.out.println("RELOAD_PRESSED?" + hasUpdated );
                    break;
                }
            }

            // add to counter for a state change. if no state change, notify user
            if(hasUpdated){
                System.out.println("State change");
                stateChangeCount++;
                view.validateStateChangeCount(stateChangeCount);
            }else{
                view.close();
            }
            listenStatus = view.listen(stateChangeCount);

        }while (listenStatus != NodeViewStatus.CLOSE);

        view.close();
        System.out.println("Stable State reached. " + stateChangeCount + " state changes happened.");

    }

    private void runSimulation() {
        while(updateNodes()){
            stateChangeCount++;
        }
    }

    // update each nodes cost matrix and update the view
    private boolean processRequest() {
        Boolean hasUpdated = updateNodes();

        if(hasUpdated){
            view.print(nodes);
            view.update();
        }else{
            view.close();
        }

        return hasUpdated;
    }

    // nodes ping each other. if there was an update, return true
    private boolean updateNodes() {
        boolean hasUpdated = false;

        for(Node node : nodes){
            hasUpdated |= node.pingNeighbors();
        }

        return hasUpdated;
    }

    private void buildNodes() {
        try {
            // get each line as a list [[v1,v2,W], ...]
            List<List<Integer>> dataInfo = Files.lines(Paths.get(filename))
                    .map(this::parseString)
                    .collect(Collectors.toList());

            nodes = new ArrayList<>();

            // builds nodes
            for(List<Integer> data : dataInfo){
                int id = data.get(0);
                int destId = data.get(1);
                int cost = data.get(2);

                Node src;
                Node dest;

                // look for the nodes in the nodes list
                Optional<Node> nodeA = nodes.stream().filter(node -> id == node.getId()).findFirst();
                Optional<Node> nodeB = nodes.stream().filter(node -> destId == node.getId()).findFirst();

                // get node if exists, else create a new one
                if(nodeA.isPresent()){
                    src = nodeA.get();
                }else{
                    src = new Node(id, nodeSize, destId, cost);
                    nodes.add(src);
                }

                // get node if exists, else create a new one
                if(nodeB.isPresent()){
                    dest = nodeB.get();
                }else{
                    dest = new Node(destId, nodeSize, id, cost);
                    nodes.add(dest);
                }

                linkNeighbors(src, dest, cost);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Adds src and dest as neighbors to each other
    private void linkNeighbors(Node src, Node dest, int cost) {
        src.addNeighbor(dest);
        dest.addNeighbor(src);

        src.addCost(dest.getId(), cost);
        dest.addCost(src.getId(), cost);
    }

    // parse file line [V1, V2, weight]
    private List<Integer> parseString(String inputLine){
        return Arrays.stream(inputLine.split(" "))
                .map(str -> Integer.parseInt(str))
                .collect(Collectors.toList());
    }

    // returns the amount of vertices in file
    private int getNodeSize(){
        int distinctNodeSize = 0;
        int maxValue = 0;

        try {
            // read file, parse, get just the vertices
            List<Integer> nodeList = Files.lines(Paths.get(filename))
                    .map(this::parseString)
                    .map(el ->el.subList(0, el.size()-1))
                    .flatMap(num -> num.stream())
                    .distinct()
                    .collect(Collectors.toList());


            // get the highest node index
            distinctNodeSize = nodeList.size();
            maxValue = nodeList.stream()
                    .max(Integer::compare)
                    .get();

            System.out.println("size of " + distinctNodeSize + " max: " + maxValue);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return maxValue;
    }
}
