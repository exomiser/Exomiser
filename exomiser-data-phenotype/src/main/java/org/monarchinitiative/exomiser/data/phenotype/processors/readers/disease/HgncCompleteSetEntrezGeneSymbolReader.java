/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.phenotype.processors.readers.disease;

import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.EntrezIdGeneSymbol;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.ResourceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class HgncCompleteSetEntrezGeneSymbolReader implements ResourceReader<List<EntrezIdGeneSymbol>> {

    private static final Logger logger = LoggerFactory.getLogger(HgncCompleteSetEntrezGeneSymbolReader.class);

    private final Resource hgncCompleteSetResource;

    public HgncCompleteSetEntrezGeneSymbolReader(Resource hgncCompleteSetResource) {
        this.hgncCompleteSetResource = hgncCompleteSetResource;
    }

    @Override
    public List<EntrezIdGeneSymbol> read() {
        List<EntrezIdGeneSymbol> entrezGeneIds = new ArrayList<>(20000);
        try (BufferedReader reader = hgncCompleteSetResource.newBufferedReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                EntrezIdGeneSymbol entrezIdGeneSymbol = parseLine(line);
                if (entrezIdGeneSymbol != null) {
                    entrezGeneIds.add(entrezIdGeneSymbol);
                }
            }
        } catch (IOException e) {
            logger.error("Error reading file {} ", hgncCompleteSetResource.getResourcePath(), e);
        }
        logger.info("Extracted {} Entrez ID - gene symbol mappings from {}", entrezGeneIds.size(), hgncCompleteSetResource.getResourcePath());
        return entrezGeneIds;
    }

    @Nullable
    private EntrezIdGeneSymbol parseLine(String line) {
        String[] split = line.split("\t");
        if (!split[0].startsWith("HGNC:") || split[4].equals("Entry Withdrawn")
                || split[3].equals("phenotype")
                || split.length <= 18 || split[18].isEmpty()) {
            return null;
        }

        try {
            if (split[1] == null || split[1].isEmpty()) {
                logger.warn("Could not extract symbol, skipping line: {}", line);
                return null;
            }
            int entrez = Integer.parseInt(split[18]);
            String symbol = split[1];

            return new EntrezIdGeneSymbol(entrez, symbol);

        } catch (NumberFormatException e) {
            logger.error("Malformed line: {} (could not parse entrez gene field: '{}')", line, split[1]);
        }
        return null;
    }
}
