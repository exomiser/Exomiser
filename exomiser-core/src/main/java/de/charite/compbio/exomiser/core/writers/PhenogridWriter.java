/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.GeneModel;
import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.prioritisers.HiPhivePriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.exomiser.core.prioritisers.util.Species;
import de.charite.compbio.exomiser.core.writers.phenogrid.PhenoGrid;
import de.charite.compbio.exomiser.core.writers.phenogrid.PhenoGridMatch;
import de.charite.compbio.exomiser.core.writers.phenogrid.PhenoGridMatchGroup;
import de.charite.compbio.exomiser.core.writers.phenogrid.PhenoGridMatchScore;
import de.charite.compbio.exomiser.core.writers.phenogrid.PhenoGridMatchTaxon;
import de.charite.compbio.exomiser.core.writers.phenogrid.PhenoGridQueryTerms;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenogridWriter implements ResultsWriter {

    private static final Logger logger = LoggerFactory.getLogger(PhenogridWriter.class);    
    private static final OutputFormat OUTPUT_FORMAT = OutputFormat.PHENOGRID;
    
    @Override
    public void writeFile(SampleData sampleData, ExomiserSettings settings) {
        String outFileName = ResultsWriterUtils.determineFileExtension(settings.getOutFileName(), OUTPUT_FORMAT);
        Path outFile = Paths.get(outFileName);

        try (BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {

            writer.write(writeString(sampleData, settings));

        } catch (IOException ex) {
            logger.error("Unable to write results to file {}.", outFileName, ex);
        }
        logger.info("{} results written to file {}.", OUTPUT_FORMAT, outFileName);

    }

    @Override
    public String writeString(SampleData sampleData, ExomiserSettings settings) {
        List<HiPhivePriorityResult> hiPhiveResults = new ArrayList<>();
        for (Gene gene : sampleData.getGenes()) {
            if (gene.getPriorityResults().containsKey(PriorityType.HI_PHIVE_PRIORITY)) {
               HiPhivePriorityResult hiPhiveResult = (HiPhivePriorityResult) gene.getPriorityResult(PriorityType.HI_PHIVE_PRIORITY);
               hiPhiveResults.add(hiPhiveResult);
            }
        }

        PhenoGrid phenogrid = makePhenoGridFromHiPhiveResults(hiPhiveResults) ;
        return writePhenoGridAsJson(phenogrid);
    }

    private PhenoGrid makePhenoGridFromHiPhiveResults(List<HiPhivePriorityResult> hiPhiveResults) {
        Set<String> phenotypeIds = new LinkedHashSet<>();
        if (!hiPhiveResults.isEmpty()) {
            HiPhivePriorityResult result = hiPhiveResults.get(0);
            for (PhenotypeTerm phenoTerm : result.getQueryPhenotypeTerms()) {
                phenotypeIds.add(phenoTerm.getId());
            }
        }
        PhenoGridQueryTerms phenoGridQueryTerms = new PhenoGridQueryTerms("hiPhive specified phenotypes", phenotypeIds);
        
        List<GeneModel> diseaseModels = new ArrayList<>();
        List<GeneModel> mouseModels = new ArrayList<>();
        List<GeneModel> fishModels = new ArrayList<>();

        for (HiPhivePriorityResult result : hiPhiveResults) {
            for (Entry<Species, GeneModel> entry : result.getPhenotypeEvidence().entrySet()) {
                switch(entry.getKey()) {
                    case HUMAN:
                        diseaseModels.add(entry.getValue());
                        break;
                    case MOUSE:
                        mouseModels.add(entry.getValue());
                        break;
                    case FISH:
                        fishModels.add(entry.getValue());
                        break;
                }
            }
        }
        List<PhenoGridMatchGroup> phenoGridMatchGroups = createPhenogridMatchGroups(phenotypeIds, diseaseModels, mouseModels, fishModels);
        
        return new PhenoGrid(phenoGridQueryTerms, phenoGridMatchGroups);        
    }

    private List<PhenoGridMatchGroup> createPhenogridMatchGroups(Set<String> phenotypeIds, List<GeneModel> diseaseModels, List<GeneModel> mouseModels, List<GeneModel> fishModels) {
        List<PhenoGridMatchGroup> phenoGridMatchGroups = new ArrayList<>();
        
        PhenoGridMatchTaxon humanTaxon = new PhenoGridMatchTaxon("NCBITaxon:9606", "Homo sapiens");
        PhenoGridMatchGroup diseaseMatchGroup = makePhenoGridMatchGroup(humanTaxon, diseaseModels, phenotypeIds);
        phenoGridMatchGroups.add(diseaseMatchGroup);
        
        PhenoGridMatchTaxon mouseTaxon = new PhenoGridMatchTaxon("NCBITaxon:10090", "Mus musculus");
        PhenoGridMatchGroup mouseMatchGroup = makePhenoGridMatchGroup(mouseTaxon, mouseModels, phenotypeIds);
        phenoGridMatchGroups.add(mouseMatchGroup);
        
        PhenoGridMatchTaxon fishTaxon = new PhenoGridMatchTaxon("NCBITaxon:7955", "Danio rerio");
        PhenoGridMatchGroup fishMatchGroup = makePhenoGridMatchGroup(fishTaxon, fishModels, phenotypeIds);
        phenoGridMatchGroups.add(fishMatchGroup);
        
        return phenoGridMatchGroups;
    }

    private PhenoGridMatchGroup makePhenoGridMatchGroup(PhenoGridMatchTaxon taxon, List<GeneModel> geneModels, Set<String> phenotypeIds) {
        List<PhenoGridMatch> phenoGridMatches = makePhenogridMatchesFromGeneModels(geneModels, taxon);
        PhenoGridMatchGroup phenoGridMatchGroup = new PhenoGridMatchGroup(phenoGridMatches, phenotypeIds);
        return phenoGridMatchGroup;
    }

    private List<PhenoGridMatch> makePhenogridMatchesFromGeneModels(List<GeneModel> geneModels, PhenoGridMatchTaxon taxon) {
        List<PhenoGridMatch> phenoGridMatches = new ArrayList<>();
        int modelCount = 0;
        for (GeneModel model : geneModels) {
            PhenoGridMatchScore score = new PhenoGridMatchScore("hiPhive", (int) (model.getScore() * 100f), modelCount++);
            List<PhenotypeMatch> phenotypeMatches = new ArrayList<>(model.getBestPhenotypeMatchForTerms().values());
            String modelType = "gene";
            if ("9606".equals(taxon.getId())) {
                modelType = "disease";
            }
            PhenoGridMatch match = new PhenoGridMatch(model.getModelId(), model.getModelSymbol(), modelType, phenotypeMatches, score, taxon);
            phenoGridMatches.add(match);            
        }
        
        return phenoGridMatches;
    }
    
    private String writePhenoGridAsJson(PhenoGrid phenogrid) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        try {
            return mapper.writeValueAsString(phenogrid);
        } catch (JsonProcessingException ex) {
            logger.error("Error mapping PhenoGrid to json", ex);
        }
        return "Error mapping PhenoGrid to json";
    }

}
