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
                        new Obstacle(0.0, 0.0, width * 0.5, height * 0.1),
                        new Obstacle(width * 0.4, height * 0.3, width * 0.3, height * 0.3),
                        new Obstacle(width * 0.2, height * 0.8, width * 0.6, height * 0.1)));
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
    public boolean inObstacle(double[] point) {
        for (Obstacle o : obstacles) {
            if (o.contains(point)) {
                return true;
            }
        }
        return false;
    }

    private static class Obstacle {
        double x, y, width, height;

        public Obstacle(double x, double y, double width, double height) {
            if (x < 0 || y < 0 || width < 0 || height < 0) {
                throw new IllegalArgumentException("Negative values not allowed");
            }
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public boolean contains(double[] point) {
            return this.x < point[0] && point[0] < this.x + this.width
                    && this.y < point[1] && point[1] < this.y + this.height;
        }

        public void render(PGraphics g) {
            g.fill(0);
            g.noStroke();
            g.rect(
                    (float) this.x,
                    (float) this.y,
                    (float) this.width,
                    (float) this.height);
        }
    }
}
