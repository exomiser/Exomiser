package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.model.Gene;

import java.util.List;
import java.util.Map;

/**
 * Class for mocking a Prioritiser of the given PriorityType. Will score genes with scores specified for the geneIds
 * supplied in the constructor with the type.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class MockPrioritiser implements Prioritiser {

    private final PriorityType priorityType;
    private final Map<Integer, Float> expectedScores;

    public MockPrioritiser(PriorityType priorityType, Map<Integer, Float> geneIdPrioritiserScores) {
        this.priorityType = priorityType;
        expectedScores = geneIdPrioritiserScores;
    }

    @Override
    public void prioritizeGenes(List<Gene> genes) {
        for (Gene gene : genes) {
            Float score = expectedScores.getOrDefault(gene.getEntrezGeneID(), 0f);
            gene.addPriorityResult(new BasePriorityResult(priorityType, score));
        }
    }

    @Override
    public PriorityType getPriorityType() {
        return priorityType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MockPrioritiser that = (MockPrioritiser) o;

        if (priorityType != that.priorityType) return false;
        return expectedScores.equals(that.expectedScores);

    }

    @Override
    public int hashCode() {
        int result = priorityType.hashCode();
        result = 31 * result + expectedScores.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "MockPrioritiser{" +
                "priorityType=" + priorityType +
                ", expectedScores=" + expectedScores +
                '}';
    }
}
