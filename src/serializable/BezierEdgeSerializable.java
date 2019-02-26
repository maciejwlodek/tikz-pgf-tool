package serializable;
import java.io.Serializable;

public class BezierEdgeSerializable implements Serializable {

    //LabeledNodeSerializable startingNode;
    //LabeledNodeSerializable endingNode;
    int startingNodeIndex;
    int endingNodeIndex;

    BezierControlNodeSerializable control1;
    BezierControlNodeSerializable control2;

    public BezierEdgeSerializable(int startingNodeIndex, int endingNodeIndex, BezierControlNodeSerializable control1, BezierControlNodeSerializable control2) {
        this.startingNodeIndex = startingNodeIndex;
        this.endingNodeIndex = endingNodeIndex;
        this.control1 = control1;
        this.control2 = control2;
    }

    //    double width;
//    Color strokeColor;

//    public BezierEdgeSerializable(LabeledNodeSerializable startingNode, LabeledNodeSerializable endingNode, BezierControlNodeSerializable control1, BezierControlNodeSerializable control2) {
////        this.startingNode = startingNode; //this maybe can be changed to index of node in list
////        this.endingNode = endingNode; //this maybe can be changed to index of node in list
//        this.control1 = control1;
//        this.control2 = control2;
//    }
//    public BezierEdgeSerializable(BezierEdge edge) {
//
//    }


//    public LabeledNodeSerializable getStartingNode() {
//        return startingNode;
//    }
//
//    public void setStartingNode(LabeledNodeSerializable startingNode) {
//        this.startingNode = startingNode;
//    }
//
//    public LabeledNodeSerializable getEndingNode() {
//        return endingNode;
//    }
//
//    public void setEndingNode(LabeledNodeSerializable endingNode) {
//        this.endingNode = endingNode;
//    }


    public int getStartingNodeIndex() {
        return startingNodeIndex;
    }

    public void setStartingNodeIndex(int startingNodeIndex) {
        this.startingNodeIndex = startingNodeIndex;
    }

    public int getEndingNodeIndex() {
        return endingNodeIndex;
    }

    public void setEndingNodeIndex(int endingNodeIndex) {
        this.endingNodeIndex = endingNodeIndex;
    }

    public BezierControlNodeSerializable getControl1() {
        return control1;
    }

    public void setControl1(BezierControlNodeSerializable control1) {
        this.control1 = control1;
    }

    public BezierControlNodeSerializable getControl2() {
        return control2;
    }

    public void setControl2(BezierControlNodeSerializable control2) {
        this.control2 = control2;
    }
}
