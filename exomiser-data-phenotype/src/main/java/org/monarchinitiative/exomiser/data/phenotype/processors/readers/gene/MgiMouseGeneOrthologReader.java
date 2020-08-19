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

package org.monarchinitiative.exomiser.data.phenotype.processors.readers.gene;

import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.gene.GeneOrtholog;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.ResourceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class MgiMouseGeneOrthologReader implements ResourceReader<List<GeneOrtholog>> {

    private static final Logger logger = LoggerFactory.getLogger(MgiMouseGeneOrthologReader.class);

    private final Resource homMouseHumanSequenceResource;

    public MgiMouseGeneOrthologReader(Resource homMouseHumanSequenceResource) {
        this.homMouseHumanSequenceResource = homMouseHumanSequenceResource;
    }

    @Override
    public List<GeneOrtholog> read() {
        List<GeneOrtholog> mouseHumanOrthologs = new ArrayList<>();

        Map<String, String> mouseId2Symbol = new LinkedHashMap<>();
        Map<String, String> humanId2Symbol = new LinkedHashMap<>();

        Map<String, Set<String>> mouseIds = new LinkedHashMap<>();
        Map<String, Set<String>> humanIds = new LinkedHashMap<>();

        try (BufferedReader reader = homMouseHumanSequenceResource.newBufferedReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\\t");
                String homoloGeneId = fields[0];
                if (fields[1].equals("human")) {
                    String humanSymbol = fields[3];
                    String humanId = fields[4];
                    Set<String> humanGeneIds = humanIds.get(homoloGeneId);
                    if (null != humanGeneIds) {
                        humanGeneIds.add(humanId);
                    } else {
                        Set<String> newSet = new LinkedHashSet<>();
                        newSet.add(humanId);
                        humanIds.put(homoloGeneId, newSet);
                    }
                    humanId2Symbol.put(humanId, humanSymbol);
                } else if (fields[1].equals("mouse, laboratory")) {
                    String mouseSymbol = fields[3];
                    String mouseId = fields[5];
                    Set<String> mouseGeneIds = mouseIds.get(homoloGeneId);
                    if (null != mouseGeneIds) {
                        mouseGeneIds.add(mouseId);
                    } else {
                        Set<String> newSet = new LinkedHashSet<>();
                        newSet.add(mouseId);
                        mouseIds.put(homoloGeneId, newSet);
                    }

                    mouseId2Symbol.put(mouseId, mouseSymbol);
                }
            }
        } catch (IOException e) {
            logger.error("", e);
        }

        for (Map.Entry<String, Set<String>> entry : mouseIds.entrySet()) {
            String homoloGeneId = entry.getKey();
            Set<String> mouseGeneIds = entry.getValue();
            Set<String> humanGeneIds = humanIds.getOrDefault(homoloGeneId, Set.of());
            for (String mouseId : mouseGeneIds) {
                for (String humanId : humanGeneIds) {
                    String mouseSymbol = mouseId2Symbol.get(mouseId);
                    String humanGeneSymbol = humanId2Symbol.get(humanId);
                    GeneOrtholog geneOrtholog = new GeneOrtholog(mouseId, mouseSymbol, humanGeneSymbol, Integer.parseInt(humanId));
                    mouseHumanOrthologs.add(geneOrtholog);
                }
            }
        }

        return mouseHumanOrthologs;
    }
}
