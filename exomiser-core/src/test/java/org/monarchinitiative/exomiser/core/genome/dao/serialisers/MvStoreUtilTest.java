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

package org.monarchinitiative.exomiser.core.genome.dao.serialisers;

import org.h2.mvstore.MVMap;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class MvStoreUtilTest {

    @Test
    public void alleleMapBuilder() throws Exception {
        MVMap.Builder<AlleleProto.AlleleKey, AlleleProto.AlleleProperties> alleleMapBuilder = MvStoreUtil.alleleMapBuilder();
        assertThat(alleleMapBuilder.getKeyType(), equalTo(AlleleKeyDataType.INSTANCE));
        assertThat(alleleMapBuilder.getValueType(), equalTo(AllelePropertiesDataType.INSTANCE));
    }

    @Test
    public void generateAlleleKey() throws Exception {
        Variant variant = VariantAnnotation.builder().chromosome(1).position(12345).ref("A").alt("T").build();
        AlleleProto.AlleleKey key = MvStoreUtil.generateAlleleKey(variant);

        assertThat(key.getChr(), equalTo(variant.getChromosome()));
        assertThat(key.getPosition(), equalTo(variant.getPosition()));
        assertThat(key.getRef(), equalTo(variant.getRef()));
        assertThat(key.getAlt(), equalTo(variant.getAlt()));
    }
}