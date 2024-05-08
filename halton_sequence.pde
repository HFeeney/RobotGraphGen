HaltonSequence hs;
VanDerCorput vdc;

void setup () {
    size(500, 500);
    background(255, 255, 255);
    hs = new HaltonSequence(2, 3);
    vdc = new VanDerCorput(2);
    frameRate(2);
}

void draw() {
    double[] point = hs.next();
    float x = (float)point[0] * width;
    float y = (float)point[1] * height;

    strokeWeight(8);
    stroke(x / width * 100 + 155, y / height * 100 + 155, 255);
    point(x, y);

    float x2 = (float) vdc.next();
    stroke(255, 55 + 200 * x2, 255 * x2);
    point(x2 * width, height / 2);
}