package fx;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Graph {

    ObservableList<LabeledNode> nodes;
    ObservableList<BezierEdge> edges;

    public Graph() {
        nodes = FXCollections.observableArrayList();
        edges = FXCollections.observableArrayList();

    }

    public void add(LabeledNode node){
        nodes.add(node);
    }
    public void add(BezierEdge edge) {
        edges.add(edge);
    }
    public void remove(LabeledNode node){
        nodes.remove(node);
    }
    public void remove(BezierEdge edge) {
        edges.remove(edge);
    }

    public ObservableList<LabeledNode> getNodes() {
        return nodes;
    }
    public ObservableList<BezierEdge> getEdges() {
        return edges;
    }


    public List<BezierEdge> getAllAdjacentEdges(LabeledNode node) {
        List<BezierEdge> neighbors =  getIncomingEdges(node);
        neighbors.addAll(getOutgoingEdges(node));
        return neighbors;
    }
    public List<BezierEdge> getOutgoingEdges(LabeledNode node) {
        return edges.stream().filter(e -> e.getStartingNode().equals(node)).collect(Collectors.toList());
    }
    public List<BezierEdge> getIncomingEdges(LabeledNode node) {
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
        String fillColorDefString = String.format("\\definecolor{nodecolor}{rgb}{%.1f,%.1f,%.1f} \n", fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue()); //TODO: not needed for independent node colors
        String strokeColorDefString = String.format("\\definecolor{strokecolor}{rgb}{%.1f,%.1f,%.1f} \n", strokeColor.getRed(), strokeColor.getGreen(), strokeColor.getBlue());
        String edgeStrokeColorDefString = String.format("\\definecolor{edgestrokecolor}{rgb}{%.1f,%.1f,%.1f} \n", edgeStrokeColor.getRed(), edgeStrokeColor.getGreen(), edgeStrokeColor.getBlue());

        int nodeSize = Node.getNodeRadius();
        double strokeWidth = Node.getWidth()/2;
        double edgeStrokeWidth = BezierEdge.getWidth()/2;
        String shape = "circle"; //TODO: make editable
        boolean directed = BezierArrow.isDirected();
        String arrowType = directed? "->" : "-";
        String tikzSets = String.format("\\tikzset{vertex/.style = {shape=%s, fill=nodecolor!%d, inner sep=0pt, draw=strokecolor!%d, line width=%.1fpt, minimum size=%dpt}}\n" +
                "\\tikzset{edge/.style = {line width=%.1fpt, draw=edgestrokecolor!%d, %s,> = latex'}}\n", shape, fillOpacity, strokeOpacity, strokeWidth, nodeSize, edgeStrokeWidth, edgeOpacity, arrowType);
        //String tikzSets = String.format("\\tikzset{vertex/.style = {shape=%s, inner sep=0pt, draw=strokecolor!%d, line width=%.1fpt, minimum size=%dpt}}\n" +
        //        "\\tikzset{edge/.style = {line width=%.1fpt, draw=edgestrokecolor!%d, %s,> = latex'}}\n", shape, strokeOpacity, strokeWidth, nodeSize, edgeStrokeWidth, edgeOpacity, arrowType); //TODO: this is correct for independent node colors

        StringBuilder labelsBuilder = new StringBuilder("\\def\\labels{{\"\", ");
        StringBuilder labelPositionsBuilder = new StringBuilder("\\def\\labelPositions{{, ");
        StringBuilder coordsBuilder = new StringBuilder("\\def\\coords{{{,}, ");
        //StringBuilder colorsBuilder = new StringBuilder("\\def\\colors{{{,,}, ");
        //StringBuilder opacitiesBuilder = new StringBuilder("\\def\\opacities{{, ");
        for(int i=0; i< nodes.size(); i++) {
            String label = nodes.get(i).getLabel();
            labelsBuilder.append("\"" + label + "\"");
            String labelAngle = String.format("%3.0f", nodes.get(i).getLabelAngle());
            labelPositionsBuilder.append(labelAngle);
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

        if(edges.size()==0) {
            String macros = "\\def\\getLabelAngle#1{\\pgfmathtruncatemacro{\\angle}{\\labelPositions[#1]}};\n";
            String closing = "   %DRAW THE NODES WITH LABELS\n" +
                    "   \\foreach \\phi in {1,...,\\numNodes}{\n" +
                    "      \\getLabelAngle{\\phi};\n" +
                    "      \\node[vertex, label={\\angle:\\pgfmathparse{\\labels[\\phi]}\\pgfmathresult}] (\\phi) at (\\coords[\\phi][0], \\coords[\\phi][1]) {};\n" +
                    "   }\n" +
                    "\\end{tikzpicture}\n";
            return header+
                    fillColorDefString+
                    strokeColorDefString+
                    tikzSets+
                    labels+
                    labelPositions+
                    numNodes+
                    coords+
                    macros+
                    closing;
        }

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
        String macros = "\\def\\getFirstNode#1{\\pgfmathtruncatemacro{\\nodeA}{\\edges[#1][0]}};\n" +
                "\\def\\getSecondNode#1{\\pgfmathtruncatemacro{\\nodeB}{\\edges[#1][1]}};\n" +
                "\\def\\getLabelAngle#1{\\pgfmathtruncatemacro{\\angle}{\\labelPositions[#1]}};\n";
        //"\\def\\getR#1{\\pgfmathtruncatemacro{\\red}{\\colors[#1][0]}};\n" +
        //        "\\def\\getG#1{\\pgfmathtruncatemacro{\\green}{\\colors[#1][1]}};\n" +
        //        "\\def\\getB#1{\\pgfmathtruncatemacro{\\blue}{\\colors[#1][2]}};\n" +
        //        "\\def\\getA#1{\\pgfmathtruncatemacro{\\opac}{\\opacities[#1]}};\n"; TODO: some more macros for independent node colors

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
//                "      \\getR{\\phi};\n" +
//                "      \\getG{\\phi};\n" +
//                "      \\getB{\\phi};\n" +
//                "      \\getA{\\phi};\n" +
//                "      \\definecolor{tempcolor}{rgb}{\\red, \\green, \\blue};\n" +
//                "      \\node[vertex, fill=tempcolor!\\opac, label={\\angle:\\pgfmathparse{\\labels[\\phi]}\\pgfmathresult}] (\\phi) at (\\coords[\\phi][0], \\coords[\\phi][1]) {};\n" +
//                "   }\n" +
//                "   %DRAW THE EDGES\n" +
//                "   \\foreach \\psi in {1,...,\\numEdges}{\n" +
//                "      \\getFirstNode{\\psi};\n" +
//                "      \\getSecondNode{\\psi};\n" +
//                "      \\draw[edge] (\\nodeA) .. controls (\\controls[\\psi][0], \\controls[\\psi][1]) and (\\controls[\\psi][2], \\controls[\\psi][3]) .. (\\nodeB);\n" +
//                "   }\n" +
//                "\\end{tikzpicture}\n"; //TODO: This is the corrected closing that accounts for independent node colors

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

    public Optional<Pair<LabeledNode, Double>> nearestNode(Point2D p) {
        if(nodes.size()==0) return Optional.empty();
        double minDist = Double.MAX_VALUE;
        LabeledNode closestNode = nodes.get(0);
        for(LabeledNode node: nodes) {
            double d = p.distance(node.getCenterX(), node.getCenterY());
            if(d < minDist) {
                minDist = d;
                closestNode = node;
            }
        }
        return Optional.of(new Pair<>(closestNode, minDist));
    }

    public Optional<LabeledNode> getNodeContainingPoint(Point2D p) {
        Optional<Pair<LabeledNode, Double>> nearestNode = nearestNode(p);
        if(!nearestNode.isPresent()) return Optional.empty();
        else {
            Pair<LabeledNode, Double> node = nearestNode.get();
            if(node.getValue() <= Node.getNodeRadius()) {
                return Optional.of(node.getKey());
            }
            else return Optional.empty();
        }
    }

    public void clear() {
        nodes.clear();
        edges.clear();
    }
}
