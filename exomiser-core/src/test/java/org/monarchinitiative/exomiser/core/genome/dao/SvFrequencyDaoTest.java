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

import org.h2.mvstore.MVStore;
import org.h2.mvstore.rtree.MVRTreeMap;
import org.h2.mvstore.rtree.SpatialKey;
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

import java.util.Iterator;
import java.util.List;

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

    @Test
    void mvStoreRtree() {
        //         private SvResult(int chr, int start, int end, int length, VariantType svType, String source, String id, int ac, float af) {
//        gnomAD_v2_DUP_1_1,1,10000,1,20000,10000,DUP,0.939508,20175,0.955696,OTH=0.920103;EAS=0.907374;AFR=0.955696;EUR=0.937878;AMR=0.904709,OTH=357;EAS=2018;AFR=9060;EUR=7126;AMR=1614,BAF;RD
        SvFrequencyDao.SvResult gnomAD_v2_DUP_1_1 = new SvFrequencyDao.SvResult(1, 10000, 20000, 10000, VariantType.DUP, "gnomad_SV", "gnomAD_v2_DUP_1_1", 20175, 0.939508f);

//        gnomAD_v2_DUP_1_3,1,20650,1,47000,26350,DUP,0.023753,241,0.046801,OTH=0.005102;EAS=0.0;AFR=0.046801;EUR=6.08E-4;AMR=0.001266,OTH=1;EAS=0;AFR=237;EUR=2;AMR=1,RD
        SvFrequencyDao.SvResult gnomAD_v2_DUP_1_3 = new SvFrequencyDao.SvResult(1, 20650, 47000, 26350, VariantType.DUP, "gnomad_SV", "gnomAD_v2_DUP_1_3", 241, 0.023753f);

//        gnomAD_v2_DEL_1_1,1,21000,1,26000,5000,DEL,0.014596,133,0.069602,OTH=0.011111;EAS=0.069602;AFR=0.006801;EUR=0.013791;AMR=0.009412,OTH=2;EAS=49;AFR=27;EUR=47;AMR=8,RD
        SvFrequencyDao.SvResult gnomAD_v2_DEL_1_1 = new SvFrequencyDao.SvResult(1, 21000, 26000, 5000, VariantType.DEL, "gnomad_SV", "gnomAD_v2_DEL_1_1", 133, 0.014596f);

//        gnomAD_v2_DUP_1_4,1,40000,1,47200,7200,DUP,0.070284,877,0.139293,OTH=0.019417;EAS=0.012542;AFR=0.139293;EUR=0.008134;AMR=0.017794,OTH=4;EAS=15;AFR=804;EUR=34;AMR=20,RD
        SvFrequencyDao.SvResult gnomAD_v2_DUP_1_4 = new SvFrequencyDao.SvResult(1, 40000, 47200, 7200, VariantType.DEL, "gnomad_SV", "gnomAD_v2_DUP_1_4", 877, 0.070284f);

//        gnomAD_v2_DEL_2_16412,2,10000,2,590000,580000,DEL,0.000047,1,0.000457,OTH=0.0;EAS=4.57E-4;AFR=0.0;EUR=0.0;AMR=0.0,OTH=0;EAS=1;AFR=0;EUR=0;AMR=0,RD
        SvFrequencyDao.SvResult gnomAD_v2_DUP_2_3 = new SvFrequencyDao.SvResult(2, 20650, 47000, 26350, VariantType.DUP, "gnomad_SV", "gnomAD_v2_DUP_2_3", 241, 0.023753f);

        SvFrequencyDao.SvResult gnomAD_v2_BND_2_7 = new SvFrequencyDao.SvResult(2, 20650, 47000, 26350, VariantType.DUP, "gnomad_SV", "gnomAD_v2_DUP_2_3", 241, 0.023753f);


        List<SvFrequencyDao.SvResult> svs = List.of(gnomAD_v2_DUP_1_1, gnomAD_v2_DUP_1_3, gnomAD_v2_DEL_1_1, gnomAD_v2_DUP_1_4, gnomAD_v2_DUP_2_3);


        // create an in-memory store
        MVStore s = MVStore.open(null);

// open an R-tree map
// The default number of dimensions is 2. To use a different number of dimensions,
// call new MVRTreeMap.Builder<String>().dimensions(3). The minimum number of
// dimensions is 1, the maximum is 32.
        MVRTreeMap<SvFrequencyDao.SvResult> r = s.openMap("data",
                new MVRTreeMap.Builder<SvFrequencyDao.SvResult>().dimensions(2));


// add two key-value pairs
// the first value is the key id (to make the key unique)
// then the min x, max x, min y, max y
//        r.add(new SpatialKey(0, -3f, -2f, 2f, 3f), "left");
//        r.add(new SpatialKey(1, 3f, 4f, 4f, 5f), "right");


        long id = 0;
        for (SvFrequencyDao.SvResult sv : svs) {
            r.add(new SpatialKey(id++, sv.getStart(), sv.getEnd(), sv.getStartContigId(), sv.getStartContigId()), sv);
        }
        r.add(new SpatialKey(id++, gnomAD_v2_BND_2_7.getStart(), gnomAD_v2_BND_2_7.getEnd(), gnomAD_v2_BND_2_7.getStartContigId(), 4), gnomAD_v2_BND_2_7);

        System.out.println("Num SV in store: " + r.size());
        r.entrySet().forEach(System.out::println);

        Variant variant = VariantAnnotation.builder()
                .chromosome(2)
                .endChromosome(4)
                .start(21000)
                .end(46000)
                .length(0)
                .variantType(VariantType.CNV_GAIN)
                .build();
        int margin = ChromosomalRegionUtil.getBoundaryMargin(variant, 0.85);

        System.out.println(variant);
        System.out.println("margin: " + margin);
        System.out.println("Searching " + variant.getStartContigId() + " from " + (variant.getStart() - margin) + " to " + (variant
                .getEnd() + margin));
// iterate over the intersecting keys
        Iterator<SpatialKey> it =
//                r.findContainedKeys(new SpatialKey(0, 0f, 9f, 3f, 6f));
                r.findIntersectingKeys(new SpatialKey(0, variant.getStart() - margin, variant.getEnd() + margin, variant
                        .getStartContigId(), variant.getEndContigId()));
        for (SpatialKey k; it.hasNext(); ) {
            k = it.next();
            SvFrequencyDao.SvResult svResult = r.get(k);
            System.out.println(k + ": " + svResult + " " + ChromosomalRegionUtil.jaccard(variant, svResult));
        }
        s.close();
    }
}