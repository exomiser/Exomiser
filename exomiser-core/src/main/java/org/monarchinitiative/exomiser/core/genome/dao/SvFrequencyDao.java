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

    private final Logger logger = LoggerFactory.getLogger(SvFrequencyDao.class);

    private final DataSource svDataSource;

    public SvFrequencyDao(DataSource svDataSource) {
        this.svDataSource = svDataSource;
    }

    @Caching(cacheable = {
            @Cacheable(cacheNames = "hg19.sv.freq", keyGenerator = "variantKeyGenerator", condition = "#variant.genomeAssembly == T(org.monarchinitiative.exomiser.core.genome.GenomeAssembly).HG19"),
            @Cacheable(cacheNames = "hg38.sv.freq", keyGenerator = "variantKeyGenerator", condition = "#variant.genomeAssembly == T(org.monarchinitiative.exomiser.core.genome.GenomeAssembly).HG38"),
    })
    @Override
    public FrequencyData getFrequencyData(Variant variant) {
        int margin = SvDaoUtil.getBoundaryMargin(variant, 0.85);

        logger.debug("{}", variant);
        logger.debug("Searching for {}:{}-{}", variant.contigId(), variant.start() - margin, variant.end() + margin);
        List<SvResult> results = runQuery(variant, margin);
        results.forEach(svResult -> logger.debug("{}", svResult));

        Map<Double, List<SvResult>> resultsByScore = results.stream()
                .collect(Collectors.groupingBy(score(variant)));

        List<SvResult> topMatches = resultsByScore.entrySet()
                .stream()
                .max(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .orElse(List.of());

        logger.debug("Top match(es)");
        topMatches.forEach(svResult -> logger.debug("{}", svResult));

        return mapToFrequencyData(topMatches);
    }

    private Function<SvResult, Double> score(Variant variant) {
        // geometric mean of num alleles and similarity - try and get the best represented and most similar allele
        return svResult -> Math.sqrt(svResult.ac * SvDaoUtil.jaccard(variant, svResult));
    }

    private FrequencyData mapToFrequencyData(List<SvResult> topMatches) {
        if (topMatches.isEmpty()) {
            return FrequencyData.empty();
        }
        SvResult first = topMatches.get(0);
        Frequency frequency = toFrequency(first);
        if (frequency.getFrequency() == 0) {
            // DGV has no frequency information, but does have an id
            return FrequencyData.of(first.id());
        }
        return FrequencyData.of(first.id(), frequency);
    }

    private Frequency toFrequency(SvResult first) {
        FrequencySource frequencySource = frequencySource(first);
        return Frequency.of(frequencySource, first.af * 100);
    }

    private FrequencySource frequencySource(SvResult first) {
        switch (first.source) {
            case "GNOMAD_SV":
                return FrequencySource.GNOMAD_SV;
            case "DBVAR":
                return FrequencySource.DBVAR;
            case "DGV":
                return FrequencySource.DGV;
            case "GONL":
                return FrequencySource.GONL;
            case "DECIPHER":
                return FrequencySource.DECIPHER;
            default:
                return FrequencySource.UNKNOWN;
        }
    }

    private List<SvResult> runQuery(Variant variant, int margin) {
        String query = "SELECT *\n" +
                "FROM (\n" +
                "         SELECT 'GNOMAD_SV' as SOURCE,\n" +
                "                CHR_ONE,\n" +
                "                POS_ONE,\n" +
                "                POS_TWO,\n" +
                "                SV_LEN,\n" +
                "                SV_TYPE,\n" +
                "                ID,\n" +
                "                AC,\n" +
                "                AF\n" +
                "         FROM GNOMAD_SV\n" +
                "         UNION ALL\n" +
                "         SELECT 'DBVAR'          as SOURCE,\n" +
                "                CHR_ONE,\n" +
                "                POS_ONE,\n" +
                "                POS_TWO,\n" +
                "                SV_LEN,\n" +
                "                SV_TYPE,\n" +
                "                DBVAR_ACC        as ID,\n" +
                "                ALLELE_COUNT     as AC,\n" +
                "                ALLELE_FREQUENCY as AF\n" +
                "         FROM DBVAR_VARIANTS\n" +
                "         UNION ALL\n" +
                "         SELECT 'GONL' as SOURCE,\n" +
                "                CHR_ONE,\n" +
                "                POS_ONE,\n" +
                "                POS_TWO,\n" +
                "                SV_LEN,\n" +
                "                SV_TYPE,\n" +
                "                ID,\n" +
                "                AN     as AC,\n" +
                "                AF\n" +
                "         FROM GONL\n" +
                "         UNION ALL\n" +
                "         SELECT 'DGV' as SOURCE,\n" +
                "                CONTIG         as CHR_ONE,\n" +
                "                POS_ONE,\n" +
                "                POS_TWO,\n" +
                "                POS_TWO - POS_ONE as SV_LEN,\n" +
                "                SV_TYPE,\n" +
                "                ACCESSION      as ID,\n" +
                "                SAMPLE_SIZE    as AC,\n" +
                "                0              as AF\n" +
                "         FROM DGV_VARIANTS\n" +
                "         UNION ALL\n" +
                "         SELECT 'DECIPHER'    as SOURCE,\n" +
                "                CONTIG            as CHR_ONE,\n" +
                "                POS_ONE,\n" +
                "                POS_TWO,\n" +
                "                POS_TWO - POS_ONE as SV_LEN,\n" +
                "                SV_TYPE,\n" +
                "                POPULATION_CNV_ID as ID,\n" +
                "                OBSERVATIONS      as AC,\n" +
                "                FREQUENCY         as AF\n" +
                "         FROM DECIPHER_CNV\n" +
                "     ) all_tables\n" +
                "WHERE CHR_ONE = ?\n" +
                "  and POS_ONE >= ?\n" +
                "  and POS_ONE <= ?\n" +
                "  and POS_TWO >= ?\n" +
                "  and POS_TWO <= ?\n" +
                "  and AC != -1;";

        try (
                Connection connection = svDataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(query)
        ) {
            ps.setInt(1, variant.contigId());
            ps.setInt(2, variant.start() - margin);
            ps.setInt(3, variant.start() + margin);
            ps.setInt(4, variant.end() - margin);
            ps.setInt(5, variant.end() + margin);

            ResultSet rs = ps.executeQuery();

// consider also complex types where CHR_ONE != CHR_TWO - there are only about 600 in gnomad and gonl combined.
            return getSvResults(rs, variant);
        } catch (SQLException e) {
            logger.error("", e);
        }
        return List.of();
    }

    private List<SvResult> getSvResults(ResultSet rs, Variant variant) throws SQLException {

//            SOURCE	CHR_ONE	POS_ONE	POS_TWO	SV_LEN	SV_TYPE	ID	AC	AF
//            GNOMAD_SV	7	4972268	4973271	1003	DEL	gnomAD_v2_DEL_7_90956	94	0.004377
//            DBVAR	7	4972268	4973271	-1004	DEL	nssv15404135	94	0.004377
//            DGV_VARIANTS	7	4972233	4973326	1093	CNV	esv2659019	1151	0
//            DGV_VARIANTS	7	4972258	4973286	1028	CNV	esv3611958	2504	0
//            DECIPHER_CNV	7	4972251	4973293	1042	DEL	23156	94	0.049682875

        List<SvResult> results = new ArrayList<>();
        while (rs.next()) {
            String source = rs.getString("SOURCE");
            int chr = rs.getInt("CHR_ONE");
            int start = rs.getInt("POS_ONE");
            int end = rs.getInt("POS_TWO");
            int length = rs.getInt("SV_LEN");
            String svType = rs.getString("SV_TYPE");
            String id = rs.getString("ID");
            int ac = rs.getInt("AC");
            float af = rs.getFloat("AF");

            VariantType variantType = VariantType.valueOf(svType);
            // there are cases such as INS_ME which won't match the database so we have to filter these here
            // consider also DEL/CNV_LOSS INS/CNV_GAIN/DUP/INS_ME and CNV
            if (variantType.baseType() == variant.variantType().baseType()) {
                SvResult svResult = SvResult.of(variant.contig(), start, end, length, variantType, id, source, ac, af);
                results.add(svResult);
            }
        }
        return results;
    }

    static class SvResult extends BaseVariant<SvResult> {

        private final String source;
        private final int ac;
        private final float af;

        private SvResult(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition, String ref, String alt, int changeLength, String source, int ac, float af) {
            super(contig, id, strand, coordinateSystem, startPosition, endPosition, ref, alt, changeLength);
            this.source = source;
            this.ac = ac;
            this.af = af;
        }

        static SvResult of(Contig contig, int start, int end, int changeLength, VariantType variantType, String id, String source, int ac, float af) {
            String alt = '<' + variantType.baseType().toString() + '>';
            int correctedChangeLength = checkChangeLength(variantType, start, end, changeLength);
//            System.out.printf("contig=%s, id=%s, start=%d, end=%d, changeLength=%d, %s, %s, ac=%d, af=%f%n", contig.name(), id, start, end, changeLength, variantType, source, ac, af);
            return new SvResult(contig, ".".equals(id) ? "" : id, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(start), Position.of(end), "", alt, correctedChangeLength, source, ac, af);
        }

        private static int checkChangeLength(VariantType variantType, int start, int end, int changeLength) {
            if (variantType.baseType() == VariantType.DEL && changeLength >= 0) {
                return start - end;
            }
            if (variantType.baseType() == VariantType.INS && changeLength <= 0) {
                // hack for DGV where INS variants don't have a length
                return +changeLength + changeLength == 0 ? 1 : changeLength;
            }
            return changeLength;
        }

        @Override
        protected SvResult newVariantInstance(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition, String ref, String alt, int changeLength) {
            return new SvResult(contig, id, strand, coordinateSystem, startPosition, endPosition, ref, alt, changeLength, source, ac, af);
        }

        @Override
        public String toString() {
            return "SvResult{" +
                    "chr=" + contigName() +
                    ", start=" + start() +
                    ", end=" + end() +
                    ", length=" + length() +
                    ", svType='" + variantType() + '\'' +
                    ", source='" + source + '\'' +
                    ", id='" + id() + '\'' +
                    ", ac=" + ac +
                    ", af=" + af +
                    '}';
        }
    }
}
