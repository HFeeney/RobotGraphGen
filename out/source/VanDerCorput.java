/**
 * Credits to: https://en.wikipedia.org/wiki/Van_der_Corput_sequence
 */

public class VanDerCorput {
    private int index;
    private int base;

    public VanDerCorput(int base) {
        this.index = 0;
        this.base = base;
    }

    public double next() {
        int n = this.index;
        double ret = 0.0;
        double multiplier = 1.0 / (double) base;

        while (n > 0) {
            ret += (n % base) * multiplier; // Get the 1s digit and apply multiplier
            multiplier /= (double) base; // Decrease multiplier
            n /= base; // Advance to the next digit
        }

        this.index++;
        return ret;
    }

    public void reset() {
        this.index = 0;
    }
}
