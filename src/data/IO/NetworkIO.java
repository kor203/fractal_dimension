package data.IO;

import data.structure.Edge;
import data.structure.Network;
import data.structure.Node;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;

public final class NetworkIO {
    //uses input files where each line is an edge written as pair of node indexes
    public static Network importNetworkByEdges (FileInputStream inputStream){
        //reading input file
        LinkedList<Integer> edgesImportedSource = new LinkedList<>();
        LinkedList<Integer> edgesImportedTarget = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            String [] importedText;
            while ((line = br.readLine()) != null) {
                importedText = line.split(" ");
                edgesImportedSource.add(Integer.parseInt(importedText[0]));
                edgesImportedTarget.add(Integer.parseInt(importedText[1]));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //calculating network size (for node indexes starting from 0)
        final int networkSize = 1 + Math.max(edgesImportedSource.stream().mapToInt(v -> v).max().getAsInt(), edgesImportedTarget.stream().mapToInt(v -> v).max().getAsInt());
        final int edgeAmount = edgesImportedSource.size();
        ArrayList<Node> nodes = new ArrayList<>(networkSize);
        ArrayList<Edge> edges = new ArrayList<>(edgeAmount);
        for (int i = 0; i < networkSize; i++) {
            nodes.add(new Node(i));
        }
        for (int i = 0; i < edgeAmount; i++) {
            Node sourceNode = nodes.get(edgesImportedSource.pop());
            Edge newEdge = sourceNode.addEdge(nodes.get(edgesImportedTarget.pop()));
            edges.add(newEdge);
        }
        return new Network(nodes, edges);
    }
}
