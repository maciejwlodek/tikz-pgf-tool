package fx;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import serializable.LabeledNodeSerializable;

public class LabeledNode extends Group {

    Node node;
    Text label;

    static DoubleProperty labelDistanceProperty = new SimpleDoubleProperty(20); //non-editable for now
    DoubleProperty labelAngleProperty = new SimpleDoubleProperty(90);
    //private DoubleProperty labelWidthProperty = new SimpleDoubleProperty(0);
//    private double width = 0;
//    private double textHeight

    public LabeledNode(Node node, Text label) {
        super(node, label);
        this.node=node;
        this.label=label;


        InvalidationListener updater = o -> {
            double textWidth = label.getLayoutBounds().getWidth();
            double textHeight = label.getLayoutBounds().getHeight();
            label.setTranslateX(-textWidth/2);
            label.setTranslateY(textHeight/4);
        };
        updater.invalidated(null);
        label.textProperty().addListener(updater);

        label.xProperty().bind(new DoubleBinding() {
            {bind(node.centerXProperty(), labelDistanceProperty, labelAngleProperty, label.textProperty());}
            @Override
            protected double computeValue() {
                return node.getCenterX()+getLabelDistance()* Math.cos(Math.toRadians(getLabelAngle()));// - width/2;
            }
        });
        label.yProperty().bind(new DoubleBinding() {
            {bind(node.centerYProperty(), labelDistanceProperty, labelAngleProperty);}
            @Override
            protected double computeValue() {
                return node.getCenterY()-getLabelDistance()* Math.sin(Math.toRadians(getLabelAngle()));
            }
        });

    }

    public static double getLabelDistance() {
        return labelDistanceProperty.get();
    }
    public static void setLabelDistance(double value) {
        labelDistanceProperty.set(value);
    }
    public static DoubleProperty labelDistanceProperty() {
        return labelDistanceProperty;
    }

    public double getLabelAngle() {
        return labelAngleProperty.get();
    }
    public void setLabelAngle(double value) {
        labelAngleProperty.set(value);
    }
    public DoubleProperty labelAngleProperty() {
        return labelAngleProperty;
    }

    public double getRescaledX() {
        return node.getRescaledX();
    }
    public double getRescaledY() {
        return node.getRescaledY();
    }

    public String getLabel(){
        return label.getText();
    }
    public void setLabel(String text){
        label.setText(text);
    }

    public double getCenterX() {
        return node.getCenterX();
    }
    public void setCenterX(double x){
        node.setCenterX(x);
    }
    public DoubleProperty centerXProperty() {
        return node.centerXProperty();
    }
    public double getCenterY() {
        return node.getCenterY();
    }
    public void setCenterY(double y) {
        node.setCenterY(y);
    }
    public DoubleProperty centerYProperty() {
        return node.centerYProperty();
    }


}
