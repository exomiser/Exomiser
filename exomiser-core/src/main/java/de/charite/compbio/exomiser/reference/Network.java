package de.charite.compbio.exomiser.reference;

import java.util.List;

public interface Network {

    public List<String> evalCandidateGene(Integer entrezID);

}