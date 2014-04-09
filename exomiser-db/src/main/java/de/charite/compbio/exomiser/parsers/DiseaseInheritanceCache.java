package de.charite.compbio.exomiser.parsers;

import de.charite.compbio.exomiser.core.InheritanceMode;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class has some common functionality for the OMIM and Orphanet parser
 * classes.
 *
 * @author Peter Robinson
 * @author Jules Jacobsen
 * @version 0.01 (9 February 2014)
 */
public class DiseaseInheritanceCache {

    private static final Logger logger = LoggerFactory.getLogger(DiseaseInheritanceCache.class);

    /**
     * Key: OMIM id (or Orphanet ID) for a disease, Value: InheritanceMode
     */
    private Map<Integer, InheritanceMode> diseaseInheritanceModeMap;

    public DiseaseInheritanceCache(String phenotypeAnnotationFile) {
        diseaseInheritanceModeMap = new HashMap<>();
        setUpCache(phenotypeAnnotationFile);
    }

    /**
     * Get an appropriate inheritance code for the disease represented by
     * phenID. Note that we return the code for somatic mutation only if AR and
     * AD and X are not true. The same is for polygenic.
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
        return diseaseInheritanceModeMap.isEmpty();
    }
    /**
     * Parse the file "phenotype_annotation.tab" in order to get the modes of
     * inheritance that match the diseases. Skip lines that do not refer to OMIM
     * or that are negated ("NOT"). Note we do not distinguish here between
     * X-linked recessive and X-linked dominant. We go through one line at a
     * time and look for annotations to modes of inheritance. The function will
     * die if used with a parameter other than OMIM or Orphanet
     *
     * @param inPath String path to the file phenotype_annotation.tab
     */
    private void setUpCache(String inPath) {
        logger.info("Parsing inheritance modes from {} ", inPath);

        try (FileReader fileReader = new FileReader(inPath);
                BufferedReader br = new BufferedReader(fileReader)) {

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
                String split[] = line.split("\t");
                if (split[3].equals("NOT")) {
                    continue;
                }

                Integer currentDiseaseId = Integer.parseInt(split[1]);
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
                String hpoTerm = split[4];
                //only add the known inheritance mode
                InheritanceMode currentInheritance = InheritanceMode.valueOfHpoTerm(hpoTerm);
                if (currentInheritance != InheritanceMode.UNKNOWN) {
                    inheritanceModes.add(currentInheritance);
                }
            }
            //remember to add the final disease
            intermadiateDiseaseInheritanceMap.put(diseaseId, inheritanceModes);
            //now we have the map of all diseases and theirt annotations we're going to extract the 
            //relevant inheritance modes and store these in the cache.
            diseaseInheritanceModeMap = finaliseInheritanceModes(intermadiateDiseaseInheritanceMap);
        } catch (IOException e) {
            logger.error("Could not read phenotype_annotation.tab file from {}", inPath, e);
        }
        logger.info("Extracted inheritance modes for {} diseases", diseaseInheritanceModeMap.size());
        logger.debug(diseaseInheritanceModeMap.toString());
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
                if (mode == InheritanceMode.DOMINANT) {
                    isDominant = true;
                }
                if (mode == InheritanceMode.RECESSIVE) {
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
                inheritanceMode = InheritanceMode.DOMINANT_AND_RECESSIVE;
            } else if (isDominant) {
                inheritanceMode = InheritanceMode.DOMINANT;
            } else if (isRecessive) {
                inheritanceMode = InheritanceMode.RECESSIVE;
            }
            logger.debug("Setting inheritanceMode for {} to {}", entry.getKey(), inheritanceMode);

            inheritanceMap.put(entry.getKey(), inheritanceMode);

        }
        return inheritanceMap;
    }
}
