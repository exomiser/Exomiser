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

import org.h2.mvstore.MVStore;
import org.h2.mvstore.rtree.MVRTreeMap;
import org.h2.mvstore.rtree.SpatialKey;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.svart.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Disabled
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestSvDataSourceConfig.class, SvFrequencyDao.class})
class SvFrequencyDaoTest {

    @Autowired
    private SvFrequencyDao instance;

    private Contig contig(int id) {
        return GenomeAssembly.HG19.getContigById(id);
    }

    @Test
    void getIns() {
        Variant variant = VariantEvaluation.builder(18, 24538029, 67519385, "A", "<INS>", 319).build();

        FrequencyData result = instance.getFrequencyData(variant);

        System.out.println(result);
    }

    @ParameterizedTest
    @CsvSource({
            "18,  24538029,  67519385, <INS>,     319",
            "7,    4972268,  62707793, <DEL>,   -1003",
            "7,      33300,     33700, <DEL>,    -400",
            "1,  119273928, 119273929, <INV>,       0",
            "1,    521332,     521332, <INS:ME>,   1",
            "1,    530886,     530887, <INS>,      50",
            "1,    530886,     530887, <INS>,      1",
            "10,    23037995,     23037995, <INS:ME>,      300",
            "15,    62706090,     62707793, <DEL>,      -1703",
            "12,    71525479,     71525479, <INS:ME:ALU>,  275",
            "7,    17094714,     17094714, <INS:ME:LINE1>,  275",
    })
    void getFrequencyData(int chr, int start, int end, String alt, int changeLength) {
        Variant variant = VariantEvaluation.builder(chr, start, end, "", alt, changeLength).build();
        FrequencyData result = instance.getFrequencyData(variant);
        System.out.println(result);
    }


    @Test
    void commonInversion() {
        Variant variant = VariantEvaluation.builder(1, 240116000, 240116800, "", "<INV>", 0).build();
        FrequencyData result = instance.getFrequencyData(variant);
        assertThat(result, equalTo(FrequencyData.of("esv3822662", Frequency.of(FrequencySource.DBVAR, 17.591854f))));
    }

    @Test
    void getInsMeExactMatch() {
        Variant variant = VariantEvaluation.builder(1, 521332, 521332, "", "<INS:ME>", 200).build();
        FrequencyData result = instance.getFrequencyData(variant);
        assertThat(result, equalTo(FrequencyData.of(Frequency.of(FrequencySource.GONL, 54.941483f))));
    }

    @Test
    void getInsMeDgvMatch() {
        // TODO - Check the SV database build - thi dbVar the coordinates are reported as (b37) 23037995 - 23037995
        //  however the database has stored these as 23037995 - 23037996
        // esv3304209 is an INS_ME
        Variant variant = VariantEvaluation.builder(10, 23037995, 23037996, "", "<INS:ME>", 300).build();
        FrequencyData result = instance.getFrequencyData(variant);
        assertThat(result, equalTo(FrequencyData.of("esv3304209", Frequency.of(FrequencySource.DGV, 5.4054055f))));
    }

    @Test
    void getDelManyPotentialMatches() {
        Variant variant = VariantEvaluation.builder(15, 62706090, 62707793, "", "<DEL>", 62706090 - 62707793).build();
        FrequencyData result = instance.getFrequencyData(variant);
        System.out.println(result);
    }
//
//    @Test
//    void submitSnv() {
//        Variant variant = VariantAnnotation.builder()
//                .contig(contig(1))
//                .start(62399)
//                .end(62489)
//                .changeLength(90)
//                .variantType(VariantType.DEL)
//                .build();
//
//        FrequencyData result = instance.getFrequencyData(variant);
//
//        System.out.println(result);
//    }
//
//    @Test
//    void getCnvManyPotentialMatches() {
//        Variant variant = VariantAnnotation.builder()
//                .contig(contig(14))
//                .start(20194092)
//                .end(20424243)
//                .changeLength(230151)
//                .ref("A")
//                .alt("<CNV")
//                .variantType(VariantType.CNV)
//                .build();
//
//        System.out.println(variant);
//        System.out.println(variant.getLength());
//        FrequencyData result = instance.getFrequencyData(variant);
//
//        System.out.println(result);
//    }
//
//    @Test
//    void getCnvLoss() {
//        Variant variant = VariantAnnotation.builder()
//                .contig(contig(15))
//                .start(62_706_194)
//                .end(62_707_654)
//                .changeLength(0)
//                .variantType(VariantType.CNV_LOSS)
//                .build();
//
//        System.out.println(variant.getLength());
//        FrequencyData result = instance.getFrequencyData(variant);
//
//        System.out.println(result);
//    }
//
//    @Test
//    void getDgvInsMe() {
//        Variant variant = VariantAnnotation.builder()
//                .contig(contig(1))
//                .start(4288450)
//                .end(4288450)
//                .changeLength(300)
//                // this should be an INS_ME
//                .variantType(VariantType.INS_ME)
//                .build();
//
//        System.out.println(variant.getLength());
//        FrequencyData result = instance.getFrequencyData(variant);
//
//        System.out.println(result);
//    }
//
//    @Test
//    void getDgvCnv() {
//        Variant variant = VariantAnnotation.builder()
////                .contig(contig(22))
////                .start(24346935)
////                .end(24394915)
////                .changeLength(-47980)
//                .variantType(VariantType.CNV_LOSS)
//                .build();
//
//        System.out.println(variant.getLength());
//        FrequencyData result = instance.getFrequencyData(variant);
//
//        System.out.println(result);
//    }

    @Test
    void getDgvCanvasGain() {
        Variant variant = VariantEvaluation.builder(2, 37958137, 38002170, "", "<CNV:GAIN>", 20).build();

        System.out.println(variant.length());
        FrequencyData result = instance.getFrequencyData(variant);

        System.out.println(result);
    }

    @Test
    void mvStoreRtree() {
        Contig chr1 = GenomicAssemblies.GRCh37p13().contigById(1);
        Contig chr2 = GenomicAssemblies.GRCh37p13().contigById(2);
//        gnomAD_v2_DUP_1_1,1,10000,1,20000,10000,DUP,0.939508,20175,0.955696,OTH=0.920103;EAS=0.907374;AFR=0.955696;EUR=0.937878;AMR=0.904709,OTH=357;EAS=2018;AFR=9060;EUR=7126;AMR=1614,BAF;RD
        SvFrequencyDao.SvResult gnomAD_v2_DUP_1_1 = SvFrequencyDao.SvResult.of(chr1, 10000, 20000, 10000, VariantType.DUP, "gnomad_SV", "gnomAD_v2_DUP_1_1", 20175, 2000);

//        gnomAD_v2_DUP_1_3,1,20650,1,47000,26350,DUP,0.023753,241,0.046801,OTH=0.005102;EAS=0.0;AFR=0.046801;EUR=6.08E-4;AMR=0.001266,OTH=1;EAS=0;AFR=237;EUR=2;AMR=1,RD
        SvFrequencyDao.SvResult gnomAD_v2_DUP_1_3 = SvFrequencyDao.SvResult.of(chr1, 20650, 47000, 26350, VariantType.DUP, "gnomad_SV", "gnomAD_v2_DUP_1_3", 241, 20);

//        gnomAD_v2_DEL_1_1,1,21000,1,26000,5000,DEL,0.014596,133,0.069602,OTH=0.011111;EAS=0.069602;AFR=0.006801;EUR=0.013791;AMR=0.009412,OTH=2;EAS=49;AFR=27;EUR=47;AMR=8,RD
        SvFrequencyDao.SvResult gnomAD_v2_DEL_1_1 = SvFrequencyDao.SvResult.of(chr1, 21000, 26000, -5000, VariantType.DEL, "gnomad_SV", "gnomAD_v2_DEL_1_1", 133, 1);

//        gnomAD_v2_DUP_1_4,1,40000,1,47200,7200,DUP,0.070284,877,0.139293,OTH=0.019417;EAS=0.012542;AFR=0.139293;EUR=0.008134;AMR=0.017794,OTH=4;EAS=15;AFR=804;EUR=34;AMR=20,RD
        SvFrequencyDao.SvResult gnomAD_v2_DUP_1_4 = SvFrequencyDao.SvResult.of(chr1, 40000, 47200, -7200, VariantType.DEL, "gnomad_SV", "gnomAD_v2_DUP_1_4", 877, 1);

//        gnomAD_v2_DEL_2_16412,2,10000,2,590000,580000,DEL,0.000047,1,0.000457,OTH=0.0;EAS=4.57E-4;AFR=0.0;EUR=0.0;AMR=0.0,OTH=0;EAS=1;AFR=0;EUR=0;AMR=0,RD
        SvFrequencyDao.SvResult gnomAD_v2_DUP_2_3 = SvFrequencyDao.SvResult.of(chr2, 20650, 47000, 26350, VariantType.DUP, "gnomad_SV", "gnomAD_v2_DUP_2_3", 241, 1);

//        1	105832823	gnomAD-SV_v2.1_BND_1_3219	N	<BND>	717	UNRESOLVED	END=105832824;SVTYPE=BND;SVLEN=11547;CHR2=1;POS2=105844370;END2=105844371;ALGORITHMS=delly;BOTHSIDES_SUPPORT;EVIDENCE=BAF,PE,SR;UNRESOLVED_TYPE=MIXED_BREAKENDS;AN=21688;AC=64;AF=0.002951;
        SvFrequencyDao.SvResult gnomAD_v2_BND_2_7 = SvFrequencyDao.SvResult.of(chr2, 20650, 47000, 26350, VariantType.DUP, "gnomad_SV", "gnomAD_v2_DUP_2_3", 241, 1);

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
            r.add(new SpatialKey(id++, sv.start(), sv.end(), sv.contigId(), sv.contigId()), sv);
        }
//        r.add(new SpatialKey(id++, gnomAD_v2_BND_2_7.start(), gnomAD_v2_BND_2_7.end(), gnomAD_v2_BND_2_7.contigId(), 4), gnomAD_v2_BND_2_7);

        System.out.println("Num SV in store: " + r.size());
        r.entrySet().forEach(System.out::println);

        GenomicRegion region = GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.FULLY_CLOSED, 21000, 46000);
        int margin = SvDaoUtil.getBoundaryMargin(region, 0.85);

        System.out.println(region);
        System.out.println("margin: " + margin);
        System.out.println("Searching chr" + region.contigId() + " from " + (region.start() - margin) + " to " + (region
                .end() + margin));
// iterate over the intersecting keys
        Iterator<SpatialKey> it =
//                r.findContainedKeys(new SpatialKey(0, 0f, 9f, 3f, 6f));
                r.findIntersectingKeys(new SpatialKey(0, region.start() - margin, region.end() + margin, region.contigId(), region.contigId()));
        while (it.hasNext()) {
            SpatialKey k = it.next();
            SvFrequencyDao.SvResult svResult = r.get(k);
            System.out.println(k + ": " + svResult + " " + SvDaoUtil.jaccard(region, svResult));
        }
        s.close();
    }
}