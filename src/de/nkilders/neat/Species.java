package neat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Species {
    private Genome representative;
    private List<Genome> genomes;
    private double adjustedFitnessSum;
    private Genome mostFitGenome;

    public Species(Genome representative) {
        this.representative = representative;
        this.genomes = new ArrayList<>();
        this.adjustedFitnessSum = 0.0D;
        this.mostFitGenome = null;

        genomes.add(representative);
    }

    // Berechnet die Adjusted Fitness
    public void calculateAdjustedFitness() {
        adjustedFitnessSum = 0.0D;

        for (Genome genome : genomes) {
            adjustedFitnessSum += genome.getFitness() / (double) genomes.size();
        }
    }

    // Sucht das Genom mit der höchsten Fitness, speichert dieses in mostFitGenome und gibt es zurück
    public Genome findMostFitGenome() {
        genomes.sort(Comparator.comparingDouble(Genome::getFitness));
        return mostFitGenome = genomes.get(genomes.size() - 1);
    }

    // Gibt das "Urgenom" zurück, auf dem die Spezies aufgebaut ist
    public Genome getRepresentative() {
        return representative;
    }

    // Gibt eine Liste aller Genome zurück
    public List<Genome> getGenomes() {
        return genomes;
    }

    // Fügt der Spezies ein Genom hinzu
    public void addGenome(Genome genome) {
        genomes.add(genome);
    }

    // Gibt die Adjusted Fitness zurück
    public double getAdjustedFitnessSum() {
        return adjustedFitnessSum;
    }

    // Gibt das Genom mit der höchsten Fitness zurück
    public Genome getMostFitGenome() {
        return mostFitGenome;
    }

    @Override
    public String toString() {
        return String.format("Species{representative=%s, numGenomes=%s, adjustedFitnessSum=%s, mostFitGenome=%s}", representative, genomes.size(), adjustedFitnessSum, mostFitGenome);
    }
}