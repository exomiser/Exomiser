/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.prioritisers.dao;

import com.google.common.collect.ImmutableSet;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TestDiseaseDao implements DiseaseDao {

    private final Set<Disease> diseases;
    private final Map<Integer, List<Disease>> geneDiseaseAssociations;

    public TestDiseaseDao(List<Disease> diseases) {
        this.diseases = new LinkedHashSet<>(diseases);
        geneDiseaseAssociations = diseases.stream().collect(groupingBy(Disease::getAssociatedGeneId));
    }

    @Override
    public Set<String> getHpoIdsForDiseaseId(String diseaseId) {
        return diseases.stream()
                .filter(entry -> entry.getDiseaseId().equals(diseaseId))
                .flatMap(entry -> entry.getPhenotypeIds().stream())
                .collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public List<Disease> getDiseaseDataAssociatedWithGeneId(int geneId) {
        return geneDiseaseAssociations.getOrDefault(geneId, Collections.emptyList());
    }
}
