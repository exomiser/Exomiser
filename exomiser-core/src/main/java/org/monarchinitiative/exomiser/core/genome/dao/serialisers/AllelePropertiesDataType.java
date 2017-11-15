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

import com.google.protobuf.InvalidProtocolBufferException;
import org.h2.mvstore.DataUtils;
import org.h2.mvstore.WriteBuffer;
import org.h2.mvstore.type.DataType;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleProperties;

import java.nio.ByteBuffer;

/**
 * Specialised {@link DataType} for (de)serialising {@link AlleleProperties} objects into and out of
 * the {@link org.h2.mvstore.MVStore}.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AllelePropertiesDataType implements DataType {

    public static final AllelePropertiesDataType INSTANCE = new AllelePropertiesDataType();

    @Override
    public int compare(Object a, Object b) {
        return -1;
    }

    @Override
    public int getMemory(Object obj) {
        AlleleProperties props = (AlleleProperties) obj;
        return props.getSerializedSize();
    }

    @Override
    public void read(ByteBuffer buff, Object[] obj, int len, boolean key) {
        for (int i = 0; i < len; i++) {
            obj[i] = read(buff);
        }
    }

    @Override
    public AlleleProperties read(ByteBuffer buff) {
        int len = DataUtils.readVarInt(buff);
        byte[] data = DataUtils.newBytes(len);
        buff.get(data);
        try {
            return AlleleProperties.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new InvalidAlleleProtoException(e);
        }
    }

    @Override
    public void write(WriteBuffer buff, Object[] obj, int len, boolean key) {
        for (int i = 0; i < len; i++) {
            write(buff, obj[i]);
        }
    }

    @Override
    public void write(WriteBuffer buff, Object obj) {
        AlleleProperties props = (AlleleProperties) obj;
        byte[] data = props.toByteArray();
        buff.putVarInt(data.length).put(data);
    }
}
