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
import java.util.*;

/**
 * @since 13.1.0
 */
public class GeneConstraints {

    private static final Map<String, GeneConstraint> GENE_CONSTRAINTS = GnomadGeneConstraintParser.readGeneConstraints("gnomad.v4.0.gene-constraints.tsv");

    private GeneConstraints() {
    }

    @Nullable
    public static GeneConstraint geneConstraint(String geneSymbol) {
        return GENE_CONSTRAINTS.get(geneSymbol);
    }

    public static Collection<GeneConstraint> geneConstraints() {
        return GENE_CONSTRAINTS.values();
    }

    private static class GnomadGeneConstraintParser {

        private static Map<String, GeneConstraint> readGeneConstraints(String constraintsFile) {
            Map<String, GeneConstraint> geneConstraints = new LinkedHashMap<>();
            // #gene	transcript	pLI	oe_lof	oe_lof_lower	oe_lof_upper
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getResourceAsStream(constraintsFile)))) {
                for (String line; (line = bufferedReader.readLine()) != null; ) {
                    if (!line.startsWith("#")) {
                        GeneConstraint geneConstraint = parseGeneConstraint(line);
                        if (geneConstraint != null) {
                            geneConstraints.put(geneConstraint.geneSymbol(), geneConstraint);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Collections.unmodifiableMap(geneConstraints);
        }

        private static GeneConstraint parseGeneConstraint(String line) {
            // gene	transcript	mane_select	lof.oe	lof.pLI	lof.oe_ci.lower	lof.oe_ci.upper	mis.z_score	syn.z_score
            String[] fields = line.split("\t");
            String geneSymbol = fields[0];
            String transcriptId = fields[1];
            boolean isManeSelect = Boolean.parseBoolean(fields[2]);
            if (geneSymbol.equals("NA") || !isManeSelect) {
                return null;
            }
            double pLI = parseDouble(fields[4]);
            double loeuf = parseDouble(fields[3]);
            double loeufLower = parseDouble(fields[5]);
            double loeufUpper = parseDouble(fields[6]);
            double missenseZ = parseDouble(fields[7]);
            double synonymousZ = parseDouble(fields[8]);
            return new GeneConstraint(geneSymbol, transcriptId, pLI, loeuf, loeufLower, loeufUpper, missenseZ, synonymousZ);
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
