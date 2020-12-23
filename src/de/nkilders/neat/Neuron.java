package neat;

import java.util.ArrayList;
import java.util.List;

public class Neuron {
    private int id;
    private NeuronType type;

    private List<ConnectionGene> inputConnections;
    private List<ConnectionGene> outputConnections;
    private double input;
    private int numReceivedInputs;
    private boolean hasFired;

    // für die grafische Ausgabe
    private int renderX;
    private int renderY;

    public Neuron(NeuronType type) {
        this.type = type;
        this.id = Counters.getNextNeuronId();
        this.renderX = 0;
        this.renderY = 0;
        this.inputConnections = new ArrayList<>();
        this.outputConnections = new ArrayList<>();
        this.input = 0.0D;
        this.numReceivedInputs = 0;
        this.hasFired = false;
    }

    public Neuron(Neuron neuron) {
        this.id = neuron.id;
        this.type = neuron.type;
        this.renderX = 0;
        this.renderY = 0;
        this.inputConnections = new ArrayList<>();
        this.outputConnections = new ArrayList<>();
        this.input = 0.0D;
        this.numReceivedInputs = 0;
        this.hasFired = false;
    }

    // Gibt eine Kopie des Gens zurück
    public Neuron copy() {
        return new Neuron(this);
    }

    // Gibt die ID des Gens zurück
    public int getId() {
        return id;
    }

    // Gibt den Typ des Gens zurück
    public NeuronType getType() {
        return type;
    }

    public int getRenderX() {
        return renderX;
    }

    public int getRenderY() {
        return renderY;
    }

    public void setRenderPos(int renderX, int renderY) {
        this.renderX = renderX;
        this.renderY = renderY;
    }

    public List<ConnectionGene> getInputConnections() {
        return inputConnections;
    }

    public void addInputConnection(ConnectionGene connection) {
        inputConnections.add(connection);
    }

    public List<ConnectionGene> getOutputConnections() {
        return outputConnections;
    }

    public void addOutputConnection(ConnectionGene connection) {
        outputConnections.add(connection);
    }

    // Gibt die Summe aller erhaltenen Eingaben zurück
    public double getInput() {
        return input;
    }

    // Fügt die erhaltene Eingabe zur Summe aller Eingaben hinzu
    public void addInput(double input) {
        this.input += input;
        this.numReceivedInputs++;
    }

    // Gibt zurück, ob das Neuron alle Eingaben erhalten hat und bereit zur Aktivierung ist
    public boolean isReadyToFire() {
        if (type == NeuronType.INPUT) {
            return numReceivedInputs == 1;
        } else {
            int cons = 0;

            for (ConnectionGene connection : inputConnections) {
                if (connection.isExpressed()) {
                    cons++;
                }
            }

            return numReceivedInputs == cons;
        }
    }

    // Setzt die erhaltene Eingabe zurück
    public void resetInput() {
        numReceivedInputs = 0;
        input = 0.0D;
    }

    // Gibt zurück, ob das Neuron bereits einen Reiz erzeugt hat
    public boolean hasFired() {
        return hasFired;
    }

    public void setHasFired(boolean hasFired) {
        this.hasFired = hasFired;
    }

    @Override
    public String toString() {
        return String.format("Neuron{id=%s, type=%s}", id, type.name());
    }

    // Neuronen-Typen
    public enum NeuronType {
        INPUT, HIDDEN, OUTPUT
    }
}