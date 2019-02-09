import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.CubicCurve;

import java.util.Objects;

public class BezierEdge extends BezierArrow {

    Node startingNode;
    Node endingNode;
    Graph graph;

    BezierControlNode control1;
    BezierControlNode control2;

    public BezierEdge(Node startingNode, Node endingNode, Graph graph) {
        super();
        this.startingNode=startingNode;
        this.endingNode=endingNode;
        this.graph=graph;

//        setStartX(startingNode.getCenterX());
//        setStartY(startingNode.getCenterY());
//        setEndX(endingNode.getCenterX());
//        setEndY(endingNode.getCenterY());

        toBack();

        startXProperty().bind(startingNode.centerXProperty());
        startYProperty().bind(startingNode.centerYProperty());
        endXProperty().bind(endingNode.centerXProperty());
        endYProperty().bind(endingNode.centerYProperty());

//        int cx1 = (int) ((getEndX()-getStartX())/3 + getStartX());
//        int cx2 = (int) (2*(getEndX()-getStartX())/3 + getStartX());
//        int cy1 = (int) ((getEndY()-getStartY())/3 + getStartY());
//        int cy2 = (int) (2*(getEndY()-getStartY())/3 + getStartY());
        int[] linearControls = computeLinearControls(getStartX(), getEndX(), getStartY(), getEndY());
        control1 = new BezierControlNode(linearControls[0], linearControls[1]);
        control2 = new BezierControlNode(linearControls[2], linearControls[3]);

        control1.setOnMouseDragged(e -> {
            if(e.getButton().equals(MouseButton.PRIMARY)) {
                control1.setCenterX(e.getX());
                control1.setCenterY(e.getY());
                this.toBack();
                //List<Edge> incomingEdges = graph.getIncomingEdges(node);
                //for(Edge e: incomingEdges) incomingEdges.
            }
        });
        control2.setOnMouseDragged(e -> {
            if(e.getButton().equals(MouseButton.PRIMARY)) {
                control2.setCenterX(e.getX());
                control2.setCenterY(e.getY());
                this.toBack();
                //List<Edge> incomingEdges = graph.getIncomingEdges(node);
                //for(Edge e: incomingEdges) incomingEdges.
            }
        });

        controlX1Property().bind(control1.centerXProperty());
        controlX2Property().bind(control2.centerXProperty());
        controlY1Property().bind(control1.centerYProperty());
        controlY2Property().bind(control2.centerYProperty());

    }

    //only for temporary edges before they are connected to an end node
    public BezierEdge(Node startingNode, double endX, double endY, Graph graph) {
        super();
        this.startingNode = startingNode;
        this.graph=graph;

        setStartX(startingNode.getCenterX());
        setStartY(startingNode.getCenterY());
        setEndX(endX);
        setEndY(endY);
    }

    public BezierEdge(double startX, double startY, double endX, double endY, Graph graph) {
        super();
        setStartX(startX);
        setStartY(startY);
        setEndX(endX);
        setEndY(endY);

    }

    static int[] computeLinearControls(double startX, double endX, double startY, double endY) {
        int cx1 = (int) ((endX-startX)/3 + startX);
        int cx2 = (int) (2*(endX-startX)/3 + startX);
        int cy1 = (int) ((endY-startY)/3 + startY);
        int cy2 = (int) (2*(endY-startY)/3 + startY);
        return new int[] {cx1, cy1, cx2, cy2};
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
        BezierEdge that = (BezierEdge) o;
        return Objects.equals(startingNode, that.startingNode) &&
                Objects.equals(endingNode, that.endingNode) &&
                Objects.equals(graph, that.graph) &&
                Objects.equals(control1, that.control1) &&
                Objects.equals(control2, that.control2);
    }

    //    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        Edge edge = (Edge) o;
//        return Objects.equals(startingNode, edge.startingNode) &&
//                Objects.equals(endingNode, edge.endingNode) &&
//                Objects.equals(graph, edge.graph);
//    }
}
