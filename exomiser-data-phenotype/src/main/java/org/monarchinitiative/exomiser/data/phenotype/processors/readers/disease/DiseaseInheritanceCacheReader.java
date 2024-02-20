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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.ResourceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class DiseaseInheritanceCacheReader implements ResourceReader<Map<String, InheritanceMode>> {

    private static final Logger logger = LoggerFactory.getLogger(DiseaseInheritanceCacheReader.class);

    private static final Map<String, InheritanceMode> hpoInheritanceCodes = new ImmutableMap.Builder<String, InheritanceMode>()
            //HP:0000005 is the root inheritance term - 'Mode of inheritance'. So not
            //really unknown, but vague enough.
            .put("HP:0000005", InheritanceMode.UNKNOWN)
            .put("HP:0000007", InheritanceMode.AUTOSOMAL_RECESSIVE)
            .put("HP:0000006", InheritanceMode.AUTOSOMAL_DOMINANT)
            // Semidominant mode of inheritance HP:0032113
            .put("HP:0032113", InheritanceMode.AUTOSOMAL_DOMINANT_AND_RECESSIVE)
            .put("HP:0001417", InheritanceMode.X_LINKED)
            .put("HP:0001419", InheritanceMode.X_RECESSIVE)
            .put("HP:0001423", InheritanceMode.X_DOMINANT)
            .put("HP:0001450", InheritanceMode.Y_LINKED)
            .put("HP:0001428", InheritanceMode.SOMATIC)
            // Somatic mosaicism HP:0001442
            .put("HP:0001442", InheritanceMode.SOMATIC)
            .put("HP:0001427", InheritanceMode.MITOCHONDRIAL)
            // Multifactorial inheritance HP:0001426
            .put("HP:0001426", InheritanceMode.POLYGENIC)
            // Polygenic inheritance HP:0010982
            .put("HP:0010982", InheritanceMode.POLYGENIC)
            // Oligogenic inheritance HP:0010983
            .put("HP:0010983", InheritanceMode.POLYGENIC)
            // Digenic inheritance HP:0010984
            .put("HP:0010984", InheritanceMode.POLYGENIC)
            .build();

    private final Resource hpoAnnotationsResource;

    public DiseaseInheritanceCacheReader(Resource hpoAnnotationsResource) {
        this.hpoAnnotationsResource = hpoAnnotationsResource;
    }

    @Override
    public Map<String, InheritanceMode> read() {
        Path phenotypeAnnotationsPath = hpoAnnotationsResource.getResourcePath();
        logger.info("Reading resource {} ", phenotypeAnnotationsPath);
        //initialise this here to avoid the ability to get false negatives if
        //getInheritanceMode is called before the cache is initialised.
        //In which case a nullPointer will be thrown.

        try (BufferedReader br = hpoAnnotationsResource.newBufferedReader()) {
            // we're going to map each disease to all it's inheritance mode
            // from the HPO annotations in the file and store them in this intermediate map
            ListMultimap<String, InheritanceMode> intermediateInheritanceMap = ArrayListMultimap.create();
            //so let's parse...
            for (String line; (line = br.readLine()) != null; ) {
                // database_id	disease_name	qualifier	hpo_id	reference	evidence	onset	frequency	sex	modifier	aspect	biocuration
                if (line.startsWith("#") || line.startsWith("database_id")) {
                    // comment line
                    continue;
                }
                String[] fields = line.split("\t");
                if (fields[2].equals("NOT")) {
                    continue;
                }
                String currentDiseaseId = fields[0];// + ":" + fields[1].replace(" ", "");
                InheritanceMode currentInheritance = hpoInheritanceCodes.getOrDefault(fields[3], InheritanceMode.UNKNOWN);
                // only add the known inheritance mode
                if (currentInheritance != InheritanceMode.UNKNOWN) {
                    logger.debug("Adding {} {}", currentDiseaseId, currentInheritance);
                    intermediateInheritanceMap.put(currentDiseaseId, currentInheritance);
                }
            }
            Map<String, InheritanceMode> diseaseInheritanceModeMap = finaliseInheritanceModes(intermediateInheritanceMap);
            logger.info("Extracted {} disease inheritance modes.", diseaseInheritanceModeMap.size());
            return diseaseInheritanceModeMap;
        } catch (Exception ex) {
            logger.error("Error processing {}", phenotypeAnnotationsPath, ex);
            throw new RuntimeException();
        }

    }

    private Map<String, InheritanceMode> finaliseInheritanceModes(ListMultimap<String, InheritanceMode> diseaseInheritanceMap) {
        ImmutableMap.Builder<String, InheritanceMode> inheritanceMap = new ImmutableMap.Builder<>();

        for (Map.Entry<String, Collection<InheritanceMode>> entry : diseaseInheritanceMap.asMap().entrySet()) {
            String diseaseId = entry.getKey();
            Collection<InheritanceMode> inheritanceModes = entry.getValue();
            logger.debug("Mapping entry {} {}", diseaseId, inheritanceModes);
            InheritanceMode inheritanceMode = InheritanceModeWrangler.wrangleInheritanceMode(inheritanceModes);
            inheritanceMap.put(diseaseId, inheritanceMode);
        }
        return inheritanceMap.build();
    }
}
