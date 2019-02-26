package serializable;

import java.io.Serializable;
import fx.LabeledNode;

public class LabeledNodeSerializable implements Serializable {

    String label;
    double labelAngle;
    double centerX;
    double centerY;
//    int radius;
//    double width;
//    Color strokeColor;
//    Color fillColor;

    public LabeledNodeSerializable(LabeledNode node) {
        this.label = node.getLabel();
        this.labelAngle = node.getLabelAngle();
        this.centerX = node.getCenterX();
        this.centerY = node.getCenterY();
//        this.radius = Node.getNodeRadius();
//        this.width = Node.getWidth();
//        this.fillColor = Node.getFillColor();
//        this.strokeColor = Node.getStrokeColor();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    public double getLabelAngle() {
        return labelAngle;
    }

    public void setLabelAngle(double labelAngle) {
        this.labelAngle = labelAngle;
    }

    public double getCenterX() {
        return centerX;
    }

    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public void setCenterY(double centerY) {
        this.centerY = centerY;
    }
}
