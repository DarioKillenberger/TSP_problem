import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;





public class TSPWithGUI_multithread {
    static void writeTourToFile(String fileName, List<Integer> finalTour, double cost, double elapsedSeconds) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentTime = LocalDateTime.now().format(formatter);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("Timestamp: " + currentTime + "\n");
            writer.write("Final Tour: " + finalTour + "\n");
            writer.write("Cost: " + String.format("%.2f", cost) + "\n");
            writer.write("Elapsed Time: " + String.format("%.2f", elapsedSeconds) + " seconds\n");
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
    static class Edge {
        int src, dest;
        double weight;

        public Edge(int src, int dest, double weight) {
            this.src = src;
            this.dest = dest;
            this.weight = weight;
        }
    }

    static class Graph {
        int V;
        List<Edge> edges;
        Map<Integer, List<Edge>> adjList;

        public Graph(int V) {
            this.V = V;
            edges = new ArrayList<>();
            adjList = new HashMap<>();
            for (int i = 0; i < V; i++) adjList.put(i, new ArrayList<>());
        }

        void addEdge(int src, int dest, double weight) {
            Edge edge = new Edge(src, dest, weight);
            edges.add(edge);
            adjList.get(src).add(edge);
            adjList.get(dest).add(new Edge(dest, src, weight));
        }

        double getWeight(int src, int dest) {
            for (Edge edge : adjList.get(src)) {
                if (edge.dest == dest) return edge.weight;
            }
            return Double.MAX_VALUE;
        }
    }

    static Graph parseGraph(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line = br.readLine();

        if (line != null && line.matches("\\d+(\\.\\d+)?")) {
            double coefficient = Double.parseDouble(line);
            List<double[]> coordinates = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 2) {
                    double x = Double.parseDouble(parts[0]);
                    double y = Double.parseDouble(parts[1]);
                    coordinates.add(new double[]{x, y});
                }
            }
            br.close();

            int nodeCount = coordinates.size();
            Graph graph = new Graph(nodeCount);

            for (int i = 0; i < nodeCount; i++) {
                for (int j = i + 1; j < nodeCount; j++) {
                    double[] coord1 = coordinates.get(i);
                    double[] coord2 = coordinates.get(j);
                    double distance = Math.sqrt(Math.pow(coord1[0] - coord2[0], 2) + Math.pow(coord1[1] - coord2[1], 2));
                    double weight = coefficient * distance;
                    graph.addEdge(i, j, weight);
                }
            }

            return graph;
        } else {
            List<String[]> rawEdges = new ArrayList<>();
            int maxNode = -1;

            do {
                String[] parts = line.split(" ");
                rawEdges.add(parts);
                maxNode = Math.max(maxNode, Math.max(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])));
            } while ((line = br.readLine()) != null);

            br.close();

            Graph graph = new Graph(maxNode + 1);
            for (String[] parts : rawEdges) {
                int src = Integer.parseInt(parts[0]);
                int dest = Integer.parseInt(parts[1]);
                double weight = Double.parseDouble(parts[2]);
                graph.addEdge(src, dest, weight);
            }
            return graph;
        }
    }

    static List<List<Integer>> clusterCities(Graph graph, int k) {
        List<List<Integer>> clusters = new ArrayList<>();
        for (int i = 0; i < k; i++) clusters.add(new ArrayList<>());
        for (int i = 0; i < graph.V; i++) {
            clusters.get(i % k).add(i);
        }
        return clusters;
    }

    static List<Integer> solveCluster(Graph graph, List<Integer> cluster) {
        List<Integer> tour = new ArrayList<>();
        boolean[] visited = new boolean[graph.V];
        int start = cluster.get(0);

        tour.add(start);
        visited[start] = true;

        while (tour.size() < cluster.size()) {
            int last = tour.get(tour.size() - 1);
            int next = -1;
            double minWeight = Double.MAX_VALUE;

            for (int node : cluster) {
                if (!visited[node]) {
                    double weight = graph.getWeight(last, node);
                    if (weight < minWeight) {
                        next = node;
                        minWeight = weight;
                    }
                }
            }

            if (next == -1) break;
            tour.add(next);
            visited[next] = true;
        }

        tour.add(start);
        return optimizeTour(graph, tour);
    }

    static List<Integer> optimizeTour(Graph graph, List<Integer> tour) {
        boolean improved = true;
        while (improved) {
            improved = false;
            for (int i = 1; i < tour.size() - 2; i++) {
                for (int j = i + 1; j < tour.size() - 1; j++) {
                    if (swapImproves(graph, tour, i, j)) {
                        Collections.reverse(tour.subList(i, j + 1));
                        improved = true;
                    }
                }
            }
        }
        return tour;
    }

    static boolean swapImproves(Graph graph, List<Integer> tour, int i, int j) {
        int a = tour.get(i - 1), b = tour.get(i);
        int c = tour.get(j), d = tour.get(j + 1);
        return graph.getWeight(a, b) + graph.getWeight(c, d) > graph.getWeight(a, c) + graph.getWeight(b, d);
    }

    static List<Integer> mergeClusters(Graph graph, List<List<Integer>> clusterTours) {
        List<Integer> globalTour = new ArrayList<>();
        for (List<Integer> clusterTour : clusterTours) {
            globalTour.addAll(clusterTour.subList(0, clusterTour.size() - 1));
        }
        globalTour.add(globalTour.get(0));
        return optimizeTour(graph, globalTour);
    }

    static double calculateTourCost(Graph graph, List<Integer> tour) {
        double totalCost = 0.0;
        for (int i = 0; i < tour.size() - 1; i++) {
            totalCost += graph.getWeight(tour.get(i), tour.get(i + 1));
        }
        return totalCost;
    }

    static class GraphPanel extends JPanel {
        Graph graph;
        List<Integer> tour;

        public GraphPanel(Graph graph, List<Integer> tour) {
            this.graph = graph;
            this.tour = tour;
            setBackground(new Color(240, 240, 255));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int radius = Math.min(getWidth(), getHeight()) / 3;
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;

            Map<Integer, Point> points = new HashMap<>();
            for (int i = 0; i < graph.V; i++) {
                int x = (int) (centerX + radius * Math.cos(2 * Math.PI * i / graph.V));
                int y = (int) (centerY + radius * Math.sin(2 * Math.PI * i / graph.V));
                points.put(i, new Point(x, y));
            }

            g2d.setColor(Color.GRAY);
            for (Edge edge : graph.edges) {
                Point p1 = points.get(edge.src);
                Point p2 = points.get(edge.dest);
                g2d.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
            }

            g2d.setColor(Color.BLUE);
            for (int i = 0; i < tour.size() - 1; i++) {
                Point p1 = points.get(tour.get(i));
                Point p2 = points.get(tour.get(i + 1));
                g2d.setStroke(new BasicStroke(2));
                g2d.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
            }

            g2d.setColor(Color.BLACK);
            for (Map.Entry<Integer, Point> entry : points.entrySet()) {
                Point point = entry.getValue();
                g2d.fillOval(point.x - 5, point.y - 5, 10, 10);
                g2d.drawString(entry.getKey().toString(), point.x + 10, point.y);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TSP Solution");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 800);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(new Color(240, 240, 255));

            JButton loadButton = new JButton("Load Graph");
            loadButton.setBackground(new Color(70, 130, 180));
            loadButton.setForeground(Color.BLACK);
            loadButton.setFont(new Font("Arial", Font.BOLD, 14));
            loadButton.setFocusPainted(false);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.setBackground(new Color(240, 240, 255));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            buttonPanel.add(loadButton);
            mainPanel.add(buttonPanel, BorderLayout.NORTH);

            JTextArea textArea = new JTextArea(3, 50);
            textArea.setEditable(false);
            textArea.setFont(new Font("Courier New", Font.PLAIN, 14));
            textArea.setBackground(new Color(245, 245, 245));
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

                        Graph graph = parseGraph(selectedFile.getAbsolutePath());
                        int k = 3;

                        List<List<Integer>> clusters = clusterCities(graph, k);

                        ExecutorService executor = Executors.newFixedThreadPool(k);
                        List<Future<List<Integer>>> futures = new ArrayList<>();

                        for (List<Integer> cluster : clusters) {
                            Callable<List<Integer>> task = () -> solveCluster(graph, cluster);
                            futures.add(executor.submit(task));
                        }

                        List<List<Integer>> clusterTours = new ArrayList<>();
                        for (Future<List<Integer>> future : futures) {
                            clusterTours.add(future.get());
                        }

                        executor.shutdown();

                        List<Integer> finalTour = mergeClusters(graph, clusterTours);
                        double cost = calculateTourCost(graph, finalTour);

                        long endTime = System.nanoTime();
                        double elapsedSeconds = (endTime - startTime) / 1e9;

                        textArea.setText("Final Tour: " + finalTour + "\n");
                        textArea.append("Cost: " + String.format("%.2f", cost) + "\n");
                        textArea.append("Elapsed Time: " + String.format("%.2f", elapsedSeconds) + " seconds");

                        // Write results to a file with timestamp
                        writeTourToFile("TSP_Solution.txt", finalTour, cost, elapsedSeconds);

                        GraphPanel graphPanel = new GraphPanel(graph, finalTour);
                        mainPanel.add(graphPanel, BorderLayout.CENTER);
                        mainPanel.revalidate();
                        mainPanel.repaint();

                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame, "Error loading graph: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (InterruptedException | ExecutionException ex) {
                        JOptionPane.showMessageDialog(frame, "Error solving clusters: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
;
            ;
            ;;
        });
    }
}

