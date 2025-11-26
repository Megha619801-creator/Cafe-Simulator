package simulation.random;

import eduni.distributions.ContinuousGenerator;

/**
 * Simple deterministic generator useful for fixed service times.
 */
public class DeterministicGenerator implements ContinuousGenerator {

    private final double value;
    private long seed = System.nanoTime();

    public DeterministicGenerator(double value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Deterministic generator requires positive value.");
        }
        this.value = value;
    }

    @Override
    public double sample() {
        return value;
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public void reseed() {
        this.seed = System.nanoTime();
    }
}
