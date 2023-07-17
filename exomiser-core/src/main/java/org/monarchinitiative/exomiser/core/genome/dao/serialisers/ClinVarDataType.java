package org.monarchinitiative.exomiser.core.genome.dao.serialisers;

import com.google.protobuf.InvalidProtocolBufferException;
import org.h2.mvstore.DataUtils;
import org.h2.mvstore.WriteBuffer;
import org.h2.mvstore.type.DataType;
import org.h2.util.Utils;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;

import java.nio.ByteBuffer;


public enum ClinVarDataType implements DataType {

    INSTANCE;

    @Override
    public int compare(Object a, Object b) {
        return -1;
    }

    @Override
    public int getMemory(Object obj) {
        AlleleProto.ClinVar clinVar = (AlleleProto.ClinVar) obj;
        return clinVar.getSerializedSize();
    }

    @Override
    public void read(ByteBuffer buff, Object[] obj, int len, boolean key) {
        for (int i = 0; i < len; i++) {
            obj[i] = read(buff);
        }
    }

    @Override
    public AlleleProto.ClinVar read(ByteBuffer buff) {
        int len = DataUtils.readVarInt(buff);
        byte[] data = Utils.newBytes(len);
        buff.get(data);
        try {
            return AlleleProto.ClinVar.parseFrom(data);
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
        AlleleProto.ClinVar clinVar = (AlleleProto.ClinVar) obj;
        byte[] data = clinVar.toByteArray();
        buff.putVarInt(data.length).put(data);
    }
}
