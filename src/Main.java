import analysis.giantComponent;
import data.IO.NetworkIO;
import data.structure.Network;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) {
        try {
            FileInputStream inputStream = new FileInputStream("test_network");
            Network network = NetworkIO.importNetworkByEdges(inputStream);
            network.printNetwork();
            Network newNetwork = giantComponent.getGiantComponent(network);
            network.printNetwork();
            newNetwork.printNetwork();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}