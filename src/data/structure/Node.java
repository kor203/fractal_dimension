package data.structure;

import java.util.LinkedList;

public class Node {
    int n;  //node number
    int k;  //node degree
    LinkedList<Edge> edges;
    public Node(int n){
        this.n = n;
        this.edges = new LinkedList<>();
    }

    public Edge addEdge(Node target){
        Edge newEdge = new Edge(this, target);
        this.edges.add(newEdge);
        target.edges.add(newEdge);
        this.k++;
        target.k++;
        return newEdge;
    }

    public Node addEdge(Edge edge){
        Node target = edge.target;
        this.edges.add(edge);
        target.edges.add(edge);
        this.k++;
        target.k++;
        return target;
    }

    public int getN() {
        return n;
    }

    public LinkedList<Edge> getEdges() {
        return edges;
    }
}
