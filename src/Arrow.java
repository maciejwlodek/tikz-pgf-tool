import javafx.beans.InvalidationListener;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class Arrow extends Group {

    //modified from https://stackoverflow.com/questions/41353685/how-to-draw-arrow-javafx-pane
    private final Line line;
    private final Line invisibleLine;

    public Arrow() {
        this(new Line(), new Line(), new Line());
    }

    private static final double arrowLength = 6;
    private static final double arrowWidth = 3;
    public static DoubleProperty cutoffProperty = new SimpleDoubleProperty();
    public static DoubleProperty widthProperty = new SimpleDoubleProperty(1);
    static ObjectProperty<Color> strokeColorProperty = new SimpleObjectProperty<>(Color.BLACK);

    //public static double cutoff = 10;

    private Arrow(Line line, Line arrow1, Line arrow2) {
        super(line, arrow1, arrow2);
        this.line = line;
        invisibleLine = new Line();

        cutoffProperty().bind(Node.widthProperty().divide(2).add(Node.nodeRadiusProperty()));
        line.strokeWidthProperty().bind(widthProperty);
        arrow1.strokeWidthProperty().bind(widthProperty);
        arrow2.strokeWidthProperty().bind(widthProperty);
        line.strokeProperty().bind(strokeColorProperty);
        arrow1.strokeProperty().bind(strokeColorProperty);
        arrow2.strokeProperty().bind(strokeColorProperty);
        
        line.startXProperty().bind(new DoubleBinding() {
            {bind(cutoffProperty, invisibleLine.startXProperty(), invisibleLine.startYProperty(), invisibleLine.endXProperty(), invisibleLine.endYProperty());}
            @Override
            protected double computeValue() {
                return computeSX();
            }
        });
        line.startYProperty().bind(new DoubleBinding() {
            {bind(cutoffProperty, invisibleLine.startXProperty(), invisibleLine.startYProperty(), invisibleLine.endXProperty(), invisibleLine.endYProperty());}
            @Override
            protected double computeValue() {
                return computeSY();
            }
        });
        line.endXProperty().bind(new DoubleBinding() {
            {bind(cutoffProperty, invisibleLine.startXProperty(), invisibleLine.startYProperty(), invisibleLine.endXProperty(), invisibleLine.endYProperty());}
            @Override
            protected double computeValue() {
                return computeEX();
            }
        });
        line.endYProperty().bind(new DoubleBinding() {
            {bind(cutoffProperty, invisibleLine.startXProperty(), invisibleLine.startYProperty(), invisibleLine.endXProperty(), invisibleLine.endYProperty());}
            @Override
            protected double computeValue() {
                return computeEY();
            }
        });
        
        InvalidationListener updater = o -> {
            double ex = line.getEndX();
            double ey = line.getEndY();
            double sx = line.getStartX();
            double sy = line.getStartY();

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

            if (ex == sx && ey == sy) {
                // arrow parts of length 0
                arrow1.setStartX(ex);
                arrow1.setStartY(ey);
                arrow2.setStartX(ex);
                arrow2.setStartY(ey);
            } else {
                double factor = arrowLength / Math.hypot(sx-ex, sy-ey);
                double factorO = arrowWidth / Math.hypot(sx-ex, sy-ey);

                // part in direction of main line
                double dx = (sx - ex) * factor;
                double dy = (sy - ey) * factor;

                // part ortogonal to main line
                double ox = (sx - ex) * factorO;
                double oy = (sy - ey) * factorO;

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
        return getEndX() + computeCut()[0];
    }
    private double computeEY() {
        return getEndY() + computeCut()[1];
    }
    private double[] computeCut() {
        double ex = getEndX();
        double ey = getEndY();
        double sx = getStartX();
        double sy = getStartY();

        double cutDist = getCutoff() / Math.hypot(sx - ex, sy - ey);
        double cutX = (sx - ex) * cutDist;
        double cutY = (sy - ey) * cutDist;
        return new double[] {cutX, cutY};
    }

    // start/end properties

    public final void setStartX(double value) {
        invisibleLine.setStartX(value);
    }

    public final double getStartX() {
        return invisibleLine.getStartX();
    }

    public final DoubleProperty startXProperty() {
        return invisibleLine.startXProperty();
    }

    public final void setStartY(double value) {
        invisibleLine.setStartY(value);
    }

    public final double getStartY() {
        return invisibleLine.getStartY();
    }

    public final DoubleProperty startYProperty() {
        return invisibleLine.startYProperty();
    }

    public final void setEndX(double value) {
        invisibleLine.setEndX(value);
    }

    public final double getEndX() {
        return invisibleLine.getEndX();
    }

    public final DoubleProperty endXProperty() {
        return invisibleLine.endXProperty();
    }

    public final void setEndY(double value) {
        invisibleLine.setEndY(value);
    }

    public final double getEndY() {
        return invisibleLine.getEndY();
    }

    public final DoubleProperty endYProperty() {
        return invisibleLine.endYProperty();
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
}