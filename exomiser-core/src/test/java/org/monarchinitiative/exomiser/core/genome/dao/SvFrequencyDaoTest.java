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
import org.h2.mvstore.db.SpatialKey;
import org.h2.mvstore.rtree.MVRTreeMap;
import org.h2.mvstore.rtree.Spatial;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.assembly.GenomicAssemblies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
        Variant variant = TestFactory.variantBuilder(18, 24538029, 24538029, "A", "<INS>", 319).build();
        FrequencyData result = instance.getFrequencyData(variant);
        assertThat(result, equalTo(FrequencyData.of("nsv4544061", Frequency.of(FrequencySource.DBVAR, 13.219134f))));
    }

    @ParameterizedTest
    @CsvSource({
            "1,     724132,    726937,  <INS>,      2806, nsv544884, DGV=0.06888238", //  nsv544884
            "1,     724131,    724132,  <INS>,      2806, '', ''", //
            "7,    4972268,  62707793,  <DEL>, -57735526, nsv3168780, ''", // nsv3168780 , -,  nsv3168780
            "7,      33300,     33700,  <DEL>,      -401, nsv1133639, ''", // nsv1133639, nsv1133639, nsv1133639
            "7,      33350,     33670,  <DEL>,      -321, nsv1133639, ''", // nsv1133639, -, nsv1133639
            "18,   2806309,   2806425,  <INV>,       117, nsv4316669, 'DBVAR=0.01844508'", // -, -, nsv4316669
            "1,     521332,    521332,  <INS:ME>,      1, '', ''", // - , GONL=54.941483, GONL=54.941483
            "1,     521332,    521332,  <INS:ME>,    300, '', 'GONL=54.941483'", // - , GONL=54.941483, GONL=54.941483
            "1,     521332,    521632,  <INS:ME>,    300, '', ''", // - , GONL=54.941483, GONL=54.941483
            "1,    1546902,   1546904,  <INS>,       200, '', ''",
            "1,      66576,     66576,  <INS>,         2, nsv4534800, 'DGV=0.009219139'", // - , nsv4534800, nsv4534800
            "1,      66575,     66576,  <INS>,         2, nsv4534800, 'DGV=0.009219139'", // - , nsv4534800, nsv4534800
            "1,      66575,     66576,  <INS>,        20, '', ''", // - , nsv4534800, - (too short)
            "1,      66575,     66576,  <INS>,        50, nsv4534800, 'DBVAR=0.02336012'", // - , nsv4534800, nsv4534800
            "1,      66576,     66577,  <INS>,        50, '', 'GNOMAD_SV=0.02336012'", // - , nsv4534800, nsv4534800
            "1,      66575,     66577,  <INS>,        50, '', 'GNOMAD_SV=0.02336012'", // - , nsv4534800, nsv4534800
            "1,     530886,     530887, <INS>,        50, '', 'GNOMAD_SV=0.009297136'", // nsv4290545, nsv4290545, nsv4290545
            "1,     530886,     530887, <INS>,       500, '', ''", // nsv4290545, nsv4290545, - (too long)
            "1,     530886,     531387, <INS>,       500, 'nsv5212639', ''", // - (maps to a CNV from DBVAR - chr=1, start=530881, end=531480, length=600, changeLength=600, svType='CNV', source='DBVAR', id='nsv5212639' )
            "1,     530886,     530887, <INS>,         1, nsv4290545, 'DGV=0.009219139'", // nsv4290545, nsv4290545, nsv4290545
            "1,     181263,     181263, <INS>,        52, '', ''",
            "10,  23037995,   23037995, <INS:ME>,    300, esv3304209, ''", // - , esv3304209, esv3304209
            "15,  62706090,   62707793, <DEL>,     -1703, nsv4635624, 'DBVAR=18.81283'", // nsv4530637, nsv4530637, nsv4530637 nsv4635624
            "12,  71525479,   71525479, <INS:ME:ALU>,    275, esv3867958, 'DBVAR=30.970448'", // - , esv3867958, esv3867958
            "7,   17094714,   17094715, <INS:ME:LINE1>,  275, esv3848531, 'DBVAR=18.530352'", // nsv3564873, esv3848531, esv3848531
            "10,  23037995,   23037996, <INS:ME>,     2, esv3304209, 'DGV=5.4054055'", // esv3304209
            "10,  23037995,   23037996, <INS:ME>,   300, esv3304209, ''", // esv3304209 - same entry but DBVAR has no frequency data
    })
    void getFrequencyData(int chr, int start, int end, String alt, int changeLength, String expectedId, String expectedFrequencies) {
        Variant variant = TestFactory.variantBuilder(chr, start, end, "", alt, changeLength).build();
        FrequencyData result = instance.getFrequencyData(variant);
        assertThat(result, equalTo(FrequencyData.of(expectedId, parsefrequencies(expectedFrequencies))));
    }

    private Frequency[] parsefrequencies(String expectedFrequencies) {
        if (expectedFrequencies.isEmpty()) {
            return new Frequency[0];
        }
        String[] tokens = expectedFrequencies.split(";");
        Frequency[] frequencies = new Frequency[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            String[] freqVal = tokens[i].split("=");
            frequencies[i] = Frequency.of(FrequencySource.valueOf(freqVal[0]), Float.parseFloat(freqVal[1]));
        }
        return frequencies;
    }

    @Test
    void commonInversion() {
        Variant variant = TestFactory.variantBuilder(1, 240116000, 240116800, "", "<INV>", 0).build();
        FrequencyData result = instance.getFrequencyData(variant);
        assertThat(result, equalTo(FrequencyData.of("esv3822662", Frequency.of(FrequencySource.DBVAR, 17.591854f))));
    }

    @Test
    void getInsMeExactMatch() {
        Variant variant = TestFactory.variantBuilder(1, 521331, 521332, "", "<INS:ME>", 300).build();
        FrequencyData result = instance.getFrequencyData(variant);
        assertThat(result, equalTo(FrequencyData.of(Frequency.of(FrequencySource.GONL, 54.941483f))));
    }

    @Test
    void getInsMeDgvMatch() {
        // esv3304209 is an INS_ME
        Variant variant = TestFactory.variantBuilder(10, 23037995, 23037996, "", "<INS:ME>", 2).build();
        FrequencyData result = instance.getFrequencyData(variant);
        assertThat(result, equalTo(FrequencyData.of("esv3304209", Frequency.of(FrequencySource.DGV, 5.4054055f))));
    }

    @Test
    void getDelManyPotentialMatches() {
        Variant variant = TestFactory.variantBuilder(15, 62706090, 62707793, "", "<DEL>", 62706090 - 62707793).build();
        FrequencyData result = instance.getFrequencyData(variant);
        assertThat(result, equalTo(FrequencyData.of("nsv4635624", Frequency.of(FrequencySource.DBVAR, 18.81283f))));
    }

    @Test
    void getCnvManyPotentialMatches() {
        Variant variant = TestFactory.variantBuilder(15, 62706090, 62707793, "", "<CNV:GAIN>", 0).build();
        FrequencyData result = instance.getFrequencyData(variant);
        assertThat(result, equalTo(FrequencyData.of("esv29792", Frequency.of(FrequencySource.DGV, 97.5f))));
    }

    @Test
    void getDgvCanvasGain() {
        Variant variant = TestFactory.variantBuilder(2, 37958137, 38002170, "", "<CNV:GAIN>", 20).build();
        FrequencyData result = instance.getFrequencyData(variant);
        assertThat(result, equalTo(FrequencyData.of("nsv4583525", Frequency.of(FrequencySource.DBVAR, 59.555553f))));
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

        GenomicRegion region = GenomicRegion.of(chr1, Strand.POSITIVE, CoordinateSystem.ONE_BASED, 21000, 46000);
        SvDaoBoundaryCalculator boundaryCalculator = new SvDaoBoundaryCalculator(region, 0.85);
        int margin = boundaryCalculator.outerBoundsOffset();

        System.out.println(region);
        System.out.println("margin: " + margin);
        System.out.println("Searching chr" + region.contigId() + " from " + (region.start() - margin) + " to " + (region
                .end() + margin));
        MVRTreeMap.RTreeCursor<SvFrequencyDao.SvResult> it =
//                r.findContainedKeys(new SpatialKey(0, 0f, 9f, 3f, 6f));
                r.findIntersectingKeys(new SpatialKey(0, boundaryCalculator.startMin(), boundaryCalculator.endMax(), region.contigId(), region.contigId()));
        while (it.hasNext()) {
            Spatial k = it.next();
            SvFrequencyDao.SvResult svResult = r.get(k);
            System.out.println(k + ": " + svResult + ", simJ=" + SvDaoUtil.jaccard(region, svResult));
        }
        s.close();
    }
}