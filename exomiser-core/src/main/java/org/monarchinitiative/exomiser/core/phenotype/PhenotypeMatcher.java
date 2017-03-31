package org.monarchinitiative.exomiser.core.phenotype;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface PhenotypeMatcher {

    PhenodigmMatchRawScore matchPhenotypeIds(List<String> phenotypeIds);

    Organism getOrganism();

    List<PhenotypeTerm> getQueryTerms();

    Map<PhenotypeTerm, Set<PhenotypeMatch>> getTermPhenotypeMatches();

    Set<PhenotypeMatch> getBestPhenotypeMatches();

    QueryPhenotypeMatch getQueryPhenotypeMatch();
}
