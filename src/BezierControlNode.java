import javafx.beans.property.*;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class BezierControlNode extends Circle {


    final Color fillColor = Color.LIGHTGREEN;
    final Color strokeColor = Color.BLACK;
    final double strokeWidth = 1;
    final static int radius = 7;

    public BezierControlNode(int centerX, int centerY) {
        super(centerX, centerY, radius);

        setFill(fillColor);
        setStroke(strokeColor);
        setStrokeWidth(strokeWidth);

//        setOnMouseDragged(e -> {
//            if(e.getButton().equals(MouseButton.PRIMARY)) {
//                setCenterX(e.getX());
//                setCenterY(e.getY());
//                //List<Edge> incomingEdges = graph.getIncomingEdges(node);
//                //for(Edge e: incomingEdges) incomingEdges.
//            }
//        });

//        setOnMouseClicked(e -> {
//            graph.nodes.forEach(n -> n.setStroke(normalColor));
//            setStroke(focusedColor);
//        });

    }

    public double getRescaledX() {
        double width = 620; //TODO: fix
        double targetRange = 5; //target range is [-5,5]
        return (getCenterX() - width/2) * (2 * targetRange / width);
    }
    public double getRescaledY() {
        double height = 620; //TODO: fix
        double targetRange = 5; //target range is [-5,5]
        return (height/2 - getCenterY()) * (2 * targetRange / height);
    }

}
