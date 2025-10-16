#!/usr/bin/env python3
"""
Random Graph Generator for TSP
Generates graph files in edge list format compatible with TSP solvers.
Now generates METRIC graphs (satisfies triangle inequality) using Euclidean coordinates.
"""

import random
import sys
import argparse
import math


def generate_random_graph(num_nodes, num_edges=None, min_weight=1.0, max_weight=120.0, output_file="graph.txt", metric=True):
    """
    Generate a random graph with specified parameters.
    
    Args:
        num_nodes: Number of nodes in the graph
        num_edges: Number of edges to generate (if None, generates complete graph)
        min_weight: Minimum edge weight (for metric graphs, controls coordinate range)
        max_weight: Maximum edge weight (for metric graphs, controls coordinate range)
        output_file: Output file name
        metric: If True, generates metric graph using Euclidean coordinates (RECOMMENDED for Christofides)
    """
    
    if num_nodes < 2:
        print("Error: Number of nodes must be at least 2")
        sys.exit(1)
    
    # Calculate maximum possible edges for a complete graph
    max_possible_edges = (num_nodes * (num_nodes - 1)) // 2
    
    # If num_edges not specified, generate complete graph
    if num_edges is None:
        num_edges = max_possible_edges
        print(f"Generating complete graph with {num_nodes} nodes and {num_edges} edges")
    else:
        if num_edges > max_possible_edges:
            print(f"Warning: Requested {num_edges} edges, but maximum for {num_nodes} nodes is {max_possible_edges}")
            num_edges = max_possible_edges
        print(f"Generating graph with {num_nodes} nodes and {num_edges} edges")
    
    if metric:
        # Generate METRIC graph using Euclidean coordinates
        # This ensures triangle inequality holds (required for Christofides 1.5x guarantee)
        print("Generating METRIC graph (Euclidean distances from random coordinates)")
        
        # Generate random coordinates for each node
        # Scale coordinates to achieve desired distance range
        coord_range = max_weight / 1.5  # Adjust coordinate range to get desired edge weights
        coordinates = []
        for i in range(num_nodes):
            x = random.uniform(0, coord_range)
            y = random.uniform(0, coord_range)
            coordinates.append((x, y))
        
        # Calculate all edge distances using Euclidean distance
        all_edges = []
        for i in range(num_nodes):
            for j in range(i + 1, num_nodes):
                dx = coordinates[i][0] - coordinates[j][0]
                dy = coordinates[i][1] - coordinates[j][1]
                distance = math.sqrt(dx * dx + dy * dy)
                all_edges.append((i, j, distance))
        
        # Select edges
        if num_edges < max_possible_edges:
            # For incomplete graphs, select edges but warn about metric property
            print(f"Warning: Incomplete metric graph. Some shortest paths may not be direct edges.")
            all_edges.sort(key=lambda e: e[2])  # Sort by distance
            selected_edges = all_edges[:num_edges]  # Take shortest edges
        else:
            selected_edges = all_edges
        
        # Shuffle for variety
        random.shuffle(selected_edges)
        
        # Write to file
        with open(output_file, 'w') as f:
            for u, v, weight in selected_edges:
                f.write(f"{u} {v} {weight:.2f}\n")
        
        actual_min = min(e[2] for e in selected_edges)
        actual_max = max(e[2] for e in selected_edges)
        print(f"Graph saved to {output_file}")
        print(f"Nodes: 0 to {num_nodes - 1}")
        print(f"Edges: {num_edges}")
        print(f"Weight range: {actual_min:.2f} to {actual_max:.2f}")
        print(f"Triangle inequality: GUARANTEED ✓ (metric graph)")
        
    else:
        # Generate NON-METRIC graph with random weights (old behavior)
        # WARNING: Christofides may perform worse than 1.5x on non-metric graphs!
        print("WARNING: Generating NON-METRIC graph (random weights)")
        print("Christofides 1.5x approximation guarantee does NOT apply!")
        
        # Generate all possible edges for a complete graph
        all_possible_edges = []
        for i in range(num_nodes):
            for j in range(i + 1, num_nodes):
                all_possible_edges.append((i, j))
        
        # Randomly select edges
        selected_edges = random.sample(all_possible_edges, num_edges)
        
        # Generate random weights for each edge
        edges_with_weights = []
        for u, v in selected_edges:
            weight = round(random.uniform(min_weight, max_weight), 2)
            edges_with_weights.append((u, v, weight))
        
        # Shuffle the edges for variety
        random.shuffle(edges_with_weights)
        
        # Write to file
        with open(output_file, 'w') as f:
            for u, v, weight in edges_with_weights:
                f.write(f"{u} {v} {weight:.2f}\n")
        
        print(f"Graph saved to {output_file}")
        print(f"Nodes: 0 to {num_nodes - 1}")
        print(f"Edges: {num_edges}")
        print(f"Weight range: {min_weight:.2f} to {max_weight:.2f}")
        print(f"Triangle inequality: NOT GUARANTEED ⚠️")


def main():
    parser = argparse.ArgumentParser(
        description="Generate random graph files for TSP problems",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python generate_graph.py 25                    # Metric complete graph with 25 nodes
  python generate_graph.py 25 -e 300            # Metric graph, 25 nodes, 300 edges
  python generate_graph.py 10 -o test.txt       # Save to test.txt
  python generate_graph.py 10 --no-metric       # Non-metric graph (random weights)
  python generate_graph.py 50 -e 500 -w 10 100  # Custom distance range

Note: Use --metric (default) for Christofides algorithm to guarantee 1.5x approximation!
      Non-metric graphs may cause Christofides to perform arbitrarily badly.
        """
    )
    
    parser.add_argument('nodes', type=int, help='Number of nodes in the graph')
    parser.add_argument('-e', '--edges', type=int, default=None, 
                        help='Number of edges (default: complete graph)')
    parser.add_argument('-o', '--output', type=str, default='graph.txt',
                        help='Output file name (default: graph.txt)')
    parser.add_argument('-w', '--weights', nargs=2, type=float, 
                        default=[1.0, 120.0], metavar=('MIN', 'MAX'),
                        help='Distance range [min max] for metric graphs (default: 1.0 120.0)')
    parser.add_argument('-s', '--seed', type=int, default=None,
                        help='Random seed for reproducibility')
    parser.add_argument('--no-metric', action='store_true',
                        help='Generate non-metric graph (random weights, breaks Christofides guarantee)')
    
    args = parser.parse_args()
    
    # Set random seed if provided
    if args.seed is not None:
        random.seed(args.seed)
        print(f"Using random seed: {args.seed}")
    
    generate_random_graph(
        num_nodes=args.nodes,
        num_edges=args.edges,
        min_weight=args.weights[0],
        max_weight=args.weights[1],
        output_file=args.output,
        metric=not args.no_metric  # Default is metric (True)
    )


if __name__ == "__main__":
    main()


