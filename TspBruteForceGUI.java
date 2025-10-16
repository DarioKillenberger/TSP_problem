import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.io.*;
import java.util.*;
import java.util.List;



public class TspBruteForceGUI {

    public static int[] tsp(double[][] matrix) {
        int n = matrix.length;
        int[] permutation = new int[n];
        for (int i = 0; i < n; i++) permutation[i] = i;

        int[] bestTour = permutation.clone();
        double bestTourCost = Double.POSITIVE_INFINITY;

        do {
            double tourCost = computeTourCost(permutation, matrix);
            if (tourCost < bestTourCost) {
                bestTourCost = tourCost;
                bestTour = permutation.clone();
            }
        } while (nextPermutation(permutation));

        return bestTour;
    }

    public static double computeTourCost(int[] tour, double[][] matrix) {
        double cost = 0;

        for (int i = 1; i < matrix.length; i++) {
            int from = tour[i - 1];
            int to = tour[i];
            if (matrix[from][to] == Double.POSITIVE_INFINITY) return Double.POSITIVE_INFINITY;
            cost += matrix[from][to];
        }

        int last = tour[matrix.length - 1];
        int first = tour[0];
        if (matrix[last][first] == Double.POSITIVE_INFINITY) return Double.POSITIVE_INFINITY;
        return cost + matrix[last][first];
    }

    public static boolean nextPermutation(int[] sequence) {
        int first = getFirst(sequence);
        if (first == -1) return false;
        int toSwap = sequence.length - 1;
        while (sequence[first] >= sequence[toSwap]) --toSwap;
        swap(sequence, first++, toSwap);
        toSwap = sequence.length - 1;
        while (first < toSwap) swap(sequence, first++, toSwap--);
        return true;
    }

    private static int getFirst(int[] sequence) {
        for (int i = sequence.length - 2; i >= 0; --i) if (sequence[i] < sequence[i + 1]) return i;
        return -1;
    }

    private static void swap(int[] sequence, int i, int j) {
        int tmp = sequence[i];
        sequence[i] = sequence[j];
        sequence[j] = tmp;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TSP Brute Force Solution");
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

                        // Warning for large graphs
                        if (distanceMatrix.length > 12) {
                            int choice = JOptionPane.showConfirmDialog(frame, 
                                "Warning: Brute force with " + distanceMatrix.length + " nodes will be EXTREMELY slow!\n" +
                                "Continue anyway?", 
                                "Performance Warning", 
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                            if (choice != JOptionPane.YES_OPTION) {
                                return;
                            }
                        }

                        int[] bestTour = tsp(distanceMatrix);
                        double tourCost = computeTourCost(bestTour, distanceMatrix);
                        
                        long endTime = System.nanoTime();
                        double elapsedSeconds = (endTime - startTime) / 1e9;

                        if (tourCost == Double.POSITIVE_INFINITY) {
                            textArea.setText("The graph is not fully connected. No valid tour exists.\n");
                        } else {
                            textArea.setText("Best Tour: " + Arrays.toString(bestTour) + "\n");
                            textArea.append("Tour Cost: " + tourCost + "\n");
                            textArea.append("Elapsed Time: " + String.format("%.4f", elapsedSeconds) + " seconds\n");
                        }

                        List<Integer> tourList = new ArrayList<>();
                        for (int node : bestTour) {
                            tourList.add(node);
                        }
                        GraphPanel graphPanel = new GraphPanel(tourList, null);

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

    static class Edge {
        int src, dest;
        double weight;

        Edge(int src, int dest, double weight) {
            this.src = src;
            this.dest = dest;
            this.weight = weight;
        }
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
