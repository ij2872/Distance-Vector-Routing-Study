package com.company.jaimes;

import org.jgrapht.graph.DefaultWeightedEdge;

public class NodeWeightedEdge extends DefaultWeightedEdge{
    @Override
    protected Object getSource() {
        return super.getSource();
    }

    @Override
    protected Object getTarget() {
        return super.getTarget();
    }

    @Override
    protected double getWeight() {
        return super.getWeight();
    }

    @Override
    public String toString() {
        return Integer.toString((int) getWeight());
//        return "20";
    }

}
