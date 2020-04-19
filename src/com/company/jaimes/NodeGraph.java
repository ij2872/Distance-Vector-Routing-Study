package com.company.jaimes;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxStyleUtils;
import com.mxgraph.util.mxUtils;
import org.jgrapht.Graph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.*;
import org.jgrapht.nio.ImportException;

import javax.swing.*;
import java.awt.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

class NodeGraph extends JApplet {
    private static final Dimension DEFAULT_SIZE = new Dimension(800, 860);
//    private static final Dimension WINDOW_SIZE = new Dimension(630, 200);

    private JGraphXAdapter<String, NodeWeightedEdge> jgxAdapter;
    private mxGraphComponent component;
    private ListenableGraph<String, NodeWeightedEdge> g;
    private Map<String, String> nodeIds = new HashMap<>();
    private Node[] nodes;
    private List<List<Integer>> connectionList;

    NodeGraph(Node[] nodes, List<List<Integer>> connectionList){
        this.nodes = nodes;
        this.connectionList = connectionList;
        init();

        JFrame frame = new JFrame();
        frame.getContentPane().add(this);
        frame.setTitle("JGraphT Adapter to JGraphX Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void init(){

        g = new DefaultListenableGraph<>(new SimpleWeightedGraph(NodeWeightedEdge.class));

        jgxAdapter = new JGraphXAdapter<>(g);
        int[][] nodes = {{1, 2, 2}, {1,3, 7}, {2, 3, 1}};
        addVertex(g);
        addNodeEdges(g, nodes);

        setPreferredSize(DEFAULT_SIZE);
        component = new mxGraphComponent(jgxAdapter);
        component.setConnectable(false);
        component.getGraph().setAllowDanglingEdges(false);


        mxGraphModel graphModel = (mxGraphModel)component.getGraph().getModel();
        Collection<Object> cells =  graphModel.getCells().values();

        for(Object cell : cells.toArray()){
            graphModel.setStyle(cell, "endArrow=none");
        }

        getContentPane().add(component);
        resize(DEFAULT_SIZE);

        int rad = 100;
        mxCircleLayout layout = new mxCircleLayout(jgxAdapter);
        layout.setX0((DEFAULT_SIZE.width/2.0) - rad);
        layout.setY0((DEFAULT_SIZE.height/2.0) - rad);
        layout.setRadius(rad);
        layout.setMoveCircle(true);

        layout.execute(jgxAdapter.getDefaultParent());

        viewUpdate();

    }

    private void addVertex(ListenableGraph<String, NodeWeightedEdge> g) {
        Arrays.stream(nodes).forEach(node -> {
            g.addVertex(node.prettyString());
        });
    }

    public void addNodeEdges(Graph<String, NodeWeightedEdge> g, int[][] nodessss){
        System.out.println("======");
        connectionList.forEach((pair) -> {
            System.out.println(pair);
            Node a = Arrays.stream(nodes)
                    .filter(node -> String.valueOf(node.prettyString().charAt(0)).equals(String.valueOf(pair.get(0))))
                    .findFirst()
                    .get();

            Node b = Arrays.stream(nodes)
                    .filter(node -> String.valueOf(node.prettyString().charAt(0)).equals(String.valueOf(pair.get(1))))
                    .findFirst()
                    .get();

            NodeWeightedEdge edge = g.addEdge(a.prettyString(), b.prettyString());
            g.setEdgeWeight(a.prettyString(), b.prettyString(), pair.get(2));
        });

        g.vertexSet().forEach((v) -> System.out.println(v.charAt(0)));


//        NodeWeightedEdge e1 = g.addEdge(a,b);
//        g.setEdgeWeight(e1, 2);

//        Arrays.stream(nodes).forEach(meta -> {
//            String src = String.valueOf(meta[0]);
//            String dest = String.valueOf(meta[1]);
//            double cost = (double) meta[2];
//
//            if(nodeIds.containsKey(src)){
//                src = nodeIds.get(src);
//            }else{
//                g.addVertex(src);
//                nodeIds.put(src, src);
//            }
//            if(nodeIds.containsKey(dest)){
//                dest = nodeIds.get(dest);
//            }else{
//                g.addVertex(dest);
//                nodeIds.put(dest, dest);
//            }
//
//            NodeWeightedEdge edge = g.addEdge(src, dest);
//            g.setEdgeWeight(edge, cost);
//        });
    }

    private void viewUpdate(){
    }

    public void update() {
        System.out.println(component.getGraph().getModel().contains(nodes[0]));
//        component.getGraph().
        component.getGraph().getModel().beginUpdate();
        g.vertexSet().forEach((v) -> {


        });
        System.out.println("new nodes");
        Arrays.stream(nodes).forEach(System.out::println);
        component.getGraph().getModel().endUpdate();
    }
}
