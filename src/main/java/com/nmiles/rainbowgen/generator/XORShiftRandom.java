package com.nmiles.rainbowgen.generator;

/**
 * A blistering fast random number generator. It takes advantage of the incredibly hard-to-predict
 * nature of XORing a number with a bit-shifted version of itself. This implementation is far from
 * the best in terms of randomness, but it is extremely fast. It is based on the algorithm shown
 * at http://www.javamex.com/tutorials/random_numbers/xorshift.shtml, but is heavily modified for
 * this use. For more on XORShift random number generators, see
 * https://en.wikipedia.org/wiki/Xorshift
 * @author Nathan Miles
 *
 */
public class XORShiftRandom {
    // The last number generated
    private long last;
    
    /**
     * Constructs a new XORShiftRandom seeded with the current system time.
     */
    public XORShiftRandom() {
        this(System.currentTimeMillis());
    }
    
    /**
     * Constructs a new XORShiftRandom seeded with the given value.
     * @param seed The seed to use
     */
    public XORShiftRandom(long seed) {
        this.last = seed;
    }
    
    /**
     * Returns a random integer in the range of 0 (inclusive) and max (exclusive).
     * @param max The exclusive maximum value to return. The returned number will ALWAYS
     *            be less than this value.
     * @return The random number generated
     */
    public int nextInt(int max) {
        last ^= (last << 21);
        last ^= (last >>> 35);
        last ^= (last << 4);
        int out = (int) last % max;     
        return (out < 0) ? -out : out;
    }
}