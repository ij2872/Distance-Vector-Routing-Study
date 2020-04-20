package com.company.jaimes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Node implements Comparable<Node>{
    private int id;
    private Costs costs;
    private List<Node> neighborNodes;

    // for new nodes
    Node(int id, int size, int dest, int cost){
        this.id = id;
        this.costs = new Costs(id, size);
        this.neighborNodes = new ArrayList<>();
        addCost(dest, cost);
    }

    // add a new cost to current nodes row
    void addCost(int destId, int cost){
        this.costs.addCost(id, destId, cost);
    }

    void addNeighbor(Node neighborNode){
        this.neighborNodes.add(neighborNode);
    }

    // Iterate through current nodes neighbors. Ask for their costs
    public boolean pingNeighbors() {
        boolean hasUpdated = false;
        for(Node neighbor : this.neighborNodes){
            hasUpdated |= updateFromNeighborsRow(neighbor);
            hasUpdated |= updateFromNeighborsOtherRows(neighbor);
        }

        return hasUpdated;
    }

    // Update current nodes rows if there are better results in neighboring nodes
    private boolean updateFromNeighborsOtherRows(Node neighbor) {
        boolean hasUpdated = false;

        for(int i=0; i< costs.getColSize(); i++){
            // ignore self and neighbors index since we already updated
            if(i == this.id || i == neighbor.getId()) continue;

            // check to see if neighbors row is better
            if(isNeighborRowBetter(this.getNodeRow(i+1), neighbor.getNodeRow(i+1))){
                // if it is, overwrite our current row
                this.update(i+1, neighbor.getNodeRow(i+1));
                hasUpdated = true;
            }
        }
        if(hasUpdated){
            System.out.println("updated at: " + getId() + " " + neighbor.getId());
            System.out.println(lineString());
            System.out.println(neighbor.lineString());
        }
        return hasUpdated;
    }

    // compare with neighbor row with what we have for the neighbors row. if neighbor is better, update cost matrix
    private boolean isNeighborRowBetter(int[] nodeRow1, int[] nodeRow2) {
        for(int i=0; i<nodeRow1.length; i++){
            if(nodeRow1[i] > nodeRow2[i]){
                return true;
            }
        }
        return false;
    }

    // Asks neighboring node if there was an update to it's row. If yes, perform the DV algorithm.
    private boolean updateFromNeighborsRow(Node neighbor) {
        boolean hasUpdated = false;
        int[] neighborsRow = neighbor.getNodeRow();
        int[] selfRowBasedOnNeighbor = getNodeRow(neighbor.getId());
        int[] selfRow = getNodeRow();

        if(!Arrays.equals(selfRowBasedOnNeighbor, neighborsRow)){
            // rows are different, update
            this.costs.updateRow(neighbor.getId(), neighborsRow.clone());
            hasUpdated = true;

            // update own row based on what it received
            for(int y=0; y<costs.getColSize(); y++){
                if(y == id-1) continue; // ignore it's own cell. it equals 0
                for(int v=0; v<costs.getColSize(); v++){
                    if(v == id-1 || v == y) continue; // ignore own cell
                    selfRow[y] = dx(y, v);
                }
            }

        }
        return hasUpdated;
    }


    // given a new row, update it with the given index
    private void update(int id, int[] selfCostRow) {
        this.costs.updateRow(id, selfCostRow);
    }

    List<Node> getNeighbors(){
        return this.neighborNodes;
    }

    int getId(){
        return this.id;
    }

    int[] getNodeRow(){
        return costs.getSelfRow();
    }

    int[] getNodeRow(int id){return costs.getRow(id); }

    // get weight of current node to node with {id}
    public int getWeight(int id) {
        return this.costs.getCost(this.id, id-1);
    }

    public String prettyString(){
        return String.format("%d\n" +
                "%s",
                this.id, this.costs.prettyString());
    }

    public String lineString(){
        return "Node{" +
                "id=" + id +
                "costs=" + costs.toString() +
                '}';
    }

    // DV algorithm
    private int dx(int yIndex, int vIndex){
        int current = this.costs.getCost(this.getId(), yIndex);
        int result = Math.min(current, minV(id, yIndex, vIndex)); // update if there is a better path

        // just in case MAX_VALUE overflows. Return infinity
        if (result <= 0 || result == Integer.MAX_VALUE){
            return current;
        }

        return result;
    }

    private int minV(int xId, int yIndex, int vIndex){
        int C = this.costs.getCost()[xId-1][vIndex];
        int dv = this.costs.getCost()[vIndex][yIndex];

        if(C == Integer.MAX_VALUE || dv == Integer.MAX_VALUE) return Integer.MAX_VALUE; // minV not possible

        return C+dv;
    }

    // ---- Used for graph api ----

    @Override
    public String toString() {
        return prettyString();
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Node) && (toString().equals(obj.toString()));
    }

    @Override
    public int compareTo(Node node2) {
        return Integer.compare(getId(), node2.getId());
    }
}

