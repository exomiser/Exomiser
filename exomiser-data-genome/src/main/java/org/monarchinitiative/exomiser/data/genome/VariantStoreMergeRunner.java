package org.monarchinitiative.exomiser.data.genome;

import org.monarchinitiative.exomiser.data.genome.model.BuildInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for orchestrating merging variant MVStores into the final release variant store. This will search for .mv.db
 * files contained in the processedDir and merge them in turn into a final compacted {buildString}_variants.mv.db file
 * for that release in the buildPath. e.g. 2211/2211_hg19/2211_hg19_variants.mv.db
 *
 * @param buildInfo     {@link BuildInfo} class containing the assembly and version for the release
 * @param buildPath     Path to the final output directory for the variants.mv.db file to be built
 * @param processedDir  Path to the directory containing the variant mv.db files to merge
 * @since 14.0.0
 */
public record VariantStoreMergeRunner(BuildInfo buildInfo, Path buildPath, Path processedDir) {

    private static final Logger logger = LoggerFactory.getLogger(VariantStoreMergeRunner.class);

    /**
     * combines all variants in buildDir/processed/*.mv into the final %buildString_variants.mv.db
     */
    // got a bunch of pre-precessed allele.mv.db you want to merge into a final store? Use this command.
    public void run() {
        Path target = buildPath.resolve(buildInfo.getBuildString() + "_variants.mv.db");
        List<Path> mvStoresToMerge = mvStoresToMerge(processedDir, "*.mv.db");
        logger.info("Merging {} from directory {} into final store: {}", mvStoresToMerge.stream().map(Path::getFileName).toList(), processedDir, target);
        VariantMvStores.mergeStores(target, mvStoresToMerge);
    }

    private List<Path> mvStoresToMerge(Path processedDir, String glob) {
        List<Path> found = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(processedDir, glob)) {
            for (Path path : directoryStream) {
                found.add(path);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return List.copyOf(found);
    }

}
