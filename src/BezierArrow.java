import javafx.beans.InvalidationListener;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;

public class BezierArrow extends Group {

    private final CubicCurve curve;
    private final CubicCurve invisibleCurve;

    public BezierArrow() {
        this(new CubicCurve(), new Line(), new Line());
    }

    private static final double arrowLength = 6;
    private static final double arrowWidth = 3;
    public static DoubleProperty cutoffProperty = new SimpleDoubleProperty();
    public static DoubleProperty widthProperty = new SimpleDoubleProperty(1);
    static ObjectProperty<Color> strokeColorProperty = new SimpleObjectProperty<>(Color.BLACK);

    //public static double cutoff = 10;

    private BezierArrow(CubicCurve curve, Line arrow1, Line arrow2) {
        super(curve, arrow1, arrow2);
        this.curve = curve;
        invisibleCurve = new CubicCurve();

//        invisibleCurve.setStrokeWidth(10);
//        //invisibleCurve.setDisable(true);
//        getChildren().add(invisibleCurve);
        curve.setFill(Color.TRANSPARENT);

        cutoffProperty().bind(Node.widthProperty().divide(2).add(Node.nodeRadiusProperty()));
        curve.strokeWidthProperty().bind(widthProperty);
        curve.controlX1Property().bind(invisibleCurve.controlX1Property());
        curve.controlX2Property().bind(invisibleCurve.controlX2Property());
        curve.controlY1Property().bind(invisibleCurve.controlY1Property());
        curve.controlY2Property().bind(invisibleCurve.controlY2Property());


        arrow1.strokeWidthProperty().bind(widthProperty);
        arrow2.strokeWidthProperty().bind(widthProperty);
        curve.strokeProperty().bind(strokeColorProperty);
        arrow1.strokeProperty().bind(strokeColorProperty);
        arrow2.strokeProperty().bind(strokeColorProperty);



//        arrow1.rotationAxisProperty().bind(new ObjectBinding<Point3D>() {
//            {bind(endXProperty(), endYProperty());}
//            @Override
//            protected Point3D computeValue() {
//                return new Point3D(getEndX(), getEndY(), 1);
//            }
//        });
//        arrow1.setRotationAxis(Rotate.);
//        arrow1.rotateProperty().bind(new DoubleBinding() {
//            {bind(endXProperty(), endYProperty(), controlX2Property(), controlY2Property());}
//            @Override
//            protected double computeValue() {
//                double slope = (getEndX() == getControlX2())? 999999 : (getControlY2() - getEndY())/(getControlX2()-getEndX());
//                double angle = Math.atan(slope);
//                //return angle*(180/Math.PI);
//                //return getControlX2()>=getEndX()? 90+angle*(180/Math.PI) : 90+(angle+Math.PI)*(180/Math.PI);
//            }
//        });
//        arrow2.rotationAxisProperty().bind(new ObjectBinding<Point3D>() {
//            {bind(endXProperty(), endYProperty());}
//            @Override
//            protected Point3D computeValue() {
//                return new Point3D(getEndX(), getEndY(), 1);
//            }
//        });
//        //arrow2.setRotationAxis(Rotate.Z_AXIS);
//        arrow2.rotateProperty().bind(new DoubleBinding() {
//            {bind(endXProperty(), endYProperty(), controlX2Property(), controlY2Property());}
//            @Override
//            protected double computeValue() {
//                double slope = (getEndX() == getControlX2())? 999999 : (getControlY2() - getEndY())/(getControlX2()-getEndX());
//                double angle = Math.atan(slope);
//                return getControlX2()>=getEndX()? angle*(180/Math.PI) : (angle+Math.PI)*(180/Math.PI);
//            }
//        });

        curve.startXProperty().bind(new DoubleBinding() {
            {bind(cutoffProperty, invisibleCurve.startXProperty(), invisibleCurve.startYProperty(), invisibleCurve.endXProperty(), invisibleCurve.endYProperty(),
                    controlX1Property(), controlX2Property(), controlY1Property(), controlY2Property());}
            @Override
            protected double computeValue() {
                return computeSX();
            }
        });
        curve.startYProperty().bind(new DoubleBinding() {
            {bind(cutoffProperty, invisibleCurve.startXProperty(), invisibleCurve.startYProperty(), invisibleCurve.endXProperty(), invisibleCurve.endYProperty(),
                    controlX1Property(), controlX2Property(), controlY1Property(), controlY2Property());}
            @Override
            protected double computeValue() {
                return computeSY();
            }
        });
        curve.endXProperty().bind(new DoubleBinding() {
            {bind(cutoffProperty, invisibleCurve.startXProperty(), invisibleCurve.startYProperty(), invisibleCurve.endXProperty(), invisibleCurve.endYProperty(),
                    controlX1Property(), controlX2Property(), controlY1Property(), controlY2Property());}
            @Override
            protected double computeValue() {
                return computeEX();
            }
        });
        curve.endYProperty().bind(new DoubleBinding() {
            {bind(cutoffProperty, invisibleCurve.startXProperty(), invisibleCurve.startYProperty(), invisibleCurve.endXProperty(), invisibleCurve.endYProperty(),
                    controlX1Property(), controlX2Property(), controlY1Property(), controlY2Property());}
            @Override
            protected double computeValue() {
                return computeEY();
            }
        });

        InvalidationListener updater = o -> {
            double ex = curve.getEndX();
            double ey = curve.getEndY();
            double sx = curve.getStartX();
            double sy = curve.getStartY();

            double cx1 = getControlX1();
            double cy1 = getControlY1();
            double cx2 = getControlX2();
            double cy2 = getControlY2();

//            double cutDist = cutoff / Math.hypot(sx-ex, sy-ey);
//            double cutX = (sx - ex) * cutDist;
//            double cutY = (sy - ey) * cutDist;
//
//            ex += cutX;
//            ey += cutY;
//            sx -= cutX;
//            sy -= cutY;

            arrow1.setEndX(ex);
            arrow1.setEndY(ey);
            arrow2.setEndX(ex);
            arrow2.setEndY(ey);

            if (ex == cx2 && ey == cy2) {
                // arrow parts of length 0
                arrow1.setStartX(ex);
                arrow1.setStartY(ey);
                arrow2.setStartX(ex);
                arrow2.setStartY(ey);
            } else {
//                double factor = arrowLength / Math.hypot(sx-ex, sy-ey);
//                double factorO = arrowWidth / Math.hypot(sx-ex, sy-ey);

                double factor = arrowLength / Math.hypot(cx2-ex, cy2-ey);
                double factorO = arrowWidth / Math.hypot(cx2-ex, cy2-ey);

                double dx = (cx2 - ex) * factor;
                double dy = (cy2 - ey) * factor;

                double ox = (cx2 - ex) * factorO;
                double oy = (cy2 - ey) * factorO;


//                double factor1 = arrowLength / Math.hypot(sx-cx1, sy-cy1);
//                double factorO1 = arrowWidth / Math.hypot(sx-cx1, sy-cy1);
//                double factor2 = arrowLength / Math.hypot(cx2-ex, cy2-ey);
//                double factorO2 = arrowWidth / Math.hypot(cx2-ex, cy2-ey);
//
//                // part in direction of main line
//                double dx1 = (sx - cx1) * factor1;
//                double dy1 = (sy - cy1) * factor1;
//                double dx2 = (cx2 - ex) * factor2;
//                double dy2 = (cy2 - ey) * factor2;
//
//                // part ortogonal to main line
//                double ox1 = (sx - cx1) * factorO1;
//                double oy1 = (sy - cy1) * factorO1;
//                double ox2 = (cx2 - ex) * factorO2;
//                double oy2 = (cy2 - ey) * factorO2;

                arrow1.setStartX(ex + dx - oy);
                arrow1.setStartY(ey + dy + ox);
                arrow2.setStartX(ex + dx + oy);
                arrow2.setStartY(ey + dy - ox);
            }
        };

        // add updater to properties
        startXProperty().addListener(updater);
        startYProperty().addListener(updater);
        endXProperty().addListener(updater);
        endYProperty().addListener(updater);
        controlX1Property().addListener(updater);
        controlX2Property().addListener(updater);
        controlY1Property().addListener(updater);
        controlY2Property().addListener(updater);

        cutoffProperty().addListener(updater);
        updater.invalidated(null);
    }

    //    private double getSlope() {
//        double ex = getEndX();
//        double ey = getEndY();
//        double sx = getStartX();
//        double sy = getStartY();
//        return ex==sx? (ey-sy)*1000000 : (ey-sy)/(ex-sx);
//    }
    private double computeSX() {
        return getStartX() - computeCut()[0];
    }
    private double computeSY() {
        return getStartY() - computeCut()[1];
    }
    private double computeEX() {
        return getEndX() + computeCut()[2];
    }
    private double computeEY() {
        return getEndY() + computeCut()[3];
    }
    private double[] computeCut() {
        double ex = getEndX();
        double ey = getEndY();
        double sx = getStartX();
        double sy = getStartY();
        if((ex==0 && ey==0) || (sx==0 && sy==0)) return new double[] {0,0,0,0};


        double cx1 = getControlX1();
        double cy1 = getControlY1();
        double cx2 = getControlX2();
        double cy2 = getControlY2();

        //double cutDist = getCutoff() / Math.hypot(sx - ex, sy - ey);
        double cutDist1 = getCutoff() / Math.hypot(sx - cx1, sy - cy1);
        double cutDist2 = getCutoff() / Math.hypot(cx2 - ex, cy2 - ey);
        double cutX1 = (sx - cx1) * cutDist1;
        double cutY1 = (sy - cy1) * cutDist1;
        double cutX2 = (cx2 - ex) * cutDist2;
        double cutY2 = (cy2 - ey) * cutDist2;
        return new double[] {cutX1, cutY1, cutX2, cutY2};
    }

    public final void setStartX(double value) {
        invisibleCurve.setStartX(value);
    }
    public final double getStartX() {
        return invisibleCurve.getStartX();
    }
    public final DoubleProperty startXProperty() {
        return invisibleCurve.startXProperty();
    }
    public final void setStartY(double value) {
        invisibleCurve.setStartY(value);
    }
    public final double getStartY() {
        return invisibleCurve.getStartY();
    }
    public final DoubleProperty startYProperty() {
        return invisibleCurve.startYProperty();
    }
    public final void setEndX(double value) {
        invisibleCurve.setEndX(value);
    }
    public final double getEndX() {
        return invisibleCurve.getEndX();
    }
    public final DoubleProperty endXProperty() {
        return invisibleCurve.endXProperty();
    }
    public final void setEndY(double value) {
        invisibleCurve.setEndY(value);
    }
    public final double getEndY() {
        return invisibleCurve.getEndY();
    }
    public final DoubleProperty endYProperty() {
        return invisibleCurve.endYProperty();
    }
    public final void setControlX1(double value) {
        invisibleCurve.setControlX1(value);
    }
    public final double getControlX1() {
        return invisibleCurve.getControlX1();
    }
    public final DoubleProperty controlX1Property() {
        return invisibleCurve.controlX1Property();
    }
    public final void setControlY1(double value) {
        invisibleCurve.setControlY1(value);
    }
    public final double getControlY1() {
        return invisibleCurve.getControlY1();
    }
    public final DoubleProperty controlY1Property() {
        return invisibleCurve.controlY1Property();
    }
    public final void setControlX2(double value) {
        invisibleCurve.setControlX2(value);
    }
    public final double getControlX2() {
        return invisibleCurve.getControlX2();
    }
    public final DoubleProperty controlX2Property() {
        return invisibleCurve.controlX2Property();
    }
    public final void setControlY2(double value) {
        invisibleCurve.setControlY2(value);
    }
    public final double getControlY2() {
        return invisibleCurve.getControlY2();
    }
    public final DoubleProperty controlY2Property() {
        return invisibleCurve.controlY2Property();
    }
    public static void setCutoff(double value) {
        cutoffProperty.set(value);
    }
    public static double getCutoff() {
        return cutoffProperty.get();
    }
    public static DoubleProperty cutoffProperty() {
        return cutoffProperty;
    }
    public static void setWidth(double value) {
        widthProperty.set(value);
    }
    public static double getWidth() {
        return widthProperty.get();
    }
    public static DoubleProperty widthProperty() {
        return widthProperty;
    }
    public static void setStrokeColor(Color value) {
        strokeColorProperty.set(value);
    }
    public static Color getStrokeColor() {
        return strokeColorProperty.get();
    }
    public static ObjectProperty<Color> strokeColorProperty() {
        return strokeColorProperty;
    }

    private Point2D eval(double t){
        Point2D p=new Point2D(Math.pow(1-t,3)*curve.getStartX()+
                3*t*Math.pow(1-t,2)*curve.getControlX1()+
                3*(1-t)*t*t*curve.getControlX2()+
                Math.pow(t, 3)*curve.getEndX(),
                Math.pow(1-t,3)*curve.getStartY()+
                        3*t*Math.pow(1-t, 2)*curve.getControlY1()+
                        3*(1-t)*t*t*curve.getControlY2()+
                        Math.pow(t, 3)*curve.getEndY());
        return p;
    }

    //returns distance from p to curve
    public double distance(Point2D p) {
        final double step = 0.1;
        double t = 0;
        double dist = Double.MAX_VALUE;
        while(t<=1) {
            dist = Math.min(dist, eval(t).distance(p));
            t+=step;
        }
        return dist;
    }

}