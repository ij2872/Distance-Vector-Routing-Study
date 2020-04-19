package com.company.jaimes;

import java.util.Arrays;

public class Node {
    private int id;
    private Costs costs;
    private Costs oldCosts;
//    private int[] oldNodeCosts;

    // for new nodes
    Node(int id, int size, int dest, int cost){
        this.id = id;
        this.costs = new Costs(id, size);
        this.oldCosts = new Costs(id, size);
//        this.oldNodeCosts = new int[size];

        addCost(dest, cost);
    }

    void printCosts(){
        System.out.println("\nData for Node " + getId());
        System.out.println(Arrays.deepToString(costs.getCost()).replace("], ", "],\n"));
    }

    void addCost(int dest, int cost){
        this.costs.addCost(id, dest, cost);
    }

    int getId(){
        return this.id;
    }

    int[][] getCosts(){
        return this.costs.getCost();
    }

    int[] getOldNodeCosts(){return this.oldCosts.getSelfCost();}

    // Get the nodes row based on id. used for pinging to other nodes
    int[] getNodeRow(){
        return costs.getSelfCost();
    }

    int[] getNodeRowOld(){ return oldCosts.getSelfCost(); }

    public String prettyString(){
        return String.format("%d\n" +
                "%s",
                this.id, this.costs.prettyString());
    }

    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                "costs=" + costs.toString() +
                '}';
    }

    // might not need this. maybe just replace row with new
    public boolean update(int node2Id, int[] nodeRow) {
        boolean flag = false;
        for(int i=0; i<nodeRow.length; i++){
            int node2OldValue = oldCosts.getCost(node2Id, i);
            if(node2OldValue > nodeRow[i]){
                costs.setLog(node2Id-1, i , nodeRow[i]);
                flag = true;
            }
        }

        return flag;
    }

    // Check if going to another node is faster than what we currently have
    public boolean updateSelf() {
        boolean selfUpdateFlag = false;

        for(int i=0; i<costs.getColSize();i++){
            if(i == id-1) continue; // ignore selfs value
            int minCost = calculateMinCost(i);

            if(minCost != costs.getCost(this.id, i)){
                costs.setLog(id-1, i, minCost);
                selfUpdateFlag = true;
            }

        }

        return selfUpdateFlag;
    }

    private int calculateMinCost(int col) {
        int minVal = Integer.MAX_VALUE;//costs.getCost(id-1, col);

        for(int i=0; i<costs.getColSize();i++){
            if(i == id-1) continue; // ignore selfs value
            int xy =  oldCosts.getCost(this.id, i);
            int dy = costs.getCost(i+1, col);
            minVal = Math.min(minVal, xy + dy);
        }

        return minVal;
    }

    public void save(){
        this.oldCosts = new Costs(this.costs);
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

