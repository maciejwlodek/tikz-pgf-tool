package serializable;

import fx.BezierControlNode;

import java.io.Serializable;

public class BezierControlNodeSerializable implements Serializable {

    double x;
    double y;

    public BezierControlNodeSerializable(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public BezierControlNodeSerializable(BezierControlNode node) {
        this.x = node.getCenterX();
        this.y = node.getCenterY();
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}
