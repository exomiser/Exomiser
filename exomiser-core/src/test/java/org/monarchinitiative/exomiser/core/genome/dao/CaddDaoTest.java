/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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
package org.monarchinitiative.exomiser.core.genome.dao;

import htsjdk.tribble.readers.TabixReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.pathogenicity.CaddScore;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@ExtendWith(MockitoExtension.class)
public class CaddDaoTest {

    private CaddDao instance;

    @Mock
    private TabixReader snvTabixReader;
    @Mock
    private TabixReader indelTabixReader;

    @BeforeEach
    public void setUp() {
        //What's all this about? Well we're removing the requirement for reading files from disk
        //by mocking the TabixReaders. Unfortunately these are concrete classes with no interface so
        //the mocking is event based with canned answers coming from Mockito which replicates an
        //internal call in the Dao:
        //TabixReader.Iterator results = inDelTabixReader.query(chromosome, start, start);
        //The MockTabixIterator will return a list of lines
        TabixDataSource snvTabixDataSource = new TabixReaderAdaptor(snvTabixReader);
        TabixDataSource inDelTabixDataSource = new TabixReaderAdaptor(indelTabixReader);
        instance = new CaddDao(inDelTabixDataSource, snvTabixDataSource);
    }

    private static VariantEvaluation variant(int chr, int pos, String ref, String alt) {
        return VariantEvaluation.builder(chr, pos, ref, alt).build();
    }

    private void assertPathDataContainsCaddScore(PathogenicityData result, float score) {
        CaddScore expected = CaddScore.valueOf(score);
        assertThat(result, equalTo(PathogenicityData.of(expected)));
    }

    @Test
    public void testGetPathogenicityDataSnvNoData() {
        Mockito.when(snvTabixReader.query("1:2-2")).thenReturn(MockTabixIterator.empty());
        PathogenicityData result = instance.getPathogenicityData(variant(1, 2, "A", "T"));
        assertThat(result, equalTo(PathogenicityData.empty()));
    }

    @Test
    public void testGetPathogenicityDataInsertionNoData() {
        Mockito.when(indelTabixReader.query("1:2-2")).thenReturn(MockTabixIterator.empty());
        PathogenicityData result = instance.getPathogenicityData(variant(1, 2, "C", "CA"));
        assertThat(result, equalTo(PathogenicityData.empty()));
    }

    @Test
    public void testGetPathogenicityDataInsertionSingleVariantAtPositionNoMatch() {
        Mockito.when(indelTabixReader.query("1:2-2")).thenReturn(MockTabixIterator.of(
                "1\t1\tA\tAT\t-0.234\t3.45",
                "1\t1\tA\tAC\t-0.234\t4.45"));

        PathogenicityData result = instance.getPathogenicityData(variant(1, 2, "A", "AG"));
        assertThat(result, equalTo(PathogenicityData.empty()));
    }

    @Test
    public void testGetPathogenicityDataInsertionSingleVariantAtPositionOneMatch() {
        Mockito.when(indelTabixReader.query("1:2-2")).thenReturn(MockTabixIterator.of("1\t2\tA\tAA\t-0.234\t3.45"));

        PathogenicityData result = instance.getPathogenicityData(variant(1, 2, "A", "AA"));
        assertPathDataContainsCaddScore(result, 0.54814404f);
    }

    @Test
    public void testGetPathogenicityDataInsertionMultipleVariantsAtPositionOneMatch() {
        Mockito.when(indelTabixReader.query("1:2-2")).thenReturn(MockTabixIterator.of(
                "\t\tA\tAC\t-0.234\t4.45",
                "\t\tA\tAT\t-0.234\t3.45"));

        PathogenicityData result = instance.getPathogenicityData(variant(1, 2, "A", "AT"));
        assertPathDataContainsCaddScore(result, 0.54814404f);
    }

    @Test
    public void testGetPathogenicityDatadeletionNoData() {
        Mockito.when(indelTabixReader.query("1:2-2")).thenReturn(MockTabixIterator.empty());
        PathogenicityData result = instance.getPathogenicityData(variant(1, 2, "AT", "T"));
        assertThat(result, equalTo(PathogenicityData.empty()));
    }

    @Test
    public void testGetPathogenicityDatadeletionSingleVariantAtPositionNoMatch() {
        Mockito.when(indelTabixReader.query("1:2-2")).thenReturn(MockTabixIterator.of(
                "1\t1\tAT\tA\t-0.234\t3.45",
                "1\t1\tAC\tA\t-0.234\t4.45"));

        PathogenicityData result = instance.getPathogenicityData(variant(1, 2, "AG", "A"));
        assertThat(result, equalTo(PathogenicityData.empty()));
    }

    @Test
    public void testGetPathogenicityDataDeletionSingleVariantAtPositionOneMatch() {
        Mockito.when(indelTabixReader.query("1:2-2")).thenReturn(MockTabixIterator.of("1\t2\tGT\tG\t-0.234\t3.45"));

        PathogenicityData result = instance.getPathogenicityData(variant(1, 2, "GT", "G"));
        assertPathDataContainsCaddScore(result, 0.54814404f);
    }

    @Test
    public void testGetPathogenicityDataDeletionMultipleVariantsAtPositionOneMatch() {
        Mockito.when(indelTabixReader.query("1:2-2")).thenReturn(MockTabixIterator.of(
                "\t\tAC\tA\t-0.234\t4.45",
                "\t\tAT\tA\t-0.234\t3.45"));

        PathogenicityData result = instance.getPathogenicityData(variant(1, 2, "AT", "A"));
        assertPathDataContainsCaddScore(result, 0.54814404f);
    }

    @Test
    public void testGetPathogenicityDataSnvSingleVariantAtPositionNoMatch() {
        Mockito.when(snvTabixReader.query("1:2-2")).thenReturn(MockTabixIterator.of(
                "1\t1\tA\tT\t-0.234\t3.45",
                "1\t1\tA\tC\t-0.234\t4.45"));

        PathogenicityData result = instance.getPathogenicityData(variant(1, 2, "A", "G"));
        assertThat(result, equalTo(PathogenicityData.empty()));
    }

    @Test
    public void testGetPathogenicityDataSnvSingleVariantAtPositionOneMatch() {
        Mockito.when(snvTabixReader.query("1:2-2")).thenReturn(MockTabixIterator.of("1\t1\tA\tT\t-0.234\t3.45"));

        PathogenicityData result = instance.getPathogenicityData(variant(1, 2, "A", "T"));
        assertPathDataContainsCaddScore(result, 0.54814404f);
    }

    @Test
    public void testGetPathogenicityDataSnvXchrSingleVariantAtPositionOneMatch() {
        Mockito.when(snvTabixReader.query("X:1-1")).thenReturn(MockTabixIterator.of("1\t1\tA\tT\t-0.234\t3.45"));

        PathogenicityData result = instance.getPathogenicityData(variant(23, 1, "A", "T"));
        assertPathDataContainsCaddScore(result, 0.54814404f);
    }

    @Test
    public void testGetPathogenicityDataSnvYchrSingleVariantAtPositionOneMatch() {
        Mockito.when(snvTabixReader.query("Y:1-1")).thenReturn(MockTabixIterator.of("1\t1\tA\tT\t-0.234\t3.45"));

        PathogenicityData result = instance.getPathogenicityData(variant(24, 1, "A", "T"));
        assertPathDataContainsCaddScore(result, 0.54814404f);
    }

    @Test
    public void testGetPathogenicityDataSnvMultipleVariantsAtPositionOneMatch() {
        Mockito.when(snvTabixReader.query("1:2-2")).thenReturn(MockTabixIterator.of(
                "\t\tA\tT\t-0.234\t3.45",
                "\t\tA\tC\t-0.234\t4.45"));

        PathogenicityData result = instance.getPathogenicityData(variant(1, 2, "A", "T"));
        assertPathDataContainsCaddScore(result, 0.54814404f);
    }

}
