package serializable;

import javafx.scene.paint.Color;

import java.io.Serializable;

//global options, to be removed
//TODO: replace all static variables with instance
public class OptionsSerializable implements Serializable {

    int radius;
    double nodeWidth;
    double[] nodeFillColor;//color is not serializable, so we use array of doubles
    double[] nodeStrokeColor;//color is not serializable, so we use array of doubles
//    Color nodeFillColor;
//    Color nodeStrokeColor;

//    int bezierNodeRadius;
//    double bezierNodeWidth;
//    Color bezierNodeFillColor;
//    Color bezierNodeStrokeColor;

    double edgeWidth;
    double[] edgeStrokeColor;
    //Color edgeStrokeColor;

//    boolean gridShown;
//    int gridSep;

    double labelDistance;


    public OptionsSerializable(int radius, double nodeWidth, Color nodeFillColor, Color nodeStrokeColor, double edgeWidth, Color edgeStrokeColor, double labelDistance) {
        this.radius = radius;
        this.nodeWidth = nodeWidth;
        this.nodeFillColor = convertColor(nodeFillColor);
        this.nodeStrokeColor = convertColor(nodeStrokeColor);
        this.edgeWidth = edgeWidth;
        this.edgeStrokeColor = convertColor(edgeStrokeColor);
        this.labelDistance = labelDistance;
//        this.gridShown = gridShown;
//        this.gridSep = gridSep;
    }
    private double[] convertColor(Color c) {
        return new double[] { c.getRed(), c.getGreen(), c.getBlue(), c.getOpacity()};
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public double getNodeWidth() {
        return nodeWidth;
    }

    public void setNodeWidth(double nodeWidth) {
        this.nodeWidth = nodeWidth;
    }

    public double[] getNodeFillColor() {
        return nodeFillColor;
    }

    public void setNodeFillColor(double[] nodeFillColor) {
        this.nodeFillColor = nodeFillColor;
    }

    public double[] getNodeStrokeColor() {
        return nodeStrokeColor;
    }

    public void setNodeStrokeColor(double[] nodeStrokeColor) {
        this.nodeStrokeColor = nodeStrokeColor;
    }

    public double getEdgeWidth() {
        return edgeWidth;
    }

    public void setEdgeWidth(double edgeWidth) {
        this.edgeWidth = edgeWidth;
    }

    public double[] getEdgeStrokeColor() {
        return edgeStrokeColor;
    }

    public void setEdgeStrokeColor(double[] edgeStrokeColor) {
        this.edgeStrokeColor = edgeStrokeColor;
    }

    public double getLabelDistance() {
        return labelDistance;
    }

    public void setLabelDistance(double labelDistance) {
        this.labelDistance = labelDistance;
    }
}
