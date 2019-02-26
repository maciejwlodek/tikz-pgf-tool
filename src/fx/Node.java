package fx;

import javafx.beans.property.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Node extends Circle {

    Graph graph;

    static IntegerProperty nodeRadiusProperty = new SimpleIntegerProperty(Defaults.DEFAULT_NODE_RADIUS);//TODO: non static
    static DoubleProperty widthProperty = new SimpleDoubleProperty(Defaults.DEFAULT_NODE_WIDTH);//TODO: non static
    static ObjectProperty<Color> strokeColorProperty = new SimpleObjectProperty<>(Defaults.DEFAULT_NODE_STROKE_COLOR);//TODO: non static
    static ObjectProperty<Color> fillColorProperty = new SimpleObjectProperty<>(Defaults.DEFAULT_NODE_FILL_COLOR);//TODO: non static

    public Node(double centerX, double centerY, Graph graph) {
        super(centerX, centerY, getNodeRadius());
        this.graph=graph;

        this.fillProperty().bind(fillColorProperty);
        this.strokeProperty().bind(strokeColorProperty);
        this.radiusProperty().bind(nodeRadiusProperty);
        this.strokeWidthProperty().bind(widthProperty);

        toFront();

//        setFill(fillColor);
//        setStroke(strokeColor);
//        setStrokeWidth(strokeWidth);

//        setOnMouseClicked(e -> {
//            graph.nodes.forEach(n -> n.setStroke(normalColor));
//            setStroke(focusedColor);
//        });

    }

    //-------------------
    public Node(double centerX, double centerY) {
        super(centerX, centerY, getNodeRadius());

        this.fillProperty().bind(fillColorProperty);
        this.strokeProperty().bind(strokeColorProperty);
        this.radiusProperty().bind(nodeRadiusProperty);
        this.strokeWidthProperty().bind(widthProperty);

        toFront();
    }
    //------------------------------------

    public double getRescaledX() {
        double width = 620; //TODO: fix by binding to actual width
        double targetRange = 5; //target range is [-5,5]
        return (getCenterX() - width/2) * (2 * targetRange / width);
    }
    public double getRescaledY() {
        double height = 620; //TODO: fix by binding to actual width
        double targetRange = 5; //target range is [-5,5]
        return (height/2 - getCenterY()) * (2 * targetRange / height);
    }


    public static int getNodeRadius() {
        return nodeRadiusProperty.get();
    }
    public static void setNodeRadius(int value) {
        nodeRadiusProperty.set(value);
    }
    public static IntegerProperty nodeRadiusProperty() {
        return nodeRadiusProperty;
    }
    public static double getWidth() {
        return widthProperty.get();
    }
    public static void setWidth(double value) {
        widthProperty.set(value);
    }
    public static DoubleProperty widthProperty() {
        return widthProperty;
    }
    public static Color getStrokeColor() {
        return strokeColorProperty.get();
    }
    public static void setStrokeColor(Color value) {
        strokeColorProperty.set(value);
    }
    public static ObjectProperty<Color> strokeColorProperty() {
        return strokeColorProperty;
    }
    public static Color getFillColor() {
        return fillColorProperty.get();
    }
    public static void setFillColor(Color value) {
        fillColorProperty.set(value);
    }
    public static ObjectProperty<Color> fillColorProperty() {
        return fillColorProperty;
    }



}
