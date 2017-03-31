package org.monarchinitiative.exomiser.core.phenotype;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@FunctionalInterface
public interface ModelScorer {

    ModelPhenotypeMatch scoreModel(Model model);

}
