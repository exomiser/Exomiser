/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers.phenogrid;

import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A cache for providing pre-built objects for use in the PhenoGrid tests.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TestPhenoGridObjectCache {

    private static TestPhenoGridObjectCache instance;

    private final List<String> queryPhenotypeTermIds;
    private final List<PhenotypeMatch> phenotypeMatches;
    private final List<PhenoGridMatch> phenoGridMatches;
    private final List<PhenoGridMatchGroup> phenoGridMatchGroups;

    public static TestPhenoGridObjectCache getInstance() {
        if (instance == null) {
            instance = new TestPhenoGridObjectCache();
        }
        return instance;
    }

    private TestPhenoGridObjectCache() {
        queryPhenotypeTermIds = Arrays.asList("GRUF:111", "GRUF:222", "GRUF:333", "GRUF:444");
        phenotypeMatches = new ArrayList<>();
        phenoGridMatches = new ArrayList<>();
        phenoGridMatchGroups = new ArrayList<>();

        
        PhenotypeMatch kneeMatch = makeKnobblyKneePhenotypeMatch();
        phenotypeMatches.add(kneeMatch);

        PhenotypeMatch prickleMatch = makePurplePricklesPhenotypeMatch();
        phenotypeMatches.add(prickleMatch);
        
        PhenotypeMatch tongueMatch = makeMonsterousTonguePhenotypeMatch();
        phenotypeMatches.add(tongueMatch);

        PhenotypeMatch noseMatch = makeNosePhenotypeMatch();
        phenotypeMatches.add(noseMatch);

        PhenoGridMatchTaxon gruffaloTaxon = new PhenoGridMatchTaxon("NCBITaxon:10090", "Gruff gruffulus");
        
        PhenoGridMatchScore bestScore = new PhenoGridMatchScore("hiPhive", 99, 0);
        PhenoGridMatch bestMatch = new PhenoGridMatch("OMIM:100000", "Gruffalo Syndrome", "disease", Arrays.asList(kneeMatch, prickleMatch, tongueMatch, noseMatch), bestScore, gruffaloTaxon);
        phenoGridMatches.add(bestMatch);
        
        PhenoGridMatchScore goodScore = new PhenoGridMatchScore("hiPhive", 90, 1);
        PhenoGridMatch goodMatch = new PhenoGridMatch("OMIM:999999", "Knobbly knees and prickly back with tongue abnormality", "disease", Arrays.asList(kneeMatch, prickleMatch, tongueMatch), goodScore, gruffaloTaxon);
        phenoGridMatches.add(goodMatch);

        PhenoGridMatchTaxon dragonTaxon = new PhenoGridMatchTaxon("NCBITaxon:35670", "Draco draco");
        PhenoGridMatchScore badScore = new PhenoGridMatchScore("hiPhive", 50, 3);
        PhenoGridMatch badMatch = new PhenoGridMatch("WYRMBASE:999999", "Smaug1", "gene", Arrays.asList(tongueMatch), badScore, dragonTaxon);
        
        
        PhenoGridMatchGroup diseaseMatchGroup = new PhenoGridMatchGroup(phenoGridMatches, queryPhenotypeTermIds);
        phenoGridMatchGroups.add(diseaseMatchGroup);
        PhenoGridMatchGroup geneMatchGroup = new PhenoGridMatchGroup(Arrays.asList(badMatch), queryPhenotypeTermIds);
        phenoGridMatchGroups.add(geneMatchGroup);

    }

    private PhenotypeMatch makeNosePhenotypeMatch() {
        PhenotypeTerm poisonouslyWartyNose = new PhenotypeTerm("GRUF:444", "Poisonously warty nose", 4.0);
        PhenotypeTerm hideouslyWartyNose = new PhenotypeTerm("GRUF:443", "Hideously warty nose", 4.0);
        PhenotypeTerm wartyNose = new PhenotypeTerm("GRUF:440", "Warty nose", 2.0);
        PhenotypeMatch noseMatch = new PhenotypeMatch(poisonouslyWartyNose, hideouslyWartyNose, 0.85, wartyNose);
        return noseMatch;
    }

    private PhenotypeMatch makeMonsterousTonguePhenotypeMatch() {
        PhenotypeTerm blackTongue = new PhenotypeTerm("GRUF:333", "Black tongue", 5.0);
        PhenotypeTerm forkedTongue = new PhenotypeTerm("GRUF:124", "Forked tongue", 5.0);
        PhenotypeTerm abnormalTongue = new PhenotypeTerm("GRUF:120", "Abnormal tongue", 3.0);
        PhenotypeMatch tongueMatch = new PhenotypeMatch(blackTongue, forkedTongue, 0.9, abnormalTongue);
        return tongueMatch;
    }

    private PhenotypeMatch makePurplePricklesPhenotypeMatch() {
        PhenotypeTerm purplePrickles = new PhenotypeTerm("GRUF:111", "Purple prickles", 4.0);
        PhenotypeTerm redPrickles = new PhenotypeTerm("GRUF:112", "Red prickles", 4.0);
        PhenotypeTerm colouredPrickles = new PhenotypeTerm("GRUF:110", "Coloured prickles", 2.0);
        PhenotypeMatch prickleMatch = new PhenotypeMatch(purplePrickles, redPrickles, 0.7, colouredPrickles);
        return prickleMatch;
    }

    private PhenotypeMatch makeKnobblyKneePhenotypeMatch() {
        PhenotypeTerm knobblyKnee = new PhenotypeTerm("GRUF:222", "Knobbly knees", 5.0);
        PhenotypeTerm wobblyKnee = new PhenotypeTerm("GRUF:224", "Wobbly knees", 5.0);
        PhenotypeTerm unstableKnee = new PhenotypeTerm("GRUF:220", "Unstable knees", 3.0);
        PhenotypeMatch kneeMatch = new PhenotypeMatch(knobblyKnee, wobblyKnee, 0.9, unstableKnee);
        return kneeMatch;
    }

    public List<String> getQueryPhenotypeTermIds() {
        return queryPhenotypeTermIds;
    }

    public List<PhenotypeMatch> getPhenotypeMatches() {
        return phenotypeMatches;
    }

    public List<PhenoGridMatch> getPhenoGridMatches() {
        return phenoGridMatches;
    }

    List<PhenoGridMatchGroup> getPhenoGridMatchGroups() {
        return phenoGridMatchGroups;
    }

}
