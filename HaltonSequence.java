public class HaltonSequence {
    private VanDerCorput x_gen;
    private VanDerCorput y_gen;

    public HaltonSequence(int p, int q) {
        this.x_gen = new VanDerCorput(p);
        this.y_gen = new VanDerCorput(q);
    }

    public double[] next() {
        return new double[]{
            this.x_gen.next(), 
            this.y_gen.next()
        };
    }

    public void reset() {
        this.x_gen.reset();
        this.y_gen.reset();
    }
}
