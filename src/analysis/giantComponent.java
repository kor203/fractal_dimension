package analysis;

import data.structure.*;

import java.util.*;

public final class giantComponent {
    private static Node findNode(LinkedList<Node> nodes, int n){
        for (Node node : nodes)
            if(node.getN() == n)
                return node;
        System.out.println("NULL");
        return null;
    }
    public static Network getGiantComponent(Network inputNetwork){
        int size = inputNetwork.getSize();
        int [] componentMark = new int[size];   //each node's component named after first node found
        Arrays.fill(componentMark, -1);
        LinkedList<Node> BFSList = new LinkedList<>();
        int componentN;
        for (Node node : inputNetwork.getNodes()){  //covers whole network
            if (componentMark[node.getN()] == -1){
                BFSList.add(node);
                componentN = node.getN();
                while (!BFSList.isEmpty()){
                    Node tmpNode = BFSList.pop();
                    componentMark[tmpNode.getN()] = componentN;
                    for (Edge connectedEdge : tmpNode.getEdges()){
                        Node neighbour = connectedEdge.getSecondNode(tmpNode);
                        if (componentMark[neighbour.getN()] == -1)
                            BFSList.add(neighbour);
                    }
                }
            }
        }
        HashMap<Integer, Integer> distinctMarks = new HashMap<>();
        for (int mark : componentMark){
            distinctMarks.put(mark, distinctMarks.getOrDefault(mark, 0) + 1);
        }
        int giantComponentMark = Collections.max(distinctMarks.entrySet(), Map.Entry.comparingByValue()).getKey();
        LinkedList<Node> nodes = new LinkedList<>();
        LinkedList<Edge> edges = new LinkedList<>();
        for (Node oldNode : inputNetwork.getNodes()){
            if (componentMark[oldNode.getN()] == giantComponentMark){
                nodes.add(new Node(oldNode.getN()));
            }
        }
        for (Edge oldEdge : inputNetwork.getEdges()){
            if (componentMark[oldEdge.getSource().getN()] == giantComponentMark && componentMark[oldEdge.getTarget().getN()] == giantComponentMark){
                Node source = findNode(nodes, oldEdge.getSource().getN());
                Node target = findNode(nodes, oldEdge.getTarget().getN());
                Edge newEdge = source.addEdge(target);
                edges.add(newEdge);
            }
        }
        return new Network(new ArrayList<>(nodes), new ArrayList<>(edges));
    }
}
