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

package org.monarchinitiative.exomiser.core.prioritisers.model;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.phenotype.Organism;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GeneMatchTest {

    private GeneMatch instance;

    @Test
    public void queryGeneId() throws Exception {
        instance = GeneMatch.builder().queryGeneId(1234).build();
        assertThat(instance.getQueryGeneId(), equalTo(1234));
    }

    @Test
    public void matchGeneId() throws Exception {
        instance = GeneMatch.builder().matchGeneId(4321).build();
        assertThat(instance.getMatchGeneId(), equalTo(4321));
    }

    @Test
    public void score() throws Exception {
        instance = GeneMatch.builder().score(1.0).build();
        assertThat(instance.getScore(), equalTo(1.0));
    }

    @Test
    public void bestMatchModels() throws Exception {
        GeneModelPhenotypeMatch geneModelPhenotypeMatch = new GeneModelPhenotypeMatch(0, new GeneOrthologModel("Model:500", Organism.HUMAN, 4321, "GENE1", "HGNC:4321", "GENE1", List.of()), List.of());
        List<GeneModelPhenotypeMatch> models = List.of(geneModelPhenotypeMatch);
        instance = GeneMatch.builder().bestMatchModels(models).build();
        assertThat(instance.getBestMatchModels(), equalTo(models));
    }

    @Test
    public void build() throws Exception {
        instance = GeneMatch.builder().build();
        assertThat(instance, equalTo(GeneMatch.NO_HIT));
    }

    @Test
    public void testToString() {
        assertThat(GeneMatch.NO_HIT.toString(), equalTo("GeneMatch{queryGeneId=0, matchGeneId=0, score=0.0, bestMatchModels=[]}"));
    }
}