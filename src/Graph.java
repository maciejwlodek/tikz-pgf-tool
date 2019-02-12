import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.util.Pair;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Graph {

//    ArrayList<Node> nodes;
//    ArrayList<Edge> edges;
    ObservableList<Node> nodes;
    ObservableList<BezierEdge> edges;

    public Graph() {
//        nodes = new ArrayList<>();
//        edges = new ArrayList<>();
        nodes = FXCollections.observableArrayList();
        edges = FXCollections.observableArrayList();
    }
//    public Graph(ArrayList<Node> nodes, ArrayList<Edge> edges) {
//        this.nodes=nodes;
//        this.edges=edges;
//    }

    public void add(Node node){
        nodes.add(node);
    }
    public void add(BezierEdge edge) {
        edges.add(edge);
    }
    public void remove(Node node){
        nodes.remove(node);
    }
    public void remove(BezierEdge edge) {
        edges.remove(edge);
    }
//    public ArrayList<Node> getNodes() {
//        return nodes;
//    }
//    public ArrayList<Edge> getEdges() {
//        return edges;
//    }

    public ObservableList<Node> getNodes() {
        return nodes;
    }

    public ObservableList<BezierEdge> getEdges() {
        return edges;
    }


    public List<BezierEdge> getAllAdjacentEdges(Node node) {
        List<BezierEdge> neighbors =  getIncomingEdges(node);
        neighbors.addAll(getOutgoingEdges(node));
        return neighbors;
    }
    public List<BezierEdge> getOutgoingEdges(Node node) {
        return edges.stream().filter(e -> e.getStartingNode().equals(node)).collect(Collectors.toList());
    }
    public List<BezierEdge> getIncomingEdges(Node node) {
        return edges.stream().filter(e -> e.getEndingNode().equals(node)).collect(Collectors.toList());
    }

    public String toLatex() {
        String header = "\\begin{tikzpicture}[auto, node distance=3cm, every loop/.style={},\n" +
                "                    thick,main node/.style={circle,draw,font=\\sffamily\\small}, label distance=0mm] \n";
        Color fillColor = Node.getFillColor();
        Color strokeColor = Node.getStrokeColor();
        Color edgeStrokeColor = BezierEdge.getStrokeColor();
        int fillOpacity = (int) (100*fillColor.getOpacity());
        int strokeOpacity = (int) (100*strokeColor.getOpacity());
        int edgeOpacity = (int) (100*edgeStrokeColor.getOpacity());
        String fillColorDefString = String.format("\\definecolor{nodecolor}{rgb}{%.1f,%.1f,%.1f} \n", fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue());
        String strokeColorDefString = String.format("\\definecolor{strokecolor}{rgb}{%.1f,%.1f,%.1f} \n", strokeColor.getRed(), strokeColor.getGreen(), strokeColor.getBlue());
        String edgeStrokeColorDefString = String.format("\\definecolor{edgestrokecolor}{rgb}{%.1f,%.1f,%.1f} \n", edgeStrokeColor.getRed(), edgeStrokeColor.getGreen(), edgeStrokeColor.getBlue());

        int nodeSize = Node.getNodeRadius();
        double strokeWidth = Node.getWidth()/2;
        double edgeStrokeWidth = BezierEdge.getWidth()/2;
        String shape = "circle"; //TODO: make editable
        boolean directed = true; //TODO: make editable
        String arrowType = directed? "->" : "-";
        String tikzSets = String.format("\\tikzset{vertex/.style = {shape=%s, fill=nodecolor!%d, inner sep=0pt, draw=strokecolor!%d, line width=%.1fpt, minimum size=%dpt}}\n" +
                "\\tikzset{edge/.style = {line width=%.1fpt, draw=edgestrokecolor!%d, %s,> = latex'}}\n", shape, fillOpacity, strokeOpacity, strokeWidth, nodeSize, edgeStrokeWidth, edgeOpacity, arrowType);
        StringBuilder labelsBuilder = new StringBuilder(" \\def\\labels{{\"\", ");
        StringBuilder labelPositionsBuilder = new StringBuilder("\\def\\labelPositions{{, ");
        StringBuilder coordsBuilder = new StringBuilder("\\def\\coords{{{,}, ");
        for(int i=0; i< nodes.size(); i++) {
            String num = String.format("\"%d\" ", i+1);
            labelsBuilder.append(num); //TODO: make labels editable
            labelPositionsBuilder.append("90"); //TODO: make label positions editable
            double rescaledX = nodes.get(i).getRescaledX();
            double rescaledY = nodes.get(i).getRescaledY();
            String coordinatePair = String.format("{%.2f, %.2f}", rescaledX, rescaledY);
            coordsBuilder.append(coordinatePair);
            if(i<nodes.size()-1) {
                labelsBuilder.append(", ");
                labelPositionsBuilder.append(", ");
                coordsBuilder.append(", ");
            }
            if(i==nodes.size()-1) {
                labelsBuilder.append("}};\n");
                labelPositionsBuilder.append("}};\n");
                coordsBuilder.append("}};\n");
            }
        }
        String labels = labelsBuilder.toString();
        String labelPositions = labelPositionsBuilder.toString();
        String coords = coordsBuilder.toString();

        String numNodes = String.format("\\def\\numNodes{%d};\n", nodes.size());
        String numEdges = String.format("\\def\\numEdges{%d};\n", edges.size());

        StringBuilder edgesBuilder = new StringBuilder("\\def\\edges{{{,}, ");
        StringBuilder bezierControlsBuilder = new StringBuilder("\\def\\controls{{{,,,}, ");

        for(int i=0; i< edges.size(); i++) {
            BezierEdge e = edges.get(i);
            int n1 = 1+nodes.indexOf(e.getStartingNode());
            int n2 = 1+nodes.indexOf(e.getEndingNode());
            String edgeString = String.format("{%d,%d}", n1, n2);
            edgesBuilder.append(edgeString);

            double rescaledCX1 = e.control1.getRescaledX();
            double rescaledCY1 = e.control1.getRescaledY();
            double rescaledCX2 = e.control2.getRescaledX();
            double rescaledCY2 = e.control2.getRescaledY();
            String bezierString = String.format("{%.2f,%.2f,%.2f,%.2f}", rescaledCX1, rescaledCY1, rescaledCX2, rescaledCY2);
            bezierControlsBuilder.append(bezierString);

            if(i<edges.size()-1) {
                edgesBuilder.append(", ");
                bezierControlsBuilder.append(", ");
            }
            if(i==edges.size()-1) {
                edgesBuilder.append("}};\n");
                bezierControlsBuilder.append("}};\n");
            }
        }
        String adjacencyString = edgesBuilder.toString();
        String bezierControlsString = bezierControlsBuilder.toString();
//        String macros = "  \\def\\getFirstNode#1{\\pgfmathtruncatemacro{\\nodeA}{\\edges[#1][0]}}; \n" +
//                "  \\def\\getSecondNode#1{\\pgfmathtruncatemacro{\\nodeB}{\\edges[#1][1]}}; \n" +
//                "  \\def\\getLabelAngle#1{\\pgfmathtruncatemacro{\\angle}{\\labelPositions[#1]}}; \n" +
//                "  \\def\\getCXA#1{\\pgfmathtruncatemacro{\\cxa}{\\controls[#1][0]}};\n" +
//                "  \\def\\getCYA#1{\\pgfmathtruncatemacro{\\cya}{\\controls[#1][1]}};\n" +
//                "  \\def\\getCXB#1{\\pgfmathtruncatemacro{\\cxb}{\\controls[#1][2]}};\n" +
//                "  \\def\\getCYB#1{\\pgfmathtruncatemacro{\\cyb}{\\controls[#1][3]}};\n";
        String macros = "  \\def\\getFirstNode#1{\\pgfmathtruncatemacro{\\nodeA}{\\edges[#1][0]}};\n" +
                "  \\def\\getSecondNode#1{\\pgfmathtruncatemacro{\\nodeB}{\\edges[#1][1]}};\n" +
                "  \\def\\getLabelAngle#1{\\pgfmathtruncatemacro{\\angle}{\\labelPositions[#1]}};\n";

        String closing = "   %DRAW THE NODES WITH LABELS\n" +
                "   \\foreach \\phi in {1,...,\\numNodes}{\n" +
                "      \\getLabelAngle{\\phi};\n" +
                "      \\node[vertex, label={\\angle:\\pgfmathparse{\\labels[\\phi]}\\pgfmathresult}] (\\phi) at (\\coords[\\phi][0], \\coords[\\phi][1]) {};\n" +
                "   }\n" +
                "   %DRAW THE EDGES\n" +
                "   \\foreach \\psi in {1,...,\\numEdges}{\n" +
                "      \\getFirstNode{\\psi};\n" +
                "      \\getSecondNode{\\psi};\n" +
                "      \\draw[edge] (\\nodeA) .. controls (\\controls[\\psi][0], \\controls[\\psi][1]) and (\\controls[\\psi][2], \\controls[\\psi][3]) .. (\\nodeB);\n" +
                "   }\n" +
                "\\end{tikzpicture}\n";

//        String closing = "   %DRAW THE NODES WITH LABELS\n" +
//                "   \\foreach \\phi in {1,...,\\numNodes}{\n" +
//                "      \\getLabelAngle{\\phi};\n" +
//                "      \\node[vertex, label={\\angle:\\pgfmathparse{\\labels[\\phi]}\\pgfmathresult}] (\\phi) at (\\coords[\\phi][0], \\coords[\\phi][1]) {};\n" +
//                "   }\n" +
//                "   %DRAW THE EDGES\n" +
//                "   \\foreach \\psi in {1,...,\\numEdges}{\n" +
//                "      \\getFirstNode{\\psi};\n" +
//                "      \\getSecondNode{\\psi};\n" +
//                "      \\getCXA{\\psi};\n" +
//                "      \\getCYA{\\psi};\n" +
//                "      \\getCXB{\\psi};\n" +
//                "      \\getCYB{\\psi};\n" +
//                "      \\draw[edge] (\\nodeA) .. controls (\\cxa, \\cya) and (\\cxb, \\cyb) .. (\\nodeB);\n" +
//                "   }\n" +
//                "\\end{tikzpicture}\n";

        return header+
                fillColorDefString+
                strokeColorDefString+
                edgeStrokeColorDefString+
                tikzSets+
                labels+
                labelPositions+
                numNodes+
                coords+
                numEdges+
                adjacencyString+
                bezierControlsString+
                macros+
                closing;
    }

    public Optional<Pair<BezierEdge, Double>> nearestEdge(Point2D p) {
        if(edges.size()==0) return Optional.empty();
        double minDist = Double.MAX_VALUE;
        BezierEdge closestEdge = edges.get(0);
        for(BezierEdge edge: edges) {
            double d = edge.distance(p);
            if(d < minDist) {
                minDist = d;
                closestEdge = edge;
            }
        }
        return Optional.of(new Pair<>(closestEdge, minDist));
    }

    public void clear() {
        nodes.clear();
        edges.clear();
    }
}
