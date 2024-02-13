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
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class SvFrequencyDao implements FrequencyDao {

    private static final Logger logger = LoggerFactory.getLogger(SvFrequencyDao.class);

    private final DataSource svDataSource;
    private final double minSimilarity = 0.80;

    public SvFrequencyDao(DataSource svDataSource) {
        this.svDataSource = svDataSource;
    }

    @Caching(cacheable = {
            @Cacheable(cacheNames = "hg19.sv.freq", keyGenerator = "variantKeyGenerator", condition = "#variant.genomeAssembly == T(org.monarchinitiative.exomiser.core.genome.GenomeAssembly).HG19"),
            @Cacheable(cacheNames = "hg38.sv.freq", keyGenerator = "variantKeyGenerator", condition = "#variant.genomeAssembly == T(org.monarchinitiative.exomiser.core.genome.GenomeAssembly).HG38"),
    })
    @Override
    public FrequencyData getFrequencyData(Variant variant) {
        logger.debug("{}", variant);
        List<SvResult> results = runQuery(variant);
        results.forEach(svResult -> logger.debug("{}, jaccard={}, jaccardChangeLength={}, score={}", svResult, SvDaoUtil.jaccard(variant, svResult), SvDaoUtil.jaccard(variant.changeLength(), svResult.changeLength()), score(variant).apply(svResult)));

        Map<Double, List<SvResult>> resultsByScore = results.stream()
                .collect(Collectors.groupingBy(score(variant)));

        List<SvResult> topMatches = resultsByScore.entrySet()
                .stream()
                .max(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .orElse(List.of());

        logger.debug("Top match(es)");
        topMatches.forEach(svResult -> logger.debug("{}, jaccard={}, jaccardChangeLength={}, score={}", svResult, SvDaoUtil.jaccard(variant, svResult), SvDaoUtil.jaccard(variant.changeLength(), svResult.changeLength()), score(variant).apply(svResult)));

        return mapToFrequencyData(topMatches);
    }

    private Function<SvResult, Double> score(Variant variant) {
        // geometric mean of num alleles and similarity - try and get the best represented and most similar allele
        return svResult -> Math.sqrt(svResult.an * SvDaoUtil.jaccard(variant, svResult));
    }

    private boolean isInsertion(Variant variant) {
        return variant.variantType().baseType().equals(VariantType.INS);
    }

    private FrequencyData mapToFrequencyData(List<SvResult> topMatches) {
        if (topMatches.isEmpty()) {
            return FrequencyData.empty();
        }
        SvResult first = topMatches.get(0);
        Frequency frequency = toFrequency(first);
        if (first.an < 10 || frequency.frequency() == 0) {
            // Don't report poorly defined frequencies
            return FrequencyData.of(first.id());
        }
        return FrequencyData.of(first.id(), frequency);
    }

    private Frequency toFrequency(SvResult svResult) {
        FrequencySource frequencySource = frequencySource(svResult);
        return Frequency.of(frequencySource, svResult.af);
    }

    private FrequencySource frequencySource(SvResult first) {
        return switch (first.source) {
            case "GNOMAD-SV" -> FrequencySource.GNOMAD_SV;
            case "DBVAR" -> FrequencySource.DBVAR;
            case "DGV" -> FrequencySource.DGV;
            case "GONL" -> FrequencySource.GONL;
            case "DECIPHER" -> FrequencySource.DECIPHER;
            default -> FrequencySource.UNKNOWN;
        };
    }

    private List<SvResult> runQuery(Variant variant) {
        String query =
                "SELECT " +
                        "       CHROMOSOME,\n" +
                        "       START,\n" +
                        "       \"end\",\n" +
                        "       CHANGE_LENGTH,\n" +
                        "       VARIANT_TYPE,\n" +
                        "       DBVAR_ID,\n" +
                        "       SOURCE,\n" +
                        "       SOURCE_ID,\n" +
                        "       ALLELE_COUNT,\n" +
                        "       ALLELE_NUMBER\n" +
                        "FROM SV_FREQ\n" +
                        "WHERE CHROMOSOME = ?\n" +
                        "  and START >= ?\n" +
                        "  and START <= ?\n" +
                        "  and \"end\" >= ?\n" +
                        "  and \"end\" <= ?\n" +
                        "  and ALLELE_COUNT != 0;";
        try (
                Connection connection = svDataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(query)
        ) {

            SvDaoBoundaryCalculator svDaoBoundaryCalculator = new SvDaoBoundaryCalculator(variant, minSimilarity);

            int startMin = svDaoBoundaryCalculator.startMin();
            int startMax = svDaoBoundaryCalculator.startMax();

            int endMin = svDaoBoundaryCalculator.endMin();
            int endMax = svDaoBoundaryCalculator.endMax();

            logger.debug("SELECT * FROM SV_FREQ WHERE CHROMOSOME = {} AND START >= {} and START <= {} and \"end\" >= {} and \"end\" <= {};",
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

//            SOURCE	CHR_ONE	POS_ONE	POS_TWO	SV_LEN	SV_TYPE	ID	AC	AF
//            GNOMAD_SV	7	4972268	4973271	1003	DEL	gnomAD_v2_DEL_7_90956	94	0.004377
//            DBVAR	7	4972268	4973271	-1004	DEL	nssv15404135	94	0.004377
//            DGV_VARIANTS	7	4972233	4973326	1093	CNV	esv2659019	1151	0
//            DGV_VARIANTS	7	4972258	4973286	1028	CNV	esv3611958	2504	0
//            DECIPHER_CNV	7	4972251	4973293	1042	DEL	23156	94	0.049682875

        List<SvResult> results = new ArrayList<>();
        while (rs.next()) {
            String source = rs.getString("SOURCE");
            int chr = rs.getInt("CHROMOSOME");
            int start = rs.getInt("START");
            int end = rs.getInt("end");
            int changeLength = rs.getInt("CHANGE_LENGTH");
            String svType = rs.getString("VARIANT_TYPE");
            String id = rs.getString("DBVAR_ID");
            int ac = rs.getInt("ALLELE_COUNT");
            int an = rs.getInt("ALLELE_NUMBER");

            VariantType variantType = VariantType.valueOf(svType);
            // there are cases such as INS_ME which won't match the database so we have to filter these here
            // consider also DEL/CNV_LOSS INS/CNV_GAIN/DUP/INS_ME and CNV
            changeLength = checkChangeLength(variantType, start, end, changeLength);

            if (SvMetaType.isEquivalent(variant.variantType(), variantType)) {
                SvResult svResult = SvResult.of(variant.contig(), start, end, changeLength, variantType, id == null ? "" : id, source, ac, an);
                if (isInsertion(variant)) {
                    if (changeLength >= 20 && SvDaoUtil.jaccard(variant.changeLength(), svResult.changeLength()) >= 0.75) {
                        results.add(svResult);
                    }
                    // both too short to apply similarity cutoff
                    if (changeLength < 20 && variant.changeLength() < 20) {
                        results.add(svResult);
                    }
                } else {
                    results.add(svResult);
                }
            }
        }
        return results;
    }

    private static int checkChangeLength(VariantType variantType, int start, int end, int changeLength) {
        if (variantType == VariantType.CNV) {
            return changeLength;
        }
        if (SvMetaType.isEquivalent(variantType, VariantType.DEL) && changeLength >= 0) {
            return start - end;
        }
        if (SvMetaType.isEquivalent(variantType, VariantType.INS) && changeLength <= 0) {
            // hack for DGV where INS variants don't have a length
            return changeLength + changeLength == 0 ? 1 : changeLength;
        }
        return changeLength;
    }

    static class SvResult extends BaseGenomicVariant<SvResult> {

        private final String source;
        private final int ac;
        private final int an;
        private final float af;

        private SvResult(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength, String source, int ac, int an) {
            super(contig, id, strand, coordinates, ref, alt, changeLength, "", "");
            this.source = source;
            this.ac = ac;
            this.an = an;
            this.af = ac == 0 ? 0 : (float) ac / (float) an * 100f;
        }

        static SvResult of(Contig contig, int start, int end, int changeLength, VariantType variantType, String id, String source, int ac, int an) {
            String alt = '<' + variantType.toString().replace("_", ":") + '>';
            return new SvResult(contig, ".".equals(id) ? "" : id, Strand.POSITIVE, Coordinates.oneBased(start, end), "", alt, changeLength, source, ac, an);
        }

        @Override
        protected SvResult newVariantInstance(Contig contig, String id, Strand strand, Coordinates coordinates, String ref, String alt, int changeLength, String mateId, String eventId) {
            return new SvResult(contig, id, strand, coordinates, ref, alt, changeLength, source, ac, an);
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
                    ", ac=" + ac +
                    ", an=" + an +
                    ", af=" + af +
                    '}';
        }
    }
}
