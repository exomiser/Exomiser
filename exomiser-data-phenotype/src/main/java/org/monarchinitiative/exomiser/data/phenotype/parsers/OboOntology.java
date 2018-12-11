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

package org.monarchinitiative.exomiser.data.phenotype.parsers;

import com.google.common.collect.ImmutableMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Very simple class to represent the ontology data found in an obo ontology which we require directly in
 * the Exomiser. This is *not* a complete representation of that data and contains no edge data.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class OboOntology {

    // parsed data
    private final String dataVersion;
    private final List<OboOntologyTerm> currentOntologyTerms;
    private final List<OboOntologyTerm> obsoleteOntologyTerms;
    private final Map<String, OboOntologyTerm> obsoleteIdToCurrentTerms;

    public OboOntology(String dataVersion, List<OboOntologyTerm> ontologyTerms) {
        Objects.requireNonNull(ontologyTerms);
        this.dataVersion = dataVersion;
        this.currentOntologyTerms = ontologyTerms.stream()
                .filter(ontologyTerm -> !ontologyTerm.isObsolete())
                .collect(Collectors.toList());
        this.obsoleteOntologyTerms = ontologyTerms.stream()
                .filter(OboOntologyTerm::isObsolete)
                .collect(Collectors.toList());
        this.obsoleteIdToCurrentTerms = makeObsoleteTerms(currentOntologyTerms, obsoleteOntologyTerms);
    }

    private Map<String, OboOntologyTerm> makeObsoleteTerms(List<OboOntologyTerm> currentOntologyTerms, List<OboOntologyTerm> obsoleteOntologyTerms) {
        Map<String, OboOntologyTerm> idToTerms = new LinkedHashMap<>();

        for (OboOntologyTerm ontologyTerm : currentOntologyTerms) {
            idToTerms.put(ontologyTerm.getId(), ontologyTerm);
            for (String altId : ontologyTerm.getAltIds()) {
                idToTerms.put(altId, ontologyTerm);
            }
        }
        // in the case of the MPO the alt ids of the current term do not contain the id of the obsolete terms which
        // contains a replaced_by term. Newer versions of the HPO handle this better by already adding the alt_id to
        // the current term.
        for (OboOntologyTerm obsoleteTerm : obsoleteOntologyTerms) {
            String replacedById = obsoleteTerm.getReplacedBy();
            OboOntologyTerm currentTerm = idToTerms.get(replacedById);
            if (currentTerm  != null) {
                idToTerms.put(obsoleteTerm.getId(), currentTerm);
                // Handle edge-case case where this happened:
                // OboOntologyTerm{id='HP:0009449', label='Hypoplastic/small phalanges of the 3rd finger', obsolete=true, altIds=[HP:0004158, HP:0004164, HP:0004165], replacedBy='HP:0009447'}
                obsoleteTerm.getAltIds().forEach(altId -> idToTerms.put(altId, currentTerm));
            }
        }

        return ImmutableMap.copyOf(idToTerms);
    }

    public String getDataVersion() {
        return dataVersion;
    }

    public List<OboOntologyTerm> getCurrentOntologyTerms() {
        return currentOntologyTerms;
    }

    public List<OboOntologyTerm> getObsoleteOntologyTerms() {
        return obsoleteOntologyTerms;
    }

    public Map<String, OboOntologyTerm> getIdToTerms() {
        return obsoleteIdToCurrentTerms;
    }
}
