import static GUI.GUI.setupFrame;

public class Main {
    public static void main(String[] args) {
        setupFrame();
        /*try {
            JCuda.setExceptionsEnabled(true);
            System.out.println("Importing network!");
            long importStart = System.currentTimeMillis();
            FileInputStream inputStream = new FileInputStream("test_network");
            Network network = NetworkIO.importNetworkByEdges(inputStream);
            System.out.println("Initial network size: " + network.getSize());
            long importEnd = System.currentTimeMillis();
            System.out.println("Getting giant component!");
            long giantStart = System.currentTimeMillis();
            Network newNetwork = GiantComponent.getGiantComponent(network);
            long giantEnd = System.currentTimeMillis();
            System.out.println("Reindexing!");
            long reindexStart = System.currentTimeMillis();
            newNetwork.reindex();
            long reindexEnd = System.currentTimeMillis();
            int minBoxSize = 1;
            int maxBoxSize = 10;
            System.out.println("Calculating for CPU!");
            long cpuStart = System.currentTimeMillis();
            //int [][] cpuCovering = BoxCovering.boxGreedyCovering(newNetwork, minBoxSize, maxBoxSize, false);
            long cpuEnd = System.currentTimeMillis();
            System.out.println("Calculating for GPU!");
            long gpuStart = System.currentTimeMillis();
            //int [][] gpuCovering = BoxCovering.boxGreedyCoveringGPU(newNetwork, minBoxSize, maxBoxSize);
            GPUBoxCoveringTask bct = new GPUBoxCoveringTask(newNetwork, minBoxSize, maxBoxSize, null);
            bct.execute();
            int [][] gpuCovering = bct.get();
            long gpuEnd = System.currentTimeMillis();
            System.out.println("Testing results!");
            boolean errorsFound = false;
            for (int i = 0; i < (maxBoxSize - minBoxSize + 1); i++){
                if (cpuCovering[0][i] != gpuCovering[0][i] || cpuCovering[1][i] != gpuCovering[1][i]){
                    System.out.println("Difference at boxSize " + (i + minBoxSize));
                    errorsFound = true;
                    break;
                }
            }
            if (!errorsFound)
                System.out.println("Results correct!");
            System.out.println("Initial network size: " + network.getSize());
            System.out.println("Giant component size: " + newNetwork.getSize());
            System.out.println("Import time: " + (importEnd - importStart));
            System.out.println("Giant time: " + (giantEnd - giantStart));
            System.out.println("Reindex time: " + (reindexEnd - reindexStart));
            System.out.println("CPU time: " + (cpuEnd - cpuStart));
            System.out.println("GPU time: " + (gpuEnd - gpuStart));
            System.out.println(Arrays.deepToString(cpuCovering));
            System.out.println(Arrays.toString(boxDimension(cpuCovering, minBoxSize, maxBoxSize)));

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }*/
    }
}