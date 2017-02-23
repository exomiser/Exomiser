package org.monarchinitiative.exomiser.core.model;

/**
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface GeneModel extends Model {

    Organism getOrganism();

    Integer getEntrezGeneId();

    String getHumanGeneSymbol();

}
