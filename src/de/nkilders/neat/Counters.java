package de.nkilders.neat;

public class Counters {
    private static int connectionInnovation = 0;

    // Gibt die nächste Innovation-Number zurück
    public static int getNextConnectionInnovation() {
        return connectionInnovation++;
    }

    private static int neuronId = 0;

    // Gibt die nächste Neuronen-ID zurück
    public static int getNextNeuronId() { return neuronId++; }
}