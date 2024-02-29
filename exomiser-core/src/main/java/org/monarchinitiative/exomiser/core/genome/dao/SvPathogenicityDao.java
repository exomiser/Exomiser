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

package org.monarchinitiative.exomiser.core.genome.dao;

import org.monarchinitiative.exomiser.core.model.SvMetaType;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityScore;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.svart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class SvPathogenicityDao implements PathogenicityDao {

    private static final Logger logger = LoggerFactory.getLogger(SvPathogenicityDao.class);

    private final DataSource svDataSource;
    private final double minSimilarity = 0.80;

    public SvPathogenicityDao(DataSource svDataSource) {
        this.svDataSource = svDataSource;
    }

    @Caching(cacheable = {
            @Cacheable(cacheNames = "hg19.sv.path", keyGenerator = "variantKeyGenerator", condition = "#variant.genomeAssembly == T(org.monarchinitiative.exomiser.core.genome.GenomeAssembly).HG19"),
            @Cacheable(cacheNames = "hg38.sv.path", keyGenerator = "variantKeyGenerator", condition = "#variant.genomeAssembly == T(org.monarchinitiative.exomiser.core.genome.GenomeAssembly).HG38"),
    })
    @Override
    public PathogenicityData getPathogenicityData(Variant variant) {
        logger.debug("{}", variant);
        List<SvResult> results = runQuery(variant);
        results.forEach(svResult -> logger.debug("{}", svResult));

        Map<Double, List<SvResult>> resultsByScore = results.stream()
                .collect(Collectors.groupingBy(svResult -> SvDaoUtil.jaccard(variant, svResult)));

        List<SvResult> topMatches = resultsByScore.entrySet()
                .stream()
                .max(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .orElse(List.of());

        logger.debug("Top match(es)");
        topMatches.forEach(svResult -> logger.debug("{}", svResult));

        return mapToPathogenicityData(topMatches);
    }

    private PathogenicityData mapToPathogenicityData(List<SvResult> topMatches) {
        if (topMatches.isEmpty()) {
            return PathogenicityData.empty();
        }
        SvResult first = topMatches.get(0);
        ClinVarData clinVarData = first.clinVarData;

        List<PathogenicityScore> pathogenicityScores = topMatches.stream()
                .map(this::toPathScore)
                .collect(toList());

        return PathogenicityData.of(clinVarData, pathogenicityScores);
    }

    private PathogenicityScore toPathScore(SvResult svResult) {
        ClinVarData clinVarData = svResult.clinVarData;
        float score = mapClinSigToScore(clinVarData.getPrimaryInterpretation());
        return PathogenicityScore.of(PathogenicitySource.CLINVAR, score);
    }

    private float mapClinSigToScore(ClinVarData.ClinSig primaryInterpretation) {
        return switch (primaryInterpretation) {
            case PATHOGENIC -> 1.0f;
            case PATHOGENIC_OR_LIKELY_PATHOGENIC -> 0.9f;
            case LIKELY_PATHOGENIC -> 0.8f;
            case UNCERTAIN_SIGNIFICANCE -> 0.6f;
            default -> 0f;
        };
    }

    private List<SvResult> runQuery(Variant variant) {
        String query = "SELECT " +
                "       CHROMOSOME,\n" +
                "       START,\n" +
                "       \"end\",\n" +
                "       CHANGE_LENGTH,\n" +
                "       VARIANT_TYPE,\n" +
                "       DBVAR_ID,\n" +
                "       SOURCE,\n" +
                "       RCV_ID,\n" +
                "       VARIATION_ID,\n" +
                "       CLIN_SIG,\n" +
                "       CLIN_REV_STAT\n" +
                "FROM SV_PATH \n" +
                "WHERE CHROMOSOME = ?\n" +
                "  and START >= ?\n" +
                "  and START <= ?\n" +
                "  and \"end\" >= ?\n" +
                "  and \"end\" <= ?\n" +
                "  and CLIN_SIG != 'UNKNOWN';";

        try (
                Connection connection = svDataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(query)
        ) {

            SvDaoBoundaryCalculator svDaoBoundaryCalculator = new SvDaoBoundaryCalculator(variant, minSimilarity);

            int startMin = svDaoBoundaryCalculator.startMin();
            int startMax = svDaoBoundaryCalculator.startMax();

            int endMin = svDaoBoundaryCalculator.endMin();
            int endMax = svDaoBoundaryCalculator.endMax();

            logger.debug("SELECT * FROM SV_PATH WHERE CHROMOSOME = {} AND START >= {} and START <= {} and \"end\" >= {} and \"end\" <= {};",
                    variant.contigId(),
                    startMin, startMax,
                    endMin, endMax
            );

            ps.setInt(1, variant.contigId());
            ps.setInt(2, startMin);
            ps.setInt(3, startMax);
            ps.setInt(4, endMin);
            ps.setInt(5, endMax);

            ResultSet rs = ps.executeQuery();

// consider also complex types where CHR_ONE != CHR_TWO - there are only about 600 in gnomad and gonl combined.
            return processSvResults(rs, variant);
        } catch (SQLException e) {
            logger.error("", e);
        }
        return List.of();
    }

    private List<SvResult> processSvResults(ResultSet rs, Variant variant) throws SQLException {
        List<SvResult> results = new ArrayList<>();
        while (rs.next()) {
            int chr = rs.getInt("CHROMOSOME");
            int start = rs.getInt("START");
            int end = rs.getInt("end");
            int length = rs.getInt("CHANGE_LENGTH");
            String svType = rs.getString("VARIANT_TYPE");
            String source = rs.getString("SOURCE");
            String id = rs.getString("RCV_ID");
            String variationId = rs.getString("VARIATION_ID");
            String clinSig = rs.getString("CLIN_SIG");
            String clinRevStat = rs.getString("CLIN_REV_STAT");

            VariantType variantType = VariantType.valueOf(svType);
            // n.b there are only 4 INS entries in the 2109 pathogenicity_sv table (all pathogenic), but the
            // lengths all == 2, so there isn't any awkward changeLength fiddling required here.
            if (SvMetaType.isEquivalent(variant.variantType(), variantType)) {
                ClinVarData.ClinSig sig = ClinVarData.ClinSig.valueOf(clinSig);
                ClinVarData.ReviewStatus reviewStatus = ClinVarData.ReviewStatus.valueOf(clinRevStat);
                ClinVarData clinVarData = ClinVarData.builder()
                        .variationId(variationId)
                        .primaryInterpretation(sig)
                        .reviewStatus(reviewStatus)
                        .build();
                SvResult svResult = SvResult.of(variant.contig(), start, end, length, variantType, source, id, clinVarData, variationId);
                results.add(svResult);
            }
        }
        return results;
    }

    private static class SvResult extends BaseGenomicVariant<SvResult> {

        private final String source;
        private final ClinVarData clinVarData;
        private final String clinVarAccession;

        private SvResult(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength, String source, ClinVarData clinVarData, String clinVarAccession) {
            super(contig, id, strand, coordinates, ref, alt, changeLength, "", "");
            this.source = source;
            this.clinVarData = clinVarData;
            this.clinVarAccession = clinVarAccession;
        }

        public static SvResult of(Contig contig, int start, int end, int changeLength, VariantType variantType, String source, String id, ClinVarData clinVarData, String clinvarAccession) {
            String alt = '<' + variantType.toString().replace("_", ":") + '>';
            return new SvResult(contig, id, Strand.POSITIVE, Coordinates.oneBased(start, end), "", alt, changeLength, source, clinVarData, clinvarAccession);
        }

        @Override
        protected SvResult newVariantInstance(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength, String mateId, String eventId) {
            return new SvResult(contig, id, strand, coordinates, ref, alt, changeLength, source, clinVarData, clinVarAccession);
        }

        @Override
        public String toString() {
            return "SvResult{" +
                    "chr=" + contigName() +
                    ", start=" + start() +
                    ", end=" + end() +
                    ", length=" + length() +
                    ", changeLength=" + changeLength() +
                    ", svType='" + variantType() + '\'' +
                    ", source='" + source + '\'' +
                    ", id='" + id() + '\'' +
                    ", clinVarData=" + clinVarData +
                    ", clinVarAccession=" + clinVarAccession +
                    '}';
        }
    }
}
