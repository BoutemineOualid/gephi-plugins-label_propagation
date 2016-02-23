gephi-plugins-label_propagation
===============================

A gephi plugin that provides an implementation for several Label Propagation-based algorithms (LPA) for community detection in graphs. 
The plugin supports animated propagation of cluster labels for educational purpose. In addition, the current implementation supports weighted edges and provides implementations for three LPA variants including the basic one. The 3 variants currently implemented are : basic LPA (1), randomized LPA (LPAr) (2) and modularity maximization LPA (LPAm) (2). Each variant is described by a specific propagation rule that guides the search for the best partition of the network. The NBM binary can be downloaded from this link : https://marketplace.gephi.org/plugin/label-propagation-clustering/

Change log
===========
Version 2 : - Added support for two more LPA algorithms.
              - Added support for weighted graphs.
              - Fixed minor issues in the original implementation.

References
===========
1. Raghavan, U. N., Albert, R., & Kumara, S. (2007). Near linear time algorithm to detect community structures in large-scale networks. Physical Review E, 76(3), 036106.

2. Barber, Michael J., and John W. Clark. Detecting network communities by propagating labels under constraints. Physical Review E 80.2 (2009): 026129.
