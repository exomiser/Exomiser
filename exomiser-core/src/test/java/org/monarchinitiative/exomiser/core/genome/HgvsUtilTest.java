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

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.GenomicVariant;
import org.monarchinitiative.svart.Strand;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class HgvsUtilTest {

    @Test
    void toHgvsSnvSubstitution() {
        Variant variant = TestFactory.variantBuilder(1, 12345, "A", "T").build();
        assertThat(HgvsUtil.toHgvsGenomic(variant), equalTo("NC_000001.10:g.12345A>T"));
    }

    @Test
    void toHgvsSnvDelSingle() {
        Variant variant = TestFactory.variantBuilder(1, 18, "AT", "A").build();
        // n.b. VCF used 1-based whereas HGVS is 0-based
        // a deletion of the T at position g.19 in the sequence AGAA_T_CACA to AGAA___CACA
        assertThat(HgvsUtil.toHgvsGenomic(variant), equalTo("NC_000001.10:g.19del"));
    }

    @Test
    void toHgvsSnvDelSingleReal() {
        Variant variant = TestFactory.variantBuilder(19, 23844937, "CA", "C").build();
        // https://www.ncbi.nlm.nih.gov/snp/rs1359868666
        // https://gnomad.broadinstitute.org/variant/19-23844937-CA-C
        assertThat(HgvsUtil.toHgvsGenomic(variant), equalTo("NC_000019.9:g.23844938del"));
    }

    @Test
    void toHgvsSnvDelMultiple() {
        Variant variant = TestFactory.variantBuilder(1, 17, "ATCA", "A").build();
        // n.b. VCF used 1-based whereas HGVS is 0-based
        // a deletion of nucleotides g.19 to g.21 in the sequence AGAA_TCA_CA to AGAA___CA
        assertThat(HgvsUtil.toHgvsGenomic(variant), equalTo("NC_000001.10:g.19_21del"));
    }

    @Test
    void toHgvsSnvDelMultipleReal() {
        Variant variant = TestFactory.variantBuilder(14, 23371269, "GCA", "G").build();
        // https://www.ncbi.nlm.nih.gov/snp/rs762848810
        // https://gnomad.broadinstitute.org/variant/14-23371269-GCA-G
        assertThat(HgvsUtil.toHgvsGenomic(variant), equalTo("NC_000014.8:g.23371270_23371271del"));
    }

    @Test
    void toHgvsSvDel() {
        Variant variant = TestFactory.variantBuilder(10, 20, 100, "T", "<DEL>", -80).build();
        assertThat(HgvsUtil.toHgvsGenomic(variant), equalTo("NC_000010.10:g.20_100del"));
    }

    @Test
    void toHgvsSvInversion() {
        Variant variant = TestFactory.variantBuilder(10, 20, 100, "T", "<INV>", 0).build();
        assertThat(HgvsUtil.toHgvsGenomic(variant), equalTo("NC_000010.10:g.20_100inv"));
    }

    @Test
    void toHgvsSnvInsSingle() {
        Variant variant = TestFactory.variantBuilder(10, 32867861, "A", "AT").build();
        // the insertion of an T nucleotide between nucleotides g.32867861 and g.32867862
        assertThat(HgvsUtil.toHgvsGenomic(variant), equalTo("NC_000010.10:g.32867861_32867862insT"));
    }

    @Test
    void toHgvsSnvInsMultiple() {
        Variant variant = TestFactory.variantBuilder(10, 32862923, "A", "ACCT").build();
        // the insertion of nucleotides CCT between nucleotides g.32862923 and g.32862924
        assertThat(HgvsUtil.toHgvsGenomic(variant), equalTo("NC_000010.10:g.32862923_32862924insCCT"));
    }

    @Test
    void toHgvsSnvInsMultipleReal() {
        Variant variant = TestFactory.variantBuilder(12, 53207583, "C", "CCCGG").build();
        // n.b this isn't strictly correct due to the way these are generated from the VCF coordinates and the fact
        // that VCF and HGVS shift in opposite directions.
        // Strictly this should be NC_000012.11:g.53207584_53207585insCGGC
        assertThat(HgvsUtil.toHgvsGenomic(variant), equalTo("NC_000012.11:g.53207583_53207584insCCGG"));
    }

    @Test
    void toHgvsSnvInsMultipleRealX() {
        Variant variant = TestFactory.variantBuilder(23, 31457623, "T", "TAAAAAA").build();
        // n.b this isn't strictly correct due to the way these are generated from the VCF coordinates and the fact
        // that VCF and HGVS shift in opposite directions.
        // Strictly this should be NC_000023.10:g.31457624_31457629dupAAAAAA
        assertThat(HgvsUtil.toHgvsGenomic(variant), equalTo("NC_000023.10:g.31457623_31457624insAAAAAA"));
    }

    @Test
    void toHgvsSvIns() {
        Variant variant = TestFactory.variantBuilder(10, 20, 100, "T", "<INS>", 80).build();
        assertThat(HgvsUtil.toHgvsGenomic(variant), equalTo("NC_000010.10:g.20_100insN[80]"));
    }

    @Test
    void toHgvsSnvDupSingle() {
        Variant variant = TestFactory.variantBuilder(10, 20, "T", "TT").build();
        // the duplication of a T at position c.20 in the sequence AGAAG_T_AGAGG to AGAAG_TT_AGAGG
        // NOTE: the recommendation is not to describe the variant as NM_004006.2:c.20dupT, i.e. describe the duplicated
        // nucleotide sequence. This description is longer, it contains redundant information and chances to make an
        // error increases (e.g. NM_004006.2:c.20dupG).
        // NOTE: it is not allowed to describe the variant as g.19_20insT (see prioritisation)
        assertThat(HgvsUtil.toHgvsGenomic(variant), equalTo("NC_000010.10:g.20dup"));
    }

    @Test
    void toHgvsSnvDupMultiple() {
        GenomicVariant variant = GenomicVariant.of(GenomeAssembly.HG19.getContigById(10), Strand.POSITIVE, CoordinateSystem.ONE_BASED, 20, "T", "TTT");
        assertThat(HgvsUtil.toHgvsGenomic(variant), equalTo("NC_000010.10:g.20_21dup"));
    }

    @Test
    void toHgvsSvDup() {
        GenomicVariant variant = GenomicVariant.of(GenomeAssembly.HG19.getContigById(10),  Strand.POSITIVE, CoordinateSystem.ONE_BASED, 20, 100, "T", "<DUP>", 80);
        assertThat(HgvsUtil.toHgvsGenomic(variant), equalTo("NC_000010.10:g.20_100dup"));
    }

    @Test
    void toHgvsSnvDelInsSingle() {
        Variant variant = TestFactory.variantBuilder(1, 6775, "T", "GA").build();
        // a deletion of nucleotide g.6775 (a T, not described),
        // replaced by nucleotides GA, changing AGGC_T_CATT to AGGC_GA_CATT
        assertThat(HgvsUtil.toHgvsGenomic(variant), equalTo("NC_000001.10:g.6775delinsGA"));
    }

    @Test
    void toHgvsSnvDelInsMultiple() {
        Variant variant = TestFactory.variantBuilder(1, 6775, "TCA", "C").build();
        // a deletion of nucleotides g.6775 to g.6777 (TCA, not described),
        // replaced by nucleotides C, changing AGGC_TCA_TT to AGGC_C_TT
        assertThat(HgvsUtil.toHgvsGenomic(variant), equalTo("NC_000001.10:g.6775_6777delinsC"));
    }
}