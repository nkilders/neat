package de.nkilders.neat;

import java.util.*;

/**
 * @author Noah Kilders
 */
public abstract class NEAT {
    private static final Random RANDOM = new Random();

    private final Genome startGenome;
    private final int populationSize;
    private Genome mostFitGenome;
    private final List<Genome> genomes;
    private final List<Species> species;
    private int generation;

    public NEAT(Genome startGenome, int populationSize) {
        this.startGenome = startGenome;
        this.populationSize = populationSize;
        this.mostFitGenome = startGenome;
        this.genomes = new ArrayList<>();
        this.species = new ArrayList<>();
        this.generation = 1;

        // Füllt die Bevölkerung mit Klonen von startGenome, deren Gewichte verändert wurden, auf
        for (int i = 0; i < populationSize; i++) {
            Genome g = startGenome.copy();
            g.randomizeWeights();
            genomes.add(g);
        }
    }

    // Führt Spezifizierung, Bewertung, Selektion, Reproduktion und Mutation für die aktuelle Generation durch und erzeugt eine neue Generation
    public void processGeneration() {
        // Spezifizierung
        species.clear();
        for (Genome genome : genomes) {
            boolean foundSpecies = false;

            // Passt genome in eine existierende Spezies?
            for (Species species : species) {
                if (genome.isCompatible(species.getRepresentative())) {
                    species.addGenome(genome);
                    foundSpecies = true;
                    break;
                }
            }

            // Erstelle eine neue Spezies
            if (!foundSpecies) {
                Species newSpecies = new Species(genome);
                species.add(newSpecies);
            }
        }

        // Bewerten
        for (Genome genome : genomes) {
            genome.setFitness(evaluateGenome(genome));
        }

        for (Species species : species) {
            species.calculateAdjustedFitness();
        }

        // Selektion
        mostFitGenome = null;

        for (Species species : species) {
            Genome genome = species.findMostFitGenome();

            if (mostFitGenome == null) {
                mostFitGenome = genome;
            } else if (genome.getFitness() > mostFitGenome.getFitness()) {
                mostFitGenome = genome;
            }
        }

        System.out.println(String.format("Generation #%s\t\t Population: %s\t\t NumSpecies: %s\t\t Fitness: %s", generation, populationSize, species.size(), mostFitGenome.getFitness()));

        // Reproduktion
        List<Genome> nextGeneration = new ArrayList<>();
        while (nextGeneration.size() < populationSize) {
            Genome parent1 = getRandomGenome(getRandomSpecies());
            Genome parent2 = getRandomGenome(getRandomSpecies());

            Genome child;

            // Mutation
            if (Math.random() <= Config.CROSSOVER_CHANCE) {
                if (parent1.getFitness() > parent2.getFitness()) {
                    child = Genome.crossover(parent1, parent2);
                } else {
                    child = Genome.crossover(parent2, parent1);
                }
            } else {
                if (RANDOM.nextBoolean()) {
                    child = parent1.copy();
                } else {
                    child = parent2.copy();
                }
            }

            child.mutate();

            nextGeneration.add(child);
        }

        genomes.clear();
        genomes.addAll(nextGeneration);

        generation++;
    }

    // Gibt eine zufällige Spezies zurück, wobei Spezies mit höherer Adjusted Fitness mit größerer Wahrscheinlichkeit zurückgegeben werden
    // Übernommen von https://stackoverflow.com/questions/20327958/random-number-with-probabilities/20329901#20329901 mit eigenen Änderungen
    private Species getRandomSpecies() {
        double totalFitness = 0.0D;

        for (Species species : species) {
            totalFitness += species.getAdjustedFitnessSum();
        }

        double choice = RANDOM.nextDouble() * totalFitness;
        double subTotalFitness = 0.0D;

        for (Species species : species) {
            subTotalFitness += species.getAdjustedFitnessSum();

            if (choice < subTotalFitness)
                return species;
        }

        return species.get(0);
    }

    // Gibt ein zufälliges Genom der übergenen Spezies zurück, wobei Genome mit höherer Fitness mit größerer Wahrscheinlichkeit zurückgegeben werden
    // Übernommen von https://stackoverflow.com/questions/20327958/random-number-with-probabilities/20329901#20329901 mit eigenen Änderungen
    private Genome getRandomGenome(Species species) {
        double totalFitness = 0.0D;

        for (Genome genome : species.getGenomes()) {
            totalFitness += genome.getFitness();
        }

        double choice = RANDOM.nextDouble() * totalFitness;
        double subTotalFitness = 0.0D;

        for (Genome genome : species.getGenomes()) {
            subTotalFitness += genome.getFitness();

            if (choice < subTotalFitness)
                return genome;
        }

        return species.getGenomes().get(0);
    }

    // Bewertet ein Genom
    // Da bei jeder Anwendung andere Parameter wichtig sind, muss diese Methode anwendungsspezifisch programmiert werden
    public abstract double evaluateGenome(Genome genome);

    // Gibt die Größe der Bevölkerung zurück
    public int getPopulationSize() {
        return populationSize;
    }

    // Gibt das Start-Genom zurück, mit dem die Evolution begonnen hat
    public Genome getStartGenome() {
        return startGenome;
    }

    // Gibt das Genom mit der höchsten Fitness zurück
    public Genome getMostFitGenome() {
        return mostFitGenome;
    }

    // Gibt eine Liste aller Genome zurück
    public List<Genome> getGenomes() {
        return genomes;
    }

    // Gibt eine Liste aller Spezies zurück
    public List<Species> getSpecies() {
        return species;
    }

    // Gibt die aktuelle Generation zurück
    public int getGeneration() {
        return generation;
    }
}