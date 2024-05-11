HaltonSequence hs;
VanDerCorput vdc;
Map map;
Graph graph;
PImage background;
public static final int K = 3;
public static final double R = 80;

void setup () {
    size(500, 500);
    background(255);
    hs = new HaltonSequence(2, 3);
    vdc = new VanDerCorput(2);
    map = new Map(width, height);
    graph = new Graph(map, K, R);
    map.render(this.g);
    graph.render(this.g);
    save("background.png");
    // System.out.println(graph.edgeIsValid(new double[]{317, 143}, new double[]{355, 178}, map));
    background = loadImage("background.png");
}

void mouseMoved() {
    System.out.println(mouseX + " " + mouseY);
}

void draw() {
    // double[] point = hs.next();
    // float x = (float)point[0] * width;
    // float y = (float)point[1] * height;
    // strokeWeight(8);
    // stroke(x / width * 100 + 155, y / height * 100 + 155, 255);
    // point(x, y);
    image(background, 0, 0);
    stroke(255, 0, 0);
    strokeWeight(2);
    noFill();
    circle(mouseX, mouseY, 2f * (float) R);
    // System.out.println(graph.distance(new double[]{(double)mouseX, (double)mouseY}, new double[]{0.0, 0.0}));
}