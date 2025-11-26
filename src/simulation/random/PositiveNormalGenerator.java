package simulation.random;

import eduni.distributions.ContinuousGenerator;
import eduni.distributions.Normal;

/**
 * Wrapper around {@link Normal} that rejects non-positive samples to keep
 * service times valid.
 */
public class PositiveNormalGenerator implements ContinuousGenerator {

    private final Normal normal;

    public PositiveNormalGenerator(double mean, double variance) {
        this.normal = new Normal(mean, variance);
    }

    @Override
    public double sample() {
        double value;
        do {
            value = normal.sample();
        } while (value <= 0);
        return value;
    }

    @Override
    public void setSeed(long seed) {
        normal.setSeed(seed);
    }

    @Override
    public long getSeed() {
        return normal.getSeed();
    }

    @Override
    public void reseed() {
        normal.reseed();
    }
}
