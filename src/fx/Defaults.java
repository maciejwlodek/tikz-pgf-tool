package fx;

import javafx.scene.paint.Color;

public class Defaults {

    //node defaults
    public static final int DEFAULT_NODE_RADIUS = 6;
    public static final double DEFAULT_NODE_WIDTH = 2;
    public static final Color DEFAULT_NODE_FILL_COLOR = new Color(0.0, 0.0, 0.0, 0.6);
    public static final Color DEFAULT_NODE_STROKE_COLOR = Color.BLACK;

    //beziercontrolnode defaults
    public static final int DEFAULT_BEZIER_CONTROL_NODE_RADIUS = 7;
    public static final double DEFAULT_BEZIER_CONTROL_NODE_WIDTH = 1;
    public static final Color DEFAULT_BEZIER_CONTROL_NODE_FILL_COLOR = Color.LIGHTGREEN;
    public static final Color DEFAULT_BEZIER_CONTROL_NODE_STROKE_COLOR = Color.BLACK;

    //edge defaults
    public static final double DEFAULT_EDGE_WIDTH = 2;
    public static final Color DEFAULT_EDGE_STROKE_COLOR = Color.BLACK;

    //directed or undirected
    public static final boolean DEFAULT_DIRECTED = true;

    //show or hide grid + grid coarseness
    public static final boolean DEFAULT_GRID = true;
    public static final int DEFAULT_GRID_SEP = 25;

    public static final double SCALE_FACTOR=1;

}
