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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;

class NodeGraph extends JApplet {
    private static final Dimension DEFAULT_SIZE = new Dimension(700, 650);
//    private static final Dimension WINDOW_SIZE = new Dimension(630, 200);

    private JGraphXAdapter<Node, NodeWeightedEdge> jgxAdapter;
    private mxGraphComponent component;
    private mxCircleLayout layout;
    private ListenableGraph<Node, NodeWeightedEdge> g;
    private Map<String, String> nodeIds = new HashMap<>();
    private Node[] nodes;
    private int stateCount;
    private List<List<Integer>> connectionList;
    private CountDownLatch latch;
    private JButton button;
    private JLabel label;

    NodeGraph(Node[] nodes, List<List<Integer>> connectionList){
        this.nodes = nodes;
        this.stateCount = 0;
        this.connectionList = connectionList;
        this.latch = latch;
        init();

        JFrame frame = new JFrame();
//        frame.setLayout();
        frame.getContentPane().add(this);
        addSettings(frame);
//        addButton();
        frame.setTitle("Ivan Jaimes - Distance Vector Routing - Project 2" );
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void addSettings(JFrame frame){
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        label= new JLabel();
        label.setText(String.valueOf(stateCount));

        button = new JButton("Next State");
        button.setSize(100,200);
        button.addActionListener(generateButtonListener());

        panel.add(label);
        panel.add(button);
        panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        frame.getContentPane().add(panel, BorderLayout.SOUTH);
    }

    private ActionListener generateButtonListener(){
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                latch.countDown();

            }
        };
    }

    public void addLatch(CountDownLatch latch){
        this.latch = latch;
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
        layout = new mxCircleLayout(jgxAdapter);
        layout.setX0(20);
        layout.setY0(20);
        layout.setX0((DEFAULT_SIZE.width/4.0) - rad);
        layout.setY0((DEFAULT_SIZE.height/4.0) - rad);
        layout.setRadius(rad);
        layout.setMoveCircle(true);

        layout.execute(jgxAdapter.getDefaultParent());
    }

    private void addVertex(ListenableGraph<Node, NodeWeightedEdge> g) {
        Arrays.stream(nodes).forEach(node -> {
            g.addVertex(node);
        });
    }

    public void addNodeEdges(ListenableGraph<Node, NodeWeightedEdge> g, int[][] nodessss){
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

            NodeWeightedEdge edge = g.addEdge(a, b);
            g.setEdgeWeight(a, b, pair.get(2));
        });

        g.vertexSet().forEach((v) -> System.out.println(v.getId()));
    }


    public void update() {
        component.getGraph().refresh();
        label.setText(String.valueOf(stateCount));
    }

    public void close() {
        button.setEnabled(false);
        button.setText("Final State Reached");
    }

    public void addStateCount() {
        this.stateCount++;
    }
}
