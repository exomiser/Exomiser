/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

import com.google.protobuf.Parser;
import org.h2.mvstore.type.DataType;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleProperties;

/**
 * Specialised {@link DataType} for (de)serialising {@link AlleleProperties} objects into and out of
 * the {@link org.h2.mvstore.MVStore}.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AllelePropertiesDataType extends ProtobufDataType<AlleleProperties> {

    public static final AllelePropertiesDataType INSTANCE = new AllelePropertiesDataType();

    private AllelePropertiesDataType() {
    }

    @Override
    public int compare(AlleleProperties a, AlleleProperties b) {
        if (a.equals(b)) {
            return 0;
        }
        throw new UnsupportedOperationException("Unable to compare " + a + " with " + b);
    }

    @Override
    public AlleleProperties[] createStorage(int size) {
        return new AlleleProperties[size];
    }

    @Override
    public Parser<AlleleProperties> messageParser() {
        return AlleleProperties.parser();
    }
}
