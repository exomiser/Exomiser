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

package org.monarchinitiative.exomiser.core.phenotype.service;


import com.google.common.collect.ImmutableMap;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for checking input HPO identifiers against those from the hp.obo ontology file.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 12.0.0
 */
public class HpoIdChecker {

    private static final Logger logger = LoggerFactory.getLogger(HpoIdChecker.class);
    private static final Pattern HPO_ID_PATTERN = Pattern.compile("HP:[0-9]{7}");

    private final Map<String, PhenotypeTerm> termIdToTerms;

    /**
     *
     * @param termIdToTerms A map of all current and
     * @return a new instance of the HpoIdChecker
     */
    public static HpoIdChecker of(Map<String, PhenotypeTerm> termIdToTerms) {
        Objects.requireNonNull(termIdToTerms);
        return new HpoIdChecker(termIdToTerms);
    }

    private HpoIdChecker(Map<String, PhenotypeTerm> termIdToTerms) {
        this.termIdToTerms = ImmutableMap.copyOf(termIdToTerms);
    }

    /**
     * Checks the input id and returns the current version of it. Invalid HPO ids will throw an exception, unrecognised
     * yet valid ids will be returned as is.
     * @param hpoId The hpoId for which the current version is required
     * @return the current hpoId for the input id
     * @throws IllegalArgumentException when fed an invalid HPO identifier.
     */
    public String getCurrentId(String hpoId) {
        PhenotypeTerm term = getCurrentTerm(hpoId);
        return term == null ? hpoId : term.getId();
    }

    /**
     * Checks the input id and returns the current {@link PhenotypeTerm} for it. Invalid HPO ids will throw an exception,
     * unrecognised yet valid ids will return null values.
     *
     * @param hpoId The hpoId for which the current version is required
     * @return the current {@link PhenotypeTerm} for the input id or null if invalid/unrecognised
     * @throws IllegalArgumentException when fed an invalid HPO identifier.
     */
    @Nullable
    public PhenotypeTerm getCurrentTerm(String hpoId) {
        logger.debug("Checking ID {} is not obsolete", hpoId);
        Matcher hpoIdMatcher = HPO_ID_PATTERN.matcher(hpoId);
        if (!hpoIdMatcher.matches()) {
            throw new IllegalArgumentException("Input '" + hpoId + "' not a valid HPO identifier!");
        }
        PhenotypeTerm term = termIdToTerms.get(hpoId);
        if (term == null) {
            logger.warn("Input {} - unable to find current id. Returning input {}", hpoId, hpoId);
            return null;
        }
        if (hpoId.equals(term.getId())) {
            logger.debug("Input is current - {} ({})", hpoId, term.getLabel());
            return term;
        } else {
            logger.info("Input term {} is obsolete. Replaced by {} ({})", hpoId, term.getId(), term.getLabel());
            return term;
        }
    }
}
