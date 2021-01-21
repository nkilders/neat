# NEAT
Implementation of the [NEAT-algorithm](https://en.wikipedia.org/wiki/Neuroevolution_of_augmenting_topologies) in Java for my term paper in high school

## Example
```java
// define a start genome from which the evolution will begin
Genome startGenome = new Genome();
// it owns three input-neurons...
startGenome.addNeuron(new Neuron(Neuron.NeuronType.INPUT));
startGenome.addNeuron(new Neuron(Neuron.NeuronType.INPUT));
startGenome.addNeuron(new Neuron(Neuron.NeuronType.INPUT));
// ... and one output-neuron, without any connections between each other
startGenome.addNeuron(new Neuron(Neuron.NeuronType.OUTPUT));

// create a NEAT-instance with the defined startGenome and a population size of 10
NEAT neat = new NEAT(startGenome, 10) {
    final double[] input = new double[]{1, 6, -8};

    @Override
    public double evaluateGenome(Genome genome) {
        // the genomes will be evaluated by the output-value they produce for the inputs [1, 6, -8]
        // genomes with a high output-value will receive a high score
        // thus the average output-value is expected to rise (towards 1) over the course of time
        return genome.query(input)[0];
    }
};
    
// process 100 generations
for (int i = 0; i < 100; i++) {
    // print the most fit genome of every fifth generation
    if (i % 5 == 0) {
        neat.getMostFitGenome().print("path/to/output/file#" + i);
    }
    
    neat.processGeneration();
}
```