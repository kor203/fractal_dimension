package analysis;

import data.structure.*;
import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.*;
import jcuda.runtime.JCuda;

import javax.swing.*;
import java.util.*;

public class BoxCovering {

    private static CUfunction BFS;
    private static CUfunction findUsedBoxes;
    private static CUfunction findFirstFreeBox;
    private static CUfunction resetBlocked;
    private static CUfunction setBox;

    private static int [] greedyCoveringDistances(Node startingNode, int networkSize){
        int [] distances = new int[networkSize];
        Arrays.fill(distances, Integer.MAX_VALUE);
        LinkedList<Node> BFSList = new LinkedList<>();
        BFSList.add(startingNode);
        distances[startingNode.getN()] = 0;

        while (!BFSList.isEmpty()){
            Node tmpNode = BFSList.pop();
            for (Edge connectedEdge : tmpNode.getEdges()){
                Node neighbour = connectedEdge.getSecondNode(tmpNode);
                if (distances[neighbour.getN()] == Integer.MAX_VALUE){
                    BFSList.add(neighbour);
                    distances[neighbour.getN()] = distances[tmpNode.getN()] + 1;
                }
            }
        }
        return distances;
    }

    private static void getCudaFunction(){
        CUmodule BFSCuda = new CUmodule();
        //String path = BoxCovering.class.getClassLoader().getResource("BFSBoxCoveringCuda.ptx").toExternalForm();
        JCudaDriver.cuModuleLoad(BFSCuda, "resources/BFSBoxCoveringCuda.ptx");
        BFS = new CUfunction();
        JCudaDriver.cuModuleGetFunction(BFS, BFSCuda, "BFS");
        findUsedBoxes = new CUfunction();
        JCudaDriver.cuModuleGetFunction(findUsedBoxes, BFSCuda, "findUsedBoxes");
        findFirstFreeBox = new CUfunction();
        JCudaDriver.cuModuleGetFunction(findFirstFreeBox, BFSCuda, "findFirstFreeBox");
        resetBlocked = new CUfunction();
        JCudaDriver.cuModuleGetFunction(resetBlocked, BFSCuda, "resetBlocked");
        setBox = new CUfunction();
        JCudaDriver.cuModuleGetFunction(setBox, BFSCuda, "setBox");
    }

    private static void setupCuda (){
        JCudaDriver.cuInit(0);
        CUdevice device = new CUdevice();
        JCudaDriver.cuDeviceGet(device, 0);
        CUcontext context = new CUcontext();
        JCudaDriver.cuCtxCreate(context, 0, device);

        getCudaFunction();
    }

    public static class CPUGreedyBoxCoveringTask extends SwingWorker<int[][], Void>{
        private Network network;
        private int minBoxSize;
        private int maxBoxSize;

        public CPUGreedyBoxCoveringTask(Network network, int minBoxSize, int maxBoxSize){
            super();
            this.network = network;
            this.minBoxSize = minBoxSize;
            this.maxBoxSize = maxBoxSize;
        }

        @Override
        protected int[][] doInBackground() throws Exception {
            int networkSize = network.getSize();
            int amountOfBoxSizes = maxBoxSize - minBoxSize + 1;
            Boolean [] correctBox = new Boolean[networkSize];
            int [][] boxes = new int[networkSize][amountOfBoxSizes];   //each node for each boxSize

            setProgress(0);

            for (int n = 1; n < networkSize; n++){
                int [] distances = greedyCoveringDistances(network.getNodes().get(n), networkSize);
                for (int boxSize = minBoxSize; boxSize <= maxBoxSize; boxSize++){
                    int boxSizeIndex = boxSize - minBoxSize;
                    Arrays.fill(correctBox, true);
                    for (int i = 0; i < n; i++){
                        if (distances[i] >= boxSize)
                            correctBox[boxes[i][boxSizeIndex]] = false;
                    }
                    int counter = 0;
                    while (!correctBox[counter])
                        counter++;
                    boxes[n][boxSizeIndex] = counter;
                }
                int progress =n * 100 / networkSize;
                if (isCancelled())
                    return null;
                else
                    setProgress(progress);
            }

            int [][] result = new int[2][amountOfBoxSizes]; //pairs of values: box size (index 0) and amount of boxes (index 1)
            for (int boxSizesCounter = 0; boxSizesCounter < amountOfBoxSizes; boxSizesCounter++){
                result[0][boxSizesCounter] = boxSizesCounter + minBoxSize;
                int boxCounter = 0;
                for (int n = 0; n < networkSize; n++){
                    if (boxes[n][boxSizesCounter] > boxCounter)
                        boxCounter = boxes[n][boxSizesCounter];
                }
                result[1][boxSizesCounter] = boxCounter + 1;
            }
            if (isCancelled())
                return null;
            else
                setProgress(100);
            return result;
        }
    }

    public static class GPUGreedyBoxCoveringTask extends SwingWorker<int[][], Void>{
        private Network network;
        private int minBoxSize;
        private int maxBoxSize;

        public GPUGreedyBoxCoveringTask(Network network, int minBoxSize, int maxBoxSize){
            super();
            this.network = network;
            this.minBoxSize = minBoxSize;
            this.maxBoxSize = maxBoxSize;
        }

        @Override
        public int[][] doInBackground() throws Exception{
            int networkSize = network.getSize();
            int amountOfBoxSizes = maxBoxSize - minBoxSize + 1;

            long [] free = {0};
            long [] total = {0};
            JCuda.cudaMemGetInfo(free, total);
            int threadAmount = (int) Math.min(20000, Math.floor(((double) free[0]) * 0.3 / 4 / networkSize));

            setProgress(0);

            int [] boxes = new int[networkSize * amountOfBoxSizes];   //each node for each boxSize

            setupCuda();


            CUdeviceptr deviceDistances = new CUdeviceptr();
            CUdeviceptr deviceNeighbourArray = new CUdeviceptr();
            CUdeviceptr deviceNeighbourStarts = new CUdeviceptr();
            CUdeviceptr deviceQueue = new CUdeviceptr();
            CUdeviceptr deviceBoxCovering = new CUdeviceptr();
            CUdeviceptr deviceBlockedBoxes = new CUdeviceptr();


            ArrayList<Edge> edges = network.getEdges();
            LinkedList<Integer>[] neis = new LinkedList[networkSize];
            for (int i = 0; i < network.getSize(); i++){
                neis[i] = new LinkedList<>();
            }
            for (Edge edge : edges){
                neis[edge.getSource().getN()].add(edge.getTarget().getN());
                neis[edge.getTarget().getN()].add(edge.getSource().getN());
            }

            int [] neighbours = new int[edges.size() * 2];
            int [] nodeNeighboursStart = new int[networkSize + 1];

            int nodeCounter = 0;
            int neisCounter = 0;
            for (LinkedList<Integer> singleNode : neis){
                nodeNeighboursStart[nodeCounter] = neisCounter;
                for (Integer neighbour : singleNode){
                    neighbours[neisCounter] = neighbour;
                    neisCounter++;
                }
                nodeCounter++;
            }
            nodeNeighboursStart[nodeCounter] = neisCounter;


            int memoryForAllNodes = networkSize * Sizeof.INT;
            JCudaDriver.cuMemAlloc(deviceDistances, threadAmount * memoryForAllNodes);
            JCudaDriver.cuMemAlloc(deviceNeighbourArray, 2 * edges.size() * Sizeof.INT);
            JCudaDriver.cuMemAlloc(deviceNeighbourStarts, (networkSize + 1) * Sizeof.INT);
            JCudaDriver.cuMemAlloc(deviceQueue, threadAmount * memoryForAllNodes);
            JCudaDriver.cuMemAlloc(deviceBoxCovering, amountOfBoxSizes * memoryForAllNodes);
            JCudaDriver.cuMemAlloc(deviceBlockedBoxes, memoryForAllNodes);


            JCudaDriver.cuMemcpyHtoD(deviceNeighbourArray, Pointer.to(neighbours), 2 * edges.size() * Sizeof.INT);
            JCudaDriver.cuMemcpyHtoD(deviceNeighbourStarts, Pointer.to(nodeNeighboursStart), (networkSize + 1) * Sizeof.INT);


            int block = 1024;
            int grid = (int) Math.ceil(((double) networkSize + block - 1) / block);


            for (int threadBlockFirstStartNode = 0; threadBlockFirstStartNode < networkSize; threadBlockFirstStartNode += threadAmount){
                Pointer kernelParameters = Pointer.to(
                        Pointer.to(new int[]{threadAmount}),
                        Pointer.to(new int[]{threadBlockFirstStartNode}),
                        Pointer.to(new int[]{networkSize}),
                        Pointer.to(deviceNeighbourArray),
                        Pointer.to(deviceNeighbourStarts),
                        Pointer.to(deviceQueue),
                        Pointer.to(deviceDistances)
                );
                JCudaDriver.cuLaunchKernel(BFS,
                        grid, 1, 1,
                        block, 1, 1,
                        0, null,
                        kernelParameters, null);
                JCudaDriver.cuCtxSynchronize();


                int threadBlockStartThreadIndex = 0;
                for (int threadBlockStartNodeIndex = threadBlockFirstStartNode; threadBlockStartNodeIndex < threadBlockFirstStartNode + threadAmount; threadBlockStartNodeIndex++){
                    if (threadBlockStartNodeIndex < networkSize)
                        for (int boxSize = minBoxSize; boxSize <= maxBoxSize; boxSize++) {
                            kernelParameters = Pointer.to(
                                    Pointer.to(new int[]{threadBlockStartNodeIndex}),
                                    Pointer.to(new int[]{threadBlockStartThreadIndex}),
                                    Pointer.to(new int[]{boxSize}),
                                    Pointer.to(new int[]{minBoxSize}),
                                    Pointer.to(new int[]{networkSize}),
                                    Pointer.to(deviceDistances),
                                    Pointer.to(deviceBoxCovering),
                                    Pointer.to(deviceBlockedBoxes)
                            );
                            JCudaDriver.cuLaunchKernel(findUsedBoxes,
                                    grid, 1, 1,
                                    block, 1, 1,
                                    0, null,
                                    kernelParameters, null);

                            kernelParameters = Pointer.to(
                                    Pointer.to(new int[]{networkSize}),
                                    Pointer.to(deviceBlockedBoxes)
                            );
                            JCudaDriver.cuLaunchKernel(findFirstFreeBox,
                                    grid, 1, 1,
                                    block, 1, 1,
                                    0, null,
                                    kernelParameters, null);
                            kernelParameters = Pointer.to(
                                    Pointer.to(new int[]{networkSize}),
                                    Pointer.to(deviceBlockedBoxes)
                            );
                            JCudaDriver.cuLaunchKernel(resetBlocked,
                                    grid, 1, 1,
                                    block, 1, 1,
                                    0, null,
                                    kernelParameters, null);
                            kernelParameters = Pointer.to(
                                    Pointer.to(new int[]{threadBlockStartNodeIndex}),
                                    Pointer.to(new int[]{boxSize - minBoxSize}),
                                    Pointer.to(new int[]{networkSize}),
                                    Pointer.to(deviceBoxCovering)
                            );
                            JCudaDriver.cuLaunchKernel(setBox,
                                    1, 1, 1,
                                    1, 1, 1,
                                    0, null,
                                    kernelParameters, null);
                        }
                    threadBlockStartThreadIndex++;
                    int progress = (int) (Math.min(networkSize, (double)threadBlockStartNodeIndex) / networkSize * 100);
                    if (isCancelled())
                        return null;
                    else
                        setProgress(progress);
                }
            }


            JCudaDriver.cuMemcpyDtoH(Pointer.to(boxes), deviceBoxCovering, amountOfBoxSizes * memoryForAllNodes);


            JCudaDriver.cuMemFree(deviceDistances);
            JCudaDriver.cuMemFree(deviceNeighbourArray);
            JCudaDriver.cuMemFree(deviceNeighbourStarts);
            JCudaDriver.cuMemFree(deviceQueue);
            JCudaDriver.cuMemFree(deviceBoxCovering);
            JCudaDriver.cuMemFree(deviceBlockedBoxes);


            int [][] result = new int[2][amountOfBoxSizes]; //pairs of values: box size (index 0) and amount of boxes (index 1)
            for (int boxSizesCounter = 0; boxSizesCounter < amountOfBoxSizes; boxSizesCounter++){
                result[0][boxSizesCounter] = boxSizesCounter + minBoxSize;
                int boxCounter = 0;
                for (int n = 0; n < networkSize; n++){
                    if (boxes[n + boxSizesCounter * networkSize] > boxCounter)
                        boxCounter = boxes[n + boxSizesCounter * networkSize];
                }
                result[1][boxSizesCounter] = boxCounter + 1;
            }
            if (isCancelled())
                return null;
            else
                setProgress(100);
            return result;
        }
    }

    public static double [][] toLogBin(int [][] boxAmounts, double base){
        int maxBoxSize = boxAmounts[0][boxAmounts[0].length - 1];
        int minPower = (int) Math.floor(Math.log(boxAmounts[0][0]) / Math.log(base));
        int maxPower = (int) Math.ceil(Math.log(maxBoxSize) / Math.log(base));
        double[][] boxCounts = new double[4][maxPower - minPower];  //0 - x, 1 - y, 2 - minX, 3 - maxX
        for (int i = minPower; i < maxPower; i++) {
            boxCounts[0][i - minPower] = Math.pow(base, 0.5 + i);
            boxCounts[2][i - minPower] = Math.pow(base, i);
            boxCounts[3][i - minPower] = Math.pow(base, i + 1);
        }
        for (int i = 0; i < boxAmounts[0].length; i++) {
            int iterator = 0;
            while (boxCounts[3][iterator] < boxAmounts[0][i])
                iterator++;
            boxCounts[1][iterator] += boxAmounts[1][i];
        }
        for (int i = 0; i < maxPower - minPower; i++) {
            boxCounts[1][i] /= (boxCounts[3][i] - boxCounts[2][i]);
        }
        int nonZeroCounter = 0;
        for (int i = 0; i < maxPower - minPower; i++) {
            if (boxCounts[1][i] != 0)
                nonZeroCounter++;
        }
        double[][] result = new double[4][nonZeroCounter];
        int iterator = 0;
        for (int i = 0; i < maxPower - minPower; i++) {
            if (boxCounts[1][i] != 0) {
                result[0][iterator] = boxCounts[0][i];
                result[1][iterator] = boxCounts[1][i];
                result[2][iterator] = boxCounts[2][i];
                result[3][iterator] = boxCounts[3][i];
                iterator++;
            }
        }
        return result;
    }

    public static double [] boxDimension(int[][] boxAmounts, int minBoxSize, int maxBoxSize){
        int minBoxSizeIndex = 0;
        int maxBoxSizeIndex = 0;
        for (int i = 0; i < boxAmounts[0].length; i++){
            if (boxAmounts[0][i] == minBoxSize)
                minBoxSizeIndex = i;
            if (boxAmounts[0][i] == maxBoxSize)
                maxBoxSizeIndex = i;
        }
        double [] x = new double[maxBoxSizeIndex - minBoxSizeIndex + 1];
        double [] y = new double[maxBoxSizeIndex - minBoxSizeIndex + 1];
        for (int i = 0; i < maxBoxSizeIndex - minBoxSizeIndex + 1; i++){
            x[i] = Math.log10(boxAmounts[0][i + minBoxSizeIndex]);
            y[i] = Math.log10(boxAmounts[1][i + minBoxSizeIndex]);
        }

        int S = x.length;
        double Sx = 0;
        double Sy = 0;
        double Sxx = 0;
        double Syy = 0;
        double Sxy = 0;
        for (int i = 0; i < S; i++){
            Sx += x[i];
            Sy += y[i];
            Sxx += x[i] * x[i];
            Syy += y[i] * y[i];
            Sxy += x[i] * y[i];
        }
        double delta = S * Sxx - Sx * Sx;

        double a = (S * Sxy - Sx * Sy) / delta;
        double b = (Sxx * Sy - Sx * Sxy) / delta;
        double deltaY = Syy - a * Sxy - b * Sy;
        double stdA = Math.sqrt(deltaY / (S - 2) * S / delta);
        double stdB = Math.sqrt(deltaY / (S - 2) * Sxx / delta);

        double [] result = {-a, stdA, a, stdA, b, stdB};
        return result;
    }

    public static double [] boxDimension(double[][] boxAmounts, int minBoxSize, int maxBoxSize){
        //boxAmounts: 0 - x, 1 - y, 2 - minX, 3 - maxX
        int minBoxSizeIndex = 0;
        int maxBoxSizeIndex = boxAmounts[0].length - 1;
        while (minBoxSize < boxAmounts[2][minBoxSizeIndex])
            minBoxSizeIndex++;
        while (maxBoxSize > boxAmounts[3][maxBoxSizeIndex])
            maxBoxSizeIndex--;
        double [] x = new double[maxBoxSizeIndex - minBoxSizeIndex + 1];
        double [] y = new double[maxBoxSizeIndex - minBoxSizeIndex + 1];
        for (int i = 0; i < maxBoxSizeIndex - minBoxSizeIndex + 1; i++){
            x[i] = Math.log10(boxAmounts[0][i + minBoxSizeIndex]);
            y[i] = Math.log10(boxAmounts[1][i + minBoxSizeIndex]);
        }

        int S = x.length;
        double Sx = 0;
        double Sy = 0;
        double Sxx = 0;
        double Syy = 0;
        double Sxy = 0;
        for (int i = 0; i < S; i++){
            Sx += x[i];
            Sy += y[i];
            Sxx += x[i] * x[i];
            Syy += y[i] * y[i];
            Sxy += x[i] * y[i];
        }
        double delta = S * Sxx - Sx * Sx;

        double a = (S * Sxy - Sx * Sy) / delta;
        double b = (Sxx * Sy - Sx * Sxy) / delta;
        double deltaY = Syy - a * Sxy - b * Sy;
        double stdA = Math.sqrt(deltaY / (S - 2) * S / delta);
        double stdB = Math.sqrt(deltaY / (S - 2) * Sxx / delta);

        double [] result = {-a, stdA, a, stdA, b, stdB};
        return result;
    }
}
