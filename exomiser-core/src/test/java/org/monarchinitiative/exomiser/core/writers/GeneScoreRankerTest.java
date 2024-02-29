package org.monarchinitiative.exomiser.core.writers;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneIdentifier;
import org.monarchinitiative.exomiser.core.model.GeneScore;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.prioritisers.MockPriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.Strand;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class GeneScoreRankerTest {

    @Test
    public void mapGenesByGeneIdentifier() {
        var fgfr2 = TestFactory.newGeneFGFR2();
        var gnrhr2 = TestFactory.newGeneGNRHR2();
        AnalysisResults analysisResults = AnalysisResults.builder().genes(List.of(fgfr2, gnrhr2)).build();

        GeneScoreRanker instance = new GeneScoreRanker(analysisResults, OutputSettings.defaults());

        Map<GeneIdentifier, Gene> expected = Map.of(fgfr2.getGeneIdentifier(), fgfr2, gnrhr2.getGeneIdentifier(), gnrhr2);
        assertThat(instance.mapGenesByGeneIdentifier(), equalTo(expected));
    }

    @Test
    public void rankedGenes() {
        var fgfr2 = TestFactory.newGeneFGFR2();
        var topGeneScore = GeneScore.builder()
                .combinedScore(1.0)
                .geneIdentifier(fgfr2.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .build();
        fgfr2.setCompatibleInheritanceModes(Set.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        fgfr2.addGeneScore(topGeneScore);

        var gnrhr2 = TestFactory.newGeneGNRHR2();
        var secondGeneScore = GeneScore.builder()
                .combinedScore(0.9)
                .geneIdentifier(gnrhr2.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .build();
        gnrhr2.setCompatibleInheritanceModes(Set.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        gnrhr2.addGeneScore(secondGeneScore);

        AnalysisResults analysisResults = AnalysisResults.builder().genes(List.of(fgfr2, gnrhr2)).build();

        GeneScoreRanker instance = new GeneScoreRanker(analysisResults, OutputSettings.defaults());
        List<GeneScoreRanker.RankedGene> expected = List.of(
                new GeneScoreRanker.RankedGene(1, fgfr2, topGeneScore),
                new GeneScoreRanker.RankedGene(2, gnrhr2, secondGeneScore)
        );
        assertThat(instance.rankedGenes().collect(toList()), equalTo(expected));
    }

    @Test
    public void rankedGenesWithFailedVariant() {
        var fgfr2 = TestFactory.newGeneFGFR2();
        var topGeneScore = GeneScore.builder()
                .combinedScore(1.0)
                .geneIdentifier(fgfr2.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .build();
        fgfr2.setCompatibleInheritanceModes(Set.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        fgfr2.addGeneScore(topGeneScore);

        VariantEvaluation failedVariant = VariantEvaluation.builder()
                .variant(GenomeAssembly.HG19.getContigById(10), Strand.POSITIVE, CoordinateSystem.oneBased(), 1234567, "G", "T")
                .filterResults(FilterResult.fail(FilterType.FREQUENCY_FILTER))
                .build();
        fgfr2.addVariant(failedVariant);

        var failedGeneScore = GeneScore.builder()
                .combinedScore(0.0)
                .geneIdentifier(fgfr2.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.ANY)
                .build();


        // genes without a variant can have an MOI other than 'ANY'
        var gnrhr2 = TestFactory.newGeneGNRHR2();
        var passedVariant = VariantEvaluation.builder()
                .variant(GenomeAssembly.HG19.getContigById(1), Strand.POSITIVE, CoordinateSystem.oneBased(), 1234567, "G", "T")
                .filterResults(FilterResult.pass(FilterType.FREQUENCY_FILTER))
                .build();
        gnrhr2.addVariant(passedVariant);
        var secondGeneScore = GeneScore.builder()
                .combinedScore(0.9)
                .geneIdentifier(gnrhr2.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .contributingVariants(List.of(passedVariant))
                .build();
        gnrhr2.setCompatibleInheritanceModes(Set.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        gnrhr2.addGeneScore(secondGeneScore);

        AnalysisResults analysisResults = AnalysisResults.builder().genes(List.of(fgfr2, gnrhr2)).build();

        GeneScoreRanker instance = new GeneScoreRanker(analysisResults, OutputSettings.defaults());
        List<GeneScoreRanker.RankedGene> expected = List.of(
                new GeneScoreRanker.RankedGene(1, fgfr2, topGeneScore),
                new GeneScoreRanker.RankedGene(2, gnrhr2, secondGeneScore),
                new GeneScoreRanker.RankedGene(3, fgfr2, failedGeneScore)
        );
        assertThat(instance.rankedGenes().collect(toList()), equalTo(expected));
    }

    @Test
    public void rankedVariants() {
        var fgfr2 = TestFactory.newGeneFGFR2();
        var topGeneScore = GeneScore.builder()
                .combinedScore(1.0)
                .geneIdentifier(fgfr2.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .build();
        fgfr2.setCompatibleInheritanceModes(Set.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        fgfr2.addPriorityResult(new MockPriorityResult(PriorityType.HIPHIVE_PRIORITY, fgfr2.getEntrezGeneID(), fgfr2.getGeneSymbol(), 1.0));
        fgfr2.addGeneScore(topGeneScore);

        VariantEvaluation fgfr2TopRankedVariant = VariantEvaluation.builder()
                .variant(GenomeAssembly.HG19.getContigById(10), Strand.POSITIVE, CoordinateSystem.oneBased(), 1234567, "G", "T")
                .filterResults(FilterResult.pass(FilterType.FREQUENCY_FILTER))
                .compatibleInheritanceModes(Set.of(ModeOfInheritance.AUTOSOMAL_DOMINANT))
                .contributingModes(Set.of(ModeOfInheritance.AUTOSOMAL_DOMINANT))
                .build();
        fgfr2.addVariant(fgfr2TopRankedVariant);


        VariantEvaluation fgfr2FailedVariant = VariantEvaluation.builder()
                .variant(GenomeAssembly.HG19.getContigById(10), Strand.POSITIVE, CoordinateSystem.oneBased(), 1234567, "G", "T")
                .filterResults(FilterResult.fail(FilterType.FREQUENCY_FILTER))
                .compatibleInheritanceModes(Set.of(ModeOfInheritance.AUTOSOMAL_DOMINANT))
                .build();
        fgfr2.addVariant(fgfr2FailedVariant);

        var failedGeneScore = GeneScore.builder()
                .combinedScore(0.0)
                .geneIdentifier(fgfr2.getGeneIdentifier())
                .phenotypeScore(fgfr2.getPriorityScore())
                .modeOfInheritance(ModeOfInheritance.ANY)
                .build();


        var gnrhr2 = TestFactory.newGeneGNRHR2();
        var secondGeneScore = GeneScore.builder()
                .combinedScore(0.9)
                .geneIdentifier(gnrhr2.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .build();
        gnrhr2.setCompatibleInheritanceModes(Set.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        gnrhr2.addGeneScore(secondGeneScore);

        VariantEvaluation gnrhr2TopRankedVariant = VariantEvaluation.builder()
                .variant(GenomeAssembly.HG19.getContigById(7), Strand.POSITIVE, CoordinateSystem.oneBased(), 654321, "G", "T")
                .filterResults(FilterResult.pass(FilterType.FREQUENCY_FILTER))
                .compatibleInheritanceModes(Set.of(ModeOfInheritance.AUTOSOMAL_DOMINANT))
                .contributingModes(Set.of(ModeOfInheritance.AUTOSOMAL_DOMINANT))
                .build();
        gnrhr2.addVariant(gnrhr2TopRankedVariant);


        AnalysisResults analysisResults = AnalysisResults.builder().genes(List.of(fgfr2, gnrhr2)).build();

        GeneScoreRanker instance = new GeneScoreRanker(analysisResults, OutputSettings.defaults());
        var expected = List.of(
                new GeneScoreRanker.RankedVariant(1, fgfr2TopRankedVariant, topGeneScore),
                new GeneScoreRanker.RankedVariant(2, gnrhr2TopRankedVariant, secondGeneScore),
                new GeneScoreRanker.RankedVariant(3, fgfr2FailedVariant, failedGeneScore)
        );
        assertThat(instance.rankedVariants().collect(toList()), equalTo(expected));
    }

    @Test
    public void rankedContributingVariants() {
        var fgfr2 = TestFactory.newGeneFGFR2();
        var topGeneScore = GeneScore.builder()
                .combinedScore(1.0)
                .geneIdentifier(fgfr2.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .build();
        fgfr2.setCompatibleInheritanceModes(Set.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        fgfr2.addPriorityResult(new MockPriorityResult(PriorityType.HIPHIVE_PRIORITY, fgfr2.getEntrezGeneID(), fgfr2.getGeneSymbol(), 1.0));
        fgfr2.addGeneScore(topGeneScore);

        VariantEvaluation fgfr2TopRankedVariant = VariantEvaluation.builder()
                .variant(GenomeAssembly.HG19.getContigById(10),  Strand.POSITIVE, CoordinateSystem.oneBased(), 1234567, "G", "T")
                .filterResults(FilterResult.pass(FilterType.FREQUENCY_FILTER))
                .compatibleInheritanceModes(Set.of(ModeOfInheritance.AUTOSOMAL_DOMINANT))
                .contributingModes(Set.of(ModeOfInheritance.AUTOSOMAL_DOMINANT))
                .build();
        fgfr2.addVariant(fgfr2TopRankedVariant);


        VariantEvaluation fgfr2FailedVariant = VariantEvaluation.builder()
                .variant(GenomeAssembly.HG19.getContigById(10), Strand.POSITIVE, CoordinateSystem.oneBased(), 1234567, "G", "T")
                .filterResults(FilterResult.fail(FilterType.FREQUENCY_FILTER))
                .compatibleInheritanceModes(Set.of(ModeOfInheritance.AUTOSOMAL_DOMINANT))
                .build();
        fgfr2.addVariant(fgfr2FailedVariant);


        var gnrhr2 = TestFactory.newGeneGNRHR2();
        var secondGeneScore = GeneScore.builder()
                .combinedScore(0.9)
                .geneIdentifier(gnrhr2.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .build();
        gnrhr2.setCompatibleInheritanceModes(Set.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        gnrhr2.addGeneScore(secondGeneScore);

        VariantEvaluation gnrhr2TopRankedVariant = VariantEvaluation.builder()
                .variant(GenomeAssembly.HG19.getContigById(7), Strand.POSITIVE, CoordinateSystem.oneBased(), 654321, "G", "T")
                .filterResults(FilterResult.pass(FilterType.FREQUENCY_FILTER))
                .compatibleInheritanceModes(Set.of(ModeOfInheritance.AUTOSOMAL_DOMINANT))
                .contributingModes(Set.of(ModeOfInheritance.AUTOSOMAL_DOMINANT))
                .build();
        gnrhr2.addVariant(gnrhr2TopRankedVariant);


        AnalysisResults analysisResults = AnalysisResults.builder().genes(List.of(fgfr2, gnrhr2)).build();

        GeneScoreRanker instance = new GeneScoreRanker(analysisResults, OutputSettings.builder().outputContributingVariantsOnly(true).build());
        var expected = List.of(
                new GeneScoreRanker.RankedVariant(1, fgfr2TopRankedVariant, topGeneScore),
                new GeneScoreRanker.RankedVariant(2, gnrhr2TopRankedVariant, secondGeneScore)
        );
        assertThat(instance.rankedVariants().collect(toList()), equalTo(expected));
    }
}