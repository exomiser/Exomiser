package org.monarchinitiative.exomiser.core.prioritisers.reference;

import java.util.List;

public interface Network {

    List<String> evalCandidateGene(Integer entrezID);

}