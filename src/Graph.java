import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Graph {

//    ArrayList<Node> nodes;
//    ArrayList<Edge> edges;
    ObservableList<Node> nodes;
    ObservableList<Edge> edges;

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
    public void add(Edge edge) {
        edges.add(edge);
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

    public ObservableList<Edge> getEdges() {
        return edges;
    }


    public List<Edge> getAllAdjacentEdges(Node node) {
        List<Edge> neighbors =  getIncomingEdges(node);
        neighbors.addAll(getOutgoingEdges(node));
        return neighbors;
    }
    public List<Edge> getOutgoingEdges(Node node) {
        return edges.stream().filter(e -> e.getStartingNode().equals(node)).collect(Collectors.toList());
    }
    public List<Edge> getIncomingEdges(Node node) {
        return edges.stream().filter(e -> e.getEndingNode().equals(node)).collect(Collectors.toList());
    }

    public String toLatex() {
        String header = "\\begin{tikzpicture}[auto, node distance=3cm, every loop/.style={},\n" +
                "                    thick,main node/.style={circle,draw,font=\\sffamily\\small}, label distance=0mm] \n";
        Color fillColor = Node.getFillColor();
        Color strokeColor = Node.getStrokeColor();
        Color edgeStrokeColor = Edge.getStrokeColor();
        int fillOpacity = (int) (100*fillColor.getOpacity());
        int strokeOpacity = (int) (100*strokeColor.getOpacity());
        int edgeOpacity = (int) (100*edgeStrokeColor.getOpacity());
        String fillColorDefString = String.format("\\definecolor{nodecolor}{rgb}{%.1f,%.1f,%.1f} \n", fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue());
        String strokeColorDefString = String.format("\\definecolor{strokecolor}{rgb}{%.1f,%.1f,%.1f} \n", strokeColor.getRed(), strokeColor.getGreen(), strokeColor.getBlue());
        String edgeStrokeColorDefString = String.format("\\definecolor{edgestrokecolor}{rgb}{%.1f,%.1f,%.1f} \n", edgeStrokeColor.getRed(), edgeStrokeColor.getGreen(), edgeStrokeColor.getBlue());

        int nodeSize = Node.getNodeRadius();
        double strokeWidth = Node.getWidth()/2;
        double edgeStrokeWidth = Edge.getWidth()/2;
        String shape = "circle"; //TODO: make editable
        boolean directed = true; //TODO: make editable
        String arrowType = directed? "->" : "-";
        String tikzSets = String.format("\\tikzset{vertex/.style = {shape=%s, fill=nodecolor!%d, inner sep=0pt, draw=strokecolor!%d, line width=%.1fpt, minimum size=%dpt}}\n" +
                "\\tikzset{edge/.style = {line width=%.1fpt, draw=edgestrokecolor!%d, %s,> = latex'}}\n", shape, fillOpacity, strokeOpacity, strokeWidth, nodeSize, edgeStrokeWidth, edgeOpacity, arrowType);
        StringBuilder labelsBuilder = new StringBuilder(" \\def\\labels{{\"\", ");
        StringBuilder labelPositionsBuilder = new StringBuilder("\\def\\labelPositions{{999, ");
        StringBuilder coordsBuilder = new StringBuilder("\\def\\coords{{{999,999}, ");
        for(int i=0; i< nodes.size(); i++) {
            String num = String.format("\"%d\" ", i+1);
            labelsBuilder.append(num); //TODO: make labels editable
            labelPositionsBuilder.append("90"); //TODO: make label positions editable
            double rescaledX = nodes.get(i).getRescaledX();
            double rescaledY = nodes.get(i).getRescaledY();
            String coordinatePair = String.format("{%4.2f, %4.2f}", rescaledX, rescaledY);
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

        StringBuilder edgesBuilder = new StringBuilder("\\def\\edges{{{999,999}, ");
        for(int i=0; i< edges.size(); i++) {
            Edge e = edges.get(i);
            int n1 = 1+nodes.indexOf(e.getStartingNode());
            int n2 = 1+nodes.indexOf(e.getEndingNode());
            String edgeString = String.format("{%d,%d}", n1, n2);
            edgesBuilder.append(edgeString);
            if(i<edges.size()-1) {
                edgesBuilder.append(", ");
            }
            if(i==edges.size()-1) {
                edgesBuilder.append("}};\n");
            }
        }
        String adjacencyString = edgesBuilder.toString();
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
                "      \\draw[edge] (\\nodeA) to (\\nodeB);\n" +
                "   }\n" +
                "\\end{tikzpicture}\n";

        return header+fillColorDefString+strokeColorDefString+edgeStrokeColorDefString+tikzSets+labels+labelPositions+numNodes+coords+numEdges+adjacencyString+macros+closing;


    }

}
