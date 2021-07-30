/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.prioritisers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.phenotype.Organism;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;
import org.monarchinitiative.exomiser.core.prioritisers.model.*;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HiPhivePriorityResultTest {
    
    private HiPhivePriorityResult instance;
    private final int geneId = 12345;
    private final String geneSymbol = "FGFR2";
    private final double score = 0.87d;
    private List<PhenotypeTerm> queryPhenotypeTerms;
    private List<GeneModelPhenotypeMatch> phenotypeEvidence;
    private List<GeneModelPhenotypeMatch> ppiEvidence;
    private final double ppiScore = 0.6d;
    private final boolean matchesCandidateGene = true;
    
    @BeforeEach
    public void setUp() {
        queryPhenotypeTerms = List.of();
        phenotypeEvidence = List.of();
        ppiEvidence = List.of();
        instance = new HiPhivePriorityResult(geneId, geneSymbol, score, queryPhenotypeTerms, phenotypeEvidence, ppiEvidence, ppiScore, matchesCandidateGene);
    }

    private GeneModelPhenotypeMatch stubGeneModelPhenotypeMatch(Organism organism, double score) {
        GeneModel model;
        // yuk! Broken generics :(
        if (organism == Organism.HUMAN) {
            Disease disease = Disease.builder().diseaseId("OMIM:12345").diseaseName("disease1").associatedGeneId(12345).associatedGeneSymbol(geneSymbol).build();
            model = new GeneDiseaseModel("gene1_disease1", organism, disease);
        } else {
            model = new GeneOrthologModel("gene1_model1", organism, 12345, geneSymbol, "MGI:12345", "gene1", Collections.emptyList());
        }
        return new GeneModelPhenotypeMatch(score, model, Collections.emptyList());
    }
        
    @Test
    public void testGetPriorityType() {
        assertThat(instance.getPriorityType(), equalTo(PriorityType.HIPHIVE_PRIORITY));
    }

    @Test
    public void testGetGeneSymbol() {
        assertThat(instance.getGeneSymbol(), equalTo(geneSymbol));
    }

    @Test
    public void testGetScore() {
//        assertThat(instance.getScore(), closeTo(score, 0.0001));
        assertThat(instance.getScore(), equalTo(score));
    }

    @Test
    public void testGetQueryPhenotypeTerms() {
        assertThat(instance.getQueryPhenotypeTerms(), equalTo(queryPhenotypeTerms));
    }

    @Test
    public void testGetPhenotypeEvidence() {
        assertThat(instance.getPhenotypeEvidence(), equalTo(phenotypeEvidence));
    }

    @Test
    public void testGetPpiEvidence() {
        assertThat(instance.getPpiEvidence(), equalTo(ppiEvidence));
    }

    @Test
    public void testGetHTMLCode() {
        assertThat(instance.getHTMLCode(), equalTo("<dl><dt>No phenotype or PPI evidence</dt></dl>"));
    }

    @Test
    public void testGetHumanScoreIsZeroWithNoDiseaseEvidence() {
        assertThat(instance.getHumanScore(), equalTo(0d));
    }

    @Test
    public void testGetHumanScoreMatchesModelScore() {
        GeneModelPhenotypeMatch geneModel = stubGeneModelPhenotypeMatch(Organism.HUMAN, 1d);

        List<GeneModelPhenotypeMatch> models = List.of(geneModel);
        instance = new HiPhivePriorityResult(geneId, geneSymbol, score, queryPhenotypeTerms, models, ppiEvidence, ppiScore, false);

        assertThat(instance.getHumanScore(), equalTo(geneModel.getScore()));
    }

    @Test
    public void testGetHumanScoreMatchesTopModelScore() {
        GeneModelPhenotypeMatch topGeneModel = stubGeneModelPhenotypeMatch(Organism.HUMAN, 1d);
        GeneModelPhenotypeMatch poorMatchModel = stubGeneModelPhenotypeMatch(Organism.HUMAN, 0.5);

        // note these are provided with the worst score first to test that the HiPhivePriorityResult orders things internally
        List<GeneModelPhenotypeMatch> models = List.of(poorMatchModel, topGeneModel);
        instance = new HiPhivePriorityResult(geneId, geneSymbol, score, queryPhenotypeTerms, models, ppiEvidence, ppiScore, false);

        assertThat(instance.getHumanScore(), equalTo(topGeneModel.getScore()));
        assertThat(instance.getPhenotypeEvidence(), equalTo(List.of(topGeneModel)));
        assertThat(instance.getDiseaseMatches(), equalTo(List.of(topGeneModel, poorMatchModel)));
    }

    @Test
    public void testGetMouseScoreIsZeroWithNoDiseaseEvidence() {
        assertThat(instance.getMouseScore(), equalTo(0d));
    }

    @Test
    public void testGetMouseScoreMatchesModelScore() {
        GeneModelPhenotypeMatch geneModel = stubGeneModelPhenotypeMatch(Organism.MOUSE, 1d);

        List<GeneModelPhenotypeMatch> models = List.of(geneModel);
        instance = new HiPhivePriorityResult(geneId, geneSymbol, score, queryPhenotypeTerms, models, ppiEvidence, ppiScore, false);

        assertThat(instance.getMouseScore(), equalTo(geneModel.getScore()));
    }
    
    @Test
    public void testGetFishScoreIsZeroWithNoDiseaseEvidence() {
        assertThat(instance.getFishScore(), equalTo(0d));
    }

    @Test
    public void testGetFishScoreMatchesModelScore() {
        GeneModelPhenotypeMatch geneModel = stubGeneModelPhenotypeMatch(Organism.FISH, 1d);

        List<GeneModelPhenotypeMatch> models = List.of(geneModel);
        instance = new HiPhivePriorityResult(geneId, geneSymbol, score, queryPhenotypeTerms, models, ppiEvidence, ppiScore, false);

        assertThat(instance.getFishScore(), equalTo(geneModel.getScore()));
    }

    @Test
    public void testGetWalkerScore() {
        assertThat(instance.getPpiScore(), equalTo(ppiScore));
    }
    
    @Test
    public void testIsCandidateGeneMatchMatchesConstructorArg() {
        assertThat(instance.isCandidateGeneMatch(), is(matchesCandidateGene));
    }

    @Test
    void testGetPhenotypeEvidenceText() {
        System.out.println(instance.getPhenotypeEvidenceText());
    }

    @Test
    public void testToString() {
        assertThat(instance.toString(), equalTo("HiPhivePriorityResult{geneId=12345, geneSymbol='FGFR2', score=0.87, humanScore=0.0, mouseScore=0.0, fishScore=0.0, ppiScore=0.6, candidateGeneMatch=true, queryPhenotypeTerms=[], phenotypeEvidence=[], ppiEvidence=[]}"));
    }
}
