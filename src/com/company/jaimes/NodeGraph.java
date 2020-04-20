package com.company.jaimes;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;

class NodeGraph extends JApplet {
    private static final Dimension DEFAULT_SIZE = new Dimension(700, 650);
    private final String BUTTON_NEXT = "Next State";
    private final String BUTTON_DONE = "Done!";
    private final String BUTTON_REEVALUATE = "Reload";
    private final String BUTTON_SIMULATE = "Simulate";


    private NodeViewStatus status;
    private JGraphXAdapter<Node, NodeWeightedEdge> jgxAdapter;
    private ListenableGraph<Node, NodeWeightedEdge> g;
    private mxGraphComponent component;
    private mxCircleLayout layout;
    private List<Node> nodes;
    private CountDownLatch latch;
    private int stateCount;
    private JButton button;
    private JButton buttonReevaluate;
    private JButton buttonSimulate;
    private JLabel label;

    NodeGraph(List<Node> nodes){
        this.nodes = nodes;
        this.stateCount = 0;
        status = NodeViewStatus.NEXT;

        init();

        JFrame frame = new JFrame();
        frame.getContentPane().add(this);
        addSettings(frame);
        frame.setTitle("Ivan Jaimes - Distance Vector Routing - Project 2" );
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void init(){
        g = new DefaultListenableGraph<>(new SimpleWeightedGraph(NodeWeightedEdge.class));
        jgxAdapter = new JGraphXAdapter<>(g);

        addVertex(g); // init vertices
        addNodeEdges(g); // init edges
        setPreferredSize(DEFAULT_SIZE);
        componentSetup(jgxAdapter); // init graph
        layoutSetup(jgxAdapter); // style graph
    }

    // adds objects to frame
    private void addSettings(JFrame frame){
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        label= new JLabel();
        label.setText(String.valueOf(stateCount));

        button = new JButton(BUTTON_NEXT);
        button.setSize(100,200);
        button.addActionListener(generateButtonListener());

        buttonReevaluate = new JButton(BUTTON_REEVALUATE);
        buttonReevaluate.setSize(100, 200);
        buttonReevaluate.addActionListener(reevaluateButtonListener());
        buttonReevaluate.setEnabled(false);

        buttonSimulate = new JButton(BUTTON_SIMULATE);
        buttonSimulate.setSize(100,200);
        buttonSimulate.addActionListener(simulateButtonListener());
        buttonSimulate.setEnabled(true);

        panel.add(label);
        panel.add(buttonSimulate);
        panel.add(button);
        panel.add(buttonReevaluate);
        panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        frame.getContentPane().add(panel, BorderLayout.SOUTH);
    }



    // onButtonClick lower the latch counter down to 0;
    private ActionListener generateButtonListener(){
        return actionEvent -> {
            buttonSimulate.setEnabled(false); // user started program. Do not simulate from now on.
            status = NodeViewStatus.NEXT;
            latch.countDown();
        }; // Tell parent that the user clicked button
    }

    private ActionListener reevaluateButtonListener(){
        return actionEvent -> {
            System.out.println("reevaluateButtonListener()");
            status = NodeViewStatus.RELOAD_PRESSED;
            latch.countDown();
        }; // user wants to re do process with new nodes. tell parent user pressed reload
    }

    private ActionListener simulateButtonListener() {
        return actionEvent -> {
          System.out.println("running simulation");;
          status = NodeViewStatus.SIMULATE;
          disableAllButtons();
          latch.countDown();
        };// user wants to simulate...
    }

    // prevent user from pressing buttons
    private void disableAllButtons() {
        button.setEnabled(false);
        buttonReevaluate.setEnabled(false);
        buttonSimulate.setEnabled(false);
    }

    // latch listener for gui actions
    public void addLatch(CountDownLatch latch){
        this.latch = latch;
    }

    // setup the positioning of the graph
    private void layoutSetup(JGraphXAdapter<Node, NodeWeightedEdge> jgxAdapter) {
        int rad = 100;
        resize(DEFAULT_SIZE);

        layout = new mxCircleLayout(jgxAdapter);
        layout.setX0(20);
        layout.setY0(20);
        layout.setX0((DEFAULT_SIZE.width/4.0) - rad);
        layout.setY0((DEFAULT_SIZE.height/4.0) - rad);
        layout.setRadius(rad);
        layout.setMoveCircle(true);
        layout.execute(jgxAdapter.getDefaultParent());
    }

    // setup styling of the graph
    private void componentSetup(JGraphXAdapter<Node, NodeWeightedEdge> jgxAdapter) {
        component = new mxGraphComponent(jgxAdapter);
        component.getGraph().setEdgeLabelsMovable(false);
        component.getGraph().setEdgeLabelsMovable(false);
        component.setConnectable(false);
        component.getGraph().setAllowDanglingEdges(false);

        mxGraphModel graphModel = (mxGraphModel)component.getGraph().getModel();
        Collection<Object> cells =  graphModel.getCells().values();

        for(Object cell : cells.toArray()){
            graphModel.setStyle(cell, "endArrow=none"); // gets rid of the arrows for edges. This took me 3 hours to figure out..
        }

        getContentPane().add(component);
    }

    // add nodes to graph as a vertex
    private void addVertex(ListenableGraph<Node, NodeWeightedEdge> g) {
        for(Node node : nodes){
            g.addVertex(node);
        }
    }

    // Add node edges to each vertex
    private void addNodeEdges(ListenableGraph<Node, NodeWeightedEdge> g){
        for(Node nodeA : nodes){
            for(Node nodeB : nodeA.getNeighbors()){
                int weight = nodeA.getWeight(nodeB.getId());

                g.addEdge(nodeA, nodeB);
                g.setEdgeWeight(nodeA, nodeB, weight);
            }
        }
    }




    private boolean edgeListener() {
        mxGraphModel graphModel = (mxGraphModel)component.getGraph().getModel();
        Collection<Object> cells =  graphModel.getCells().values();
        boolean isEdgeUpdate = false;
        for(Object cell : cells.toArray()){
            if(((mxCell)cell).isEdge()){
                mxCell cellData = ((mxCell) cell);
                String weight = String.valueOf((cellData.getValue()));
                Node source = (Node) ((mxCell)cellData.getSource()).getValue();
                Node target = (Node) ((mxCell)cellData.getTarget()).getValue();

                isEdgeUpdate |= handleEdgeEvent(source, target, weight);
            }
        }
        return isEdgeUpdate;
    }

    private boolean handleEdgeEvent(Node source, Node target, String weightStr) {
        int weight = Integer.valueOf(weightStr);
        // check to see if this is an updated weight
        int sourceToTargetWeight = source.getNodeRow()[target.getId()-1];
        if(sourceToTargetWeight !=  weight){
            if(weight < sourceToTargetWeight){
                // weight has been updated, the new weight is less than the previous
                if(weight == 16) weight = Integer.MAX_VALUE; // 16 is infinity
                System.out.println("\nNEW CHANGE. SMALLER THAN PREV");
                System.out.println("handleEdgeEvent() - updating " +sourceToTargetWeight + ">" + weight + " [" + source.getId() + ", "+target.getId() + "]");
                source.addCost(target.getId(), weight);
                target.addCost(source.getId(), weight);
                this.status = NodeViewStatus.UPDATED;
                return true;
            }
        }
        return false;
    }

    // No more state changes possible
    public void close() {
        enableReloadButton();
        status = NodeViewStatus.RELOAD_UPDATED;
    }

    public NodeViewStatus getStatus() {
        return this.status;
    }

    // Re-renders the GUI for updated state
    public boolean update() {
        boolean hasUpdated = edgeListener();
        component.getGraph().refresh();
        label.setText(String.valueOf(stateCount));

        return hasUpdated;
    }

    // reloads buttons based on state
    public void reload(boolean hasUpdated) {
        status = hasUpdated ? NodeViewStatus.UPDATED : NodeViewStatus.RELOAD_UPDATED;
        System.out.println("reload: " + hasUpdated);
        if(hasUpdated){
            status = NodeViewStatus.UPDATED;
            enableButton();
        }else{
            status = NodeViewStatus.RELOAD_PRESSED;
            enableReloadButton();
        }
    }

    private void enableButton(){
        button.setEnabled(true);
        button.setText(BUTTON_NEXT);
        buttonReevaluate.setEnabled(false);
    }
    private void enableReloadButton(){
        button.setEnabled(false);
        button.setText(BUTTON_DONE);
        buttonReevaluate.setEnabled(true);
    }

    public void setStateChangeCount(int stateChangeCount) {
        this.stateCount = stateChangeCount;
//        component.getGraph().refresh();
        label.setText(String.valueOf(stateCount));
    }

    public void overwriteLabel(String text) {
        label.setText(text);
    }
}
