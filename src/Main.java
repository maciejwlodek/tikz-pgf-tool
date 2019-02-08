import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
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
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Optional;

public class Main extends Application {

    /*
    Summary of TODOS
    TODO: Fix rescaling of coordinates
    TODO: Allow deletion of nodes by drawing reverse arrow
    TODO: Implement directed vs undirected
    TODO: Allow editing of labels and label positions
     */


    Group group;
    Button addNodeButton;
    Button generateLatexButton;
    Button editNodeShapeButton;
    Button editEdgeShapeButton;
    BooleanProperty addingNodeProperty = new SimpleBooleanProperty(false);
    double threshold = 50;
    Circle followingNode;
    Node selectedNode;
    Text text = new Text("");
    //Text helpText = new Text("Press the Add Node \n button to add vertices \n to the graph. \n Right click to \n finish adding nodes. \n Left click drag \n a node to move \n it around, and \n right click drag \n from one node \n to another to create \n an edge between them.");
    Button helpButton = new Button("Help");

    Graph graph = new Graph();
    Edge tempEdge;

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
            alert.setContentText("Press the Add Node button to add vertices to the graph. Right click to finish adding nodes. Left click and drag a node to move it around, and right click and drag from one node to another to create an edge between them. Redraw an edge to remove it from the graph. Once your graph is finished, press the Generate Latex button to copy the tikz code into your clipboard.");
            alert.showAndWait();
            //System.out.println("Press the Add Node button to add vertices to the graph. Right click to finish adding nodes. \nLeft click drag a node to move it around, and right click drag from one node to another to create an edge between them. \nOnce your graph is finished, press the Generate Latex button to copy the tikz code into your clipboard.");
        });
        toolbar.getChildren().addAll(addNodeButton, editNodeShapeButton, editEdgeShapeButton, generateLatexButton, helpButton, text);

        Pane pane = new Pane();
        pane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        borderPane.setCenter(pane);
        borderPane.setRight(toolbar);


        pane.getChildren().add(group);
        group.getChildren().add(followingNode);
        group.setCursor(Cursor.HAND);

        pane.setOnMouseMoved(e -> {
            followingNode.setCenterX(e.getX());
            followingNode.setCenterY(e.getY());
            String s = String.format("x = %3.0f, y = %3.0f", e.getX(), e.getY());
            text.setText(s);
        });
        pane.setOnMouseClicked(e -> {
            if(e.getButton().equals(MouseButton.PRIMARY)) {
                if(addingNodeProperty.get()) {
                    int centerX = (int) e.getX();
                    int centerY = (int) e.getY();
                    addNode(centerX, centerY);
                }
            }
            else {
                if(addingNodeProperty.get()) {
                    addingNodeProperty.setValue(false);
                }
            }

        });
//        pane.setOnMouseReleased(e -> {
//            if(e.getButton().equals(MouseButton.SECONDARY)) {
//                group.getChildren().remove(tempEdge);
//                tempEdge = null;
//            }
//        });
        pane.setOnMouseDragged(e -> {
            if(e.getButton().equals(MouseButton.SECONDARY) && tempEdge != null) {
                tempEdge.setEndX(e.getX());
                tempEdge.setEndY(e.getY());
            }
        });

        Scene root = new Scene(borderPane);
        primaryStage.setScene(root);
        primaryStage.setWidth(800);
        primaryStage.setHeight(500);
        primaryStage.setTitle("Tikz graph generator");
        primaryStage.show();
    }

    public void addEdge(Edge e) {

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
            if(addingNodeProperty.get()) return;
//            if(selectedNode != null) {
//                selectedNode.setStroke(Node.normalColor); //TODO: set focus?
//            }
            selectedNode = node;
            //node.setStroke(Node.focusedColor);
        });
        node.setOnMousePressed(e -> {
            if(e.getButton().equals(MouseButton.SECONDARY)) {
                tempEdge = new Edge(node, node.getCenterX(), node.getCenterY(), graph);
                group.getChildren().add(tempEdge);
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
                    Edge edge = new Edge(node, closestNode, graph);
                    if(graph.getEdges().contains(edge)) {
                        graph.getEdges().remove(edge);
                        group.getChildren().remove(edge);
                    }
                    else {
                        group.getChildren().add(edge);
                        graph.add(edge);
                    }
                }
                group.getChildren().remove(tempEdge);
                tempEdge = null;
            }
        });
        node.setOnMouseDragged(e -> {
            if(e.getButton().equals(MouseButton.PRIMARY)) {
                node.setCenterX(e.getX());
                node.setCenterY(e.getY());
                //List<Edge> incomingEdges = graph.getIncomingEdges(node);
                //for(Edge e: incomingEdges) incomingEdges.
            }
        });
        group.getChildren().add(node);
    }
    public void showEditEdgeShapeDialog() {
        Dialog<Object[]> dialog = new Dialog<>();
        dialog.setTitle("Edit Edges");
        dialog.setHeaderText("Edit the parameters of all edges");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.APPLY);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField strokeWidthField = new TextField();
        strokeWidthField.setText(String.format("%.1f", Edge.getWidth()));
        TextField strokeField = new TextField();
        strokeField.setText(formatColorString(Edge.getStrokeColor()));

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
                return !testDouble(strokeWidthField.getText()) && !testColor(strokeField.getText());
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
            Edge.setWidth((double) parameters[0]);
            Edge.setStrokeColor((Color) parameters[1]);
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
                return !testInt(radiusField.getText()) && !testDouble(strokeWidthField.getText()) && !testColor(colorField.getText()) && !testColor(strokeField.getText());
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
            Node.setWidth((double) parameters[1]);
            Node.setFillColor((Color) parameters[2]);
            Node.setStrokeColor((Color) parameters[3]);
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


}
