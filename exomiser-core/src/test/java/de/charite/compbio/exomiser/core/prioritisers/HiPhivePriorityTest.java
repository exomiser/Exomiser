/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.prioritisers.util.DataMatrix;
import org.jblas.DoubleMatrix;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 *
 * @author jj8
 */
public class HiPhivePriorityTest {
    
    private HiPhivePriority instance;
    
    private List<String> hpoIds;
    private String candidateGene;
    private String disease;
    private String exomiser2params;

    @Before
    public void setUp() {
        hpoIds = new ArrayList<>();
        hpoIds.add("HP:0010055");
        hpoIds.add("HP:0001363");
        hpoIds.add("HP:0001156");
        hpoIds.add("HP:0011304");

        candidateGene = "FGFR2";
        disease = "OMIM:101600";
        exomiser2params = "";

        double[][] ppiMatrix = {
                {0.707653999329, 0.000000009625, 0.000000008875, 0.000000372898, 0.000000268611, 0.000000023074, 0.000000040680, 0.000000133227, 0.000000064774, 0.000000113817},
                {0.000000005477, 0.713751792908, 0.000008168789, 0.000000000210, 0.000000001862, 0.000000013144, 0.000000000679, 0.000000001696, 0.000000001134, 0.000000002901},
                {0.000000032869, 0.000053159707, 0.703132450581, 0.000000005321, 0.000001411398, 0.000000034059, 0.000000138069, 0.000000080616, 0.000000016616, 0.000001185472},
                {0.000001284835, 0.000000001273, 0.000000004950, 0.701313674450, 0.000000023567, 0.000000434552, 0.000001564053, 0.000000178326, 0.000000213718, 0.000000099803},
                {0.000000758085, 0.000000009233, 0.000001075537, 0.000000019304, 0.702176809311, 0.000000022661, 0.000000009667, 0.000000021993, 0.000000019951, 0.000000053246},
                {0.000000047331, 0.000000047378, 0.000000018865, 0.000000258712, 0.000000016471, 0.707689404488, 0.000006197341, 0.000000126649, 0.000000020872, 0.000000080030},
                {0.000000023518, 0.000000000690, 0.000000021553, 0.000000262441, 0.000000001980, 0.000001746652, 0.702001333237, 0.000000298203, 0.000000004386, 0.000000007073},
                {0.000000442888, 0.000000009907, 0.000000072361, 0.000000172052, 0.000000025906, 0.000000205243, 0.000001714670, 0.700654745102, 0.000000116321, 0.000000084246},
                {0.000000010099, 0.000000000311, 0.000000000700, 0.000000009671, 0.000000001102, 0.000000001586, 0.000000001183, 0.000000005456, 0.701215505600, 0.000000269338},
                {0.000000034608, 0.000000001550, 0.000000097330, 0.000000008808, 0.000000005737, 0.000000011863, 0.000000003720, 0.000000007706, 0.000000525256, 0.705851793289}
        };
        DoubleMatrix doubleMatrix = new DoubleMatrix(ppiMatrix);

        Arrays.stream(doubleMatrix.getRow(5).toArray()).forEach(val -> System.out.printf("%.12f, ", val));
        System.out.println();
        Arrays.stream(doubleMatrix.getColumn(5).toArray()).forEach(val -> System.out.printf("%.12f, ", val));
        System.out.println();


        DataMatrix testMatrix = new DataMatrix(doubleMatrix.toFloat(), new HashMap<>());

        instance = new HiPhivePriority(hpoIds, new HiPhiveOptions(), testMatrix);
//        instance.setPriorityService();

//        HP:0010055-HP:0010055=2.7796420474783035
//        HP:0001363-HP:0001363=2.4336466530917122
//        HP:0001156-HP:0001156=1.9399600254934881
//        HP:0011304-HP:0011304=2.7485534157741984

//        HP:0010055-MP:0009049=2.530139606718946
//        HP:0001363-MP:0000081=2.6914281512491542
//        HP:0001156-MP:0002544=2.2666863877455286
//        HP:0011304-MP:0002543=2.170374978266971

//        HP:0010055-ZP:0012193=1.0920661382591106
//        HP:0001363-ZP:0003564=1.9560677583289208
//        HP:0001156-ZP:0012193=1.1921761629223673
//        HP:0011304-ZP:0001082=1.1965410868294204
    }



    @Test
    public void testGetPriorityType() {
        assertThat(instance.getPriorityType(), equalTo(PriorityType.HIPHIVE_PRIORITY));
    }

    @Ignore
    @Test
    public void testPrioritizeGenes() {
        instance.prioritizeGenes(new ArrayList<Gene>());
    }


    @Ignore
    @Test
    public void testPrioritizeGenesInBenchmarkingMode() {
        instance = new HiPhivePriority(hpoIds, new HiPhiveOptions(disease, candidateGene), null);
        instance.prioritizeGenes(new ArrayList<Gene>());
    }

    @Ignore
    @Test
    public void testSetPriorityService() {
        ArrayListMultimap<Integer, Double>  multimapInsertOrderTest = ArrayListMultimap.create();
        multimapInsertOrderTest.put(1, 1.0);
        multimapInsertOrderTest.put(1, 2.0);
        multimapInsertOrderTest.put(1, 3.0);

        multimapInsertOrderTest.put(3, 1.0);

        multimapInsertOrderTest.put(2, 1.0);
        multimapInsertOrderTest.put(2, 1.0);

        System.out.println(multimapInsertOrderTest);
        List<Integer> expectedOrder = Lists.newArrayList(1, 3, 2);
        assertThat(multimapInsertOrderTest.keySet(), equalTo(expectedOrder));
    }

    @Test
    public void testToString() {
        System.out.println(instance);
    }
    
}
