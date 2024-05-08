import processing.core.*;
import java.util.ArrayList;
import java.util.List;

public class Map {
    private double width;
    private double height;
    private List<Obstacle> obstacles;

    public Map(double width, double height) {
        this.width = width;
        this.height = height;
        this.obstacles = new ArrayList<>();
        this.obstacles.addAll(
            List.of(
                new Obstacle(0.0, 50.0, 0.0, 50.0),
                new Obstacle(80.0, 90.0, 80.0, 90.0),
                new Obstacle(40.0, 90.0, 100.0, 120.0)
            )
        );
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public void render(PGraphics g) {
        for (Obstacle o : obstacles) {
            o.render(g);
        }
    }

    /**
     * Checks whether a point is in collision with any obstacle in the map.
     * 
     * @param point the point to check
     * @return whether the point is contained within any obstacle
     */
    public boolean isOpenSpace(double[] point) {
        for (Obstacle o : obstacles) {
            if (o.contains(point)) {
                return true;
            }
        }
        return false;
    }

    private static class Obstacle {
        private double x0;
        private double x1;
        private double y0;
        private double y1;

        public Obstacle(double x0, double x1, double y0, double y1) {
            this.x0 = x0;
            this.x1 = x1;
            this.y0 = y0;
            this.y1 = y1;
        }

        public boolean contains(double[] point) {
            return this.x0 < point[0] && point[0] < this.x1
                    && this.y0 < point[1] && point[1] < this.y1;
        }

        public void render(PGraphics g) {
            g.rect((float) this.x0, (float) this.x0, (float) this.x0, (float) this.x0);
        }
    }
}
