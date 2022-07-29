/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.analysis.util;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @since 13.1.0
 */
public class GeneConstraints {

    private static final Map<String, GeneConstraint> geneConstraints = GnomadGeneConstraintParser.readGeneConstraints("gnomad.v2.1.1.gene-constraints.tsv");

    private GeneConstraints() {
    }

    @Nullable
    public static GeneConstraint geneContraint(String geneSymbol) {
        return geneConstraints.get(geneSymbol);
    }

    private static class GnomadGeneConstraintParser {

        private static Map<String, GeneConstraint> readGeneConstraints(String constraintsFile) {
            Map<String, GeneConstraint> geneConstraints = new LinkedHashMap<>();
            // #gene	transcript	pLI	oe_lof	oe_lof_lower	oe_lof_upper
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getResourceAsStream(constraintsFile)))) {
                for (String line; (line = bufferedReader.readLine()) != null; ) {
                    if (!line.startsWith("#")) {
                        GeneConstraint geneContraint = parseGeneConstraint(line);
                        if (!Double.isNaN(geneContraint.loeufUpper())) {
                            geneConstraints.put(geneContraint.geneSymbol(), geneContraint);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Collections.unmodifiableMap(geneConstraints);
        }

        private static GeneConstraint parseGeneConstraint(String line) {
            String[] fields = line.split("\t");
            String geneSymbol = fields[0];
            String transcriptId = fields[1];
            double pLI = parseDouble(fields[2]);
            double loeuf = parseDouble(fields[3]);
            double loeufLower = parseDouble(fields[4]);
            double loeufUpper = parseDouble(fields[5]);
            return new GeneConstraint(geneSymbol, transcriptId, pLI, loeuf, loeufLower, loeufUpper);
        }

        private static double parseDouble(String field) {
            try {
                return Double.parseDouble(field);
            } catch (NumberFormatException e) {
                // swallow
            }
            return Double.NaN;
        }

        private static InputStream getResourceAsStream(String path) {
            InputStream localResourceStream = GnomadGeneConstraintParser.class.getClassLoader().getResourceAsStream(path);
            if (localResourceStream != null) {
                return localResourceStream;
            }
            localResourceStream = ClassLoader.getSystemClassLoader().getResourceAsStream(path);
            if (localResourceStream != null) {
                return localResourceStream;
            }
            // load from the module path
            Optional<Module> moduleOptional = ModuleLayer.boot().findModule("org.monarchitiative.exomiser");
            if (moduleOptional.isPresent()) {
                Module module = moduleOptional.get();
                try {
                    return module.getResourceAsStream(path);
                } catch (IOException e) {
                    // swallow and fall through
                }
            }
            throw new IllegalStateException("Unable to load resource " + path);
        }
    }
}
