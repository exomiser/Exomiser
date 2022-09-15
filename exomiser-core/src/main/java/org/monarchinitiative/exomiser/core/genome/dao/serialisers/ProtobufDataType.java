package org.monarchinitiative.exomiser.core.genome.dao.serialisers;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import org.h2.mvstore.DataUtils;
import org.h2.mvstore.WriteBuffer;
import org.h2.mvstore.type.BasicDataType;
import org.h2.util.Utils;

import java.nio.ByteBuffer;

public abstract class ProtobufDataType<T extends Message> extends BasicDataType<T> {

    @Override
    public int getMemory(T obj) {
        return obj.getSerializedSize();
    }

    @Override
    public void write(WriteBuffer buff, T obj) {
        byte[] data = obj.toByteArray();
        buff.putVarInt(data.length).put(data);
    }

    @Override
    public T read(ByteBuffer buff) {
        int len = DataUtils.readVarInt(buff);
        byte[] data = Utils.newBytes(len);
        buff.get(data);
        try {
            return messageParser().parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new InvalidAlleleProtoException(e);
        }
    }

    public abstract Parser<T> messageParser();

}
