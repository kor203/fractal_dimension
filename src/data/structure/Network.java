package data.structure;

import java.util.ArrayList;

public class Network {
    ArrayList<Node> nodes;
    ArrayList<Edge> edges;

    public Network(ArrayList<Node> nodes, ArrayList<Edge> edges){
        this.nodes = nodes;
        this.edges = edges;
    }

    public void printNetwork(){
        for (Node node : nodes) {
            String neis = "";
            for (Edge edge : node.edges){
                int n;
                if (edge.target == node)
                    n = edge.source.n;
                else
                    n = edge.target.n;
                neis += n + ", ";
            }
            System.out.println(node.n + ": " + neis);
        }
        System.out.println("Nodes: " + nodes.size() + " Edges: " + edges.size());
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public int getSize() {return nodes.size();}

    public ArrayList<Edge> getEdges() {
        return edges;
    }
    /*public Node getNode (int n){
        for (Node node : nodes){
            return
        }
    }*/
}
