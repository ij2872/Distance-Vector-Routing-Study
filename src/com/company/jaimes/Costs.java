package com.company.jaimes;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Costs {
    private int parentIndex;
    private int[][] log;

    Costs(int id, int size){
        this.parentIndex = id - 1;
        this.log = new int[size][size];
        for(int i=0; i<log.length; ++i){
            if(i == this.parentIndex){
                Arrays.fill(log[i], Integer.MAX_VALUE);

            }else{
                Arrays.fill(log[i], Integer.MAX_VALUE);
            }
        }
        log[id-1][id-1] = 0;
    }

    void addCost(int nodeId, int destId, int cost ){
        this.log[nodeId-1][destId-1] = cost;
    }

    int[] getSelfRow(){
        return log[parentIndex];
    }

    public int[][] getCost() {
        return log;
    }

    // NOTE!!! Indexing at 1 base
    public int getCost(int nodeId, int col) {
        return log[nodeId-1][col];
    }

    public int[] getRow(int id){return log[id-1];}

    public int getColSize(){return log[0].length;}

    public String prettyString(){
        StringBuilder sb = new StringBuilder();
        for(int[] arr : this.log){
            for(int num : arr){
                if(num == Integer.MAX_VALUE)sb.append(String.format("%5s", "∞"));
                else sb.append(String.format("%5d ", num));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Costs{" +
                "log=" + Arrays.deepToString(log) +
                '}';
    }

    public void updateRow(int id, int[] selfCostRow) {
        int[] newRow = selfCostRow.clone();
        this.log[id-1] = newRow;
    }
}
