package com.company.jaimes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Node {
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

    void addCost(int dest, int cost){
        this.costs.addCost(id, dest, cost);
    }

    void addNeighbor(Node neighborNode){
        this.neighborNodes.add(neighborNode);
    }

    public boolean pingNeighbors() {
        boolean hasUpdated = false;
        for(Node neighbor : this.neighborNodes){
            hasUpdated |= updateFromNeighborsRow(neighbor);
            hasUpdated |= updateFromNeighborOtherRow(neighbor);
        }

        return hasUpdated;
    }

    private boolean updateFromNeighborOtherRow(Node neighbor) {
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
        return hasUpdated;
    }

    private boolean isNeighborRowBetter(int[] nodeRow1, int[] nodeRow2) {
        for(int i=0; i<nodeRow1.length; i++){
            if(nodeRow1[i] > nodeRow2[i]){
                return true;
            }
        }
        return false;
    }

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

    // Get the nodes row based on id. used for pinging to other nodes
    int[] getNodeRow(){
        return costs.getSelfRow();
    }

    int[] getNodeRow(int id){return costs.getRow(id); }


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

    private int dx(int yIndex, int vIndex){
        int current = this.costs.getCost(this.getId(), yIndex);
        int result = Math.min(current, minV(id, yIndex, vIndex));

        if (result <= 0 || result == Integer.MAX_VALUE){
            // just in case MAX_VALUE overflows
            return current;
        }

        return result;
    }

    private int minV(int xId, int yIndex, int vIndex){
        int C = this.costs.getCost()[xId-1][vIndex];
        int dv = this.costs.getCost()[vIndex][yIndex];

        if(C == Integer.MAX_VALUE || dv == Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return C+dv;
    }

    @Override
    public String toString() {
        return prettyString();
//        return "Node{" +
//                "id=" + id +
//                "costs=" + costs.toString() +
//                '}';
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Node) && (toString().equals(obj.toString()));
    }
}

