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
package org.monarchinitiative.exomiser.core.genome.dao;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.pathogenicity.*;
import org.monarchinitiative.exomiser.core.prioritisers.config.TestDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestDataSourceConfig.class, DefaultPathogenicityDao.class})
@Sql(scripts = {
        "file:src/test/resources/sql/create_pathogenicity.sql",
        "file:src/test/resources/sql/pathogenicityDaoTestData.sql"
})
public class DefaultPathogenicityDaoTest {

    @Autowired
    private DefaultPathogenicityDao instance;

    private static final SiftScore SIFT_SCORE = SiftScore.valueOf(0f);
    private static final PolyPhenScore POLY_PHEN_SCORE = PolyPhenScore.valueOf(0.998f);
    private static final MutationTasterScore MUTATION_TASTER_SCORE = MutationTasterScore.valueOf(1.0f);
    private static final CaddScore CADD_SCORE = CaddScore.valueOf(23.7f);

    private static final PathogenicityData NO_PATH_DATA = PathogenicityData.EMPTY_DATA;

    private final Variant missenseVariantInDatabase = makeMissenseVariant(10, 123256215, "T", "G");

    private Variant makeMissenseVariant(int chr, int pos, String ref, String alt) {
        return new VariantEvaluation.Builder(chr, pos, ref, alt)
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();
    }

    @Test
    public void testNonMissenseVariantReturnsAnEmptyPathogenicityData() {
        Variant nonMissenseVariant = new VariantEvaluation.Builder(0, 0, "A", "T")
                .variantEffect(VariantEffect.DOWNSTREAM_GENE_VARIANT)
                .build();
        PathogenicityData result = instance.getPathogenicityData(nonMissenseVariant);

        assertThat(result, equalTo(NO_PATH_DATA));
        assertThat(result.hasPredictedScore(), is(false));
    }

    @Test
    public void testMissenseVariantReturnsAnEmptyPathogenicityDataWhenNotInDatabase() {
        Variant missenseVariantNotInDatabase = makeMissenseVariant(0, 0, "T", "G");
        PathogenicityData result = instance.getPathogenicityData(missenseVariantNotInDatabase);

        assertThat(result, equalTo(NO_PATH_DATA));
        assertThat(result.hasPredictedScore(), is(false));
    }

    @Test
    public void testMissenseVariantReturnsPathogenicityDataWhenInDatabase() {
        PathogenicityData result = instance.getPathogenicityData(missenseVariantInDatabase);
        PathogenicityData expected = new PathogenicityData(POLY_PHEN_SCORE, MUTATION_TASTER_SCORE, SIFT_SCORE);
        assertThat(result, equalTo(expected));
    }

    /**
     * As of 20150511 we're not going to use the CADD data from the database as
     * it requires normalising and hasn't been using it will COMPLETELY FUBAR
     * THE PATHOGENICITY FILTER, so don't add it back until it's normalised on
     * a 0-1 scale.
     */
    @Test
    public void testPathogenicityDaoDoesNotReturnCaddScoreEvenWhenPresentInDatabase() {
        PathogenicityData result = instance.getPathogenicityData(missenseVariantInDatabase);
        assertThat(result.hasPredictedScore(PathogenicitySource.CADD), is(false));
    }

    @Test
    public void testMissenseVariantInDatabaseWithNullSift() {
        Variant missenseVariantWithNullSift = makeMissenseVariant(1, 1, "A", "T");
        PathogenicityData result = instance.getPathogenicityData(missenseVariantWithNullSift);
        PathogenicityData expected = new PathogenicityData(POLY_PHEN_SCORE, MUTATION_TASTER_SCORE);
        assertThat(result, equalTo(expected));
    }

    @Test
    public void testMissenseVariantInDatabaseWithNullCadd() {
        Variant missenseVariantWithNullCadd = makeMissenseVariant(1, 4, "A", "T");
        PathogenicityData result = instance.getPathogenicityData(missenseVariantWithNullCadd);
        PathogenicityData expected = new PathogenicityData(POLY_PHEN_SCORE, MUTATION_TASTER_SCORE, SIFT_SCORE);
        assertThat(result, equalTo(expected));
    }

    @Test
    public void testMissenseVariantInDatabaseWithNullPolyPhen() {
        Variant missenseVariantWithNullPolyPhen = makeMissenseVariant(1, 2, "A", "T");
        PathogenicityData result = instance.getPathogenicityData(missenseVariantWithNullPolyPhen);
        PathogenicityData expected = new PathogenicityData(MUTATION_TASTER_SCORE, SIFT_SCORE);
        assertThat(result, equalTo(expected));
    }

    @Test
    public void testMissenseVariantInDatabaseWithNullMutTaster() {
        Variant missenseVariantWithNullMutTaster = makeMissenseVariant(1, 3, "A", "T");
        PathogenicityData result = instance.getPathogenicityData(missenseVariantWithNullMutTaster);
        PathogenicityData expected = new PathogenicityData(POLY_PHEN_SCORE, SIFT_SCORE);
        assertThat(result, equalTo(expected));
    }

    @Test
    public void testMissenseVariantWithMultipleRowsReturnsBestScores() {
        Variant missenseVariantWithMultipleRows = makeMissenseVariant(1, 5, "A", "T");
        PathogenicityData result = instance.getPathogenicityData(missenseVariantWithMultipleRows);
        PathogenicityData expected = new PathogenicityData(POLY_PHEN_SCORE, MUTATION_TASTER_SCORE, SIFT_SCORE);
        assertThat(result, equalTo(expected));
    }
}
