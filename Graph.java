import java.util.*;

import processing.core.PGraphics;

public class Graph {
    private static final int NUM_VERTICES = 150;
    private static final int P = 2; // Halton sequence base
    private static final int Q = 3; // Halton sequence bas
    // The maximum allowed distance between points checked for collision on
    // an edge.
    private static final double EDGE_CHECK_PRECISION = 10.0;
    private static final int EDGE_CHECK_BASE = 2; // Base for Van der Corput

    private List<double[]> coordinates;
    private List<List<Integer>> neighbors;

    /**
     * 
     * @param map the map to generate a graph over
     * @param k   the maximum number of neighbors to connect each vertex with
     * @param r   the maximum distance between neighbors in pixels
     */
    public Graph(Map map, int k, double r) {
        this.coordinates = new ArrayList<>();
        this.neighbors = new ArrayList<>();
        buildGraph(map, k, r);

        // prints out coords of vertices with fewer than k neighbors
        for (int i = 0; i < this.neighbors.size(); i++) {
            if (this.neighbors.get(i).size() < k) {
                for (double d : this.coordinates.get(i))
                    System.out.print(d + ", ");

                System.out.println();
            }
        }
    }

    public int numVertices() {
        return this.coordinates.size();
    }

    public void render(PGraphics g) {
        for (int i = 0; i < this.numVertices(); i++) {
            // Render vertex
            double[] currCoords = this.coordinates.get(i);
            g.stroke(0);
            g.strokeWeight(5);
            g.point((float) currCoords[0], (float) currCoords[1]);

            for (int n : this.neighbors.get(i)) {
                double[] neighborCoords = this.coordinates.get(n);
                g.strokeWeight(1);
                g.stroke(90, 200, 255);
                g.line((float) currCoords[0],
                        (float) currCoords[1],
                        (float) neighborCoords[0],
                        (float) neighborCoords[1]);
            }
        }
    }

    /**
     * @param map the map to build the graph on top of
     * @param k   the maximum number of neighbors to connect each vertex with
     * @param r   the maximum distance between neighbors in pixels
     */
    private void buildGraph(Map map, int k, double r) {
        // Generate vertices on the map.
        generateVertices(map);

        // The number of vertices is now known. Initialize the neighbors list
        // with empty lists.
        for (int i = 0; i < this.numVertices(); i++) {
            this.neighbors.add(new ArrayList<>());
        }

        // Generate edges by connecting each vertex with its neighbors.
        for (int i = 0; i < this.numVertices(); i++) {
            // Add all neighbors that can be connected to this vertex.
            updateNeighbors(i, k, r, map);
        }
    }

    private void generateVertices(Map map) {
        // Generate vertices over the map, discarding those that intersect with
        // obstacles.
        HaltonSequence hs = new HaltonSequence(P, Q);
        while (this.coordinates.size() < NUM_VERTICES) {
            // Gives the position of the next with coordinates as fractions.
            double[] hs_val = hs.next();

            // Convert the fractional coordinates to a real point on the map.
            double[] point = new double[] {
                    hs_val[0] * map.getWidth(),
                    hs_val[1] * map.getHeight()
            };

            if (!map.inObstacle(point)) {
                this.coordinates.add(point);
            }
        }
    }

    /**
     * Evaluates whether an edge between v1, v2 on map would pass through any
     * obstacles.
     * 
     * @param v1  the coordinates of the first vertex
     * @param v2  the coordinates of the second vertex
     * @param map the map the edge will exist in
     * @return whether the edge
     */
    public boolean edgeIsValid(double[] v1, double[] v2, Map map) {
        // Van der Corput sequence will supply values to check between v1, v2.
        VanDerCorput vdc = new VanDerCorput(EDGE_CHECK_BASE);

        double total_distance = distance(v1, v2);
        double next_pos = vdc.next();

        // Continue checking points along the edge until the edge has been
        // split into pieces of at most EDGE_CHECK_PRECISION size.
        while (next_pos * total_distance < total_distance - EDGE_CHECK_PRECISION) {

            double[] checkPoint = new double[] {
                    v1[0] + (v2[0] - v1[0]) * next_pos,
                    v1[1] + (v2[1] - v1[1]) * next_pos
            };

            if (map.inObstacle(checkPoint)) {
                return false;
            }

            next_pos = vdc.next();
        }

        return true;
    }

    /**
     * Update the neighbors list associated with the provided point such that
     * it contains the k closest points within a range of the point that aren't
     * blocked by obstacles.
     * 
     * @param currPoint the index of the point within points to compare others to
     * @param k         the number of close points to return
     * @param r         the maximum distance away a neighbor can be from the point
     * @param map       a map of all obstacles within the space
     */
    private void updateNeighbors(
            int currPoint,
            int k,
            double r,
            Map map) {
        // There's no work to do if this point already has k neighbors.
        if (this.neighbors.get(currPoint).size() == k)
            return;

        // Create a priority queue, sorting on the distance to pointIndex.
        double[] currCoords = this.coordinates.get(currPoint);
        Queue<Integer> sorted = new PriorityQueue<>(
                (Integer p1_idx, Integer p2_idx) -> {
                    double d1 = distance(currCoords, coordinates.get(p1_idx));
                    double d2 = distance(currCoords, coordinates.get(p2_idx));
                    return d1 < d2 ? -1 : 1;
                });

        // Add each of the points to the priority queue.
        for (int i = 0; i < this.numVertices(); i++) {
            if (i == currPoint) { // Don't add the reference point itself.
                continue;
            }
            sorted.add(i);
        }

        // Add the k closest points to this point's neighbors, as long as they
        // are within range and aren't obscured by an obstacle.
        List<Integer> currNeighbors = this.neighbors.get(currPoint);
        while (currNeighbors.size() < k && !sorted.isEmpty()) {
            int otherPoint = sorted.remove();
            double[] otherCoords = this.coordinates.get(otherPoint);

            // The loop should stop as soon as no vertices will be in range.
            if (distance(currCoords, otherCoords) > r)
                break;

            // As long as the edge is valid
            // add the nodes to each others' neighbors lists.
            // TODO: restrict the in-degree of points?
            List<Integer> otherNeighbors = this.neighbors.get(otherPoint);
            if (edgeIsValid(currCoords, otherCoords, map)) {
                currNeighbors.add(otherPoint);
                otherNeighbors.add(currPoint);
            }
        }
    }

    public double distance(double[] p1, double[] p2) {
        return Math.sqrt(
                Math.pow(p1[0] - p2[0], 2) +
                        Math.pow(p1[1] - p2[1], 2));
    }
}
