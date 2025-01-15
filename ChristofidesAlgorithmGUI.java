import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.io.*;
import java.util.*;
import java.util.List;

public class ChristofidesAlgorithmGUI {

    static class Edge {
        int u, v;
        double weight;

        Edge(int u, int v, double weight) {
            this.u = u;
            this.v = v;
            this.weight = weight;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Christofides Algorithm Solution");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 800);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(new Color(240, 240, 255));

            JButton loadButton = new JButton("Load Graph");
            loadButton.setBackground(new Color(70, 130, 180));
            loadButton.setForeground(Color.BLACK);
            loadButton.setFont(new Font("Arial", Font.BOLD, 14));
            loadButton.setFocusPainted(false);
            loadButton.setToolTipText("Click to load a graph from a file");

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.setBackground(new Color(240, 240, 255));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            buttonPanel.add(loadButton);
            mainPanel.add(buttonPanel, BorderLayout.NORTH);

            JTextArea textArea = new JTextArea(3, 50);
            textArea.setEditable(false);
            textArea.setFont(new Font("Courier New", Font.PLAIN, 14));
            textArea.setBackground(new Color(245, 245, 245));
            textArea.setForeground(Color.DARK_GRAY);
            textArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(800, 150));
            scrollPane.setBorder(BorderFactory.createTitledBorder("Tour Details"));
            mainPanel.add(scrollPane, BorderLayout.SOUTH);

            frame.add(mainPanel);
            frame.setVisible(true);

            loadButton.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(selectedFile));
                        String line = br.readLine();
                        List<Edge> edges = new ArrayList<>();
                        Set<Integer> vertices = new HashSet<>();

                        if (line.matches("\\d+(\\.\\d+)?")) {
                            double coefficient = Double.parseDouble(line);
                            List<double[]> coordinates = new ArrayList<>();

                            while ((line = br.readLine()) != null) {
                                String[] parts = line.split(" ");
                                double x = Double.parseDouble(parts[0]);
                                double y = Double.parseDouble(parts[1]);
                                coordinates.add(new double[]{x, y});
                            }

                            for (int i = 0; i < coordinates.size(); i++) {
                                for (int j = i + 1; j < coordinates.size(); j++) {
                                    double dx = coordinates.get(i)[0] - coordinates.get(j)[0];
                                    double dy = coordinates.get(i)[1] - coordinates.get(j)[1];
                                    double distance = coefficient * Math.sqrt(dx * dx + dy * dy);
                                    edges.add(new Edge(i, j, distance));
                                    edges.add(new Edge(j, i, distance));
                                    vertices.add(i);
                                    vertices.add(j);
                                }
                            }
                        } else {
                            do {
                                String[] parts = line.split(" ");
                                int u = Integer.parseInt(parts[0]);
                                int v = Integer.parseInt(parts[1]);
                                double weight = Double.parseDouble(parts[2]);
                                edges.add(new Edge(u, v, weight));
                                vertices.add(u);
                                vertices.add(v);
                            } while ((line = br.readLine()) != null);
                        }

                        br.close();

                        List<Edge> mst = getMinimumSpanningTree(edges, vertices.size());
                        List<Integer> oddVertices = findOddDegreeVertices(mst);
                        List<Edge> perfectMatching = findPerfectMatching(oddVertices, edges);
                        List<Edge> multigraph = new ArrayList<>(mst);
                        multigraph.addAll(perfectMatching);

                        List<Integer> eulerianCircuit = findEulerianCircuit(multigraph, vertices.size());
                        List<Integer> hamiltonianCircuit = makeHamiltonian(eulerianCircuit);

                        double cost = calculateCost(hamiltonianCircuit, edges);

                        textArea.setText("Hamiltonian Circuit: " + hamiltonianCircuit + "\n");
                        textArea.append("Cost: " + cost + "\n");

                        GraphPanel graphPanel = new GraphPanel(vertices, edges, hamiltonianCircuit);
                        mainPanel.add(graphPanel, BorderLayout.CENTER);
                        mainPanel.revalidate();
                        mainPanel.repaint();

                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame, "Error loading graph: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        });
    }

    static List<Edge> getMinimumSpanningTree(List<Edge> edges, int vertexCount) {
        edges.sort(Comparator.comparingDouble(e -> e.weight));
        List<Edge> mst = new ArrayList<>();
        UnionFind uf = new UnionFind(vertexCount);

        for (Edge edge : edges) {
            if (uf.union(edge.u, edge.v)) {
                mst.add(edge);
                if (mst.size() == vertexCount - 1) break;
            }
        }
        return mst;
    }

    static List<Integer> findOddDegreeVertices(List<Edge> mst) {
        Map<Integer, Integer> degree = new HashMap<>();
        for (Edge edge : mst) {
            degree.put(edge.u, degree.getOrDefault(edge.u, 0) + 1);
            degree.put(edge.v, degree.getOrDefault(edge.v, 0) + 1);
        }
        List<Integer> oddVertices = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : degree.entrySet()) {
            if (entry.getValue() % 2 != 0) {
                oddVertices.add(entry.getKey());
            }
        }
        return oddVertices;
    }

    static List<Edge> findPerfectMatching(List<Integer> oddVertices, List<Edge> edges) {
        List<Edge> matching = new ArrayList<>();
        boolean[] used = new boolean[oddVertices.size()];
        for (int i = 0; i < oddVertices.size(); i++) {
            if (used[i]) continue;
            double minWeight = Double.MAX_VALUE;
            int bestJ = -1;
            for (int j = i + 1; j < oddVertices.size(); j++) {
                if (used[j]) continue;
                int u = oddVertices.get(i);
                int v = oddVertices.get(j);
                double weight = findWeight(u, v, edges);
                if (weight < minWeight) {
                    minWeight = weight;
                    bestJ = j;
                }
            }
            if (bestJ != -1) {
                used[i] = used[bestJ] = true;
                matching.add(new Edge(oddVertices.get(i), oddVertices.get(bestJ), minWeight));
            }
        }
        return matching;
    }

    static List<Integer> findEulerianCircuit(List<Edge> multigraph, int vertexCount) {
        Map<Integer, List<Edge>> adj = new HashMap<>();
        for (Edge edge : multigraph) {
            adj.computeIfAbsent(edge.u, k -> new ArrayList<>()).add(edge);
            adj.computeIfAbsent(edge.v, k -> new ArrayList<>()).add(new Edge(edge.v, edge.u, edge.weight));
        }

        Stack<Integer> stack = new Stack<>();
        List<Integer> circuit = new ArrayList<>();
        stack.push(multigraph.get(0).u);

        while (!stack.isEmpty()) {
            int node = stack.peek();
            if (adj.get(node) == null || adj.get(node).isEmpty()) {
                circuit.add(stack.pop());
            } else {
                Edge edge = adj.get(node).remove(0);
                adj.get(edge.v).removeIf(e -> e.u == node);
                stack.push(edge.v);
            }
        }
        return circuit;
    }

    static List<Integer> makeHamiltonian(List<Integer> eulerianCircuit) {
        Set<Integer> visited = new HashSet<>();
        List<Integer> hamiltonian = new ArrayList<>();
        int startNode = 0;

        for (int node : eulerianCircuit) {
            if (!visited.contains(node)) {
                visited.add(node);
                hamiltonian.add(node);
            }
        }

        if (hamiltonian.get(0) != startNode) {
            int index = hamiltonian.indexOf(startNode);
            if (index != -1) {
                List<Integer> reordered = new ArrayList<>();
                reordered.addAll(hamiltonian.subList(index, hamiltonian.size()));
                reordered.addAll(hamiltonian.subList(0, index));
                hamiltonian = reordered;
            }
        }

        hamiltonian.add(startNode);
        return hamiltonian;
    }

    static double calculateCost(List<Integer> circuit, List<Edge> edges) {
        double cost = 0;
        for (int i = 0; i < circuit.size() - 1; i++) {
            cost += findWeight(circuit.get(i), circuit.get(i + 1), edges);
        }
        return cost;
    }

    static double findWeight(int u, int v, List<Edge> edges) {
        for (Edge edge : edges) {
            if ((edge.u == u && edge.v == v) || (edge.u == v && edge.v == u)) {
                return edge.weight;
            }
        }
        return Double.MAX_VALUE;
    }

    static class UnionFind {
        int[] parent, rank;

        UnionFind(int size) {
            parent = new int[size];
            rank = new int[size];
            for (int i = 0; i < size; i++) parent[i] = i;
        }

        int find(int u) {
            if (parent[u] != u) parent[u] = find(parent[u]);
            return parent[u];
        }

        boolean union(int u, int v) {
            int rootU = find(u);
            int rootV = find(v);
            if (rootU != rootV) {
                if (rank[rootU] > rank[rootV]) {
                    parent[rootV] = rootU;
                } else if (rank[rootU] < rank[rootV]) {
                    parent[rootU] = rootV;
                } else {
                    parent[rootV] = rootU;
                    rank[rootU]++;
                }
                return true;
            }
            return false;
        }
    }

    static class GraphPanel extends JPanel {
        Set<Integer> vertices;
        List<Edge> edges;
        List<Integer> hamiltonianCircuit;

        GraphPanel(Set<Integer> vertices, List<Edge> edges, List<Integer> hamiltonianCircuit) {
            this.vertices = vertices;
            this.edges = edges;
            this.hamiltonianCircuit = hamiltonianCircuit;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int radius = 250;
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;

            Map<Integer, Point> points = new HashMap<>();
            int index = 0;
            for (int vertex : vertices) {
                int x = (int) (centerX + radius * Math.cos(2 * Math.PI * index / vertices.size()));
                int y = (int) (centerY + radius * Math.sin(2 * Math.PI * index / vertices.size()));
                points.put(vertex, new Point(x, y));
                index++;
            }

            for (Edge edge : edges) {
                Point p1 = points.get(edge.u);
                Point p2 = points.get(edge.v);
                g2d.setColor(Color.GRAY);
                g2d.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
            }

            g2d.setColor(Color.blue);
            for (int i = 0; i < hamiltonianCircuit.size() - 1; i++) {
                Point p1 = points.get(hamiltonianCircuit.get(i));
                Point p2 = points.get(hamiltonianCircuit.get(i + 1));
                g2d.setColor(Color.blue);
                g2d.setStroke(new BasicStroke(2));
                g2d.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
            }

            g2d.setColor(Color.BLACK);
            for (Map.Entry<Integer, Point> entry : points.entrySet()) {
                g2d.fillOval(entry.getValue().x - 5, entry.getValue().y - 5, 10, 10);
                g2d.drawString(entry.getKey().toString(), entry.getValue().x + 10, entry.getValue().y + 10);
            }
        }
    }
}
