package data.structure;

import java.util.ArrayList;

public class Network {
    ArrayList<Node> nodes;
    ArrayList<Edge> edges;

    public Network(ArrayList<Node> nodes, ArrayList<Edge> edges){
        this.nodes = nodes;
        this.edges = edges;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public int getSize() {return nodes.size();}

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public void reindex(){
        int counter = 0;
        for (Node node : nodes) {
            node.n = counter;
            counter++;
        }
    }
}
