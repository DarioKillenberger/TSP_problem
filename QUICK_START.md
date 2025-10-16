# Quick Start Guide

## 🚀 Get Started in 3 Steps

### Step 1: Generate a Test Graph

```bash
# Small graph for testing all algorithms (including brute force)
python generate_graph.py 10 -o test_small.txt

# Medium graph (skip brute force, too slow)
python generate_graph.py 15 -o test_medium.txt

# Large graph (Christofides and 2-Opt only)
python generate_graph.py 50 -o test_large.txt
```

### Step 2: Compile

```bash
javac -d bin *.java
```

### Step 3: Run & Compare

```bash
# For small graphs (n ≤ 12) - run all:
java -cp bin TspBruteForceGUI
java -cp bin TspDynamicProgrammingGUI
java -cp bin ChristofidesAlgorithmGUI
java -cp bin TSPWithGUI_multithread

# For larger graphs - skip brute force:
java -cp bin ChristofidesAlgorithmGUI
```

---

## 📊 What to Expect

### For a 10-node graph:

- **Brute Force**: ~1 second, cost = 123.45 (OPTIMAL)
- **Dynamic Programming**: < 0.1 seconds, cost = 123.45 (OPTIMAL)
- **Christofides**: < 0.01 seconds, cost ≤ 185.18 (≤ 1.5× optimal) ✓
- **2-Opt**: < 0.01 seconds, cost varies (heuristic)

---

## ✅ How to Verify Christofides is Working

Load the same graph into:

1. **Brute Force** (or Dynamic Programming)
2. **Christofides**

**Check**: `Christofides Cost ÷ Optimal Cost ≤ 1.5`

Example:

- Brute Force: 172.92
- Christofides: 231.45
- Ratio: 231.45 ÷ 172.92 = **1.34** ✓ (< 1.5, good!)

If ratio > 1.5, the graph is **not metric**! Regenerate with the fixed script.

---

## 🎯 Algorithm Selection Guide

| Nodes | Use This            | Why                        |
| ----- | ------------------- | -------------------------- |
| ≤ 10  | Brute Force         | Fast enough, gives optimal |
| 11-12 | Brute Force         | A bit slow but manageable  |
| 13-20 | Dynamic Programming | Optimal, reasonably fast   |
| 21+   | Christofides        | Fast with good guarantee   |
| 100+  | 2-Opt/Clustering    | Very fast, approximate     |

---

## 🔧 Common Issues

### "Cost is way higher than 1.5x!"

→ You're using an old non-metric graph. Regenerate:

```bash
python generate_graph.py <nodes> -o new_graph.txt
```

### "Brute force is taking forever"

→ Graph too large (n > 12). Use Dynamic Programming or Christofides instead.

### "I want to test non-metric graphs"

→ Use `--no-metric` flag:

```bash
python generate_graph.py 10 --no-metric -o bad_graph.txt
```

(Christofides will perform poorly, as expected)

---

## 📝 Example Session

```bash
# Generate test graph
python generate_graph.py 12 -o test.txt
# Output: "Generating METRIC graph... ✓"

# Compile
javac -d bin *.java

# Test brute force (optimal)
java -cp bin TspBruteForceGUI
# Load test.txt
# Result: "Tour Cost: 234.56, Elapsed Time: 2.3 seconds"

# Test Christofides (fast approximation)
java -cp bin ChristofidesAlgorithmGUI
# Load test.txt
# Result: "Cost: 298.12, Elapsed Time: 0.01 seconds"

# Verify: 298.12 ÷ 234.56 = 1.27 ✓ (< 1.5, working correctly!)
```

---

Happy Testing! 🎉
