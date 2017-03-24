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

package org.monarchinitiative.exomiser.db.parsers;

import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.db.resources.Resource;
import org.monarchinitiative.exomiser.db.resources.ResourceOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class has some common functionality for the OMIM and Orphanet parser
 * classes.
 *
 * @author Peter Robinson
 * @author Jules Jacobsen
 * @version 0.01 (9 February 2014)
 */
public class DiseaseInheritanceCache implements ResourceParser {

    private static final Logger logger = LoggerFactory.getLogger(DiseaseInheritanceCache.class);

    private final Map<String, InheritanceMode> hpoInheritanceCodes;

    /**
     * Key: OMIM id (or Orphanet ID) for a disease, Value: InheritanceMode
     */
    private Map<Integer, InheritanceMode> diseaseInheritanceModeMap;

    public DiseaseInheritanceCache() {
        hpoInheritanceCodes = setUpHpoInheritanceCodes();
    }

    private Map<String,InheritanceMode> setUpHpoInheritanceCodes() {
        Map map = new HashMap<>();
        //HP:0000005 is the root inheritance term - 'Mode of inheritance'. So not
        //really unknown, but vague enough.
        map.put("HP:0000005", InheritanceMode.UNKNOWN);
        map.put("HP:0000007", InheritanceMode.AUTOSOMAL_RECESSIVE);
        map.put("HP:0000006", InheritanceMode.AUTOSOMAL_DOMINANT);
        map.put("HP:0001417", InheritanceMode.X_LINKED);
        map.put("HP:0001419", InheritanceMode.X_RECESSIVE);
        map.put("HP:0001423", InheritanceMode.X_DOMINANT);
        map.put("HP:0001450", InheritanceMode.Y_LINKED);
        map.put("HP:0001428", InheritanceMode.SOMATIC);
        map.put("HP:0001427", InheritanceMode.MITOCHONDRIAL);
        map.put("HP:0010982", InheritanceMode.POLYGENIC);
        return map;
    }

    @Override
    public void parseResource(Resource resource, Path inDir, Path outDir) {
        
        Path phenotypeAnnotationFile = inDir.resolve(resource.getExtractedFileName());
        
        ResourceOperationStatus status = setUpCache(phenotypeAnnotationFile);
        
        resource.setParseStatus(status);
        logger.info("{}", status);
    }

    
    /**
     * Get an appropriate inheritance code for the disease represented by
     * phenID. Note that we return the code for somatic mutation only if AR and
     * AD and X are not true. The same is for polygenic.
     * 
     * Ensure that the parseResource() method has been successfully called before
     * trying to 
     *
     * @param diseaseId
     * @return
     */
    public InheritanceMode getInheritanceMode(Integer diseaseId) {
        InheritanceMode inheritanceMode = diseaseInheritanceModeMap.get(diseaseId);
        //maybe the disease hasn't been annotated so expect a null
        if (inheritanceMode == null) {
            inheritanceMode = InheritanceMode.UNKNOWN;
        }

        return inheritanceMode;
    }

    public boolean isEmpty() {
        return diseaseInheritanceModeMap == null || diseaseInheritanceModeMap.isEmpty();
    }
    /**
     * Parse the file "phenotype_annotation.tab" in order to get the modes of
     * inheritance that match the diseases. Skip lines that do not refer to OMIM
     * or that are negated ("NOT"). Note we do not distinguish here between
     * X-linked recessive and X-linked dominant. We go through one line at a
     * time and look for annotations to modes of inheritance. The function will
     * die if used with a parameter other than OMIM or Orphanet
     *
     * @param inFile String path to the file phenotype_annotation.tab
     */
    private ResourceOperationStatus setUpCache(Path inFile) {
        logger.info("Parsing inheritance modes from {} ", inFile);
        //initialise this here to avoid the ability to get false negatives if 
        //getInheritanceMode is called before the cache is initialised. 
        //In which case a nullPointer will be thrown.
        diseaseInheritanceModeMap = new HashMap<>();
        
        Charset charSet = Charset.forName("UTF-8");
        try (
                BufferedReader br = Files.newBufferedReader(inFile, charSet)) {

            String line;
            Integer diseaseId = null;

            //we're going to map each disease to all it's inheritance mode
            //from the HPO annotations in the file
            List<InheritanceMode> inheritanceModes = new ArrayList<>();
            //and store them in this intermediate map
            Map<Integer, List<InheritanceMode>> intermadiateDiseaseInheritanceMap = new HashMap<>();
            //so let's parse...
            while ((line = br.readLine()) != null) {
//                System.out.println(line);
                String[] fields = line.split("\t");
                if (fields[3].equals("NOT")) {
                    continue;
                }

                Integer currentDiseaseId = Integer.parseInt(fields[1]);
                //first line will have a null diseaseId
                if (diseaseId == null) {
                    diseaseId = currentDiseaseId;
                }

                //we've reached the end of this current set of disease annotations
                if (!currentDiseaseId.equals(diseaseId)) {
                    //add the current disease and HPO annotations
                    intermadiateDiseaseInheritanceMap.put(diseaseId, inheritanceModes);
                    //reset the counters for the next set of disease annotations
                    diseaseId = currentDiseaseId;
                    inheritanceModes = new ArrayList<>();
                }
                //get the hpo term
                String hpoTerm = fields[4];
                //only add the known inheritance mode
                InheritanceMode currentInheritance = hpoInheritanceCodes.getOrDefault(hpoTerm, InheritanceMode.UNKNOWN); //InheritanceMode.valueOfHpoTerm(hpoTerm);
                if (currentInheritance != InheritanceMode.UNKNOWN) {
                    inheritanceModes.add(currentInheritance);
                }
            }
            //remember to add the final disease
            intermadiateDiseaseInheritanceMap.put(diseaseId, inheritanceModes);
            //now we have the map of all diseases and theirt annotations we're going to extract the 
            //relevant inheritance modes and store these in the cache.
            diseaseInheritanceModeMap = finaliseInheritanceModes(intermadiateDiseaseInheritanceMap);
        } catch (FileNotFoundException ex) {
            logger.error("Could not find phenotype_annotation.tab file at location {}", inFile, ex);
            return ResourceOperationStatus.FILE_NOT_FOUND;
        } catch (IOException ex) {
            logger.error("Tried using Charset: {}", charSet);
            logger.error("Could not read phenotype_annotation.tab file from {}", inFile, ex);
            return ResourceOperationStatus.FAILURE;
        } 
        
        logger.debug(diseaseInheritanceModeMap.toString());
        return ResourceOperationStatus.SUCCESS;
    }

    private Map<Integer, InheritanceMode> finaliseInheritanceModes(Map<Integer, List<InheritanceMode>> diseaseInheritanceMap) {
        Map<Integer, InheritanceMode> inheritanceMap = new HashMap<>();

        for (Entry<Integer, List<InheritanceMode>> entry : diseaseInheritanceMap.entrySet()) {
            logger.debug("Mapping entry {} {}", entry.getKey(), entry.getValue());
            boolean isDominant = false;
            boolean isRecessive = false;
            InheritanceMode inheritanceMode = InheritanceMode.UNKNOWN;
            //trim out the unknowns
            for (InheritanceMode mode : entry.getValue()) {
                //bizzarrely some diseases appear to be both dominant and recessive
                if (mode == InheritanceMode.AUTOSOMAL_DOMINANT) {
                    isDominant = true;
                }
                if (mode == InheritanceMode.AUTOSOMAL_RECESSIVE) {
                    isRecessive = true;
                }
                if (mode != InheritanceMode.UNKNOWN) {
                    inheritanceMode = mode;
                }
            }
            logger.debug("InheritanceModes for {}: Dominant:{} Recessive:{}", entry.getKey(), isDominant, isRecessive);

            //now decide the inheritance - this ordering is important as mainly
            //we're interested in whether the disease is dominant or recessive in order to 
            //check wether the observed inheritance patterns of the exome sequences match
            //that of the known disease.
            if (isDominant && isRecessive) {
                inheritanceMode = InheritanceMode.AUTOSOMAL_DOMINANT_AND_RECESSIVE;
            } else if (isDominant) {
                inheritanceMode = InheritanceMode.AUTOSOMAL_DOMINANT;
            } else if (isRecessive) {
                inheritanceMode = InheritanceMode.AUTOSOMAL_RECESSIVE;
            }
            logger.debug("Setting inheritanceMode for {} to {}", entry.getKey(), inheritanceMode);

            inheritanceMap.put(entry.getKey(), inheritanceMode);

        }
        return inheritanceMap;
    }
}
