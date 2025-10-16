import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.io.*;
import java.util.*;
import java.util.List;

public class TspDynamicProgrammingGUI {

    static class TspDynamicProgrammingIterative {
        private final int N, start;
        private final double[][] distance;
        private List<Integer> tour = new ArrayList<>();
        private double minTourCost = Double.POSITIVE_INFINITY;
        private boolean ranSolver = false;

        public TspDynamicProgrammingIterative(double[][] distance) {
            this(0, distance);
        }

        public TspDynamicProgrammingIterative(int start, double[][] distance) {
            N = distance.length;

            if (N <= 2) throw new IllegalStateException("N <= 2 not yet supported.");
            if (N != distance[0].length) throw new IllegalStateException("Matrix must be square (n x n)");
            if (start < 0 || start >= N) throw new IllegalArgumentException("Invalid start node.");
            if (N > 20)
                throw new IllegalArgumentException(
                        "Matrix too large!");

            this.start = start;
            this.distance = distance;
        }

        public List<Integer> getTour() {
            if (!ranSolver) solve();
            return tour;
        }

        public double getTourCost() {
            if (!ranSolver) solve();
            return minTourCost;
        }

        public void solve() {

            if (ranSolver) return;

            final int END_STATE = (1 << N) - 1;
            Double[][] memo = new Double[N][1 << N];

            for (int end = 0; end < N; end++) {
                if (end == start) continue;
                memo[end][(1 << start) | (1 << end)] = distance[start][end];
            }

            for (int r = 3; r <= N; r++) {
                for (int subset : combinations(r, N)) {
                    if (notIn(start, subset)) continue;
                    for (int next = 0; next < N; next++) {
                        if (next == start || notIn(next, subset)) continue;
                        int subsetWithoutNext = subset ^ (1 << next);
                        double minDist = Double.POSITIVE_INFINITY;
                        for (int end = 0; end < N; end++) {
                            if (end == start || end == next || notIn(end, subset)) continue;
                            double newDistance = memo[end][subsetWithoutNext] + distance[end][next];
                            if (newDistance < minDist) {
                                minDist = newDistance;
                            }
                        }
                        memo[next][subset] = minDist;
                    }
                }
            }

            for (int i = 0; i < N; i++) {
                if (i == start) continue;
                double tourCost = memo[i][END_STATE] + distance[i][start];
                if (tourCost < minTourCost) {
                    minTourCost = tourCost;
                }
            }

            int lastIndex = start;
            int state = END_STATE;
            tour.add(start);

            for (int i = 1; i < N; i++) {

                int bestIndex = -1;
                double bestDist = Double.POSITIVE_INFINITY;
                for (int j = 0; j < N; j++) {
                    if (j == start || notIn(j, state)) continue;
                    double newDist = memo[j][state] + distance[j][lastIndex];
                    if (newDist < bestDist) {
                        bestIndex = j;
                        bestDist = newDist;
                    }
                }

                tour.add(bestIndex);
                state = state ^ (1 << bestIndex);
                lastIndex = bestIndex;
            }

            tour.add(start);
            Collections.reverse(tour);

            ranSolver = true;
        }

        private static boolean notIn(int elem, int subset) {
            return ((1 << elem) & subset) == 0;
        }

        public static List<Integer> combinations(int r, int n) {
            List<Integer> subsets = new ArrayList<>();
            combinations(0, 0, r, n, subsets);
            return subsets;
        }

        private static void combinations(int set, int at, int r, int n, List<Integer> subsets) {
            int elementsLeftToPick = n - at;
            if (elementsLeftToPick < r) return;

            if (r == 0) {
                subsets.add(set);
            } else {
                for (int i = at; i < n; i++) {
                    set ^= (1 << i);
                    combinations(set, i + 1, r - 1, n, subsets);
                    set ^= (1 << i);
                }
            }
        }
    }

    static class Edge {
        int src, dest;
        double weight;

        Edge(int src, int dest, double weight) {
            this.src = src;
            this.dest = dest;
            this.weight = weight;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TSP Dynamic Programming Solution");
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
                        long startTime = System.nanoTime();
                        
                        BufferedReader br = new BufferedReader(new FileReader(selectedFile));
                        String line = br.readLine();
                        double[][] distanceMatrix;

                        if (line.matches("\\d+(\\.\\d+)?")) {
                            double coefficient = Double.parseDouble(line);
                            List<double[]> coordinates = new ArrayList<>();

                            while ((line = br.readLine()) != null) {
                                String[] parts = line.split(" ");
                                double x = Double.parseDouble(parts[0]);
                                double y = Double.parseDouble(parts[1]);
                                coordinates.add(new double[]{x, y});
                            }

                            int n = coordinates.size();
                            distanceMatrix = new double[n][n];

                            for (int i = 0; i < n; i++) {
                                for (int j = 0; j < n; j++) {
                                    if (i == j) {
                                        distanceMatrix[i][j] = 0;
                                    } else {
                                        double dx = coordinates.get(i)[0] - coordinates.get(j)[0];
                                        double dy = coordinates.get(i)[1] - coordinates.get(j)[1];
                                        distanceMatrix[i][j] = coefficient * Math.sqrt(dx * dx + dy * dy);
                                    }
                                }
                            }

                        } else {
                            List<Edge> edges = new ArrayList<>();
                            int maxNode = -1;

                            do {
                                String[] parts = line.split(" ");
                                int src = Integer.parseInt(parts[0]);
                                int dest = Integer.parseInt(parts[1]);
                                double weight = Double.parseDouble(parts[2]);
                                edges.add(new Edge(src, dest, weight));
                                maxNode = Math.max(maxNode, Math.max(src, dest));
                            } while ((line = br.readLine()) != null);

                            int n = maxNode + 1;
                            distanceMatrix = new double[n][n];
                            for (double[] row : distanceMatrix) Arrays.fill(row, Double.POSITIVE_INFINITY);

                            for (Edge edge : edges) {
                                distanceMatrix[edge.src][edge.dest] = edge.weight;
                                distanceMatrix[edge.dest][edge.src] = edge.weight;
                            }
                        }

                        br.close();

                        int startNode = 0;
                        TspDynamicProgrammingIterative solver = new TspDynamicProgrammingIterative(startNode, distanceMatrix);

                        List<Integer> tour = solver.getTour();
                        double cost = solver.getTourCost();
                        
                        long endTime = System.nanoTime();
                        double elapsedSeconds = (endTime - startTime) / 1e9;

                        textArea.setText("Tour: " + tour + "\n");
                        textArea.append("Cost: " + cost + "\n");
                        textArea.append("Elapsed Time: " + String.format("%.4f", elapsedSeconds) + " seconds\n");

                        GraphPanel graphPanel = new GraphPanel(tour, null);
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

    static class GraphPanel extends JPanel {
        List<Integer> tour;
        List<Edge> edges;

        GraphPanel(List<Integer> tour, List<Edge> edges) {
            this.tour = tour;
            this.edges = edges;
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
            Set<Integer> vertices = new HashSet<>();
            if (edges != null) {
                for (Edge edge : edges) {
                    vertices.add(edge.src);
                    vertices.add(edge.dest);
                }
            } else {
                for (int v : tour) {
                    vertices.add(v);
                }
            }

            for (int vertex : vertices) {
                int x = (int) (centerX + radius * Math.cos(2 * Math.PI * index / vertices.size()));
                int y = (int) (centerY + radius * Math.sin(2 * Math.PI * index / vertices.size()));
                points.put(vertex, new Point(x, y));
                index++;
            }

            if (edges != null) {
                for (Edge edge : edges) {
                    Point p1 = points.get(edge.src);
                    Point p2 = points.get(edge.dest);
                    g2d.setColor(Color.GRAY);
                    g2d.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
                }
            }

            g2d.setColor(Color.BLUE);
            for (int i = 0; i < tour.size() - 1; i++) {
                Point p1 = points.get(tour.get(i));
                Point p2 = points.get(tour.get(i + 1));
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
