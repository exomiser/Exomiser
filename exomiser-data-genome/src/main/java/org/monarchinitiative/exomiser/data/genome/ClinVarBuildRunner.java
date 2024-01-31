package org.monarchinitiative.exomiser.data.genome;

import de.charite.compbio.jannovar.data.JannovarData;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.MVStoreTool;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.JannovarVariantAnnotator;
import org.monarchinitiative.exomiser.core.genome.VariantAnnotator;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegionIndex;
import org.monarchinitiative.exomiser.core.model.TranscriptAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.data.genome.indexers.AlleleConverter;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.exomiser.data.genome.model.BuildInfo;
import org.monarchinitiative.exomiser.data.genome.model.resource.ClinVarAlleleResource;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.Coordinates;
import org.monarchinitiative.svart.GenomicVariant;
import org.monarchinitiative.svart.Strand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


public class ClinVarBuildRunner {

    private static final Logger logger = LoggerFactory.getLogger(ClinVarBuildRunner.class);

    private final Path outDir;
    private final BuildInfo buildInfo;
    private final ClinVarAlleleResource clinVarAlleleResource;
    private final Path outFile;
    private final VariantAnnotator variantAnnotator;
    private final GenomeAssembly genomeAssembly;

    public ClinVarBuildRunner(BuildInfo buildInfo, Path outDir, ClinVarAlleleResource clinVarAlleleResource, JannovarData jannovarData) {
        this.outDir = outDir.toAbsolutePath();
        this.buildInfo = buildInfo;
        this.clinVarAlleleResource = clinVarAlleleResource;
        this.outFile = outDir.toAbsolutePath().resolve(buildInfo.getBuildString() + "_clinvar.mv.db");
        genomeAssembly = buildInfo.getAssembly();
        variantAnnotator = new JannovarVariantAnnotator(genomeAssembly, jannovarData, ChromosomalRegionIndex.empty());
    }

    public Path getOutFile() {
        return outFile;
    }

    public void run() {
        String outFileName = outFile.toString();
        try {
            // we don't want to accidentally add to an existing file
            Files.deleteIfExists(outFile);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to delete pre-existing ClinVar file: " + outFileName, e);
        }
        logger.info("Writing ClinVar data to {}", outFileName);
        try (MVStore clinvarStore = MVStore.open(outFileName)) {
            MVMap<AlleleProto.AlleleKey, AlleleProto.ClinVar> clinVarMap = MvStoreUtil.openClinVarMVMap(clinvarStore);
            try (Stream<Allele> alleleStream = clinVarAlleleResource.parseResource()) {
                alleleStream
                        .forEach(allele -> {
                            ClinVarData clinVarData = annotateClinvar(allele);
                            allele.setClinVarData(clinVarData);
                            logger.debug("{}-{}-{}-{} {}", allele.getChr(), allele.getPos(), allele.getRef(), allele.getAlt(), clinVarData);
                            var alleleKey = AlleleConverter.toAlleleKey(allele);
                            var clinvarProto = AlleleConverter.toProtoClinVar(clinVarData);
                            clinVarMap.put(alleleKey, clinvarProto);
                        });
            }
            logger.info("Wrote {} ClinVar records", clinVarMap.size());
            clinvarStore.commit();
        }
        logger.info("Compacting MVStore");
        MVStoreTool.compact(outFileName, true);
    }

    private ClinVarData annotateClinvar(Allele allele) {
        GenomicVariant genomicVariant = GenomicVariant.of(genomeAssembly.getContigById(allele.getChr()), Strand.POSITIVE, Coordinates.ofAllele(CoordinateSystem.ONE_BASED, allele.getPos(), allele.getRef()), allele.getRef(), allele.getAlt());
        List<VariantAnnotation> variantAnnotations = variantAnnotator.annotate(genomicVariant);
        if (!variantAnnotations.isEmpty()) {
            VariantAnnotation variantAnnotation = variantAnnotations.get(0);
            if (!variantAnnotation.getTranscriptAnnotations().isEmpty()) {
                TranscriptAnnotation transcriptAnnotation = variantAnnotation.getTranscriptAnnotations().get(0);
                return allele.getClinVarData()
                        .toBuilder()
                        .geneSymbol(variantAnnotation.getGeneSymbol())
                        .variantEffect(variantAnnotation.getVariantEffect())
                        .hgvsCdna(transcriptAnnotation == null ? "" : transcriptAnnotation.getHgvsCdna())
                        .hgvsProtein(transcriptAnnotation == null ? "" : transcriptAnnotation.getHgvsProtein())
                        .build();
            }
        }
        return allele.getClinVarData();
    }
}
