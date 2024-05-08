import java.util.*;

public class Graph {
    private static final int NUM_VERTICES = 100;
    private static final int P = 2; // Halton sequence base
    private static final int Q = 3; // Halton sequence base

    private List<double[]> coordinates;
    private List<List<Integer>> neighbors;

    /**
     * 
     * @param map the map to generate a graph over
     * @param k   the maximum number of neighbors to connect each vertex with
     * @param r   the maximum distance between neighbors
     */
    public Graph(Map map, int k, int r) {
        this.coordinates = new ArrayList<>();
        this.neighbors = new ArrayList<>();
        generateMap(map, k, r);
    }

    /**
     * 
     * @param k the maximum number of neighbors to connect each vertex with
     * @param r the maximum distance between neighbors
     */
    private void generateMap(Map map, int k, int r) {
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

        // Generate edges by attempting to connect each vertex with its k
        // nearest neighbors, not exceeding a separation distance of r.
        for (int i = 0; i < coordinates.size(); i++) {
            double[] currPoint = this.coordinates.get(i);
            List<double[]> kClosest = kClosest(i, k, this.coordinates);
            kClosest.removeIf((el) -> distance(currPoint, el) > r);

            // TODO: finish! need to check van der corput points along
            // proposed edges. discard the point if any of these fail.
        }
    }

    private static List<double[]> kClosest(
            int pointIndex,
            int k,
            List<double[]> points) {
        // Add all points except for pointIndex to a priority queue, sorting on
        // the distance to pointIndex.
        double[] from = points.get(pointIndex);
        Queue<double[]> sorted = new PriorityQueue<>(new Comparator<double[]>() {
            public int compare(double[] p1, double[] p2) {
                return distance(from, p1) < distance(from, p2) ? -1 : 1;
            }
        });
        for (double[] p : points) {
            if (p.equals(from)) {
                continue;
            }
            sorted.add(p);
        }

        // Add the k closest points to the result.
        List<double[]> result = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            result.add(sorted.remove());
        }
        return result;
    }

    private static double distance(double[] p1, double[] p2) {
        return Math.sqrt(
            Math.pow(p1[0] - p2[0], 2) + 
            Math.pow(p1[1] - p2[1], 2));
    }
}
