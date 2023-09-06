package org.monarchinitiative.exomiser.data.genome;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.MVStoreTool;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.data.genome.indexers.AlleleConverter;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.exomiser.data.genome.model.BuildInfo;
import org.monarchinitiative.exomiser.data.genome.model.resource.ClinVarAlleleResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;


public class ClinVarBuildRunner {

    private static final Logger logger = LoggerFactory.getLogger(ClinVarBuildRunner.class);

    private final Path outDir;
    private final BuildInfo buildInfo;
    private final ClinVarAlleleResource clinVarAlleleResource;
    private final Path outFile;

    public ClinVarBuildRunner(BuildInfo buildInfo, Path outDir, ClinVarAlleleResource clinVarAlleleResource) {
        this.outDir = outDir.toAbsolutePath();
        this.buildInfo = buildInfo;
        this.clinVarAlleleResource = clinVarAlleleResource;
        outFile = outDir.toAbsolutePath().resolve(buildInfo.getBuildString() + "_clinvar.mv.db");
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
                            var alleleKey = AlleleConverter.toAlleleKey(allele);
                            var clinvarProto = AlleleConverter.toProtoClinVar(allele.getClinVarData());
                            clinVarMap.put(alleleKey, clinvarProto);
                        });
            }
            logger.info("Wrote {} ClinVar records", clinVarMap.size());
            clinvarStore.commit();
            clinvarStore.compactMoveChunks();
        }
        logger.info("Compacting MVStore");
        MVStoreTool.compact(outFileName, true);
    }
}