/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.monarchinitiative.exomiser.db.reference;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantPathogenicityTest {

    private VariantPathogenicity instance;

    private static final float SIFT = 0.1f;
    private static final float POLYPHEN = 0.2f;
    private static final float MUT_TASTER = 0.3f;
    private static final float CADD_RAW_RANK = 0.4f;
    private static final float CADD_RAW_SCORE = 0.5f;

    @Before
    public void setUp() {
        instance = new VariantPathogenicity(1, 2, "A", "B", SIFT, POLYPHEN, MUT_TASTER, CADD_RAW_RANK, CADD_RAW_SCORE);
    }

    @Test
    public void testGetDumpLine() {
        String expResult = String.format("1|2|A|B|0.1|0.2|0.3|0.4|0.5%n");
        String result = instance.toDumpLine();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetDumpLineWithNullSift() {
        String expResult = String.format("1|2|A|B|null|0.2|0.3|0.4|0.5%n");
        instance = new VariantPathogenicity(1, 2, "A", "B", null, POLYPHEN, MUT_TASTER, CADD_RAW_RANK, CADD_RAW_SCORE);
        String result = instance.toDumpLine();
        assertEquals(expResult, result);
    }
}
