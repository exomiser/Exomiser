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
package org.monarchinitiative.exomiser.core.factories;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.SampleData;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SampleDataFactoryTest {
    
    private SampleDataFactory instance = new SampleDataFactory(variantFactory(), jannovarData());

    private JannovarData jannovarData() {
        return TestFactory.buildDefaultJannovarData();
    }

    private VariantFactory variantFactory() {
        return new VariantFactory(jannovarData());
    }


    @Test(expected = NullPointerException.class)
    public void testNullVcfThrowsANullPointer() {
        Path pedPath = Paths.get("ped");
        instance.createSampleData(null, pedPath);
    }
    
    @Test
    public void createsSampleDataWithSingleSampleVcfAndNoPedFile() {
        Path vcfPath = Paths.get("src/test/resources/smallTest.vcf");
        SampleData sampleData = instance.createSampleData(vcfPath, null);
        
        String sampleName = "manuel"; 
        List<String> sampleNames = new ArrayList<>();
        sampleNames.add(sampleName);
        
        Pedigree pedigree = Pedigree.constructSingleSamplePedigree(sampleName);

        assertThat(sampleData, notNullValue());
        assertThat(sampleData.getVcfPath(), equalTo(vcfPath));
        assertThat(sampleData.getSampleNames(), equalTo(sampleNames));
        assertThat(sampleData.getNumberOfSamples(), equalTo(1));
        assertThat(sampleData.getPedigree().getMembers().get(0), equalTo(pedigree.getMembers().get(0)));
        assertThat(sampleData.getVariantEvaluations().isEmpty(), is(false));
        assertThat(sampleData.getVariantEvaluations().size(), equalTo(3));
        assertThat(sampleData.getGenes().isEmpty(), is(false));
    }
    
    @Test
    public void returnsEmptySampleDataFromEmptyVcfFileAndNullPedFile() {
        Path vcfPath = Paths.get("src/test/resources/headerOnly.vcf");
        SampleData sampleData = instance.createSampleData(vcfPath, null);
        
        String sampleName = "manuel"; 
        List<String> sampleNames = new ArrayList<>();
        sampleNames.add(sampleName);
        
        Pedigree pedigree = Pedigree.constructSingleSamplePedigree(sampleName);
        
        assertThat(sampleData, notNullValue());
        assertThat(sampleData.getVcfPath(), equalTo(vcfPath));
        assertThat(sampleData.getSampleNames(), equalTo(sampleNames));
        assertThat(sampleData.getNumberOfSamples(), equalTo(1));
        assertThat(sampleData.getPedigree().getMembers().get(0), equalTo(pedigree.getMembers().get(0)));
        assertThat(sampleData.getVariantEvaluations().isEmpty(), is(true));
        assertThat(sampleData.getVariantEvaluations().size(), equalTo(0));
        assertThat(sampleData.getGenes().isEmpty(), is(true));
    }
    
    @Test(expected = RuntimeException.class)
    public void throwsErrorWithNonVcfPathAndNullPedFile() {
        Path vcfPath = Paths.get("");
        instance.createSampleData(vcfPath, null);
    }

    @Test(expected = RuntimeException.class)
    public void throwsErrorWithNonVcfFileAndNullPedFile() {
        Path vcfPath = Paths.get("src/test/resources/invalidPedTestFile.ped");
        instance.createSampleData(vcfPath, null);
    }

    @Test
    public void testGetVariantFactory() {
        assertThat(instance.getVariantFactory(), notNullValue());
    }

    @Test
    public void testCreateKnownGenes() {
        assertThat(instance.createKnownGenes().size(), equalTo(4));
    }
}
