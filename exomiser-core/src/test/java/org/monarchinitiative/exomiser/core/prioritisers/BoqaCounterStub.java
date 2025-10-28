package org.monarchinitiative.exomiser.core.prioritisers;

import org.p2gx.boqa.core.Counter;
import org.p2gx.boqa.core.PatientData;
import org.p2gx.boqa.core.algorithm.BoqaCounts;

import java.util.Set;

public class BoqaCounterStub implements Counter {
    @Override
    public BoqaCounts computeBoqaCounts(String s, PatientData patientData) {
        return new BoqaCounts("", "", 0, 0, 0, 0 );
    }

    @Override
    public Set<String> getDiseaseIds() {
        return Set.of();
    }
}
