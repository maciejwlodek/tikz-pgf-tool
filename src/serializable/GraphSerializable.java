package serializable;

import fx.*;

import java.io.Serializable;
import java.util.ArrayList;

public class GraphSerializable implements Serializable {

    OptionsSerializable options;
    ArrayList<LabeledNodeSerializable> nodes;
    ArrayList<BezierEdgeSerializable> edges;

    public GraphSerializable(Graph graph) {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        graph.getNodes().forEach(n -> {
            nodes.add(new LabeledNodeSerializable(n));
        });
        graph.getEdges().forEach(e -> {
            //LabeledNodeSerializable startingNode = nodes.get(indexOf(e.getStartingNode()));
            //LabeledNodeSerializable endingNode = nodes.get(indexOf(e.getEndingNode()));
            int startingNodeIndex = indexOf(e.getStartingNode());
            int endingNodeIndex = indexOf(e.getEndingNode());
            BezierControlNodeSerializable control1 = new BezierControlNodeSerializable(e.getControlX1(), e.getControlY1());
            BezierControlNodeSerializable control2 = new BezierControlNodeSerializable(e.getControlX2(), e.getControlY2());
            edges.add(new BezierEdgeSerializable(startingNodeIndex, endingNodeIndex, control1, control2));
        });
        options = new OptionsSerializable(Node.getNodeRadius(), Node.getWidth(), Node.getFillColor(), Node.getStrokeColor(), BezierEdge.getWidth(), BezierEdge.getStrokeColor(), LabeledNode.getLabelDistance());
    }

    private int indexOf(LabeledNode node) {
        for(int i=0; i<nodes.size(); i++) {
            LabeledNodeSerializable nodeSerializable = nodes.get(i);
            if(nodeSerializable.centerX == node.getCenterX() && nodeSerializable.centerY == node.getCenterY()) {
                return i;
            }
        }
        return -1;
    }

    public OptionsSerializable getOptions() {
        return options;
    }

    public void setOptions(OptionsSerializable options) {
        this.options = options;
    }
    public ArrayList<LabeledNodeSerializable> getNodes() {
        return nodes;
    }
    public void setNodes(ArrayList<LabeledNodeSerializable> nodes) {
        this.nodes = nodes;
    }
    public ArrayList<BezierEdgeSerializable> getEdges() {
        return edges;
    }
    public void setEdges(ArrayList<BezierEdgeSerializable> edges) {
        this.edges = edges;
    }
}
