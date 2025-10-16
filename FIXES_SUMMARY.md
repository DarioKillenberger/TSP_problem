### Bug #3: Non-Metric Graph Generation (ROOT CAUSE!)

**File**: `generate_graph.py`  
**Impact**: CRITICAL - Made Christofides unreliable

**Problem**: Generated graphs with random weights that violate triangle inequality

**Solution**: Completely rewrote to generate metric graphs using Euclidean coordinates:

1. Generate random (x, y) coordinates for each node
2. Calculate Euclidean distances: `sqrt((x1-x2)² + (y1-y2)²)`
3. This **automatically guarantees** triangle inequality!

---

## How to Use the Fixed Code

### Generate Proper Test Graphs

```bash
# Default: Metric complete graph (RECOMMENDED for Christofides)
python generate_graph.py 14 -o test_metric.txt

# Metric graph with specific number of edges
python generate_graph.py 25 -e 300 -o metric_25.txt

# Control distance range
python generate_graph.py 20 -w 10 100 -o test_20.txt

# Old behavior (non-metric, breaks Christofides)
python generate_graph.py 10 --no-metric -o bad_graph.txt
```

### Compile and Run

```bash
# Compile all
javac -d bin *.java

# Test with different algorithms on METRIC graphs
java -cp bin TspBruteForceGUI          # Optimal (slow, n ≤ 12)
java -cp bin TspDynamicProgrammingGUI  # Optimal (faster, n ≤ 20)
java -cp bin ChristofidesAlgorithmGUI  # 1.5x approx (fast, any n)
java -cp bin TSPWithGUI_multithread    # Heuristic (very fast, any n)
```

---

## Expected Results Now

### On METRIC graphs (generated with new script):

| Algorithm           | Time Complexity | Quality              | Speed              |
| ------------------- | --------------- | -------------------- | ------------------ |
| Brute Force         | O(n!)           | **Optimal**          | Very slow (n ≤ 12) |
| Dynamic Programming | O(n² × 2ⁿ)      | **Optimal**          | Slow (n ≤ 20)      |
| **Christofides**    | **O(V²)**       | **≤ 1.5x optimal** ✓ | **Fast**           |
| 2-Opt/Clustering    | O(V²)           | Variable             | Very fast          |

### Example Test Case (14 nodes, metric graph):

- **Brute Force**: 172.92 (optimal)
- **Christofides**: Should be ≤ 259.38 (1.5 × 172.92) ✓
- Previous buggy version: 376.49 or worse ❌

---

## Key Takeaways

1. **Always use metric graphs** for Christofides algorithm
2. The **new graph generator** creates metric graphs by default
3. **Triangle inequality is crucial** for approximation guarantees
4. Use `--no-metric` flag only if you want to demonstrate how badly Christofides can fail on non-metric graphs

---

## Testing Recommendations

### Small Graphs (n ≤ 12):

```bash
python generate_graph.py 10 -o small.txt
```

- Run **all** algorithms
- Compare Christofides cost vs Brute Force optimal
- Verify: Christofides cost ≤ 1.5 × Brute Force cost

### Medium Graphs (12 < n ≤ 20):

```bash
python generate_graph.py 15 -o medium.txt
```

- Run Dynamic Programming (optimal)
- Run Christofides (should be close!)
- Verify: Christofides cost ≤ 1.5 × DP cost

### Large Graphs (n > 20):

```bash
python generate_graph.py 100 -o large.txt
```

- Run Christofides and 2-Opt
- Compare their results
- Both should complete quickly

---

## What to Avoid

❌ **Don't** use old non-metric graphs (`graph.txt`, `2.txt`) for testing Christofides  
❌ **Don't** expect 1.5x guarantee on non-metric graphs  
❌ **Don't** use `--no-metric` flag unless intentionally testing failure cases  
✓ **Do** regenerate all test graphs using the new script  
✓ **Do** verify triangle inequality if using external graphs  
✓ **Do** use complete graphs (all edges) for best Christofides performance

---

Generated: October 2025
