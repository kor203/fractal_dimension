extern "C" {
    __device__ unsigned int firsOccurrence;

    __global__ void BFS (int threadsAmount, int firstStartNode, int networkSize, int *neighbourArray, int *neighbourStarts, int *queue, int *distances){
        int startThreadIndex = blockIdx.x * blockDim.x + threadIdx.x;

        if (startThreadIndex < threadsAmount){
            int startNodeIndex = startThreadIndex + firstStartNode;
            int threadOffset = startThreadIndex * networkSize;
            int queueNextElement = threadOffset;
            int queueLastElement = threadOffset;
            for (int i = 0; i < networkSize; i++)
                distances[i + threadOffset] = INT_MAX;
            if (startNodeIndex < networkSize)
                distances[startNodeIndex + threadOffset] = 0;
            queue[queueNextElement] = startNodeIndex;
            queueLastElement++;

            while (queueNextElement != queueLastElement){
                for (int connectionIndex = neighbourStarts[queue[queueNextElement]]; connectionIndex < neighbourStarts[queue[queueNextElement] + 1]; connectionIndex++){
                    int neighbourIndex = neighbourArray[connectionIndex];
                    if (distances[neighbourIndex + threadOffset] == INT_MAX){
                        distances[neighbourIndex + threadOffset] = distances[queue[queueNextElement] + threadOffset] + 1;
                        queue[queueLastElement] = neighbourIndex;
                        queueLastElement++;
                    }
                }
                queueNextElement++;
            }
        }
    }

    __global__ void findUsedBoxes (int startNodeIndex, int startThreadIndex, int boxSize, int minBoxSize, int networkSize, int *distances, int *boxCovering, int *blockedBoxes){
        int threadID = blockIdx.x * blockDim.x + threadIdx.x;

        if (threadID < startNodeIndex)
            if (distances[threadID + startThreadIndex * networkSize] >= boxSize)
                blockedBoxes[boxCovering[threadID + (boxSize - minBoxSize) * networkSize]] = 1;
            //else
            //    blockedBoxes[boxCovering[threadID + (boxSize - minBoxSize) * networkSize]] = 0;
    }

    __global__ void findFirstFreeBox (int networkSize, int *blockedBoxes){
        /*int iterator = 0;
        while (blockedBoxes[iterator] == 1)
            iterator++;
        firsOccurrence = iterator;*/

        int threadID = blockIdx.x * blockDim.x + threadIdx.x;

        if (threadID < networkSize){
            if (threadID == 0){
                firsOccurrence = networkSize;
                __threadfence();
            }
            if (blockedBoxes[threadID] == 0)
                atomicMin(&firsOccurrence, threadID);
        }
    }

    __global__ void resetBlocked (int networkSize, int *blockedBoxes){
        int threadID = blockIdx.x * blockDim.x + threadIdx.x;

        blockedBoxes[threadID] = 0;
    }

    __global__ void setBox (int startNodeIndex, int boxSizeIndex, int networkSize, int *boxCovering){
        boxCovering[startNodeIndex + boxSizeIndex * networkSize] = firsOccurrence;
    }
}