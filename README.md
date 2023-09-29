The application was made for the CyberSummer@WUT-3 programme by Kordian Makulski.

Launching the application requires updated version of JDK by Oracle.
It is available at https://www.oracle.com/pl/java/technologies/downloads/

External libraries used in the code:
 - JCuda - http://javagl.de/jcuda.org/
 - JFreeChart - https://www.jfree.org/jfreechart/

The GPU based module is based on Nvidia CUDA technology and as a result, it will only run on campatible Nvidia graphic cards.
For this functionality CUDA Toolkit is required, which is available at https://developer.nvidia.com/cuda-downloads
CUDA functions are stored in the "BFSBoxCoveringCuda.ptx" file in "resources", so its presence in the application's folder is required.
The graphic cards based functionality is not required to run full analysis. It only accelerates the most time consuming part of the process.

Applications aim is to help in analysis of box dimension of complex networks. It allows to:
 - cover the biggest cluster of the network with boxes, sizes range of which are predefined by the user,
 - fit linear function to relation between logarithm of box sizes and logarithm of box amount for range determined by the user,
 - rescale the characteristic by logarithmic binning of box sizes, which counteracts bias of fitting due to uneven probing density,
 - calculate box dimension for given parameters and export results to files.

For the application to properly work, imported network needs to be saved in correct format.
File must contain only edges of the network, one per line of the file.
A single edge is represented by a pair of indexes of nodes it connects, separated by a single space.
Node indexes are consecutive integers, starting from 0. 
Integers not beeing consecutive or not starting from 0 may result in slower detection of biggest cluster, but will not otherwise affect analysis.
Negative values of indexes are not allowed and will result in application error.

3 sample networks were provided both for purposes of testing and showing correct formatting.
