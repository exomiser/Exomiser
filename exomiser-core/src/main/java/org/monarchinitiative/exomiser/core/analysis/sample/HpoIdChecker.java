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

package org.monarchinitiative.exomiser.core.analysis.sample;


import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class HpoIdChecker {

    private static final Logger logger = LoggerFactory.getLogger(HpoIdChecker.class);
    private static final Pattern HPO_ID_PATTERN = Pattern.compile("HP:[0-9]{7}");

    private final Map<String, PhenotypeTerm> termIdToTerms;

    public static HpoIdChecker parse(Path hpoOboFilePath) {
        logger.info("Parsing HPO from {}", hpoOboFilePath);

        Map<String, PhenotypeTerm> termIdToTerms = OboParser.parseOntology(hpoOboFilePath);

        logger.info("Loaded HPO with {} nodes", termIdToTerms.size());
        return new HpoIdChecker(termIdToTerms);
    }

    private HpoIdChecker(Map<String, PhenotypeTerm> termIdToTerms) {
        this.termIdToTerms = termIdToTerms;
    }

    public String getCurrentTermForId(String hpoId) {
        logger.debug("Checking ID {} is not obsolete", hpoId);
        Matcher hpoIdMatcher = HPO_ID_PATTERN.matcher(hpoId);
        if (!hpoIdMatcher.matches()) {
            throw new IllegalArgumentException("Input '" + hpoId + "' not a valid HPO identifier!");
        }
        PhenotypeTerm term = termIdToTerms.get(hpoId);
        if (term == null) {
            logger.warn("Input {} - unable to find current id so returning input {}", hpoId, hpoId);
            return hpoId;
        }
        if (hpoId.equals(term.getId())) {
            logger.debug("Input is current - {} ({})", hpoId, term.getLabel());
            return hpoId;
        } else {
            logger.info("Input term {} is obsolete. Replaced by {} ({})", hpoId, term.getId(), term.getLabel());
            return term.getId();
        }
    }

    /**
     * Yep, another OBO parser.
     */
    private static class OboParser {

        private static Map<String, PhenotypeTerm> parseOntology(Path oboFile) {
            Map<String, PhenotypeTerm> termIdToTerms = new HashMap<>();

            try(BufferedReader bufferedReader = Files.newBufferedReader(oboFile)) {
                // [Term]
                // id: HP:0000316
                // name: Hypertelorism
                // alt_id: HP:0000578
                // alt_id: HP:0002001
                // alt_id: HP:0004657
                // alt_id: HP:0007871

                String id = null;
                String name = null;
                List<String> altIds = new ArrayList<>();

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.startsWith("data-version:")){
                        String dataVersion = line.substring(13).trim();
                        logger.info("HPO version: {}", dataVersion);
                    }
                    else if (line.startsWith("id:")) {
                        id = line.substring(3).trim();
                    }
                    else if (line.startsWith("name:")) {
                        name = line.substring(5).trim();
                    }
                    else if (line.startsWith("alt_id:")){
                        altIds.add(line.substring(7).trim());
                    }
                    else if (line.isEmpty()) {
                        PhenotypeTerm phenotypeTerm = PhenotypeTerm.of(id, name);
                        // add current id
                        termIdToTerms.put(id, phenotypeTerm);
                        // add obsoleted ids
                        for (String altId : altIds) {
                            termIdToTerms.put(altId, phenotypeTerm);
                        }
                        altIds = new ArrayList<>();
                    }
                }

            } catch (IOException ex) {
                throw new RuntimeException("Error parsing OBO file " + oboFile, ex);
            }

            return termIdToTerms;
        }

    }
}
