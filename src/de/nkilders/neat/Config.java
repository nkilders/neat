package de.nkilders.neat;

/**
 * @author Noah Kilders
 */
public class Config {
    // Wahrscheinlichkeit für allgemeine Gewichtsmutation
    public static double WEIGHT_MUTATION_CHANCE = 0.80D;
    // Wahrscheinlichkeit für Störung eines Gewichtes
    public static double WEIGHT_PERTURBED_CHANCE = 0.90D;

    // Wahrscheinlichkeit für eine Verbindungsmutation
    public static double ADD_CONNECTION_MUTATION_CHANCE = 0.05D;
    // Wahrscheinlichkeit für eine Neuronmutation
    public static double ADD_NEURON_MUTATION_CHANCE = 0.03D;

    // Warhscheinlichkeit für ein Crossover
    public static double CROSSOVER_CHANCE = 0.75D;

    // Koeffizienten der Compatibility Distance
    public static double C1 = 1.0D;
    public static double C2 = 1.0D;
    public static double C3 = 0.4D;

    // Compatibility Threshold
    public static double COMPATIBILITY_THRESHOLD = 3.0D;
}