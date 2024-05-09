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
        System.out.println(this.neighbors);
        for (int i = 0; i < this.neighbors.size(); i++) {
            if (this.neighbors.get(i).size() < 3) {
                for (double d : this.coordinates.get(i)) System.out.print(d +", ");

                System.out.println();
            }
        }
    }

    public int numVertices() {
        return this.coordinates.size();
    }

    public void render(PGraphics g) {
        // Track which vertices have been visited.
        Set<Integer> visited = new HashSet<>();

        // Iterate through all the vertices.
        for (int i = 0; i < this.numVertices(); i++) {
            if (visited.contains(i))
                continue;

            // Do a BFS from this vertex if it hasn't been visited
            Queue<Integer> toVisit = new LinkedList<>();
            toVisit.add(i);
            while (!toVisit.isEmpty()) {
                // Dequeue a vertex and consider it visited.
                int currVertex = toVisit.remove();
                visited.add(currVertex);

                // Render the current vertex
                double[] currCoords = this.coordinates.get(currVertex);
                g.stroke(0);
                g.strokeWeight(5);
                g.point((float) currCoords[0], (float) currCoords[1]);

                // Iterate through all neighbors of this vertex.
                for (Integer neighbor : this.neighbors.get(currVertex)) {
                    // If the neighbor has not been visited, add it to the queue
                    // and draw the edge between it and this vertex.
                    if (!visited.contains(neighbor)) {
                        toVisit.add(neighbor);
                        // double[] neighborCoords = this.coordinates.get(neighbor);

                        // g.strokeWeight(1);
                        // g.stroke(90, 200, 255);
                        // g.line((float) currCoords[0],
                        //        (float) currCoords[1],
                        //        (float) neighborCoords[0],
                        //        (float) neighborCoords[1]);
                    }
                    double[] neighborCoords = this.coordinates.get(neighbor);

                    g.strokeWeight(1);
                    g.stroke(90, 200, 255);
                    g.line((float) currCoords[0],
                           (float) currCoords[1],
                           (float) neighborCoords[0],
                           (float) neighborCoords[1]);
                }
            }
        }
    }

    /**
     * 
     * @param k the maximum number of neighbors to connect each vertex with
     * @param r the maximum distance between neighbors in pixels
     */
    private void buildGraph(Map map, int k, double r) {
        // Generate vertices over the map, discarding those that intersect with
        // obstacles.
        HaltonSequence hs = new HaltonSequence(P, Q);
        for (int i = 0; i < NUM_VERTICES; i++) {
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

        // The number of vertices is now known. Finish initializing the
        // neighbors list.
        for (int i = 0; i < this.coordinates.size(); i++) {
            this.neighbors.add(new ArrayList<>());
        }

        // Generate edges by connecting each vertex with its neighbors.
        for (int i = 0; i < coordinates.size(); i++) {
            double[] currPoint = this.coordinates.get(i); // Current vertex

            // Generate the list of up to k closest neighbors. Remove from
            // these any neighbors that exceed a distance r from the current
            // vertex. Additionally, remove any which would form an invalid
            // edge that runs through obstacles.
            List<Integer> kClosest = kClosest(i, k, this.coordinates);
            kClosest.removeIf((el) -> {
                double[] otherPoint = this.coordinates.get(el);
                return distance(currPoint, otherPoint) > r
                        || !edgeIsValid(otherPoint, currPoint, map);
            });

            // Add all of these edges to this node's neighbors.
            this.neighbors.get(i).addAll(kClosest);
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
    private boolean edgeIsValid(double[] v1, double[] v2, Map map) {
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
     * Returns a list of point indices that are the k closest to a given point.
     * 
     * @param pointIndex the index of the point within points to compare others to
     * @param k          the number of close points to return
     * @param points     the coordinates of each point, where points.get(i) are the
     *                   coordinates of point i
     * @return a list of the indices of the k closest points to the point at
     *         pointIndex in points
     * @throws IllegalArgumentException if k is greater than points.size() - 1,
     *                                  or points is empty.
     */
    private List<Integer> kClosest(
            int pointIndex,
            int k,
            List<double[]> points) {
        if (points.size() == 0 || k > points.size() - 1) {
            throw new IllegalArgumentException();
        }

        // Add all points except for pointIndex to a priority queue, sorting on
        // the distance to pointIndex.
        double[] from = points.get(pointIndex);
        Queue<Integer> sorted = new PriorityQueue<>(new Comparator<Integer>() {
            public int compare(Integer p1_idx, Integer p2_idx) {
                return distance(from, points.get(p1_idx)) < distance(from, points.get(p2_idx)) ? -1 : 1;
            }
        });

        // Add each of the points to the priority queue.
        for (int i = 0; i < points.size(); i++) {
            if (i == pointIndex) { // Don't add the reference point itself.
                continue;
            }
            sorted.add(i);
        }

        // Add the k closest points to the result.
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            result.add(sorted.remove());
        }
        return result;
    }

    public double distance(double[] p1, double[] p2) {
        return Math.sqrt(
                Math.pow(p1[0] - p2[0], 2) +
                        Math.pow(p1[1] - p2[1], 2));
    }
}
