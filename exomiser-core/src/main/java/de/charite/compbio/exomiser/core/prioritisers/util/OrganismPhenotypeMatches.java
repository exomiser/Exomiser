package de.charite.compbio.exomiser.core.prioritisers.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.charite.compbio.exomiser.core.model.Organism;
import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OrganismPhenotypeMatches {

    private static Logger logger = LoggerFactory.getLogger(OrganismPhenotypeMatches.class);

    private final Organism organism;
    private final Map<PhenotypeTerm, Set<PhenotypeMatch>> termPhenotypeMatches;

    public OrganismPhenotypeMatches(Organism organism, Map<PhenotypeTerm, Set<PhenotypeMatch>> termPhenotypeMatches) {
        this.organism = organism;
        this.termPhenotypeMatches = ImmutableMap.copyOf(termPhenotypeMatches);
    }

    public Organism getOrganism() {
        return organism;
    }

    public List<PhenotypeTerm> getQueryTerms() {
        return ImmutableList.copyOf(termPhenotypeMatches.keySet());
    }

    public Map<PhenotypeTerm, Set<PhenotypeMatch>> getTermPhenotypeMatches() {
        return termPhenotypeMatches;
    }

    public Set<PhenotypeMatch> getBestPhenotypeMatches() {
        Map<PhenotypeTerm, PhenotypeMatch> bestMatches = new HashMap<>();

        for (Map.Entry<PhenotypeTerm, Set<PhenotypeMatch>> entry : termPhenotypeMatches.entrySet()) {
            PhenotypeTerm queryTerm = entry.getKey();
            for (PhenotypeMatch match : entry.getValue()) {
                ///todo: simple sort on score and pick top value
                double score = match.getScore();
                if (bestMatches.containsKey(queryTerm)) {
                    if (score > bestMatches.get(queryTerm).getScore()) {
                        bestMatches.put(queryTerm, match);
                    }
                } else {
                    bestMatches.put(queryTerm, match);
                }
            }
        }
        logger.info("Best {} phenotype matches:", organism);
        for (PhenotypeMatch bestMatch : bestMatches.values()) {
            logger.info("{}-{}={}", bestMatch.getQueryPhenotypeId(), bestMatch.getMatchPhenotypeId(), bestMatch.getScore());
        }
        return ImmutableSet.copyOf(bestMatches.values());
    }

    public Map<String, PhenotypeMatch> getCompoundKeyIndexedPhenotypeMatches() {
        //'hpId + mpId' : phenotypeMatch
        Map<String, PhenotypeMatch> speciesPhenotypeMatches = new HashMap<>();

        for (Map.Entry<PhenotypeTerm, Set<PhenotypeMatch>> entry : termPhenotypeMatches.entrySet()) {
            PhenotypeTerm queryTerm = entry.getKey();
            String hpId = queryTerm.getId();
            for (PhenotypeMatch match : entry.getValue()) {
                PhenotypeTerm matchTerm = match.getMatchPhenotype();
                String mpId = matchTerm.getId();
                String matchIds = hpId + mpId;
                speciesPhenotypeMatches.put(matchIds, match);
            }
        }
        return ImmutableMap.copyOf(speciesPhenotypeMatches);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrganismPhenotypeMatches)) return false;
        OrganismPhenotypeMatches that = (OrganismPhenotypeMatches) o;
        return organism == that.organism &&
                Objects.equals(termPhenotypeMatches, that.termPhenotypeMatches);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organism, termPhenotypeMatches);
    }

    @Override
    public String toString() {
        return "OrganismPhenotypeMatches{" +
                "organism=" + organism +
                ", termPhenotypeMatches=" + termPhenotypeMatches +
                '}';
    }
}
