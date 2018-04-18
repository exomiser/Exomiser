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

import com.google.protobuf.InvalidProtocolBufferException;
import org.h2.mvstore.DataUtils;
import org.h2.mvstore.WriteBuffer;
import org.h2.mvstore.type.DataType;
import org.h2.util.Utils;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleKey;

import java.nio.ByteBuffer;

/**
 * Specialised {@link DataType} for (de)serialising {@link AlleleKey} objects into and out of
 * the {@link org.h2.mvstore.MVStore}.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AlleleKeyDataType implements DataType {

    public static final AlleleKeyDataType INSTANCE = new AlleleKeyDataType();

    /**
     * Sorts variants according to their natural ordering of genome position. Variants are sorted according to
     * chromosome number, chromosome position, reference sequence then alternative sequence.
     *
     * @param b
     * @return comparator score consistent with equals.
     */
    @Override
    public int compare(Object a, Object b) {
        AlleleKey keyA = (AlleleKey) a;
        AlleleKey keyB = (AlleleKey) b;

        if (keyA.getChr() != keyB.getChr()) {
            return Integer.compare(keyA.getChr(), keyB.getChr());
        }
        if (keyA.getPosition() != keyB.getPosition()) {
            return Integer.compare(keyA.getPosition(), keyB.getPosition());
        }
        if (!keyA.getRef().equals(keyB.getRef())) {
            return keyA.getRef().compareTo(keyB.getRef());
        }
        return keyA.getAlt().compareTo(keyB.getAlt());
    }

    @Override
    public int getMemory(Object obj) {
        AlleleKey key = (AlleleKey) obj;
        return key.getSerializedSize();
    }

    @Override
    public void read(ByteBuffer buff, Object[] obj, int len, boolean key) {
        for (int i = 0; i < len; i++) {
            obj[i] = read(buff);
        }
    }

    @Override
    public void write(WriteBuffer buff, Object[] obj, int len, boolean key) {
        for (int i = 0; i < len; i++) {
            write(buff, obj[i]);
        }
    }

    @Override
    public AlleleKey read(ByteBuffer buff) {
        int len = DataUtils.readVarInt(buff);
        byte[] data = Utils.newBytes(len);
        buff.get(data);
        try {
            return AlleleKey.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new InvalidAlleleProtoException(e);
        }
    }

    @Override
    public void write(WriteBuffer buff, Object obj) {
        AlleleKey key = (AlleleKey) obj;
        byte[] data = key.toByteArray();
        buff.putVarInt(data.length).put(data);
    }

}
