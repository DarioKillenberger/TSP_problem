# Design and Analysis of Algorithms

**Final project (TSP problem)**

---

This document explains four algorithms for solving the **Traveling Salesman Problem (TSP**):

1. **Brute Force Algorithm**
2. **Dynamic Programming Approach**
3. **Christofides Algorithm** 
4. **Clustering and 2-Opt Algorithm**

---

## 1. Brute Force Algorithm
### Approach

The brute force approach involves generating all possible permutations of the cities and calculating the total distance for each permutation. It guarantees to find the optimal solution but becomes computationally infeasible for large problem sizes due to its exponential time complexity.

### Key Functions and Complexity

1. **`tsp(matrix)`**
    - Iterates through all permutations of nodes.
    - Calls `computeTourCost` for each permutation.
    - **Time Complexity**: O(n!) (due to permutations).
    - **Space Complexity**: O(n^2) (for storing permutations).
2. **`computeTourCost(tour, matrix)`**
    - Computes the cost of a given tour.
    - **Time Complexity**: O(n).
3. **`nextPermutation(sequence)`**
    - Generates the next lexicographical permutation.
    - **Time Complexity**: O(n).

### Overall Complexity

- **Time Complexity**: O(n × n!)
- **Space Complexity**: O(n^2).

---

## 2. Dynamic Programming Approach
### Approach

This approach uses a dynamic programming table to store subproblem solutions. It builds solutions iteratively by combining smaller subproblems. It is efficient for small to medium-sized inputs. 

### Key Functions and Complexity

1. **`solve()`**
    - Implements dynamic programming to solve the TSP.
    - Uses a bitmask to represent subsets.
    - **Time Complexity**: O(n² × 2ⁿ).
    - **Space Complexity**: O(n × 2ⁿ).
2. **`combinations(r, n)`**
    - Generates all combinations of size `r` from a set of `n` elements.
    - **Time Complexity**: O(2ⁿ).
3. **`notIn(elem, subset)`**
    - Checks if an element is in a subset.
    - **Time Complexity**: O(1).

### Overall Complexity

- **Time Complexity**: O(n² × 2ⁿ).
- **Space Complexity**: O(n × 2ⁿ).

---

## 3. Christofides Algorithm
### Approach

The Christofides Algorithm is a polynomial-time **approximation algorithm** that guarantees a solution **no worse than 1.5 times the optimal**. This algorithm is suitable for complete graphs.  The graph should also be **metric** which means: 

1. The distance from a point to itself is zero:
    
    d(x,x)=0
    
2. (Positivity) The distance between two distinct points is always positive:
    
    If x≠y, then d(x,y)>0
    
3.  The distance from *x* to *y* is always the same as the distance from *y* to *x*: (symmetry) 
    
    d(x,y)=d(y,x)
    
4. The triangle inequality holds:
    
    d(x,z)≤d(x,y)+d(y,z)
    

 The steps include:

1. Compute the Minimum Spanning Tree (MST).
2. Find vertices with odd degrees in the MST. ( There is always an even number of odd vertices)
3. Find a minimum weight perfect matching for the odd-degree vertices.
4. Combine the MST and perfect matching into a multigraph.
5. Find an Eulerian circuit in the multigraph using Hierholzer's algorithm. This algorithm maintains an adjacency list and uses a stack to simulate depth-first traversal. The circuit is built by backtracking once a vertex has no more edges to traverse.
6. Convert the Eulerian circuit into a Hamiltonian circuit.

### Key Functions and Complexity

1. **`getMinimumSpanningTree(edges, vertexCount)`**
    - Uses Kruskal's algorithm to compute MST.
    - **Time Complexity**: O(E log E).
2. **`findOddDegreeVertices(MST)`**
    - Identifies vertices with an odd degree.
    - **Time Complexity**: O(V).
3. **`findPerfectMatching(oddVertices, edges)`**
    - Finds a minimum weight perfect matching.
    - **Time Complexity**: O(V²).
4. **`findEulerianCircuit(multigraph, vertexCount)`**
    - Constructs an Eulerian circuit using Hierholzer's algorithm.
    - **Time Complexity**: O(E).
5. **`makeHamiltonian(eulerianCircuit)`**
    - Converts an Eulerian circuit into a Hamiltonian circuit by removing duplicate vertices.
    - **Time Complexity**: O(V).
6. **`calculateCost(circuit, edges)`**
    - Computes the total cost of the Hamiltonian circuit.
    - **Time Complexity**: O(V).

### Overall Complexity

- **Time Complexity**: O(V²logE )
- **Space Complexity**: O(V + E)

---

## 4. Clustering and 2-Opt Algorithm
### Approach

This algorithm provides an approximate solution by:

1. Clustering cities into smaller groups.
2. Solving TSP for each cluster using a greedy approach with 2-opt optimization.
3. Merging the clusters into a global tour and applying 2-opt again for refinement.

   This algorithm is running each cluster (k) on a seperate thread. **Parallel processing** of clusters significantly reduces the runtime, especially when the number of clusters and their sizes are large.

### 2-Opt Algorithm:

![IMG_8668](https://github.com/user-attachments/assets/c0b7fef0-5fa4-4372-982c-59d43ceb4fd7)

step1 

![IMG_8669](https://github.com/user-attachments/assets/35851f61-8e9b-4827-a57b-98801e2fe624)

step2

![IMG_8670](https://github.com/user-attachments/assets/2131437f-fac9-4a11-9534-823eeafa2dc4)

step3

![IMG_8671](https://github.com/user-attachments/assets/c2ec9469-b568-420d-a22f-0e3518125d0a)

step4

### Key Functions and Complexity

1. **`clusterCities(graph, k)`**
    - Divides the graph's vertices into `k` clusters using basic indexing.
    - **Time Complexity**: O(V).
    - **Space Complexity**: O(V).
2. **`solveCluster(graph, cluster)`**
    - Finds an approximate tour for a cluster using a greedy approach and 2-opt optimization.
    - **Time Complexity**: O(c²), where `c` is the cluster size.
    - **Space Complexity**: O(c).
3. **`optimizeTour(graph, tour)`**
    - Refines a tour using the 2-opt algorithm.
    - **Time Complexity**: O(c²).
    - **Space Complexity**: O(1).
4. **`mergeClusters(graph, clusterTours)`**
    - Combines cluster tours into a global tour and applies 2-opt.
    - **Time Complexity**: O(V²).
    - **Space Complexity**: O(V).

### Overall Complexity

- **Time Complexity**: O(V² ).
- **Space Complexity**: O(V + E).

---

## Comparison of Approaches

| Algorithm | Time Complexity | Space Complexity | Notes |
| --- | --- | --- | --- |
| Brute Force | O(n × n!) | O(n^2) | Guarantees optimal solution, but infeasible for large n. |
| Dynamic Programming | O(n² × 2ⁿ) | O(n × 2ⁿ) | Efficient for moderate-sized graphs (n<20) |
| Christofides Algorithm | O(V^2logE) | O(V + E) | Polynomial-time approximation algorithm (1.5-optimal). |
| Clustering and 2-Opt | O(V² ) | O(V + E) | Scalable and efficient for large graphs, but does not guarantee optimality. |
