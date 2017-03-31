/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.writers.phenogrid;

import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatch;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;

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
        PhenotypeTerm poisonouslyWartyNose = PhenotypeTerm.of("GRUF:444", "Poisonously warty nose");
        PhenotypeTerm hideouslyWartyNose = PhenotypeTerm.of("GRUF:443", "Hideously warty nose");
        PhenotypeTerm wartyNose = PhenotypeTerm.of("GRUF:440", "Warty nose");
        return PhenotypeMatch.builder()
                .query(poisonouslyWartyNose)
                .match(hideouslyWartyNose)
                .lcs(wartyNose)
                .ic(2.0)
                .simj(0.85)
                .score(2.0)
                .build();
    }

    private PhenotypeMatch makeMonsterousTonguePhenotypeMatch() {
        PhenotypeTerm blackTongue = PhenotypeTerm.of("GRUF:333", "Black tongue");
        PhenotypeTerm forkedTongue = PhenotypeTerm.of("GRUF:124", "Forked tongue");
        PhenotypeTerm abnormalTongue = PhenotypeTerm.of("GRUF:120", "Abnormal tongue");
        return PhenotypeMatch.builder()
                .query(blackTongue)
                .match(forkedTongue)
                .lcs(abnormalTongue)
                .ic(3.0)
                .simj(0.9)
                .score(3.0)
                .build();
    }

    private PhenotypeMatch makePurplePricklesPhenotypeMatch() {
        PhenotypeTerm purplePrickles = PhenotypeTerm.of("GRUF:111", "Purple prickles");
        PhenotypeTerm redPrickles = PhenotypeTerm.of("GRUF:112", "Red prickles");
        PhenotypeTerm colouredPrickles = PhenotypeTerm.of("GRUF:110", "Coloured prickles");
        return PhenotypeMatch.builder()
                .query(purplePrickles)
                .match(redPrickles)
                .lcs(colouredPrickles)
                .ic(2.0)
                .simj(0.7)
                .score(3.0)
                .build();
    }

    private PhenotypeMatch makeKnobblyKneePhenotypeMatch() {
        PhenotypeTerm knobblyKnee = PhenotypeTerm.of("GRUF:222", "Knobbly knees");
        PhenotypeTerm wobblyKnee = PhenotypeTerm.of("GRUF:224", "Wobbly knees");
        PhenotypeTerm unstableKnee = PhenotypeTerm.of("GRUF:220", "Unstable knees");
        return PhenotypeMatch.builder()
                .query(knobblyKnee)
                .match(wobblyKnee)
                .lcs(unstableKnee)
                .ic(3.0)
                .simj(0.9)
                .score(3.0)
                .build();
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
