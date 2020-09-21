/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.monarchinitiative.exomiser.core.genome.ChromosomalRegionUtil;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantType;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Disabled
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestSvDataSourceConfig.class, SvFrequencyDao.class})
class SvFrequencyDaoTest {

    @Autowired
    private SvFrequencyDao instance;

    @Test
    void getIns() {
        Variant variant = VariantAnnotation.builder()
                .chromosome(18)
                .start(24538029)
                .end(67519385)
                .length(319)
                .variantType(VariantType.INS)
                .build();

        FrequencyData result = instance.getFrequencyData(variant);

        System.out.println(result);
    }

    @Test
    void getDel() {
        Variant variant = VariantAnnotation.builder()
                .chromosome(7)
                .start(4972268)
                .end(4973271)
                .length(1003)
                .variantType(VariantType.DEL)
                .build();

        FrequencyData result = instance.getFrequencyData(variant);

        System.out.println(result);
    }

    @Test
    void getInsMeExactMatch() {
        Variant variant = VariantAnnotation.builder()
                .chromosome(1)
                .start(521332)
                .end(521332)
                .length(0)
                .variantType(VariantType.INS_ME)
                .build();

        FrequencyData result = instance.getFrequencyData(variant);

        System.out.println(result);
    }

    @Test
    void getInsMeDgvMatch() {
        // esv3304209 is an INS_ME
        Variant variant = VariantAnnotation.builder()
                .chromosome(10)
                .start(23037996)
                .end(23037996)
                .length(0)
                .variantType(VariantType.CNV)
                .build();

        FrequencyData result = instance.getFrequencyData(variant);

        System.out.println(result);
    }

    @Test
    void getDelManyPotentialMatches() {
        Variant variant = VariantAnnotation.builder()
                .chromosome(15)
                .start(62706090)
                .end(62707793)
                .length(0)
                .variantType(VariantType.DEL)
                .build();

        FrequencyData result = instance.getFrequencyData(variant);

        System.out.println(result);
    }

    @Test
    void getCnvManyPotentialMatches() {
        Variant variant = VariantAnnotation.builder()
                .chromosome(14)
                .start(20194092)
                .end(20424243)
                .length(230151)
                .variantType(VariantType.CNV)
                .build();

        System.out.println(variant.getLength());
        FrequencyData result = instance.getFrequencyData(variant);

        System.out.println(result);
    }

    @Test
    void getCnvLoss() {
        Variant variant = VariantAnnotation.builder()
                .chromosome(15)
                .start(62_706_194)
                .end(62_707_654)
                .length(0)
                .variantType(VariantType.CNV_LOSS)
                .build();

        System.out.println(variant.getLength());
        FrequencyData result = instance.getFrequencyData(variant);

        System.out.println(result);
    }

    @Test
    void getDgvInsMe() {
        Variant variant = VariantAnnotation.builder()
                .chromosome(1)
                .start(4288450)
                .end(4288450)
                .length(300)
                // this should be an INS_ME
                .variantType(VariantType.CNV_LOSS)
                .build();

        System.out.println(variant.getLength());
        FrequencyData result = instance.getFrequencyData(variant);

        System.out.println(result);
    }

    @Test
    void getDgvCnv() {
        Variant variant = VariantAnnotation.builder()
                .chromosome(22)
                .start(24346935)
                .end(24394915)
                .length(-47980)
                .variantType(VariantType.CNV_LOSS)
                .build();

        System.out.println(variant.getLength());
        FrequencyData result = instance.getFrequencyData(variant);

        System.out.println(result);
    }

    @Test
    void getDgvCanvasGain() {
        Variant variant = VariantAnnotation.builder()
                .chromosome(2)
                .start(37958137)
                .end(38002170)
                .length(0)
                .variantType(VariantType.CNV_GAIN)
                .build();

        System.out.println(variant.getLength());
        FrequencyData result = instance.getFrequencyData(variant);

        System.out.println(result);
    }

}