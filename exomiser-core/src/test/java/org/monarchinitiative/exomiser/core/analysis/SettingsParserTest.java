
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

package org.monarchinitiative.exomiser.core.analysis;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.analysis.Settings.SettingsBuilder;
import org.monarchinitiative.exomiser.core.filters.*;
import org.monarchinitiative.exomiser.core.genome.VariantDataServiceStub;
import org.monarchinitiative.exomiser.core.model.GeneticInterval;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.prioritisers.NoneTypePrioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.NoneTypePriorityFactoryStub;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityFactory;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.monarchinitiative.exomiser.core.analysis.SettingsParser.NON_EXONIC_VARIANT_EFFECTS;

/**
*
* @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
*/
public class SettingsParserTest {

    private static final Set<PathogenicitySource> DEFAULT_PATH_SCORES = Sets.immutableEnumSet(PathogenicitySource.POLYPHEN, PathogenicitySource.MUTATION_TASTER, PathogenicitySource.SIFT);

    private SettingsParser instance;

    private final PriorityFactory stubPriorityFactory = new NoneTypePriorityFactoryStub();

    @Before
    public void setUp() {
        instance = new SettingsParser(stubPriorityFactory, new VariantDataServiceStub());
    }

    private Analysis.Builder analysisBuilder() {
        return Analysis.builder()
                .analysisMode(AnalysisMode.SPARSE)
                .vcfPath(Paths.get("vcf"))
                .frequencySources(FrequencySource.ALL_EXTERNAL_FREQ_SOURCES)
                .pathogenicitySources(DEFAULT_PATH_SCORES);
    }

    private SettingsBuilder settingsBuilder() {
        return Settings.builder()
                .vcfFilePath(Paths.get("vcf"));
    }

    private List<AnalysisStep> defaultAnalysisSteps() {
        return ImmutableList.of(
                new VariantEffectFilter(NON_EXONIC_VARIANT_EFFECTS),
                new FrequencyFilter(100f),
                new PathogenicityFilter(false));
    }

    @Test
    public void testCanSpecifyPedFilePath() {
        Path pedpath = Paths.get("testPed.ped");
        Settings settings = settingsBuilder().pedFilePath(pedpath).build();
        Analysis result = instance.parse(settings);
        assertThat(result.getPedPath(), equalTo(pedpath));
    }

    @Test
    public void testCanSpecifyProbandSampleName() {
        String probandSampleName = "Nemo";
        Settings settings = settingsBuilder().probandSampleName(probandSampleName).build();
        Analysis result = instance.parse(settings);
        assertThat(result.getProbandSampleName(), equalTo(probandSampleName));
    }

    @Test
    public void testDefaultAnalysisModeIsSparse() {
        Analysis result = instance.parse(settingsBuilder().build());
        assertThat(result.getAnalysisMode(), equalTo(AnalysisMode.SPARSE));
    }

    @Test
    public void testCanSpecifyNotFullAnalysisMode() {
        Analysis result = instance.parse(settingsBuilder().runFullAnalysis(false).build());
        assertThat(result.getAnalysisMode(), equalTo(AnalysisMode.SPARSE));
    }

    @Test
    public void testCanSpecifyFullAnalysisMode() {
        Analysis result = instance.parse(settingsBuilder().runFullAnalysis(true).build());
        assertThat(result.getAnalysisMode(), equalTo(AnalysisMode.FULL));
    }

    @Test
    public void testDefaultFrequencyDataSources() {
        Analysis result = instance.parse(settingsBuilder().build());
        assertThat(result.getFrequencySources(), equalTo(FrequencySource.ALL_EXTERNAL_FREQ_SOURCES));
    }

    @Test
    public void testDefaultPathogenicityDataSources() {
        Analysis result = instance.parse(settingsBuilder().build());
        assertThat(result.getPathogenicitySources(), equalTo(DEFAULT_PATH_SCORES));
    }
    
    @Test
    public void testDefaultAnalysisIsTargetFrequencyAndPathogenicityFilters() {
        Settings settings = settingsBuilder().build();
        
        Analysis analysis = analysisBuilder().steps(defaultAnalysisSteps()).build();
        
        Analysis result = instance.parse(settings);
        assertThat(result, equalTo(analysis));
    }

    @Test
    public void testSpecifyingInheritanceModeAddsAnInheritanceFilter() {
        
        Settings settings = settingsBuilder()
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .build();
        
        Analysis analysis = analysisBuilder()
                .steps(defaultAnalysisSteps())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .addStep(new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT))
                .build();
        
        Analysis result = instance.parse(settings);
        assertThat(result, equalTo(analysis));
        
    }
    
    @Test
    public void testCanMakeAllTypesOfFilter() {
        //make a new Settings object specifying a Pathogenicity, Frequency, Quality and Interval filters
        Set<Integer> geneIdsToKeep = new HashSet<>();
        geneIdsToKeep.add(1);
        GeneticInterval interval = new GeneticInterval(2, 12345, 67890);

        Settings settings = settingsBuilder()
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE)
                .genesToKeep(geneIdsToKeep)
                .keepNonPathogenic(true)
                .removeFailed(true)
                .removeKnownVariants(true)
                .maximumFrequency(0.25f)
                .minimumQuality(2f)
                .geneticInterval(interval)
                .build();

        Analysis analysis = analysisBuilder()
                .addStep(new FailedVariantFilter())
                .addStep(new EntrezGeneIdFilter(geneIdsToKeep))
                .addStep(new IntervalFilter(interval))
                .addStep(new VariantEffectFilter(NON_EXONIC_VARIANT_EFFECTS))
                .addStep(new QualityFilter(2f))
                .addStep(new KnownVariantFilter())
                .addStep(new FrequencyFilter(0.25f))
                .addStep(new PathogenicityFilter(true))
                .addStep(new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_RECESSIVE))
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE)
                .build();
        
        Analysis result = instance.parse(settings);
        assertThat(result, equalTo(analysis));
    }
    
    @Test
    public void testSpecifyingOmimPrioritiserOnlyAddsOmimPrioritiser() {

        Settings settings = settingsBuilder()
                .usePrioritiser(PriorityType.OMIM_PRIORITY)
                .build();

        Analysis analysis = analysisBuilder()
                .steps(defaultAnalysisSteps())
                .addStep(stubPriorityFactory.makeOmimPrioritiser())
                .build();
        
        Analysis result = instance.parse(settings);
        assertThat(result, equalTo(analysis));
        
    }
    
    @Test
    public void testSpecifyingPrioritiserAddsAnOmimAndTheSpecifiedPrioritiser() {

        List<String> hpoIds = Lists.newArrayList("HP:000001", "HP:000002", "HP:000003");
        
        Settings settings = settingsBuilder()
                .usePrioritiser(PriorityType.PHIVE_PRIORITY)
                .hpoIdList(hpoIds)
                .build();
        
        Analysis analysis = analysisBuilder()
                .hpoIds(hpoIds)
                .steps(defaultAnalysisSteps())
                .addStep(stubPriorityFactory.makeOmimPrioritiser())
                .addStep(new NoneTypePrioritiser())
                .build();

        System.out.println(analysis);
        
        Analysis result = instance.parse(settings);
        assertThat(result, equalTo(analysis));        
    }

}
