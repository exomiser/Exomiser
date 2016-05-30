
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

package de.charite.compbio.exomiser.core.analysis;

import static de.charite.compbio.exomiser.core.analysis.SettingsParser.NON_EXONIC_VARIANT_EFFECTS;
import de.charite.compbio.exomiser.core.analysis.Settings.SettingsBuilder;
import de.charite.compbio.exomiser.core.factories.VariantDataServiceStub;
import de.charite.compbio.exomiser.core.filters.EntrezGeneIdFilter;
import de.charite.compbio.exomiser.core.filters.FrequencyFilter;
import de.charite.compbio.exomiser.core.filters.InheritanceFilter;
import de.charite.compbio.exomiser.core.filters.IntervalFilter;
import de.charite.compbio.exomiser.core.filters.KnownVariantFilter;
import de.charite.compbio.exomiser.core.filters.PathogenicityFilter;
import de.charite.compbio.exomiser.core.filters.QualityFilter;
import de.charite.compbio.exomiser.core.filters.VariantEffectFilter;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicitySource;
import de.charite.compbio.exomiser.core.prioritisers.NoneTypePrioritiser;
import de.charite.compbio.exomiser.core.prioritisers.NoneTypePriorityFactoryStub;
import de.charite.compbio.exomiser.core.prioritisers.OMIMPriority;
import de.charite.compbio.exomiser.core.prioritisers.PriorityFactory;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
*
* @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
*/
public class SettingsParserTest {

    private SettingsParser instance;
            
    private SettingsBuilder settingsBuilder;
    private Analysis analysis;
    
    private final ModeOfInheritance autosomal_dominant = ModeOfInheritance.AUTOSOMAL_DOMINANT;
    private final GeneticInterval interval = new GeneticInterval(2, 12345, 67890);
    
    @Before
    public void setUp() {       
        PriorityFactory stubPriorityFactory = new NoneTypePriorityFactoryStub();
        instance = new SettingsParser(stubPriorityFactory, new VariantDataServiceStub());
        
        settingsBuilder = new SettingsBuilder().vcfFilePath(Paths.get("vcf"));
        analysis = new Analysis();
        analysis.setAnalysisMode(AnalysisMode.SPARSE);
        analysis.setVcfPath(Paths.get("vcf"));
        analysis.setFrequencySources(FrequencySource.ALL_EXTERNAL_FREQ_SOURCES);
        analysis.setPathogenicitySources(EnumSet.of(PathogenicitySource.MUTATION_TASTER, PathogenicitySource.POLYPHEN, PathogenicitySource.SIFT));
    }

    private void addDefaultVariantFilters(Analysis analysis) {
        analysis.addStep(new VariantEffectFilter(NON_EXONIC_VARIANT_EFFECTS));
        analysis.addStep(new FrequencyFilter(100f));
        analysis.addStep(new PathogenicityFilter(false));
    }

    @Test
    public void testDefaultAnalysisModeIsSparse() {
        Analysis result = instance.parse(settingsBuilder.build());
        assertThat(result.getAnalysisMode(), equalTo(AnalysisMode.SPARSE));
    }

    @Test
    public void testCanSpecifyNotFullAnalysisMode() {
        Analysis result = instance.parse(settingsBuilder.runFullAnalysis(false).build());
        assertThat(result.getAnalysisMode(), equalTo(AnalysisMode.SPARSE));
    }

    @Test
    public void testCanSpecifyFullAnalysisMode() {
        Analysis result = instance.parse(settingsBuilder.runFullAnalysis(true).build());
        assertThat(result.getAnalysisMode(), equalTo(AnalysisMode.FULL));
    }

    @Test
    public void testDefaultFrequencyDataSources() {
        Analysis result = instance.parse(settingsBuilder.build());
        assertThat(result.getFrequencySources(), equalTo(analysis.getFrequencySources()));
    }
    
    @Test
    public void testDefaultPathogenicityDataSources() {
        Analysis result = instance.parse(settingsBuilder.build());
        assertThat(result.getPathogenicitySources(), equalTo(analysis.getPathogenicitySources()));
    }
    
    @Test
    public void testDefaultAnalysisIsTargetFrequencyAndPathogenicityFilters() {
        Settings settings = settingsBuilder.build();
        
        addDefaultVariantFilters(analysis);
        
        Analysis result = instance.parse(settings);
        assertThat(result, equalTo(analysis));
    }

    @Test
    public void testSpecifyingInheritanceModeAddsAnInheritanceFilter() {
        
        Settings settings = settingsBuilder
                .modeOfInheritance(autosomal_dominant).build();
        
        addDefaultVariantFilters(analysis);
        analysis.setModeOfInheritance(autosomal_dominant);
        analysis.addStep(new InheritanceFilter(autosomal_dominant));
        
        Analysis result = instance.parse(settings);
        assertThat(result, equalTo(analysis));
        
    }
    
    @Test
    public void testCanMakeAllTypesOfFilter() {
        //make a new Settings object specifying a Pathogenicity, Frequency, Quality and Interval filters
        Set<Integer> geneIdsToKeep = new HashSet<>();
        geneIdsToKeep.add(1);
        
        Settings settings = settingsBuilder
                .modeOfInheritance(autosomal_dominant)
                .genesToKeep(geneIdsToKeep)
                .keepNonPathogenic(true)
                .removeKnownVariants(true)
                .maximumFrequency(0.25f)
                .minimumQuality(2f)
                .geneticInterval(interval)
                .build();

        analysis.addStep(new EntrezGeneIdFilter(geneIdsToKeep));
        analysis.addStep(new IntervalFilter(interval));
        analysis.addStep(new VariantEffectFilter(NON_EXONIC_VARIANT_EFFECTS));
        analysis.addStep(new QualityFilter(2f));
        analysis.addStep(new KnownVariantFilter());
        analysis.addStep(new FrequencyFilter(0.25f));
        analysis.addStep(new PathogenicityFilter(true));
        analysis.addStep(new InheritanceFilter(autosomal_dominant));
        analysis.setModeOfInheritance(autosomal_dominant);
        
        Analysis result = instance.parse(settings);
        assertThat(result, equalTo(analysis));
    }
    
    @Test
    public void testSpecifyingOmimPrioritiserOnlyAddsOmimPrioritiser() {

        Settings settings = settingsBuilder
                .usePrioritiser(PriorityType.OMIM_PRIORITY)
                .build();
        
        addDefaultVariantFilters(analysis);
        analysis.addStep(new OMIMPriority());
        
        Analysis result = instance.parse(settings);
        assertThat(result, equalTo(analysis));
        
    }
    
    @Test
    public void testSpecifyingPrioritiserAddsAnOmimAndTheSpecifiedPrioritiser() {

        List<String> hpoIds = new ArrayList<>();
        hpoIds.add("HP:000001");
        hpoIds.add("HP:000002");
        hpoIds.add("HP:000003");
        
        Settings settings = settingsBuilder
                .usePrioritiser(PriorityType.PHIVE_PRIORITY)
                .hpoIdList(hpoIds)
                .build();
        
        analysis.setHpoIds(hpoIds);
        addDefaultVariantFilters(analysis);
        analysis.addStep(new OMIMPriority());
        analysis.addStep(new NoneTypePrioritiser());
        System.out.println(analysis);
        
        Analysis result = instance.parse(settings);
        assertThat(result, equalTo(analysis));        
    }

}
