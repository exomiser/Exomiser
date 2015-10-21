/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

import java.util.List;
import java.util.Map;

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
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface Model {

    public String getModelId();
    
    public Organism getOrganism();
    
    public int getEntrezGeneId();

    public String getHumanGeneSymbol();

    public List<String> getPhenotypeIds();

    public double getScore();

    public void setScore(double score);

    public void addMatchIfAbsentOrBetterThanCurrent(PhenotypeMatch match);

    public Map<PhenotypeTerm, PhenotypeMatch> getBestPhenotypeMatchForTerms();

}
