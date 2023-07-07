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

package org.monarchinitiative.exomiser.core.genome;

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegionIndex;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneIdentifier;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.Coordinates;
import org.monarchinitiative.svart.Strand;

import java.nio.file.Path;
import java.util.List;

/**
 * Allows the easy creation of {@link JannovarData} {@link VariantFactoryImpl} objects for testing.
 *
 * The default data contains one transcript each for the genes FGFR2, GNRHR2A, RBM8A (overlaps with GNRHR2A), and SHH based on the HG19/GRCh37 build.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TestFactory {

    private static final ReferenceDictionary DEFAULT_REF_DICT = HG19RefDictBuilder.build();

    private static final TranscriptModel tmFGFR2 = TestTranscriptModelFactory.buildTMForFGFR2();
    private static final TranscriptModel tmGNRHR2A = TestTranscriptModelFactory.buildTMForGNRHR2A();
    private static final TranscriptModel tmRBM8A = TestTranscriptModelFactory.buildTMForRBM8A();
    private static final TranscriptModel tmSHH = TestTranscriptModelFactory.buildTMForSHH();

    private static final GenomeAssembly DEFAULT_GENOME_ASSEMBLY = GenomeAssembly.HG19;
    private static final JannovarData DEFAULT_JANNOVAR_DATA = new JannovarData(DEFAULT_REF_DICT, ImmutableList.of(tmFGFR2, tmGNRHR2A, tmRBM8A, tmSHH));
    private static final GeneFactory DEFAULT_GENE_FACTORY = new GeneFactory(DEFAULT_JANNOVAR_DATA);
    private static final VariantAnnotator DEFAULT_VARIANT_ANNOTATOR = new JannovarVariantAnnotator(DEFAULT_GENOME_ASSEMBLY, DEFAULT_JANNOVAR_DATA, ChromosomalRegionIndex
            .empty());

    private TestFactory() {
        //this class should be used in a static context.
    }

    public static GenomeAssembly getDefaultGenomeAssembly() {
        return DEFAULT_GENOME_ASSEMBLY;
    }

    public static ReferenceDictionary getDefaultRefDict() {
        return DEFAULT_REF_DICT;
    }

    public static JannovarData buildDefaultJannovarData() {
        return DEFAULT_JANNOVAR_DATA;
    }

    public static VariantAnnotator buildDefaultVariantAnnotator() {
        return DEFAULT_VARIANT_ANNOTATOR;
    }

    public static VariantFactory buildDefaultVariantFactory(Path vcfPath) {
        return new VariantFactoryImpl(DEFAULT_VARIANT_ANNOTATOR, new VcfFileReader(vcfPath));
    }

    public static VariantFactory buildDefaultVariantFactory(VcfReader vcfReader) {
        return new VariantFactoryImpl(DEFAULT_VARIANT_ANNOTATOR, vcfReader);
    }

    public static JannovarVariantAnnotator buildJannovarVariantAnnotator(TranscriptModel... transcriptModels) {
        JannovarData jannovarData = buildJannovarData(transcriptModels);
        return new JannovarVariantAnnotator(DEFAULT_GENOME_ASSEMBLY, jannovarData, ChromosomalRegionIndex.empty());
    }

    public static JannovarData buildJannovarData(TranscriptModel... transcriptModels) {
        return new JannovarData(DEFAULT_REF_DICT, ImmutableList.copyOf(transcriptModels));
    }

    public static GeneFactory buildDefaultGeneFactory() {
        return DEFAULT_GENE_FACTORY;
    }

    public static List<GeneIdentifier> buildGeneIdentifiers() {
        return TestGeneFactory.buildGeneIdentifiers();
    }

    public static List<Gene> buildGenes() {
        return TestGeneFactory.buildGenes();
    }

    public static Gene newGeneFGFR2() { return new Gene(TestGeneFactory.FGFR2_IDENTIFIER);}

    public static Gene newGeneGNRHR2() { return new Gene(TestGeneFactory.GNRHR2_IDENTIFIER);}

    public static Gene newGeneRBM8A() { return new Gene(TestGeneFactory.RBM8A_IDENTIFIER);}

    public static Gene newGeneSHH() { return new Gene(TestGeneFactory.SHH_IDENTIFIER);}

    public static GenomeDataService buildDefaultGenomeDataService() {
        return TestGenomeDataService.builder().genes(buildGenes()).geneIdentifiers(buildGeneIdentifiers()).build();
    }

    public static GenomeAnalysisService buildDefaultHg19GenomeAnalysisService() {
        return new GenomeAnalysisServiceImpl(DEFAULT_GENOME_ASSEMBLY, buildDefaultGenomeDataService(), TestVariantDataService.stub(), DEFAULT_VARIANT_ANNOTATOR);
    }

    public static GenomeAnalysisService buildStubGenomeAnalysisService(GenomeAssembly genomeAssembly) {
        return new GenomeAnalysisServiceImpl(genomeAssembly, buildDefaultGenomeDataService(), TestVariantDataService.stub(), new JannovarVariantAnnotator(genomeAssembly, DEFAULT_JANNOVAR_DATA, ChromosomalRegionIndex.empty()));
    }

    public static VariantEvaluation.Builder variantBuilder(int chr, int pos, String ref, String alt) {
        return VariantEvaluation.builder()
                .variant(DEFAULT_GENOME_ASSEMBLY.getContigById(chr), Strand.POSITIVE, CoordinateSystem.ONE_BASED, pos, ref, alt)
                .genomeAssembly(DEFAULT_GENOME_ASSEMBLY);
    }

    public static VariantEvaluation.Builder variantBuilder(int chr, int start, int end, String ref, String alt, int changeLength) {
        return VariantEvaluation.builder()
                .variant(DEFAULT_GENOME_ASSEMBLY.getContigById(chr), Strand.POSITIVE, Coordinates.oneBased(start, end), ref, alt, changeLength)
                .genomeAssembly(DEFAULT_GENOME_ASSEMBLY);
    }
}
