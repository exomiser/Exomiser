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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.svart.Contig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Disabled
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestSvDataSourceConfig.class, SvPathogenicityDao.class})
class SvPathogenicityDaoTest {

    @Autowired
    private SvPathogenicityDao instance;

    private Contig contig(int id) {
        return GenomeAssembly.HG19.getContigById(id);
    }

    @ParameterizedTest
    @CsvSource({
            "1,  19225,  4401691, <DUP>,     319",
            "1,    155205541, 155205595, <DEL>,   -54",
            "14, 105814886, 107285437, <DEL>,  -1470551",
            "8, 7268819, 7752586, <CNV:GAIN>,  483767",
    })
    void getPathogenicityData(int chr, int start, int end, String alt, int changeLength) {
        Variant variant = TestFactory.variantBuilder(chr, start, end, "", alt, changeLength).build();
        PathogenicityData result = instance.getPathogenicityData(variant);
        System.out.println(result);
    }
//
//    @Test
//    void getDupInexactMatch() {
//        Variant variant = VariantAnnotation.builder()
//                .contig(contig(17))
//                .start(500)
//                .end(82_041_938)
//                .variantType(VariantType.DUP)
//                .build();
//
//        PathogenicityData result = instance.getPathogenicityData(variant);
//
//        System.out.println(result);
//    }
//
//    @Test
//    void getInsExactMatch() {
//        Variant variant = VariantAnnotation.builder()
//                .contig(contig(10))
//                .start(105_817_214)
//                .end(105_817_214)
//                .variantType(VariantType.DUP)
//                .build();
//
//        PathogenicityData result = instance.getPathogenicityData(variant);
//
//        System.out.println(result);
//    }
}