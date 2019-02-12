import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Optional;

public class Main extends Application {

    /*
    Summary of TODOS
    TODO: Implement directed vs undirected
    TODO: Allow editing of labels and label positions
    TODO: Allow editing of node shapes (circle, rectangle, etc).
     */


    Group group;
    Group grid = new Group();
    Button addNodeButton;
    Button generateLatexButton;
    Button editNodeShapeButton;
    Button editEdgeShapeButton;
    Button clearButton;
    CheckBox gridCheckBox;
    BooleanProperty addingNodeProperty = new SimpleBooleanProperty(false);
    double threshold = 50;
    double edgeThreshold = 30;
    Circle followingNode;
    Node selectedNode;
    BezierEdge selectedEdge;
    Text text = new Text("");
    //Text helpText = new Text("Press the Add Node \n button to add vertices \n to the graph. \n Right click to \n finish adding nodes. \n Left click drag \n a node to move \n it around, and \n right click drag \n from one node \n to another to create \n an edge between them.");
    Button helpButton = new Button("Help");

    TextField gridSepField;// = new TextField();

    Graph graph = new Graph();
    BezierEdge tempEdge;

    Point2D tempPoint = new Point2D(-1,-1);

    int gridSepX = 30;
    int gridSepY = 30;

    Point2D mouseDragStartPosition;
    Point2D translateStartPosition;

    BooleanProperty gridShownProperty = new SimpleBooleanProperty(false);

//    include in preamble:
//    \\usepackage{tikz}
//      \\usepackage{pgfmath}
//      \\usetikzlibrary{arrows}
//      \\usetikzlibrary{shapes}


    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane borderPane = new BorderPane();

        group = new Group();
        VBox toolbar = new VBox();
        toolbar.setPadding(new Insets(10,10,10,10));
        toolbar.prefWidthProperty().bind(primaryStage.widthProperty().divide(5));

        followingNode = new Circle();
        followingNode.visibleProperty().bind(addingNodeProperty);
        followingNode.radiusProperty().bind(Node.nodeRadiusProperty());
        followingNode.setFill(Color.TRANSPARENT);
        followingNode.setStroke(Color.RED);

        addNodeButton = new Button("Add node");
        addNodeButton.setOnAction(e -> {
            addingNodeProperty.setValue(!addingNodeProperty.get());
        });
        editNodeShapeButton = new Button("Edit all nodes");
        editNodeShapeButton.setOnAction(e -> {
            showEditNodeShapeDialog();
        });

        editEdgeShapeButton = new Button("Edit all edges");
        editEdgeShapeButton.setOnAction(e -> {
            showEditEdgeShapeDialog();
        });

        clearButton = new Button("Clear graph");
        clearButton.setOnAction(e -> {
            graph.clear();
            group.getChildren().clear();
            group.getChildren().add(followingNode);
        });
        clearButton.disableProperty().bind(Bindings.size(graph.getNodes()).greaterThan(0).not());

        gridCheckBox = new CheckBox("Show grid lines");
        gridCheckBox.setSelected(false);

        gridShownProperty.bind(gridCheckBox.selectedProperty());

        gridSepField = new TextField();
        gridSepField.setPromptText("Grid separation");
        gridSepField.disableProperty().bind(gridCheckBox.selectedProperty().not());
        gridSepField.textProperty().addListener((e,o,n) -> {
            boolean testInteger = testGridSep(n);
            if(testInteger) {
                gridSepX = Integer.parseInt(n);
                gridSepY = gridSepX;
                setGridLines(gridSepX, gridSepY);
            }
        });

        generateLatexButton = new Button("Generate LaTeX");
        generateLatexButton.disableProperty().bind(Bindings.size(graph.getNodes()).greaterThan(0).not());
        generateLatexButton.setOnAction(e -> {
            String latex = graph.toLatex();
            System.out.println(latex);
            StringSelection stringSelection = new StringSelection(latex);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Tikz code copied to clipboard");
            alert.showAndWait();
        });
        helpButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Press the Add Node button to add vertices to the graph. " +
                    "Right click to finish adding nodes. Left click and drag a node to move it around, " +
                    "and right click and drag from one node to another to create an edge between them. " +
                    "Click on any edge and move the two green circles (representing the Bezier control points) " +
                    "to curve the edge. Double click an edge or node to remove it from the graph. " +
                    "Once your graph is finished, press the Generate Latex button to copy the tikz code " +
                    "into your clipboard. Note: please include the following in your preamble: \n" +
                    "\\usepackage{tikz}\n" +
                    "\\usepackage{pgfmath}\n" +
                    "\\usetikzlibrary{arrows}\n" +
                    "\\usetikzlibrary{shapes}");
            alert.showAndWait();
            //System.out.println("Press the Add Node button to add vertices to the graph. Right click to finish adding nodes. \nLeft click drag a node to move it around, and right click drag from one node to another to create an edge between them. \nOnce your graph is finished, press the Generate Latex button to copy the tikz code into your clipboard.");
        });
        toolbar.getChildren().addAll(addNodeButton, editNodeShapeButton, editEdgeShapeButton, clearButton, generateLatexButton, helpButton, gridCheckBox, gridSepField, text);

        Pane pane = new Pane();
        pane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        borderPane.setCenter(pane);
        borderPane.setRight(toolbar);

        //setGridLines(gridSepX, gridSepY);


        pane.getChildren().add(group);
        //pane.getChildren().add(grid);
        group.getChildren().add(followingNode);
        group.setCursor(Cursor.HAND);

        gridCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                setGridLines(gridSepX, gridSepY);
                pane.getChildren().add(grid);
            }
            else {
                pane.getChildren().remove(grid);
            }
        });

        pane.setOnMouseMoved(e -> {
            //int centerX = gridSepX * ((int) (e.getX()/gridSepX + 0.5));
            //int centerY = gridSepY * ((int) (e.getY()/gridSepY + 0.5));
            followingNode.setCenterX(computeX(e.getX()));
            followingNode.setCenterY(computeY(e.getY()));
            //followingNode.setCenterX(e.getX());
            //followingNode.setCenterY(e.getY());
            String s = String.format("x = %3.0f, y = %3.0f", e.getX(), e.getY());
            text.setText(s);
        });
        pane.setOnMousePressed(e -> {
            mouseDragStartPosition = new Point2D(e.getX(), e.getY());
            translateStartPosition = new Point2D(group.getTranslateX(), group.getTranslateY());
            if(e.getButton().equals(MouseButton.PRIMARY)) {
                if(addingNodeProperty.get()) {
                    //int centerX = gridSepX * ((int) (e.getX()/gridSepX + 0.5));
                    //int centerY = gridSepY * ((int) (e.getY()/gridSepY + 0.5));
                    //int centerX = (int) e.getX();
                    //int centerY = (int) e.getY();
                    addNode(computeX(e.getX()), computeY(e.getY()));
                }
                else {
                    Point2D clickPoint = new Point2D(e.getX(), e.getY());
                    boolean notClickedOnNode = true;
                    for (javafx.scene.Node node : group.getChildren()) {
                        if ((node instanceof Node || node instanceof BezierControlNode) && node.contains(clickPoint)) {
                            notClickedOnNode = false;
                        }
                    }

                    if (notClickedOnNode) {
                        Optional<Pair<BezierEdge, Double>> closestEdge = graph.nearestEdge(clickPoint);
                        if (closestEdge.isPresent()) {
                            double dist = closestEdge.get().getValue();
                            if (dist >= edgeThreshold) {
                                if (selectedEdge != null) {
                                    group.getChildren().remove(selectedEdge.control1);
                                    group.getChildren().remove(selectedEdge.control2);
                                    selectedEdge = null;
                                }
                            } else {
                                BezierEdge edge = closestEdge.get().getKey();
                                if (selectedEdge != null) {
                                    group.getChildren().removeAll(selectedEdge.control1, selectedEdge.control2);
                                }
                                selectedEdge = edge;
                                group.getChildren().addAll(selectedEdge.control1, selectedEdge.control2);

                            }
                        }
                    }
//                        if(!group.contains(new Point2D(e.getX(), e.getY()))) {
//                            group.getChildren().remove(selectedEdge.control1);
//                            group.getChildren().remove(selectedEdge.control2);
//                            selectedEdge = null;
//                        }

                }
            }
            else {
                if(addingNodeProperty.get()) {
                    addingNodeProperty.setValue(false);
                }
            }

        });
        pane.setOnMouseReleased(e -> {
           mouseDragStartPosition = null;
           translateStartPosition = null;
        });
//        pane.setOnMouseReleased(e -> {
//            if(e.getButton().equals(MouseButton.SECONDARY)) {
//                group.getChildren().remove(tempEdge);
//                tempEdge = null;
//            }
//        });
        pane.setOnMouseDragged(e -> {
//            if(!group.contains(mouseDragStartPosition) && e.getButton().equals(MouseButton.PRIMARY)) {
//                double dx = e.getX() - mouseDragStartPosition.getX();
//                double dy = e.getY() - mouseDragStartPosition.getY();
//                group.setTranslateX(translateStartPosition.getX() + dx);
//                group.setTranslateY(translateStartPosition.getY() + dy);
//            }
            if(e.getButton().equals(MouseButton.SECONDARY) && tempEdge != null) {
                tempEdge.setEndX(e.getX());
                tempEdge.setEndY(e.getY());
                double dist = Math.hypot(tempEdge.getEndX() - tempEdge.getStartX(), tempEdge.getStartY() - tempEdge.getEndY());
                if(dist>3*Node.getNodeRadius() && !group.getChildren().contains(tempEdge)) group.getChildren().add(tempEdge);
                else if (dist<=3*Node.getNodeRadius()) group.getChildren().remove(tempEdge);
                int[] linearControls = BezierEdge.computeLinearControls(tempEdge.getStartX(), e.getX(), tempEdge.getStartY(), e.getY());
                tempEdge.setControlX1(linearControls[0]);
                tempEdge.setControlY1(linearControls[1]);
                tempEdge.setControlX2(linearControls[2]);
                tempEdge.setControlY2(linearControls[3]);
            }
        });

        Scene root = new Scene(borderPane);
        primaryStage.setScene(root);
        primaryStage.setWidth(800);
        primaryStage.setHeight(500);
        primaryStage.setTitle("Tikz graph generator");
        primaryStage.show();
    }

    public void addEdge(Node startingNode, Node endingNode) {
        BezierEdge edge = new BezierEdge(startingNode, endingNode, graph);
        edge.toBack();
        if(graph.getEdges().contains(edge)) {
            graph.getEdges().remove(edge);
            group.getChildren().remove(edge);
        }
        else {
            group.getChildren().add(edge);
            graph.add(edge);
            edge.setOnMouseClicked(e -> {
                if(e.getClickCount()==1) {
//                    if (selectedEdge != null) {
//                        group.getChildren().removeAll(selectedEdge.control1, selectedEdge.control2);
//                    }
//                    selectedEdge = edge;
//                    group.getChildren().addAll(selectedEdge.control1, selectedEdge.control2);
                }
                else if(e.getClickCount()==2){
                    group.getChildren().remove(edge.control1);
                    group.getChildren().remove(edge.control2);
                    group.getChildren().remove(edge);
                    graph.remove(edge);
                }
            });

        }
    }
    public void addNode(int centerX, int centerY) {
        for(Node n: graph.getNodes()) {
            double dist = Math.sqrt((centerX-n.getCenterX())*(centerX-n.getCenterX()) + (centerY-n.getCenterY())*(centerY-n.getCenterY()));
            if(dist<=2*n.getRadius()) {
                return;
            }
        }

        Node node = new Node(centerX, centerY, graph);
        //System.out.println("DEBUG - " + node.getRescaledX()+", "+node.getRescaledY());
        graph.add(node);

        node.setOnMouseClicked(e -> {
            if(e.getClickCount()==1) {
                if (addingNodeProperty.get()) return;
//            if(selectedNode != null) {
//                selectedNode.setStroke(Node.normalColor); //TODO: set focus?
//            }
                selectedNode = node;
                //node.setStroke(Node.focusedColor);
            }
            else if(e.getClickCount()==2) {
                group.getChildren().remove(node);
                ArrayList<BezierEdge> adjacentEdges = (ArrayList<BezierEdge>) graph.getAllAdjacentEdges(node);
                for(BezierEdge edge : adjacentEdges) {
                    group.getChildren().remove(edge.control1);
                    group.getChildren().remove(edge.control2);
                    group.getChildren().remove(edge);
                    graph.remove(edge);
                }
                graph.remove(node);
            }
        });
        node.setOnMousePressed(e -> {
            if(e.getButton().equals(MouseButton.SECONDARY)) {
                tempEdge = new BezierEdge(node, node.getCenterX(), node.getCenterY(), graph);
                //group.getChildren().add(tempEdge);
            }
            if(e.getButton().equals(MouseButton.PRIMARY)) {
                tempPoint = new Point2D(node.getCenterX(), node.getCenterY());
            }
        });
        node.setOnMouseReleased(e -> {
            if(e.getButton().equals(MouseButton.SECONDARY) && tempEdge !=null) {

                Node closestNode = null;
                double minDistance = Double.MAX_VALUE;
                for(Node n: graph.getNodes()) {
                    double dist = Math.sqrt((e.getX() - n.getCenterX())*(e.getX() - n.getCenterX()) + (e.getY() - n.getCenterY())*(e.getY() - n.getCenterY()));
                    if(dist < minDistance) {
                        minDistance = dist;
                        closestNode = n;
                    }
                }

                if(minDistance < threshold && closestNode != null && closestNode != node) {
//                    BezierEdge edge = new BezierEdge(node, closestNode, graph);
//                    if(graph.getEdges().contains(edge)) {
//                        graph.getEdges().remove(edge);
//                        group.getChildren().remove(edge);
//                    }
//                    else {
//                        group.getChildren().add(edge);
//                        graph.add(edge);
//                    }
                    addEdge(node, closestNode);
                }
                group.getChildren().remove(tempEdge);
                tempEdge = null;
            }
        });
        node.setOnMouseDragged(e -> {
            if(e.getButton().equals(MouseButton.PRIMARY)) {
                node.setCenterX(computeX(e.getX()));
                node.setCenterY(computeY(e.getY()));

                Point2D currentPos = new Point2D(computeX(e.getX()), computeY(e.getY()));

                ArrayList<BezierEdge> neighboringEdges = (ArrayList<BezierEdge>) graph.getAllAdjacentEdges(node);
                for(BezierEdge edge: neighboringEdges) {
                    Node pivotNode = edge.getStartingNode().equals(node)? edge.getEndingNode() : edge.getStartingNode();
                    Point2D pivot = new Point2D(pivotNode.getCenterX(), pivotNode.getCenterY());

                    Point2D transform = getTransformation(tempPoint.subtract(pivot), currentPos.subtract(pivot));

                    Point2D c1 = new Point2D(edge.control1.getCenterX(), edge.control1.getCenterY());
                    Point2D c2 = new Point2D(edge.control2.getCenterX(), edge.control2.getCenterY());
                    Point2D newC1 = applyTransformation(c1.subtract(pivot), transform).add(pivot);
                    Point2D newC2 = applyTransformation(c2.subtract(pivot), transform).add(pivot);
                    edge.control1.setCenterX(newC1.getX());
                    edge.control1.setCenterY(newC1.getY());
                    edge.control2.setCenterX(newC2.getX());
                    edge.control2.setCenterY(newC2.getY());

                }
                tempPoint = currentPos;

            }
        });
        group.getChildren().add(node);
    }
    public Point2D getTransformation(Point2D p1, Point2D p2) {
        double x = (p1.getX()*p2.getX()+p1.getY()*p2.getY())/(p1.getX()*p1.getX()+p1.getY()*p1.getY());
        double y = (p1.getX()*p2.getY()-p1.getY()*p2.getX())/(p1.getX()*p1.getX()+p1.getY()*p1.getY());
        return new Point2D(x,y);
    }
    public Point2D applyTransformation(Point2D p, Point2D transform) {
        double x = p.getX()*transform.getX()-p.getY()*transform.getY();
        double y = p.getX()*transform.getY()+p.getY()*transform.getX();
        return new Point2D(x,y);
    }
    public void showEditEdgeShapeDialog() {
        Dialog<Object[]> dialog = new Dialog<>();
        dialog.setTitle("Edit Edges");
        dialog.setHeaderText("Edit the parameters of all edges");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField strokeWidthField = new TextField();
        strokeWidthField.setText(String.format("%.1f", BezierEdge.getWidth()));
        TextField strokeField = new TextField();
        strokeField.setText(formatColorString(BezierEdge.getStrokeColor()));

        grid.add(new Label("Edit stroke width:"), 0, 0);
        grid.add(strokeWidthField, 1, 0);
        grid.add(new Label("Edit stroke (rgba):"), 0, 1);
        grid.add(strokeField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Button applyButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.APPLY);
        applyButton.disableProperty().bind(new BooleanBinding() {
            {bind(strokeField.textProperty(), strokeWidthField.textProperty());}
            @Override
            protected boolean computeValue() {
                return !testDouble(strokeWidthField.getText()) || !testColor(strokeField.getText());
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.APPLY) {
                String strokeWidthFieldText = strokeWidthField.getText();
                String strokeFieldText = strokeField.getText();
                //Pair<Color, Color> fillAndStroke = new Pair<>(parseColor(colorFieldText), parseColor(strokeFieldText));
                double w = Double.parseDouble(strokeWidthFieldText);
                Color stroke = parseColor(strokeFieldText);
                return new Object[] {w,stroke};
            }
            return null;
        });
        Optional<Object[]> result = dialog.showAndWait();
        if(result.isPresent()) {
            Object[] parameters = result.get();
            BezierEdge.setWidth((double) parameters[0]);
            BezierEdge.setStrokeColor((Color) parameters[1]);
        }
    }
    public void showEditNodeShapeDialog() {
        Dialog<Object[]> dialog = new Dialog<>();
        dialog.setTitle("Edit Nodes");
        dialog.setHeaderText("Edit the parameters of all nodes");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.APPLY);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField radiusField = new TextField();
        radiusField.setText(Node.getNodeRadius()+"");
        TextField strokeWidthField = new TextField();
        strokeWidthField.setText(String.format("%.1f", Node.getWidth()));
        //radiusField.setPromptText("Radius");
        TextField colorField = new TextField();
        //colorField.setText(Node.getFillColor().getRed()+", "+ Node.getFillColor().getGreen()+", "+ Node.getFillColor().getBlue()+", "+ Node.getFillColor().getOpacity());
        colorField.setText(formatColorString(Node.getFillColor()));
        TextField strokeField = new TextField();
        //strokeField.setText(Node.getStrokeColor().getRed()+", "+ Node.getStrokeColor().getGreen()+", "+ Node.getStrokeColor().getBlue()+", "+ Node.getStrokeColor().getOpacity());
        strokeField.setText(formatColorString(Node.getStrokeColor()));

        grid.add(new Label("Edit radius:"), 0, 0);
        grid.add(radiusField, 1, 0);
        grid.add(new Label("Edit stroke width:"), 0, 1);
        grid.add(strokeWidthField, 1, 1);
        grid.add(new Label("Edit fill (rgba):"), 0, 2);
        grid.add(colorField, 1, 2);
        grid.add(new Label("Edit stroke (rgba):"), 0, 3);
        grid.add(strokeField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Button applyButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.APPLY);
        applyButton.disableProperty().bind(new BooleanBinding() {
            {bind(radiusField.textProperty(), strokeWidthField.textProperty(), colorField.textProperty(), strokeField.textProperty());}
            @Override
            protected boolean computeValue() {
                return !testInt(radiusField.getText()) || !testDouble(strokeWidthField.getText()) || !testColor(colorField.getText()) || !testColor(strokeField.getText());
            }
        });

        Platform.runLater(() -> radiusField.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.APPLY) {
                String radiusFieldText = radiusField.getText();
                String strokeWidthFieldText = strokeWidthField.getText();
                String colorFieldText = colorField.getText();
                String strokeFieldText = strokeField.getText();
                //Pair<Color, Color> fillAndStroke = new Pair<>(parseColor(colorFieldText), parseColor(strokeFieldText));
                int r = Integer.parseInt(radiusFieldText);
                double w = Double.parseDouble(strokeWidthFieldText);
                Color fill = parseColor(colorFieldText);
                Color stroke = parseColor(strokeFieldText);
                return new Object[] {r,w,fill,stroke};
            }
            return null;
        });

        Optional<Object[]> result = dialog.showAndWait();
        if(result.isPresent()) {
            Object[] parameters = result.get();
            Node.setNodeRadius((int) parameters[0]);
//            if(Node.getNodeRadius() > gridSepX) {
//                gridSepX = Node.getNodeRadius();
//                gridSepY = Node
//            }
            Node.setWidth((double) parameters[1]);
            Node.setFillColor((Color) parameters[2]);
            Node.setStrokeColor((Color) parameters[3]);
        }

    }
    public boolean testGridSep(String s) {
        try {
            int x = Integer.parseInt(s);
            //if(x >= 2 * Node.getNodeRadius()) return true;
            if(x >= 5) return true;
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public boolean testInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public boolean testDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public boolean testColor(String s) {
        if(s.length()==0) return false;
        String[] rgba = s.split(",");
        if(rgba.length != 4) return false;
        for(String component : rgba) {
            String trimmed = component.trim();
            try {
                Double d = Double.parseDouble(trimmed);
                if(d<0 || d>1) return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }
    public Color parseColor(String s) {
        String[] rgb = s.split(",");
        Double r = Double.parseDouble(rgb[0].trim());
        Double g = Double.parseDouble(rgb[1].trim());
        Double b = Double.parseDouble(rgb[2].trim());
        Double a = Double.parseDouble(rgb[3].trim());
        return new Color(r,g,b,a);
    }
    public String formatColorString(Color c) {
        return String.format("%.1f, %.1f, %.1f, %.1f", c.getRed(), c.getGreen(), c.getBlue(), c.getOpacity());
    }
//    public void updateAllEdges() {
//        double cutoff = radius + Node.strokeWidth/2;
//        Arrow.setCutoff(cutoff);
//    }


    public int computeX(double x) {
        return gridShownProperty.get()? gridSepX * ((int) (x/gridSepX + 0.5)) : (int) x;
    }
    public int computeY(double y) {
        return gridShownProperty.get()? gridSepY * ((int) (y/gridSepY + 0.5)) : (int) y;
    }

    public void setGridLines(int gridSepX, int gridSepY) {
        grid.getChildren().clear();
        for(int i = 0; i<4000; i+=gridSepX) {
            Line l = new Line(i, 0, i, 2500);
            l.setStrokeWidth(0.3);
            grid.getChildren().add(l);
            l.toBack();
        }
        for(int i = 0; i<2500; i+=gridSepY) {
            Line l = new Line(0, i, 4000, i);
            l.setStrokeWidth(0.3);
            grid.getChildren().add(l);
            l.toBack();
        }
        //grid.toBack();
    }

}
