package org.monarchinitiative.exomiser.core.prioritisers.model;

import org.monarchinitiative.exomiser.core.phenotype.Model;
import org.monarchinitiative.exomiser.core.phenotype.Organism;

/**
 * Interface for encapsulating data involved in a disease/animal model to human
 * gene association. This interface defines the common features of a model -
 * gene and disease models are somewhat different otherwise.
 *
 * For example the disease Pfeiffer syndrome (OMIM:101600) has a set of defined
 * phenotypes encoded using HPO terms is associated with two causative genes,
 * FGFR1 (Entrez:2260) and FGFR2 (Entrez:2263).
 *
 * There are also mouse models where the mouse homologue of FGFR1 and FGFR2 have
 * been knocked-out and they too have a set of defined phenotypes. However the
 * mouse phenotypes are encoded using the MPO.
 *
 * Due to the phenotypic similarities of the mouse knockout and/or the human
 * disease it is possible to infer a likely causative gene for a given set of
 * input phenotypes.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface GeneModel extends Model {

    Organism getOrganism();

    Integer getEntrezGeneId();

    String getHumanGeneSymbol();

}
