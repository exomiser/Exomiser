package org.monarchinitiative.exomiser.core.prioritisers;

import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDiseases;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.Iterator;
import java.util.Optional;

public class HpoDiseasesStub implements HpoDiseases {
    @Override
    public Optional<HpoDisease> diseaseById(TermId termId) {
        return Optional.empty();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Iterator<HpoDisease> iterator() {
        return null;
    }

    @Override
    public Optional<String> version() {
        return Optional.empty();
    }
}
