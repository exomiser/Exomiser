package de.charite.compbio.exomiser.core.prioritisers.reference;

import java.util.List;

public interface Network {

    public List<String> evalCandidateGene(Integer entrezID);

}