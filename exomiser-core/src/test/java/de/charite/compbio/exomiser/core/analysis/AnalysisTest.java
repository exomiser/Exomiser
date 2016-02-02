
/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2015  Charite Universitätsmedizin Berlin and Genome Research Ltd.
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

import de.charite.compbio.exomiser.core.filters.EntrezGeneIdFilter;
import de.charite.compbio.exomiser.core.filters.GeneFilter;
import de.charite.compbio.exomiser.core.filters.InheritanceFilter;
import de.charite.compbio.exomiser.core.filters.PassAllVariantEffectsFilter;
import de.charite.compbio.exomiser.core.filters.VariantFilter;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicitySource;
import de.charite.compbio.exomiser.core.prioritisers.NoneTypePrioritiser;
import de.charite.compbio.exomiser.core.prioritisers.Prioritiser;
import de.charite.compbio.exomiser.core.prioritisers.ScoringMode;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AnalysisTest {

    private Analysis instance;

    private SampleData sampleData;

    @Before
    public void setUp() {
        sampleData = new SampleData();
        instance = new Analysis();
    }

    @Test
    public void canSetVcfPathInConstructor() {
        Path vcfPath = Paths.get("vcf");
        instance = new Analysis(vcfPath);
        assertThat(instance.getVcfPath(), equalTo(vcfPath));
    }
    
    @Test
    public void testCanSetAndGetVcfFilePath() {
        Path vcfPath = Paths.get("vcf");
        instance.setVcfPath(vcfPath);
        assertThat(instance.getVcfPath(), equalTo(vcfPath));
    }

    @Test
    public void testCanSetAndGetPedFilePath() {
        Path pedPath = Paths.get("ped");
        instance.setPedPath(pedPath);
        assertThat(instance.getPedPath(), equalTo(pedPath));
    }

    @Test
    public void testCanGetSampleData() {
        assertThat(instance.getSampleData(), equalTo(sampleData));
    }

    @Test
    public void modeOfInheritanceDefaultsToUnspecified() {
        assertThat(instance.getModesOfInheritance(), equalTo(EnumSet.of(ModeOfInheritance.UNINITIALIZED)));
    }

    @Test
    public void testCanMakeAnalysis_specifyModeOfInheritance() {
        ModeOfInheritance modeOfInheritance = ModeOfInheritance.AUTOSOMAL_DOMINANT;
        instance.setModeOfInheritance(modeOfInheritance);
        assertThat(instance.getModesOfInheritance(), equalTo(EnumSet.of(modeOfInheritance)));
    }

    @Test
    public void testGeneScoringModeDefaultsToRawScore() {
        assertThat(instance.getScoringMode(), equalTo(ScoringMode.RAW_SCORE));
    }

    @Test
    public void testGeneScoringModeCanBeSpecified() {
        ScoringMode scoringMode = ScoringMode.RANK_BASED;
        instance.setScoringMode(scoringMode);
        assertThat(instance.getScoringMode(), equalTo(scoringMode));
    }

    @Test
    public void analysisModeDefaultsToPassOnly() {
        assertThat(instance.getAnalysisMode(), equalTo(AnalysisMode.PASS_ONLY));
    }

    @Test
    public void analysisCanSpecifyAlternativeAnalysisMode() {
        instance.setAnalysisMode(AnalysisMode.FULL);
        assertThat(instance.getAnalysisMode(), equalTo(AnalysisMode.FULL));
    }

    @Test
    public void testFrequencySourcesAreEmptyByDefault() {
        assertThat(instance.getFrequencySources().isEmpty(), is(true));
    }

    @Test
    public void canSpecifyFrequencySources() {
        Set<FrequencySource> sources = EnumSet.allOf(FrequencySource.class);
        instance.setFrequencySources(sources);
        assertThat(instance.getFrequencySources(), equalTo(sources));
    }
    
    @Test
    public void testPathogenicitySourcesAreEmptyByDefault() {
        assertThat(instance.getPathogenicitySources().isEmpty(), is(true));
    }

    @Test
    public void canSpecifyPathogenicitySources() {
        Set<PathogenicitySource> sources = EnumSet.allOf(PathogenicitySource.class);
        instance.setPathogenicitySources(sources);
        assertThat(instance.getPathogenicitySources(), equalTo(sources));
    }
    
    @Test
    public void testGetAnalysisSteps_ReturnsEmptyListWhenNoStepsHaveBeedAdded() {
        List<AnalysisStep> steps = Collections.emptyList();
        assertThat(instance.getAnalysisSteps(), equalTo(steps));
    }

    @Test
    public void testCanAddVariantFilterAsAnAnalysisStep() {
        VariantFilter variantFilter = new PassAllVariantEffectsFilter();
        instance.addStep(variantFilter);
        assertThat(instance.getAnalysisSteps(), hasItem(variantFilter));
    }

    @Test
    public void testCanAddGeneFilterAsAnAnalysisStep() {
        GeneFilter geneFilter = new InheritanceFilter(ModeOfInheritance.UNINITIALIZED);
        instance.addStep(geneFilter);
        assertThat(instance.getAnalysisSteps(), hasItem(geneFilter));
    }

    @Test
    public void testCanAddPrioritiserAsAnAnalysisStep() {
        Prioritiser prioritiser = new NoneTypePrioritiser();
        instance.addStep(prioritiser);
        assertThat(instance.getAnalysisSteps(), hasItem(prioritiser));
    }

    @Test
    public void testGetAnalysisSteps_ReturnsListOfStepsAdded() {
        List<AnalysisStep> steps = new ArrayList<>();
        VariantFilter geneIdFilter = new EntrezGeneIdFilter(new HashSet<>());
        Prioritiser noneType = new NoneTypePrioritiser();
        GeneFilter inheritanceFilter = new InheritanceFilter(ModeOfInheritance.UNINITIALIZED);
        VariantFilter targetFilter = new PassAllVariantEffectsFilter();

        steps.add(geneIdFilter);
        steps.add(noneType);
        steps.add(inheritanceFilter);
        steps.add(targetFilter);

        instance.addStep(geneIdFilter);
        instance.addStep(noneType);
        instance.addStep(inheritanceFilter);
        instance.addStep(targetFilter);

        assertThat(instance.getAnalysisSteps(), equalTo(steps));
    }

}
