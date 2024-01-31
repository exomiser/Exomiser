package org.monarchinitiative.exomiser.core.genome.dao.serialisers;

import com.google.protobuf.Parser;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.ClinVar;


public class ClinVarDataType extends ProtobufDataType<ClinVar> {

    public static final ClinVarDataType INSTANCE = new ClinVarDataType();

    @Override
    public int compare(ClinVar a, ClinVar b) {
        if (a.equals(b)) {
            return 0;
        }
        throw new UnsupportedOperationException("Unable to compare " + a + " with " + b);
    }

    @Override
    public ClinVar[] createStorage(int size) {
        return new ClinVar[size];
    }

    @Override
    public Parser<ClinVar> messageParser() {
        return ClinVar.parser();
    }
}
