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

    private final Logger logger = LoggerFactory.getLogger(SvFrequencyDao.class);

    private final DataSource svDataSource;

    public SvPathogenicityDao(DataSource svDataSource) {
        this.svDataSource = svDataSource;
    }

    @Caching(cacheable = {
            @Cacheable(cacheNames = "hg19.sv.path", keyGenerator = "variantKeyGenerator", condition = "#variant.genomeAssembly == T(org.monarchinitiative.exomiser.core.genome.GenomeAssembly).HG19"),
            @Cacheable(cacheNames = "hg38.sv.path", keyGenerator = "variantKeyGenerator", condition = "#variant.genomeAssembly == T(org.monarchinitiative.exomiser.core.genome.GenomeAssembly).HG38"),
    })
    @Override
    public PathogenicityData getPathogenicityData(Variant variant) {
        int margin = SvDaoUtil.getBoundaryMargin(variant, 0.85);

        logger.debug("{}", variant);
        List<SvResult> results = runQuery(variant, margin);
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
        ClinVarData clinVarData = ClinVarData.builder()
                .alleleId(first.clinVarAccession)
                .primaryInterpretation(first.clinSig)
                .build();

        List<PathogenicityScore> pathogenicityScores = topMatches.stream()
                .map(this::toPathScore)
                .collect(toList());

        return PathogenicityData.of(clinVarData, pathogenicityScores);
    }

    private PathogenicityScore toPathScore(SvResult svResult) {
        PathogenicitySource source = mapToSource(svResult.source);
        float score = mapClinSigToScore(svResult.clinSig);
        return PathogenicityScore.of(source, score);
    }

    private PathogenicitySource mapToSource(String source) {
        switch (source) {
            case "DBVAR":
                return PathogenicitySource.DBVAR;
            case "ISCA":
                return PathogenicitySource.ISCA;
            default:
                return PathogenicitySource.TEST;
        }
    }

    private float mapClinSigToScore(ClinVarData.ClinSig primaryInterpretation) {
        switch (primaryInterpretation) {
            case PATHOGENIC:
                return 1.0f;
            case PATHOGENIC_OR_LIKELY_PATHOGENIC:
                return 0.9f;
            case LIKELY_PATHOGENIC:
                return 0.8f;
            case UNCERTAIN_SIGNIFICANCE:
                return 0.6f;
            default:
                return 0f;
        }
    }

    private List<SvResult> runQuery(Variant variant, int margin) {
        String query = "SELECT *\n" +
                "FROM (\n" +
                "         SELECT 'DBVAR' as SOURCE,\n" +
                "                CHR_ONE,\n" +
                "                POS_ONE,\n" +
                "                POS_TWO,\n" +
                "                SV_LEN,\n" +
                "                SV_TYPE,\n" +
                "                DBVAR_ACC as ID,\n" +
                "                CLNSIG,\n" +
                "                CLNSIG_SOURCE,\n" +
                "                CLINVAR_ACCESSIONS\n" +
                "         FROM DBVAR_VARIANTS\n" +
                "         UNION ALL\n" +
                "         SELECT 'ISCA' as SOURCE,\n" +
                "                CONTIG as CHR_ONE,\n" +
                "                POS_ONE,\n" +
                "                POS_TWO,\n" +
                "                SV_LEN,\n" +
                "                SV_TYPE,\n" +
                "                ID,\n" +
                "                CLNSIG,\n" +
                "                CLNSIG_SOURCE,\n" +
                "                CLINVAR_ACCESSIONS\n" +
                "         FROM ISCA\n" +
                "     ) all_tables\n" +
                "WHERE CHR_ONE = ?\n" +
                "  and POS_ONE >= ?\n" +
                "  and POS_ONE <= ?\n" +
                "  and POS_TWO >= ?\n" +
                "  and POS_TWO <= ?\n" +
                "  and CLNSIG != 'UNKNOWN';";

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

//            SOURCE	CHR_ONE	POS_ONE	POS_TWO	SV_LEN	SV_TYPE	ID	CLINSIG	CLINSIG_SOURCE	CLINVAR_ACCESSIONS
//        DBVAR	20	61569	62915555	62853987	DUP	nssv15161429	PATHOGENIC	clinvar	RCV000512450.1
//        ISCA	20	61569	62915555	62853987	DUP	nssv13652173	PATHOGENIC	clinvar	SCV000586014

//        UNCERTAIN_SIGNIFICANCE
//        UNKNOWN
//        LIKELY_PATHOGENIC
//        BENIGN
//        LIKELY_BENIGN
//        PATHOGENIC

//        clinvar
//        not_provided

//        submitter
//        clinvar
//        clingen_dosage_sensitivity_map
//        not_provided

        List<SvResult> results = new ArrayList<>();
        while (rs.next()) {
            int chr = rs.getInt("CHR_ONE");
            int start = rs.getInt("POS_ONE");
            int end = rs.getInt("POS_TWO");
            int length = rs.getInt("SV_LEN");
            String svType = rs.getString("SV_TYPE");
            String source = rs.getString("SOURCE");
            String id = rs.getString("ID");
            String clnsig = rs.getString("CLNSIG");
            String clinvarAccession = rs.getString("CLINVAR_ACCESSIONS");

            VariantType variantType = VariantType.valueOf(svType);
            // there are cases such as INS_ME which won't match the database so we have to filter these here
            if (variantType.baseType() == variant.variantType().baseType()) {
                SvResult svResult = SvResult.of(variant.contig(), start, end, length, variantType, source, id, ClinVarData.ClinSig.valueOf(clnsig), clinvarAccession);
                results.add(svResult);
            }
        }
        return results;
    }

    private static class SvResult extends BaseVariant<SvResult> {

        private final String source;
        private final ClinVarData.ClinSig clinSig;
        private final String clinVarAccession;

        private SvResult(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition, String ref, String alt, int changeLength, String source, ClinVarData.ClinSig clinSig, String clinVarAccession) {
            super(contig, id, strand, coordinateSystem, startPosition, endPosition, "", alt, changeLength);
            this.source = source;
            this.clinSig = clinSig;
            this.clinVarAccession = clinVarAccession;
        }

        public static SvResult of(Contig contig, int start, int end, int changeLength, VariantType variantType, String source, String id, ClinVarData.ClinSig clinSig, String clinvarAccession) {
            String alt = '<' + variantType.baseType().toString() + '>';
            return new SvResult(contig, id, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, Position.of(start), Position.of(end), "",  alt, changeLength, source, clinSig, clinvarAccession);
        }

        @Override
        protected SvResult newVariantInstance(Contig contig, String id, Strand strand, CoordinateSystem coordinateSystem, Position startPosition, Position endPosition, String ref, String alt, int changeLength) {
            return new SvResult(contig, id, strand, coordinateSystem, startPosition, endPosition, "", alt, changeLength, source, clinSig, clinVarAccession);
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
                    ", clinsig=" + clinSig +
                    ", clinVarAccession=" + clinVarAccession +
                    '}';
        }
    }
}
