/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.genome.dao;

import htsjdk.tribble.readers.TabixReader;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityScore;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.io.IOException;

/**
 * Class for querying test pathogenicity scores from a tabix file.
 *
 * The file should be defined as tab-delimited lines in 'VCF-lite' format with the corresponding requirements e.g.:
 *
 * #CHR   POS REF ALT SCORE
 * 10      123279555       G       A       0.947294473648071
 * 10      123279555       G       C       0.993917524814606
 * 10      123279555       G       T       0.980554223060608
 *
 * Where:
 * # is the comment character - lines starting with this will be ignored
 * CHR - Chromosomes must be in the form [1-22,X,Y,M].
 * POS - Positions must be 1-based.
 * ALT - alternate allele column must only contain one allele per line.
 * SCORE - Scores must be normalised to the range 0.0-1.0 such that 0.0 is considered benign and 1.0 pathogenic.
 *
 * Files can be created using the commands:
 * '$ bgzip file > file.gz'
 * '$ tabix -s1 -b2 -e2 -c# file.gz'
 *
 * @since 12.0.0
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class TestPathogenicityScoreDao implements PathogenicityDao {

    private static final Logger logger = LoggerFactory.getLogger(TestPathogenicityScoreDao.class);

    private final TabixDataSource tabixDataSource;

    public TestPathogenicityScoreDao(TabixDataSource tabixDataSource) {
        this.tabixDataSource = tabixDataSource;
    }

    @Caching(cacheable = {
            @Cacheable(cacheNames = "hg19.test_path", keyGenerator = "variantKeyGenerator", condition = "#variant.genomeAssembly == T(org.monarchinitiative.exomiser.core.genome.GenomeAssembly).HG19"),
            @Cacheable(cacheNames = "hg38.test_path", keyGenerator = "variantKeyGenerator", condition = "#variant.genomeAssembly == T(org.monarchinitiative.exomiser.core.genome.GenomeAssembly).HG38"),
    })
    @Override
    public PathogenicityData getPathogenicityData(Variant variant) {
        logger.debug("Getting TEST pathogenicity score for {}", variant);
        return processResults(variant);
    }

    private PathogenicityData processResults(Variant variant) {
        String chromosome = variant.getStartContigName();
        String ref = variant.getRef();
        String alt = variant.getAlt();
        int start = variant.getStart();

        return getPositionPathogenicityData(chromosome, start, ref, alt);
    }

    private PathogenicityData getPositionPathogenicityData(String chromosome, int start, String ref, String alt) {
        // Test pathogenicity score file defined as tab-delimited lines in 'VCF-lite' format:
        // it is expected that the scores be normalised such that 0.0 is considered benign and 1.0 pathogenic.
        // chr   pos ref alt score (range 0.0-1.0)
        // 1 12345   A   T   1.0  (an A->T SNP on chr1 at position 12345 with score of 1.0 (pathogenic))
        // 1 12345   A   TG   0.01  (an A->TG insertion on chr1 at position 12345 with score of 0.01 (benign))
        // note in the usual VCF format these would be on a single line
        // 1 12345   AT   G   0.9  (an AT->G deletion on chr1 at position 12345 with score of 0.9 (likely pathogenic))
        // 1 12345   T   .   0.3  (an T->. monomorphic site (no alt allele) on chr1 at position 12345 with score of 0.3 (likely benign))
        // non-autosomes
        // X 12345   AT   G   0.2  (an AT->G deletion on chrX at position 12345 with score of 0.2)
        // Y 12345   AT   G   0.2  (an AT->G deletion on chrY at position 12345 with score of 0.2)
        // M 12345   AT   G   0.2  (an AT->G deletion on chrM at position 12345 with score of 0.2)
        // this can be indexed using the bgzip and tabix commands:
        // bgzip outfile.tsv
        // tabix -s 1 -b 2 -e 2 -c# outfile.tsv.gz
        try {
            TabixReader.Iterator results = tabixDataSource.query(chromosome + ":" + start + "-" + start);
            String line;
            while ((line = results.next()) != null) {
                String[] elements = line.split("\t");
                String refField = elements[2];
                String altField = elements[3];
                if (refField.equals(ref) && altField.equals(alt)) {
                    return parsePathogenicityScore(elements[4]);
                }
            }
        } catch (IOException e) {
            logger.error("Unable to read from local frequency tabix file {}", tabixDataSource.getSource(), e);
        }
        return PathogenicityData.empty();
    }

    private PathogenicityData parsePathogenicityScore(String pathogenicityScore) {
        float value = Float.parseFloat(pathogenicityScore);
        PathogenicityScore testScore = PathogenicityScore.of(PathogenicitySource.TEST, value);
        return PathogenicityData.of(testScore);
    }
}
