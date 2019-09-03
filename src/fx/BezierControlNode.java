package fx;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class BezierControlNode extends Circle {


    final Color fillColor = Defaults.DEFAULT_BEZIER_CONTROL_NODE_FILL_COLOR;
    final Color strokeColor = Defaults.DEFAULT_BEZIER_CONTROL_NODE_STROKE_COLOR;
    final double strokeWidth = Defaults.DEFAULT_BEZIER_CONTROL_NODE_WIDTH;
    final static int radius = Defaults.DEFAULT_BEZIER_CONTROL_NODE_RADIUS;

    public BezierControlNode(int centerX, int centerY) {
        super(centerX, centerY, radius);

        setFill(fillColor);
        setStroke(strokeColor);
        setStrokeWidth(strokeWidth);

//        setOnMouseDragged(e -> {
//            if(e.getButton().equals(MouseButton.PRIMARY)) {
//                setCenterX(e.getX());
//                setCenterY(e.getY());
//                //List<fx.Edge> incomingEdges = graph.getIncomingEdges(node);
//                //for(fx.Edge e: incomingEdges) incomingEdges.
//            }
//        });

//        setOnMouseClicked(e -> {
//            graph.nodes.forEach(n -> n.setStroke(normalColor));
//            setStroke(focusedColor);
//        });

    }

    public double getRescaledX() {
        double width = 620; //TODO: fix
        double targetRange = Defaults.SCALE_FACTOR*5; //target range is [-5,5]
        return (getCenterX() - width/2) * (2 * targetRange / width);
    }
    public double getRescaledY() {
        double height = 620; //TODO: fix
        double targetRange = Defaults.SCALE_FACTOR*5; //target range is [-5,5]
        return (height/2 - getCenterY()) * (2 * targetRange / height);
    }

}
