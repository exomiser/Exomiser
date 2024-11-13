package org.monarchinitiative.exomiser.core.model;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;

import static de.charite.compbio.jannovar.annotation.VariantEffect.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class GeneStatisticsTest {

    @Test
    void testBuilderNoStats() {
        var instance = GeneStatistics.builder("WIBBLE").build();
        assertThat(instance.geneSymbol(), equalTo("WIBBLE"));
        assertThat(instance.pathCount(), equalTo(0));
        assertThat(instance.vusCount(), equalTo(0));
        assertThat(instance.benignCount(), equalTo(0));
    }


    @Test
    void testBuilderStats() {
        var instance = GeneStatistics.builder("WIBBLE")
                .put(VariantEffect.MISSENSE_VARIANT, ClinVarData.ClinSig.PATHOGENIC)
                .put(VariantEffect.MISSENSE_VARIANT, ClinVarData.ClinSig.LIKELY_PATHOGENIC)
                .put(VariantEffect.MISSENSE_VARIANT, ClinVarData.ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC)
                .put(VariantEffect.MISSENSE_VARIANT, ClinVarData.ClinSig.UNCERTAIN_SIGNIFICANCE)
                .put(VariantEffect.MISSENSE_VARIANT, ClinVarData.ClinSig.UNCERTAIN_SIGNIFICANCE)
                .put(VariantEffect.MISSENSE_VARIANT, ClinVarData.ClinSig.BENIGN)
                // LOF effects
                .put(VariantEffect.STOP_GAINED, ClinVarData.ClinSig.PATHOGENIC)
                .put(VariantEffect.STOP_LOST, ClinVarData.ClinSig.PATHOGENIC)
                .put(VariantEffect.SPLICE_ACCEPTOR_VARIANT, ClinVarData.ClinSig.PATHOGENIC)
                .put(VariantEffect.SPLICE_DONOR_VARIANT, ClinVarData.ClinSig.PATHOGENIC)
                .put(VariantEffect.EXON_LOSS_VARIANT, ClinVarData.ClinSig.PATHOGENIC)
                .put(VariantEffect.START_LOST, ClinVarData.ClinSig.PATHOGENIC)
                .put(VariantEffect.START_LOST, ClinVarData.ClinSig.UNCERTAIN_SIGNIFICANCE)
                .put(VariantEffect.TRANSCRIPT_ABLATION, ClinVarData.ClinSig.PATHOGENIC)
                .put(VariantEffect.FRAMESHIFT_ELONGATION, ClinVarData.ClinSig.PATHOGENIC)
                .put(VariantEffect.FRAMESHIFT_TRUNCATION, ClinVarData.ClinSig.PATHOGENIC)
                .put(VariantEffect.FRAMESHIFT_VARIANT, ClinVarData.ClinSig.PATHOGENIC)
                // other effect
                .put(VariantEffect.INTRON_VARIANT, ClinVarData.ClinSig.BENIGN)
                .build();
        assertThat(instance.geneSymbol(), equalTo("WIBBLE"));
        assertThat(instance.pathCount(), equalTo(13));
        assertThat(instance.vusCount(), equalTo(3));
        assertThat(instance.benignCount(), equalTo(2));

        assertThat(instance.missensePathCount(), equalTo(3));
        assertThat(instance.missenseVusCount(), equalTo(2));
        assertThat(instance.missenseBenignCount(), equalTo(1));

        assertThat(instance.lofPathCount(), equalTo(10));
        assertThat(instance.lofVusCount(), equalTo(1));
        assertThat(instance.lofBenignCount(), equalTo(0));
    }

    @Test
    void testIgnoresNonPathVusBenignStatus() {
        var instance = GeneStatistics.builder("WIBBLE", MISSENSE_VARIANT, ClinVarData.ClinSig.ESTABLISHED_RISK_ALLELE).build();
        assertThat(instance.pathCount(), equalTo(0));
        assertThat(instance.vusCount(), equalTo(0));
        assertThat(instance.benignCount(), equalTo(0));

    }

    @Test
    void testToString() {
        var instance = GeneStatistics.builder("WIBBLE")
                .put(VariantEffect.MISSENSE_VARIANT, ClinVarData.ClinSig.PATHOGENIC)
                .put(VariantEffect.MISSENSE_VARIANT, ClinVarData.ClinSig.LIKELY_PATHOGENIC)
                .put(VariantEffect.MISSENSE_VARIANT, ClinVarData.ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC)
                .put(VariantEffect.MISSENSE_VARIANT, ClinVarData.ClinSig.UNCERTAIN_SIGNIFICANCE)
                .put(VariantEffect.MISSENSE_VARIANT, ClinVarData.ClinSig.UNCERTAIN_SIGNIFICANCE)
                .put(VariantEffect.MISSENSE_VARIANT, ClinVarData.ClinSig.BENIGN)
                .put(VariantEffect.STOP_GAINED, ClinVarData.ClinSig.PATHOGENIC)
                .put(VariantEffect.INTRON_VARIANT, ClinVarData.ClinSig.BENIGN)
                .build();

        var expected = "GeneStatistics{geneSymbol=WIBBLE, counts={STOP_GAINED=[P=1, VUS=0, B=0], MISSENSE_VARIANT=[P=3, VUS=2, B=1], INTRON_VARIANT=[P=0, VUS=0, B=1]}}";
        assertThat(instance.toString(), equalTo(expected));
    }

    @Test
    void testEmptyString() {
        assertThat(GeneStatistics.builder("WIBBLE").build().toString(), equalTo("GeneStatistics{geneSymbol=WIBBLE, counts={}}"));
    }

    @Test
    void testCanGetStatsForArbitraryVariantEffect() {
        var instance = GeneStatistics.builder("WIBBLE")
                .put(SYNONYMOUS_VARIANT, ClinVarData.ClinSig.UNCERTAIN_SIGNIFICANCE)
                .build();

        assertThat(instance.vusCount(), equalTo(1));
        assertThat(instance.vusCount(SYNONYMOUS_VARIANT), equalTo(instance.vusCount()));
    }

    @Test
    void testCanGetStatsForArbitraryVariantEffects() {
        var instance = GeneStatistics.builder("WIBBLE")
                .put(FIVE_PRIME_UTR_EXON_VARIANT, ClinVarData.ClinSig.UNCERTAIN_SIGNIFICANCE)
                .put(INTRON_VARIANT, ClinVarData.ClinSig.UNCERTAIN_SIGNIFICANCE)
                .put(MISSENSE_VARIANT, ClinVarData.ClinSig.PATHOGENIC)
                .build();

        assertThat(instance.vusCount(), equalTo(2));
        assertThat(instance.vusCount(FIVE_PRIME_UTR_EXON_VARIANT, INTRON_VARIANT, MISSENSE_VARIANT), equalTo(instance.vusCount()));
        assertThat(instance.vusCount(FIVE_PRIME_UTR_EXON_VARIANT, INTRON_VARIANT), equalTo(instance.vusCount()));
        assertThat(instance.pathCount(FIVE_PRIME_UTR_EXON_VARIANT, INTRON_VARIANT, MISSENSE_VARIANT), equalTo(instance.pathCount()));
        assertThat(instance.pathCount(MISSENSE_VARIANT), equalTo(instance.pathCount()));
    }
}