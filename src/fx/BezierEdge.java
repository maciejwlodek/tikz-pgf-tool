package fx;

import javafx.scene.input.MouseButton;

import java.util.Objects;

public class BezierEdge extends BezierArrow {

    LabeledNode startingNode;
    LabeledNode endingNode;

    BezierControlNode control1;
    BezierControlNode control2;

    public BezierEdge(LabeledNode startingNode, LabeledNode endingNode) {
        super();
        this.startingNode=startingNode;
        this.endingNode=endingNode;

        toBack();

        startXProperty().bind(startingNode.centerXProperty());
        startYProperty().bind(startingNode.centerYProperty());
        endXProperty().bind(endingNode.centerXProperty());
        endYProperty().bind(endingNode.centerYProperty());

        int[] linearControls = computeLinearControls(getStartX(), getEndX(), getStartY(), getEndY());
        control1 = new BezierControlNode(linearControls[0], linearControls[1]);
        control2 = new BezierControlNode(linearControls[2], linearControls[3]);

        control1.setOnMouseDragged(e -> {
            if(e.getButton().equals(MouseButton.PRIMARY)) {
                control1.setCenterX(e.getX());
                control1.setCenterY(e.getY());
                this.toBack();
            }
        });
        control2.setOnMouseDragged(e -> {
            if(e.getButton().equals(MouseButton.PRIMARY)) {
                control2.setCenterX(e.getX());
                control2.setCenterY(e.getY());
                this.toBack();
            }
        });

        controlX1Property().bind(control1.centerXProperty());
        controlX2Property().bind(control2.centerXProperty());
        controlY1Property().bind(control1.centerYProperty());
        controlY2Property().bind(control2.centerYProperty());

    }

    public BezierEdge(LabeledNode startingNode, LabeledNode endingNode, double cx1, double cy1, double cx2, double cy2) {
        this(startingNode, endingNode);
        control1.centerXProperty().set(cx1);
        control1.centerYProperty().set(cy1);
        control2.centerXProperty().set(cx2);
        control2.centerYProperty().set(cy2);
//        setControlX1(cx1);
//        setControlX2(cx2);
//        setControlY1(cy1);
//        setControlY2(cy2);
    }


    public BezierEdge(LabeledNode startingNode, double endX, double endY) {
        super();
        this.startingNode = startingNode;

        setStartX(startingNode.getCenterX());
        setStartY(startingNode.getCenterY());
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

    public LabeledNode getStartingNode() {
        return startingNode;
    }
    public void setStartingNode(LabeledNode startingNode) {
        this.startingNode = startingNode;
    }
    public LabeledNode getEndingNode() {
        return endingNode;
    }
    public void setEndingNode(LabeledNode endingNode) {
        this.endingNode = endingNode;
    }
    public BezierControlNode getControl1() {
        return control1;
    }
    public void setControl1(BezierControlNode control1) {
        this.control1 = control1;
    }
    public BezierControlNode getControl2() {
        return control2;
    }
    public void setControl2(BezierControlNode control2) {
        this.control2 = control2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BezierEdge that = (BezierEdge) o;
        return Objects.equals(startingNode, that.startingNode) &&
                Objects.equals(endingNode, that.endingNode) &&
                Objects.equals(control1, that.control1) &&
                Objects.equals(control2, that.control2);
    }
}
