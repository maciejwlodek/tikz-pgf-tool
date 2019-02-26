package fx;

import java.util.Objects;

public class Edge extends Arrow {

    Node startingNode;
    Node endingNode;
    Graph graph;

    public Edge(Node startingNode, Node endingNode, Graph graph) {
        super();
        this.startingNode=startingNode;
        this.endingNode=endingNode;
        this.graph=graph;

//        setStartX(startingNode.getCenterX());
//        setStartY(startingNode.getCenterY());
//        setEndX(endingNode.getCenterX());
//        setEndY(endingNode.getCenterY());

        startXProperty().bind(startingNode.centerXProperty());
        startYProperty().bind(startingNode.centerYProperty());
        endXProperty().bind(endingNode.centerXProperty());
        endYProperty().bind(endingNode.centerYProperty());

    }

    public Edge(Node startingNode, double endX, double endY, Graph graph) {
        super();
        this.startingNode = startingNode;
        this.graph=graph;

        setStartX(startingNode.getCenterX());
        setStartY(startingNode.getCenterY());
        setEndX(endX);
        setEndY(endY);
    }

    public Edge(double startX, double startY, double endX, double endY, Graph graph) {
        super();
        setStartX(startX);
        setStartY(startY);
        setEndX(endX);
        setEndY(endY);

    }

    public Node getStartingNode() {
        return startingNode;
    }

    public void setStartingNode(Node startingNode) {
        this.startingNode = startingNode;
    }

    public Node getEndingNode() {
        return endingNode;
    }

    public void setEndingNode(Node endingNode) {
        this.endingNode = endingNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return Objects.equals(startingNode, edge.startingNode) &&
                Objects.equals(endingNode, edge.endingNode) &&
                Objects.equals(graph, edge.graph);
    }
}
