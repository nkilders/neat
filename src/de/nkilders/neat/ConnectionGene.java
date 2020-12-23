package de.nkilders.neat;

public class ConnectionGene {
    private int inputNeuron;
    private int outputNeuron;
    private double weight;
    private boolean expressed;
    private int innovationNumber;

    public ConnectionGene(int inputNeuronId, int outputNeuronId, double weight, boolean expressed, int innovationNumber) {
        this.inputNeuron = inputNeuronId;
        this.outputNeuron = outputNeuronId;
        this.weight = weight;
        this.expressed = expressed;
        this.innovationNumber = innovationNumber;
    }

    public ConnectionGene(Neuron inputNeuron, Neuron outputNeuron, double weight, boolean expressed, int innovationNumber) {
        this(inputNeuron.getId(), outputNeuron.getId(), weight, expressed, innovationNumber);
    }

    public ConnectionGene(Neuron inputNeuron, Neuron outputNeuron, double weight, boolean expressed) {
        this(inputNeuron, outputNeuron, weight, expressed, Counters.getNextConnectionInnovation());
    }

    // Kopiert die Verbindung
    public ConnectionGene copy() {
        return new ConnectionGene(inputNeuron, outputNeuron, weight, expressed, innovationNumber);
    }

    public int getInputNeuron() {
        return inputNeuron;
    }

    public int getOutputNeuron() {
        return outputNeuron;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isExpressed() {
        return expressed;
    }

    public void setExpressed(boolean expressed) {
        this.expressed = expressed;
    }

    public int getInnovationNumber() {
        return innovationNumber;
    }
}