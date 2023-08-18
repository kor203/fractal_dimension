package data.structure;

public class Edge {
    Node source;
    Node target;
    public Edge(Node source, Node target){
        this.source = source;
        this.target = target;
    }

    public Node getSecondNode(Node first){
        Node out = source;
        if (first == source)
            out = target;
        if (first != source && first != target)
            return null;
        return out;
    }

    public Node getSource() {
        return source;
    }

    public Node getTarget() {
        return target;
    }
}
