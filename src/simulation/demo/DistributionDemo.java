package simulation.demo;

import eduni.distributions.Negexp;
import eduni.distributions.Normal;

/**
 * Small demo program to ensure the eduni.distributions library is wired in.
 */
public class DistributionDemo {
    public static void main(String[] args) {
        Negexp exp = new Negexp(5.0, System.currentTimeMillis());
        Normal normal = new Normal(0.0, 1.0, System.currentTimeMillis());

        System.out.println("Negexp samples:");
        for (int i = 0; i < 3; i++) {
            System.out.printf("  sample %d = %.4f%n", i + 1, exp.sample());
        }

        System.out.println("Normal samples:");
        for (int i = 0; i < 3; i++) {
            System.out.printf("  sample %d = %.4f%n", i + 1, normal.sample());
        }
    }
}
