/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.priority;

import de.charite.compbio.exomiser.priority.util.DataMatrix;
import de.charite.compbio.exomiser.core.ExomiserSettings;
import jannovar.common.ModeOfInheritance;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Factory class for handling creation of FilterType objects.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class PriorityFactory {

    private static final Logger logger = LoggerFactory.getLogger(PriorityFactory.class);

    @Autowired
    private DataSource dataSource;
    @Autowired
    @Lazy
    private DataMatrix randomWalkMatrix;
    @Autowired
    private Path phenomizerDataDirectory;
    @Autowired
    private Path hpoOntologyFilePath;
    @Autowired
    private Path hpoAnnotationFilePath;
    
    public PriorityFactory() {
    }

    public PriorityFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    public List<Priority> makePrioritisers(ExomiserSettings exomiserSettings) {
        
        String disease = exomiserSettings.getDiseaseId();
        String candidateGene = exomiserSettings.getCandidateGene();
        List<String> hpoIds = exomiserSettings.getHpoIds();
        List<Integer> entrezSeedGenes = exomiserSettings.getSeedGeneList();
        
        List<Priority> genePriorityList = new ArrayList<>();
        //TODO: these chaps are usually specified implicitly - perhaps they should be different types of ExomiserSettings?
        //probably better as a specific type of Exomiser - either a RareDiseaseExomiser or DefaultExomiser. These might be badly named as the OMIM proritiser is currently the default.
        //InheritancePriority ALWAYS runs but uses a default InheritanceMode.UNSPECIFIED
//        commandLineToPriorityMap.put(vcfFilePath, InheritancePriority.class);
        //TODO: check how this works - I think it is always included.
//        commandLineToPriorityMap.put(vcfFilePath, OMIMPriority.class);
        //not sure if this is actually used at all....
//        commandLineToPriorityMap.put(vcfFilePath, DynamicPhenodigmPriority.class);

//        this.prioritiser.addOMIMPrioritizer();
        genePriorityList.add(getOmimPrioritizer());
//            this.prioritiser.addInheritancePrioritiser(this.inheritance_filter_type);
        genePriorityList.add(new InheritancePriority(exomiserSettings.getModeOfInheritance()));

        switch (exomiserSettings.getPrioritiserType()) {
            case PHENODIGM_MGI_PRIORITY:
                genePriorityList.add(getMGIPhenodigmPrioritiser(disease));
                break;
            case PHENOMIZER_PRIORITY:
                genePriorityList.add(getPhenomizerPrioritiser(hpoIds));
                break;
            case BOQA_PRIORITY:
                genePriorityList.add(getBOQAPrioritiser(hpoIds));
                break;            
            case DYNAMIC_PHENOWANDERER_PRIORITY:
                genePriorityList.add(getDynamicPhenoWandererPrioritiser(hpoIds, candidateGene, disease));
                break; 
            case DYNAMIC_PHENOWANDERER_PRIORITY_MONARCH_TABLES:
                genePriorityList.add(getDynamicPhenoWandererPrioritiserMonarchTables(hpoIds, candidateGene, disease));
                break;   
            case DYNAMIC_PHENODIGM_PRIORITY:
                genePriorityList.add(getDynamicPhenodigmPrioritiser(hpoIds));
                break;
            case GENEWANDERER_PRIORITY:
                genePriorityList.add(getGeneWandererPrioritiser(entrezSeedGenes));
                break;               
            
        }

        return genePriorityList;
    }

    public Priority getOmimPrioritizer() {
        Priority priority = new OMIMPriority();

        try {
            priority.setDatabaseConnection(dataSource.getConnection());
        } catch (SQLException ex) {
            logger.error(null, ex);
        }

        logger.info("Made new OMIM Priority: {}", priority);
        return priority;
    }

    public Priority getInheritancePrioritiser(ModeOfInheritance modeOfInheritance){
        Priority priority = new InheritancePriority(modeOfInheritance);
        logger.info("Made new Inheritance Priority: {}", priority);
        return priority;
    }

    public Priority getPhenomizerPrioritiser(List<String> hpoIds) {
        Set<String> hpoIDset = new HashSet<>();
        hpoIDset.addAll(hpoIds);
        
        boolean symmetric = false;
        Priority priority = new PhenomizerPriority(phenomizerDataDirectory.toString(), hpoIDset, symmetric);
        logger.info("Made new Phenomizer Priority: {}", priority);
        return priority;
    }

    public Priority getMGIPhenodigmPrioritiser(String disease) {

        Priority priority = new MGIPhenodigmPriority(disease);
        try {
            priority.setDatabaseConnection(dataSource.getConnection());
        } catch (SQLException ex) {
            logger.error(null, ex);
        }
        logger.info("Made new MGIPhenodigm Priority: {}", priority);
        return priority;
    }

    public Priority getBOQAPrioritiser(List<String> hpoIds) {
        Priority priority = new BoqaPriority(hpoOntologyFilePath, hpoAnnotationFilePath, hpoIds);
        try {
            priority.setDatabaseConnection(dataSource.getConnection());
        } catch (SQLException ex) {
            logger.error(null, ex);
        }
        logger.info("Made new BOQA Priority: {}", priority);
        return priority;
    }

    public Priority getDynamicPhenodigmPrioritiser(List<String> hpoIds) {
        Priority priority = new DynamicPhenodigmPriority(hpoIds);
        try {
            priority.setDatabaseConnection(dataSource.getConnection());
        } catch (SQLException ex) {
            logger.error(null, ex);
        }
        logger.info("Made new DynamicPhenodigm Priority: {}", priority);
        return priority;
    }

    public Priority getGeneWandererPrioritiser(List<Integer> entrezSeedGenes) {
        Priority priority = new GenewandererPriority(randomWalkMatrix, entrezSeedGenes);
        logger.info("Made new GeneWanderer Priority: {}", priority);
        return priority;
    }

    public Priority getDynamicPhenoWandererPrioritiser(List<String> hpoIds, String candGene, String disease) {
        Priority priority = new DynamicPhenoWandererPriority(hpoIds, candGene, disease, randomWalkMatrix);
        try {
            priority.setDatabaseConnection(dataSource.getConnection());
        } catch (SQLException ex) {
            logger.error(null, ex);
        }
        logger.info("Made new DynamicPhenoWanderer Priority: {}", priority);
        return priority;
    }
    
    public Priority getDynamicPhenoWandererPrioritiserMonarchTables(List<String> hpoIds, String candGene, String disease) {
        Priority priority = new DynamicPhenoWandererPriorityMonarchTables(hpoIds, candGene, disease, randomWalkMatrix);
        try {
            priority.setDatabaseConnection(dataSource.getConnection());
        } catch (SQLException ex) {
            logger.error(null, ex);
        }
        logger.info("Made new DynamicPhenoWanderer Priority: {}", priority);
        return priority;
    }

}
