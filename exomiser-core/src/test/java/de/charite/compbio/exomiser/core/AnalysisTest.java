/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.charite.compbio.exomiser.core.AnalysisRunner.AnalysisMode;
import de.charite.compbio.exomiser.core.filters.EntrezGeneIdFilter;
import de.charite.compbio.exomiser.core.filters.GeneFilter;
import de.charite.compbio.exomiser.core.filters.InheritanceFilter;
import de.charite.compbio.exomiser.core.filters.PassAllVariantEffectsFilter;
import de.charite.compbio.exomiser.core.filters.VariantEffectFilter;
import de.charite.compbio.exomiser.core.filters.VariantFilter;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.prioritisers.NoneTypePrioritiser;
import de.charite.compbio.exomiser.core.prioritisers.Prioritiser;
import de.charite.compbio.exomiser.core.prioritisers.ScoringMode;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Before;
import org.junit.Ignore;
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
        assertThat(instance.getModeOfInheritance(), equalTo(ModeOfInheritance.UNINITIALIZED));
    }

    @Test
    public void testCanMakeAnalysis_specifyModeOfInheritance() {
        ModeOfInheritance modeOfInheritance = ModeOfInheritance.AUTOSOMAL_DOMINANT;
        instance.setModeOfInheritance(modeOfInheritance);
        assertThat(instance.getModeOfInheritance(), equalTo(modeOfInheritance));
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
    public void analysisCanSpeciftAlternativeAnalysisMode() {
        instance.setAnalysisMode(AnalysisMode.FULL);
        assertThat(instance.getAnalysisMode(), equalTo(AnalysisMode.FULL));
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
        assertThat(instance.getAnalysisSteps().contains(variantFilter), is(true));
    }

    @Test
    public void testCanAddGeneFilterAsAnAnalysisStep() {
        GeneFilter geneFilter = new InheritanceFilter(ModeOfInheritance.UNINITIALIZED);
        instance.addStep(geneFilter);
        assertThat(instance.getAnalysisSteps().contains(geneFilter), is(true));
    }

    @Test
    public void testCanAddPrioritiserAsAnAnalysisStep() {
        Prioritiser prioritiser = new NoneTypePrioritiser();
        instance.addStep(prioritiser);
        assertThat(instance.getAnalysisSteps().contains(prioritiser), is(true));
    }

    @Test
    public void testGetAnalysisSteps_ReturnsListOfStepsAdded() {
        List<AnalysisStep> steps = new ArrayList<>();
        VariantFilter geneIdFilter = new EntrezGeneIdFilter(new HashSet<Integer>());
        Prioritiser noneType = new NoneTypePrioritiser();
        GeneFilter inheritanceFilter = new InheritanceFilter(ModeOfInheritance.UNINITIALIZED);
        VariantFilter targetFilter = (VariantFilter) new PassAllVariantEffectsFilter();

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
