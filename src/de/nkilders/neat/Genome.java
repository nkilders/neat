package de.nkilders.neat;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Genome {
    private static final Random RANDOM = new Random();

    // <Neuron-ID, Neuron>
    private Map<Integer, Neuron> neurons;
    private List<Neuron> inputNeurons;
    private List<Neuron> outputNeurons;
    // <Connection-Innovation, ConnectionGene>
    private Map<Integer, ConnectionGene> connectionGenes;

    private double fitness;

    public Genome() {
        neurons = new HashMap<>();
        inputNeurons = new ArrayList<>();
        outputNeurons = new ArrayList<>();
        connectionGenes = new HashMap<>();
        fitness = 0.0D;
    }

    // Kopiert das Genom
    public Genome copy() {
        Genome newGenome = new Genome();

        for (Neuron neuron : neurons.values()) {
            newGenome.addNeuron(neuron.copy());
        }

        for (ConnectionGene connection : connectionGenes.values()) {
            newGenome.addConnectionGene(connection.copy());
        }

        return newGenome;
    }

    // Berechnet die Netzausgabe zu den übergebenen Eingabewerten
    public double[] query(double[] inputValues) {
        if (inputValues.length != inputNeurons.size()) {
            throw new IllegalArgumentException("Brauche " + inputNeurons.size() + " Eingabewert(e)!");
        }

        for (int i = 0; i < inputNeurons.size(); i++) {
            Neuron inputNeuron = inputNeurons.get(i);

            inputNeuron.addInput(inputValues[i]);
            fireNeuron(inputNeuron);
        }

        boolean done = false;
        while (!done) {
            done = true;

            for (Neuron neuron : neurons.values()) {
                if (neuron.getType() == Neuron.NeuronType.HIDDEN) {
                    if (neuron.isReadyToFire() && !neuron.hasFired()) {
                        fireNeuron(neuron);
                    }

                    if (!neuron.hasFired()) {
                        done = false;
                    }
                }
            }
        }

        for (Neuron neuron : neurons.values()) {
            neuron.setHasFired(false);
        }

        double[] output = new double[outputNeurons.size()];

        for (int o = 0; o < outputNeurons.size(); o++) {
            output[o] = sigmoid(outputNeurons.get(o).getInput());
        }

        return output;
    }

    // Aktiviert ein Neuron, sofern dieses alle Eingaben erhalten hat
    private void fireNeuron(Neuron neuron) {
        if (neuron.isReadyToFire()) {
            double output = neuron.getType() == Neuron.NeuronType.INPUT ? neuron.getInput() : sigmoid(neuron.getInput());

            for (ConnectionGene connection : neuron.getOutputConnections()) {
                if (connection.isExpressed()) {
                    neurons.get(connection.getOutputNeuron()).addInput(output * connection.getWeight());
                }
            }

            neuron.resetInput();
            neuron.setHasFired(true);
        }
    }

    // Aktivierungsfunktion
    private double sigmoid(double x) {
        return x / (1D + Math.abs(x));
    }

    // Mutiert das Genom
    public void mutate() {
        // Gewichte mutieren
        if (Math.random() <= Config.WEIGHT_MUTATION_CHANCE) {
            for (ConnectionGene c : connectionGenes.values()) {
                if (Math.random() <= Config.WEIGHT_PERTURBED_CHANCE) {
                    // Gewicht stören
                    c.setWeight(c.getWeight() * ((Math.random() * 4.0D) - 2.0D));
                } else {
                    // neues Gewicht
                    c.setWeight((Math.random() * 4.0D) - 2.0D);
                }
            }
        }

        // addConnectionMutation()
        if (Math.random() <= Config.ADD_CONNECTION_MUTATION_CHANCE) {
            addConnectionMutation();
        }

        // addNeuronMutation()
        if (Math.random() <= Config.ADD_NEURON_MUTATION_CHANCE) {
            addNeuronMutation();
        }
    }

    // Führt eine Verbindungsmutation durch
    public void addConnectionMutation() {
        if (neurons.size() >= 2) {
            for (int i = 0; i < 10; i++) {
                List<Neuron> list = new ArrayList<>(neurons.values());
                Neuron neuron1 = list.get(RANDOM.nextInt(neurons.size()));
                Neuron neuron2 = list.get(RANDOM.nextInt(neurons.size()));

                // Gibt's die Verbindung schon?
                boolean connectionExists = false;

                for (ConnectionGene c : connectionGenes.values()) {
                    if (c.getInputNeuron() == neuron1.getId() && c.getOutputNeuron() == neuron2.getId()
                            || c.getInputNeuron() == neuron2.getId() && c.getOutputNeuron() == neuron1.getId()) {
                        connectionExists = true;
                    }
                }

                if (connectionExists)
                    continue;

                // Verbindung möglich?
                if (neuron1.getType() != Neuron.NeuronType.HIDDEN && neuron1.getType() == neuron2.getType()) {
                    continue;
                }

                double weight = (Math.random() * 4.0D) - 2.0D;

                if (neuron2.getType() == Neuron.NeuronType.INPUT && neuron1.getType() == Neuron.NeuronType.HIDDEN
                        || neuron2.getType() == Neuron.NeuronType.HIDDEN && neuron1.getType() == Neuron.NeuronType.OUTPUT
                        || neuron2.getType() == Neuron.NeuronType.INPUT && neuron1.getType() == Neuron.NeuronType.OUTPUT) {
                    addConnectionGene(new ConnectionGene(neuron2, neuron1, weight, true, Counters.getNextConnectionInnovation()));
                } else {
                    addConnectionGene(new ConnectionGene(neuron1, neuron2, weight, true, Counters.getNextConnectionInnovation()));
                }

                return;
            }
        }
    }

    // Führt eine Neuronmutation durch
    public void addNeuronMutation() {
        if (connectionGenes.isEmpty()) {
            return;
        }

        List<ConnectionGene> list = new ArrayList<>(connectionGenes.values());
        ConnectionGene connection = list.get(RANDOM.nextInt(connectionGenes.size()));
        Neuron inputNeuron = neurons.get(connection.getInputNeuron());
        Neuron outputNeuron = neurons.get(connection.getOutputNeuron());

        // Neues Neuron erstellen
        Neuron newNeuron = new Neuron(Neuron.NeuronType.HIDDEN);

        // Alte Verbindung deaktivieren
        connection.setExpressed(false);

        // Neue Verbindungen und Neuron hinzufügen
        addNeuron(newNeuron);
        addConnectionGene(new ConnectionGene(inputNeuron, newNeuron, 1, true, Counters.getNextConnectionInnovation()));
        addConnectionGene(new ConnectionGene(newNeuron, outputNeuron, connection.getWeight(), true, Counters.getNextConnectionInnovation()));
    }

    // Kreuzt zwei Genome miteinander
    public static Genome crossover(Genome moreFitParent, Genome lessFitParent) {
        Genome child = new Genome();

        // Neuronen
        for (Neuron neuron : moreFitParent.getNeurons().values()) {
            child.addNeuron(neuron.copy());
        }

        // Connections
        for (ConnectionGene connection : moreFitParent.getConnectionGenes().values()) {
            if (lessFitParent.getConnectionGenes().containsKey(connection.getInnovationNumber())) {
                // Matching Genes
                if (RANDOM.nextBoolean()) {
                    child.addConnectionGene(connection.copy());
                } else {
                    child.addConnectionGene(lessFitParent.getConnectionGenes().get(connection.getInnovationNumber()).copy());
                }
            } else {
                // Disjoint/Excess Genes
                child.addConnectionGene(connection.copy());
            }
        }

        return child;
    }

    // Gibt zurück, ob das Genom mit genome kompatibel ist
    public boolean isCompatible(Genome genome) {
        return compatibilityDistance(genome) <= Config.COMPATIBILITY_THRESHOLD;
    }

    // Berechnet die Compatibility Distance mit einem anderen Genom und gibt diese zurück
    public double compatibilityDistance(Genome genome) {
        // Anzahl an Excess-Genes
        double E = countExcessGenes(genome);

        // Anzahl an Disjoint-Genes
        double D = countDisjointGenes(genome);

        // Durchschnittlicher Gewichtsunterschied der Matching-Genes
        double W = getAverageWeightDifference(genome);

        // Anzahl der Connection-Gene im größeren Genome
        int N = 1;
        if (genome.getConnectionGenes().size() < connectionGenes.size()) {
            N = connectionGenes.size();
        } else {
            N = genome.getConnectionGenes().size();
        }

        if(N == 0)
            N = 1;

        return ((Config.C1 * E) / N) + ((Config.C2 * D) / N) + (Config.C3 * W);
    }

    // Zählt die Excess-Gene mit einem anderen Genom und gibt diese zurück
    public int countExcessGenes(Genome genome) {
        List<ConnectionGene> c1 = new ArrayList<>(this.getConnectionGenes().values());
        List<ConnectionGene> c2 = new ArrayList<>(genome.getConnectionGenes().values());

        int in1 = c1.size() == 0 ? -1 : c1.get(c1.size() - 1).getInnovationNumber();
        int in2 = c2.size() == 0 ? -1 : c2.get(c2.size() - 1).getInnovationNumber();

        int excessGenes = 0;

        if (in1 < in2) {
            for (ConnectionGene c : c2) {
                if (c.getInnovationNumber() > in1) {
                    excessGenes++;
                }
            }
        } else if (in2 < in1) {
            for (ConnectionGene c : c1) {
                if (c.getInnovationNumber() > in2) {
                    excessGenes++;
                }
            }
        }

        return excessGenes;
    }

    // Zählt die Disjoint-Gene mit einem anderen Genom und gibt diese zurück
    public int countDisjointGenes(Genome genome) {
        List<ConnectionGene> c1 = new ArrayList<>(this.getConnectionGenes().values());
        List<ConnectionGene> c2 = new ArrayList<>(genome.getConnectionGenes().values());

        int in1 = c1.size() == 0 ? -1 : c1.get(c1.size() - 1).getInnovationNumber();
        int in2 = c2.size() == 0 ? -1 : c2.get(c2.size() - 1).getInnovationNumber();

        int disjointGenes = 0;

        if (in1 < in2) {
            for (ConnectionGene c : c1) {
                if (c.getInnovationNumber() < in2) {
                    disjointGenes++;
                }
            }
        } else if (in2 < in1) {
            for (ConnectionGene c : c2) {
                if (c.getInnovationNumber() < in1) {
                    disjointGenes++;
                }
            }
        }

        return disjointGenes;
    }

    // Berechnet den durchschnittlichen Gewichtsunterschied der Matching-Gene mit einem anderen Genom und gibt diesen zurück
    public double getAverageWeightDifference(Genome genome) {
        double weightDifference = 0.0D;
        int matchingGenes = 0;

        for (ConnectionGene connectionGene : this.getConnectionGenes().values()) {
            if (genome.getConnectionGenes().containsKey(connectionGene.getInnovationNumber())) {
                weightDifference += Math.abs(connectionGene.getWeight() - genome.getConnectionGenes().get(connectionGene.getInnovationNumber()).getWeight());
                matchingGenes++;
            }
        }

        if (matchingGenes == 0) {
            return 0;
        } else {
            return weightDifference / (double) matchingGenes;
        }
    }

    // Verbindet alle Eingabeneuronen mit allen Ausgabeneuronen
    public void connectInOut() {
        for (Neuron inputNeuron : inputNeurons) {
            for (Neuron outputNeuron : outputNeurons) {
                addConnectionGene(new ConnectionGene(inputNeuron, outputNeuron, 1, true, Counters.getNextConnectionInnovation()));
            }
        }

        randomizeWeights();
    }

    // Gibt jeder Verbindung des Genoms ein zufälliges Gewicht
    public void randomizeWeights() {
        for (ConnectionGene connection : connectionGenes.values()) {
            connection.setWeight((Math.random() * 4.0D) - 2.0D);
        }
    }

    // Erzeugt eine grafische Ausgabe des Genoms
    public void print(String path) {
        try {
            BufferedImage image = new BufferedImage(1200, 800, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g.setColor(Color.WHITE);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());

            List<Neuron> inputNeurons = new ArrayList<>();
            List<Neuron> hiddenNeurons = new ArrayList<>();
            List<Neuron> outputNeurons = new ArrayList<>();

            for (Neuron ng : neurons.values()) {
                if (ng.getType() == Neuron.NeuronType.INPUT)
                    inputNeurons.add(ng);
                else if (ng.getType() == Neuron.NeuronType.HIDDEN)
                    hiddenNeurons.add(ng);
                else if (ng.getType() == Neuron.NeuronType.OUTPUT)
                    outputNeurons.add(ng);
            }

            { // Input-Positionen bestimmen
                int i = image.getHeight() / (inputNeurons.size() + 1);
                for (int j = 0; j < inputNeurons.size(); j++) {
                    inputNeurons.get(j).setRenderPos(20, i * (j + 1));
                }
            }

            { // Hidden-Positionen bestimmen
                for (Neuron hiddenNeuron : hiddenNeurons) {
                    hiddenNeuron.setRenderPos(RANDOM.nextInt(image.getWidth() - 100) + 50,
                            RANDOM.nextInt(image.getHeight() - 20) + 10);
                }
            }

            { // Output-Positionen bestimmen
                int i = image.getHeight() / (outputNeurons.size() + 1);
                for (int j = 0; j < outputNeurons.size(); j++) {
                    outputNeurons.get(j).setRenderPos(1180, i * (j + 1));
                }
            }

            // Connections zeichnen
            AffineTransform tx;

            Polygon polygon = new Polygon();
            polygon.addPoint(0, 7);
            polygon.addPoint(-5, -7);
            polygon.addPoint(5, -7);

            for (ConnectionGene c : connectionGenes.values()) {
                g.setColor(c.isExpressed() ? (c.getWeight() >= 0.0D ? Color.GREEN : Color.RED) : Color.GRAY);

                float x1 = neurons.get(c.getInputNeuron()).getRenderX();
                float y1 = neurons.get(c.getInputNeuron()).getRenderY();

                float x2 = neurons.get(c.getOutputNeuron()).getRenderX();
                float y2 = neurons.get(c.getOutputNeuron()).getRenderY();

                g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);

                tx = new AffineTransform();
                tx.setToIdentity();

                double angle = Math.atan2(y2 - y1, x2 - x1);
                tx.translate(x2 - (int) (Math.cos(angle) * 14), y2 + (int) (-Math.sin(angle) * 14));
                tx.rotate((angle - Math.PI / 2D));
                g.setTransform(tx);

                g.fill(polygon);

                tx = new AffineTransform();
                tx.setToIdentity();

                tx.translate(x2 - (int) (Math.cos(angle) * 100), y2 + (int) (-Math.sin(angle) * 100));
                g.setTransform(tx);

                String weightString = "" + (Math.round(c.getWeight() * 1000.0D) / 1000.0D);

                g.setColor(Color.BLACK);
                g.fillRect(-1, -11, g.getFontMetrics().stringWidth(weightString) + 2, 13);
                g.setColor(Color.WHITE);
                g.drawString(weightString, 0, 0);

                g.setTransform(new AffineTransform());
            }

            // Neuronen zeichnen
            Color colIn = new Color(3, 201, 168);
            Color colHid = new Color(250, 147, 8);
            Color colOut = new Color(22, 183, 251);
            for (Neuron n : neurons.values()) {
                if (n.getType() == Neuron.NeuronType.INPUT)
                    g.setColor(colIn);
                else if (n.getType() == Neuron.NeuronType.HIDDEN)
                    g.setColor(colHid);
                else
                    g.setColor(colOut);

                g.fillOval(n.getRenderX() - 7, n.getRenderY() - 7, 14, 14);

                g.setColor(Color.BLACK);
                g.drawString("" + n.getId(), n.getRenderX() - (g.getFontMetrics().stringWidth("" + n.getId()) / 2), n.getRenderY() + 4);
            }

            g.drawString("Fitness: " + fitness, 2, 16);

            // In Datei speichern
            ImageIO.write(image, "PNG", new File(path + ".png"));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public Map<Integer, Neuron> getNeurons() {
        return neurons;
    }

    public void addNeuron(Neuron neuron) {
        neurons.put(neuron.getId(), neuron);

        if (neuron.getType() == Neuron.NeuronType.INPUT) {
            inputNeurons.add(neuron);
        } else if (neuron.getType() == Neuron.NeuronType.OUTPUT) {
            outputNeurons.add(neuron);
        }
    }

    public Map<Integer, ConnectionGene> getConnectionGenes() {
        return connectionGenes;
    }

    public void addConnectionGene(ConnectionGene connection) {
        boolean inOut = connection.getInputNeuron() == connection.getOutputNeuron();

        Neuron inputNeuron = neurons.get(connection.getInputNeuron());
        Neuron outputNeuron = neurons.get(connection.getOutputNeuron());
        boolean loop = checkLoop(outputNeuron, inputNeuron);

        if (!inOut && !loop) {
            connectionGenes.put(connection.getInnovationNumber(), connection);
            neurons.get(connection.getInputNeuron()).addOutputConnection(connection);
            neurons.get(connection.getOutputNeuron()).addInputConnection(connection);
        }
    }

    // Überprüft, ob ein Neuron Eingaben erwartet, die von seinen eigenen Ausgaben abhängig sind, da da die Netzwerkausgabe so nicht berechnet werden kann
    private boolean checkLoop(Neuron startNeuron, Neuron currentNeuron) {
        for (ConnectionGene connection : currentNeuron.getInputConnections()) {
            if (connection.getInputNeuron() == startNeuron.getId()) {
                return true;
            }

            Neuron inputNeuron = neurons.get(connection.getInputNeuron());

            if (inputNeuron.getType() == Neuron.NeuronType.INPUT)
                continue;

            if (checkLoop(startNeuron, inputNeuron)) {
                return true;
            }
        }

        return false;
    }

    // Gibt die Fitness des Genoms zurück
    public double getFitness() {
        return fitness;
    }

    // Setzt die Fitness des Genoms
    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    @Override
    public String toString() {
        return String.format("Genome{numNeurons=%s, numConnections=%s, fitness=%s}", neurons.size(), connectionGenes.size(), fitness);
    }
}