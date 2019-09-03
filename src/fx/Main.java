package fx;

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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import serializable.BezierEdgeSerializable;
import serializable.GraphSerializable;
import serializable.LabeledNodeSerializable;
import serializable.OptionsSerializable;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.BiConsumer;

public class Main extends Application {

    /*
    Summary of TODOS
    TODO: Clean up fx.LabeledNode and fx.Graph
    TODO: Integrate with djf and jtps
    TODO: Rework mouselisteners to make more concise and modular.
    TODO: Independent colors for each node/edge (maybe automatically find chromatic number)
    TODO: Label distance needs to be editable
     */

    Button addNodeButton;
    Button editNodeShapeButton;
    Button editEdgeShapeButton;
    Button clearButton;
    Button generateLatexButton;
    Button saveAsButton;
    Button loadButton;
    Button helpButton = new Button("Help");
    CheckBox gridCheckBox;
    TextField gridSepField;
    CheckBox directedBox;
    Text text = new Text("");

    Group group;
    Group grid = new Group();

    BooleanProperty gridShownProperty = new SimpleBooleanProperty(Defaults.DEFAULT_GRID);
    BooleanProperty addingNodeProperty = new SimpleBooleanProperty(false);
    final double threshold = 50;
    final double edgeThreshold = 30;

    Circle followingNode;
    Node selectedNode;
    BezierEdge selectedEdge;
    BezierEdge tempEdge;

    Graph graph = new Graph();

    Point2D tempPoint = new Point2D(-1,-1);

    int gridSepX = Defaults.DEFAULT_GRID_SEP;
    int gridSepY = Defaults.DEFAULT_GRID_SEP;

    Point2D mouseDragStartPosition;
    Point2D translateStartPosition;

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
        toolbar.setBackground(new Background(new BackgroundFill(new Color(0.9, 0.9, 0.9, 1), CornerRadii.EMPTY, Insets.EMPTY)));

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
        saveAsButton = new Button("Save as");
        saveAsButton.disableProperty().bind(generateLatexButton.disableProperty());
        saveAsButton.setOnAction(e -> {
            serializeGraph(primaryStage);
        });
        loadButton = new Button("Load");
        loadButton.setOnAction(e -> {
            deserializeGraph(primaryStage);
        });
        helpButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Press the Add Node button to add vertices to the graph. " +
                    "Right click to finish adding nodes. Left click and drag a node to move it around, " +
                    "and right click and drag from one node to another to create an edge between them. " +
                    "Click on any edge and move the two green circles (representing the Bezier control points) " +
                    "to curve the edge. Double click an edge or node to remove it from the graph. " +
                    "Edit the color/stroke weight of nodes and edges by clicking the edit node and edit edge buttons. " +
                    "Edit a node's label by CTRL-clicking on it. The position of a node's label is given in degrees." +
                    "Once your graph is finished, press the Generate Latex button to copy the tikz code " +
                    "into your clipboard. You may also save your graph and load it later. " +
                    "Note: please include the following in your preamble: \n" +
                    "\\usepackage{tikz}\n" +
                    "\\usepackage{pgfmath}\n" +
                    "\\usetikzlibrary{arrows}\n" +
                    "\\usetikzlibrary{shapes}");
            alert.showAndWait();
        });

        gridCheckBox = new CheckBox("Show grid lines");
        gridCheckBox.setSelected(Defaults.DEFAULT_GRID);
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

        directedBox = new CheckBox("Directed");
        directedBox.setSelected(true);
        BezierArrow.directedProperty.bind(directedBox.selectedProperty());

        toolbar.getChildren().addAll(addNodeButton, editNodeShapeButton, editEdgeShapeButton, clearButton, generateLatexButton, saveAsButton, loadButton, helpButton, gridCheckBox, gridSepField, directedBox, text);

        Pane pane = new Pane();
        pane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        borderPane.setCenter(pane);
        borderPane.setRight(toolbar);
        pane.getChildren().add(group);

        group.getChildren().add(followingNode);
        group.setCursor(Cursor.HAND);

        gridCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                setGridLines(gridSepX, gridSepY);
                pane.getChildren().add(0, grid);
            }
            else {
                pane.getChildren().remove(grid);
            }
        });

        pane.setOnMouseMoved(e -> {
            followingNode.setCenterX(computeX(e.getX()));
            followingNode.setCenterY(computeY(e.getY()));
            String s = String.format("x = %3.0f, y = %3.0f", e.getX(), e.getY());
            text.setText(s);
        });
        pane.setOnMousePressed(e -> {
            mouseDragStartPosition = new Point2D(e.getX(), e.getY());
            translateStartPosition = new Point2D(group.getTranslateX(), group.getTranslateY());
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                if (addingNodeProperty.get()) {
                    addNode(computeX(e.getX()), computeY(e.getY()));
                } else {
                    Point2D clickPoint = new Point2D(e.getX(), e.getY());
                    boolean notClickedOnNode = true;
                    for (javafx.scene.Node node : group.getChildren()) {
                        if ((node instanceof LabeledNode || node instanceof BezierControlNode) && node.contains(clickPoint)) {
                            notClickedOnNode = false;
                        }
                    }

                    if (notClickedOnNode) {
                        if(e.getClickCount()==1) {
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
                        else if(e.getClickCount()==2) {
                            Optional<Pair<BezierEdge, Double>> closestEdge = graph.nearestEdge(clickPoint);
                            if (closestEdge.isPresent()) {
                                double dist = closestEdge.get().getValue();
                                if (dist < edgeThreshold) {
                                    BezierEdge edge = closestEdge.get().getKey();
                                    group.getChildren().remove(edge.control1);
                                    group.getChildren().remove(edge.control2);
                                    group.getChildren().remove(edge);
                                    graph.remove(edge);
                                }
                            }
                        }
                    }
                }
            } else {
                if (addingNodeProperty.get()) {
                    addingNodeProperty.setValue(false);
                }
            }

            Optional<LabeledNode> nodeClicked = graph.getNodeContainingPoint(new Point2D(e.getX(), e.getY()));
            if (nodeClicked.isPresent()) {
                LabeledNode node = nodeClicked.get();
                if (e.getButton().equals(MouseButton.SECONDARY)) {
                    tempEdge = new BezierEdge(node, node.getCenterX(), node.getCenterY());
                }
                if (e.getButton().equals(MouseButton.PRIMARY)) {
                    tempPoint = new Point2D(node.getCenterX(), node.getCenterY());
                }
            }

        });
        pane.setOnMouseReleased(e -> {
            mouseDragStartPosition = null;
            translateStartPosition = null;
        });
        pane.setOnMouseDragged(e -> {
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

        pane.setOnMouseClicked(e -> {
            Point2D clickPoint = new Point2D(e.getX(), e.getY());
            Optional<LabeledNode> nodeOptional = graph.getNodeContainingPoint(clickPoint);
            if(nodeOptional.isPresent()) {
                LabeledNode node = nodeOptional.get();
                if(e.getClickCount()==1) {
                    if(e.isControlDown()) {
                        showEditNodeLabelDialog(node);
                    }
                    else {
                        if (addingNodeProperty.get()) return;
                        selectedNode = node.node;
                    }
                }
//                else if(e.getClickCount()==2) {
//                    showEditNodeLabelDialog(node);
//                }
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
            }
        });

        if(Defaults.DEFAULT_GRID) {
            setGridLines(gridSepX, gridSepY);
            pane.getChildren().add(0, grid);
        }

        Scene root = new Scene(borderPane);
        primaryStage.setScene(root);
        primaryStage.setWidth(800);
        primaryStage.setHeight(500);
        primaryStage.setTitle("Tikz graph generator");
        primaryStage.show();
    }

    private void deserializeGraph(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph files", "*.graph"));
        fileChooser.setInitialDirectory(new File("./graphs"));
        File dest = fileChooser.showOpenDialog(stage);
        if(dest!=null) {
            try {
                FileInputStream fis = new FileInputStream(dest);
                ObjectInputStream ois = new ObjectInputStream(fis);
                GraphSerializable graphSerializable = (GraphSerializable) ois.readObject();
                loadGraph(graphSerializable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void loadGraph(GraphSerializable graphSerializable) {
        clearButton.fire();

        OptionsSerializable optionsSerializable = graphSerializable.getOptions();
        Node.setNodeRadius(optionsSerializable.getRadius());
        Node.setWidth(optionsSerializable.getNodeWidth());
        Node.setFillColor(convertColor(optionsSerializable.getNodeFillColor()));
        Node.setStrokeColor(convertColor(optionsSerializable.getNodeStrokeColor()));
        BezierEdge.setWidth(optionsSerializable.getEdgeWidth());
        BezierEdge.setStrokeColor(convertColor(optionsSerializable.getEdgeStrokeColor()));

        ArrayList<LabeledNodeSerializable> serializedNodes = graphSerializable.getNodes();
        serializedNodes.forEach(e -> {
            LabeledNode labeledNode = getNodeFromSerializable(e);
            graph.add(labeledNode);
            group.getChildren().add(labeledNode);
            setNodeListeners(labeledNode);
        });
        ArrayList<BezierEdgeSerializable> serializedEdges = graphSerializable.getEdges();
        serializedEdges.forEach(e -> {
            LabeledNode startingNode = graph.getNodes().get(e.getStartingNodeIndex());
            LabeledNode endingNode = graph.getNodes().get(e.getEndingNodeIndex());
            BezierEdge bezierEdge = new BezierEdge(startingNode, endingNode, e.getControl1().getX(), e.getControl1().getY(), e.getControl2().getX(), e.getControl2().getY());
            graph.add(bezierEdge);
            group.getChildren().add(bezierEdge);
            setEdgeListeners(bezierEdge);
        });
    }
    private LabeledNode getNodeFromSerializable(LabeledNodeSerializable nodeSerializable) {
        Text label = new Text(nodeSerializable.getLabel());
        Node node = new Node(nodeSerializable.getCenterX(), nodeSerializable.getCenterY());
        LabeledNode labeledNode = new LabeledNode(node, label);
        labeledNode.setLabelAngle(nodeSerializable.getLabelAngle());
        return labeledNode;
    }

    private Color convertColor(double[] c) {
        return new Color(c[0],c[1],c[2],c[3]);
    }

    private void serializeGraph(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph files", "*.graph"));
        fileChooser.setInitialDirectory(new File("./graphs"));
        File dest = fileChooser.showSaveDialog(stage);
        if(dest!=null) {
            try {
                FileOutputStream fos = new FileOutputStream(dest);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                GraphSerializable graphSerializable = new GraphSerializable(graph);
                oos.writeObject(graphSerializable);
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addEdge(LabeledNode startingNode, LabeledNode endingNode) {
        BezierEdge edge = new BezierEdge(startingNode, endingNode);
        edge.toBack();
        if(graph.getEdges().contains(edge)) {
            graph.getEdges().remove(edge);
            group.getChildren().remove(edge);
        }
        else {
            group.getChildren().add(edge);
            graph.add(edge);

            setEdgeListeners(edge);

        }
    }
    public void setEdgeListeners(BezierEdge edge) {
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
    public void addNode(int centerX, int centerY) {
        for(LabeledNode n: graph.getNodes()) {
            double dist = Math.sqrt((centerX-n.getCenterX())*(centerX-n.getCenterX()) + (centerY-n.getCenterY())*(centerY-n.getCenterY()));
            if(dist<=2*Node.getNodeRadius()) {
                return;
            }
        }

        Node node = new Node(centerX, centerY);
        int numNodes = graph.getNodes().size()+1;
        LabeledNode labeledNode = new LabeledNode(node, new Text(numNodes+""));

        graph.add(labeledNode);

        setNodeListeners(labeledNode);
        //group.getChildren().add(node);
        group.getChildren().add(labeledNode);
        node.toFront();
    }
    public void setNodeListeners(LabeledNode labeledNode) {
        Node node = labeledNode.node;
        node.setOnMouseReleased(e -> {
            if(e.getButton().equals(MouseButton.SECONDARY) && tempEdge !=null) {

                LabeledNode closestNode = null;
                double minDistance = Double.MAX_VALUE;
                for(LabeledNode n: graph.getNodes()) {
                    double dist = Math.sqrt((e.getX() - n.getCenterX())*(e.getX() - n.getCenterX()) + (e.getY() - n.getCenterY())*(e.getY() - n.getCenterY()));
                    if(dist < minDistance) {
                        minDistance = dist;
                        closestNode = n;
                    }
                }

                if(minDistance < threshold && closestNode != null && closestNode != labeledNode) {
//                    fx.BezierEdge edge = new fx.BezierEdge(node, closestNode, graph);
//                    if(graph.getEdges().contains(edge)) {
//                        graph.getEdges().remove(edge);
//                        group.getChildren().remove(edge);
//                    }
//                    else {
//                        group.getChildren().add(edge);
//                        graph.add(edge);
//                    }
                    addEdge(labeledNode, closestNode);
                }
                group.getChildren().remove(tempEdge);
                tempEdge = null;
            }
        });

        node.setOnMouseDragged(e -> {
            if(e.getButton().equals(MouseButton.PRIMARY)) {
                double x = computeX(e.getX());
                double y = computeY(e.getY());

                if(!testPoint(x,y,labeledNode)) return;

                node.setCenterX(x);
                node.setCenterY(y);

                Point2D currentPos = new Point2D(computeX(e.getX()), computeY(e.getY()));

                ArrayList<BezierEdge> neighboringEdges = (ArrayList<BezierEdge>) graph.getAllAdjacentEdges(labeledNode);
                for(BezierEdge edge: neighboringEdges) {
                    LabeledNode pivotNode = edge.getStartingNode().equals(labeledNode)? edge.getEndingNode() : edge.getStartingNode();
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
    }
    public boolean testPoint(double x, double y, LabeledNode labeledNode) {
        for (LabeledNode n : graph.getNodes()) {
            if (n == labeledNode) continue;
            double dist = Math.sqrt((x - n.getCenterX()) * (x - n.getCenterX()) + (y - n.getCenterY()) * (y - n.getCenterY()));
            if (dist <= 2 * Node.getNodeRadius()) {
                return false;
            }
        }
        return true;
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
    public void showEditNodeLabelDialog(LabeledNode node) {
        Dialog<Object[]> dialog = new Dialog<>();
        dialog.setTitle("Edit node label");
        dialog.setHeaderText("Edit this node's label");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.APPLY);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField labelField = new TextField();
        labelField.setText(node.getLabel());
        TextField angleField = new TextField();
        angleField.setText(String.format("%.1f", node.getLabelAngle()));

        grid.add(new Label("Edit label:"), 0, 0);
        grid.add(labelField, 1, 0);
        grid.add(new Label("Edit label angle:"), 0, 1);
        grid.add(angleField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Button applyButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.APPLY);
        applyButton.disableProperty().bind(new BooleanBinding() {
            {bind(labelField.textProperty(), angleField.textProperty());}
            @Override
            protected boolean computeValue() {
                return !testDouble(angleField.getText());
            }
        });

        Platform.runLater(labelField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.APPLY) {
                String labelFieldText = labelField.getText();
                String angleFieldText = angleField.getText();
                double a = Double.parseDouble(angleFieldText);
                return new Object[] {labelFieldText, a};
            }
            return null;
        });

        Optional<Object[]> result = dialog.showAndWait();
        if(result.isPresent()) {
            Object[] parameters = result.get();
            node.setLabel((String) parameters[0]);
            node.setLabelAngle((Double) parameters[1]);
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
        TextField colorField = new TextField();
        colorField.setText(formatColorString(Node.getFillColor()));
        TextField strokeField = new TextField();
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

        Platform.runLater(radiusField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.APPLY) {
                String radiusFieldText = radiusField.getText();
                String strokeWidthFieldText = strokeWidthField.getText();
                String colorFieldText = colorField.getText();
                String strokeFieldText = strokeField.getText();
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
            Node.setWidth((double) parameters[1]);
            Node.setFillColor((Color) parameters[2]);
            Node.setStrokeColor((Color) parameters[3]);
        }

    }
    public boolean testGridSep(String s) {
        try {
            int x = Integer.parseInt(s);
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
    }

}
