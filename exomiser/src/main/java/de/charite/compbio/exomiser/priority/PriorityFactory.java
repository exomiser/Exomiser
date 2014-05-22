/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.priority;

import de.charite.compbio.exomiser.exception.ExomizerInitializationException;
import de.charite.compbio.exomiser.priority.util.DataMatrix;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for handling creation of Priority objects.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PriorityFactory {

    private static final Logger logger = LoggerFactory.getLogger(PriorityFactory.class);

    private DataSource dataSource;

    public PriorityFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public Priority getOmimPrioritizer() throws ExomizerInitializationException {
        Priority priority = new OMIMPriority();
         
        try {
            priority.setDatabaseConnection(dataSource.getConnection());
        } catch (SQLException ex) {
            logger.error(null, ex);
        }
        
        logger.info("Made new OMIM Priority: {}", priority);
        return priority;
    }

    public Priority getInheritancePrioritiser(String inheritanceFilterType) throws ExomizerInitializationException {
        Priority priority = new InheritancePriority();
        priority.setParameters(inheritanceFilterType);
        logger.info("Made new Inheritance Priority: {}", priority);
        return priority;
    }

    public Priority getPhenomizerPrioritiser(String phenomizerDataDirectory, String hpoTermList) throws ExomizerInitializationException {
        Set<String> hpoIDset = new HashSet<String>();
        String A[] = hpoTermList.split(",");
        for (String s : A) {
            hpoIDset.add(s.trim());
        }
        boolean symmetric = false;
        Priority priority = new PhenomizerPriority(phenomizerDataDirectory, hpoIDset, symmetric);
        logger.info("Made new Phenomizer Priority: {}", priority);
        return priority;
    }

    public Priority getMGIPhenodigmPrioritiser(String disease) throws ExomizerInitializationException {

        Priority priority = new MGIPhenodigmPriority(disease);
        try {
            priority.setDatabaseConnection(dataSource.getConnection());
        } catch (SQLException ex) {
            logger.error(null, ex);
        }        
        logger.info("Made new MGIPhenodigm Priority: {}", priority);
        return priority;
    }

    public Priority getBOQAPrioritiser(String hpoOntologyFile, String hpoAnnotationFile, String hpoTermList) throws ExomizerInitializationException {
        Priority priority = new BoqaPriority(hpoOntologyFile, hpoAnnotationFile, hpoTermList);
        logger.info("Made new BOQA Priority: {}", priority);
        return priority;
    }

    public Priority getDynamicPhenodigmPrioritiser(String hpoTermList) throws ExomizerInitializationException {
        Priority priority = new DynamicPhenodigmPriority(hpoTermList);
        logger.info("Made new DynamicPhenodigm Priority: {}", priority);
        return priority;
    }

    public Priority getZFINPrioritiser(String disease) throws ExomizerInitializationException {
        Priority priority = new ZFINPhenodigmPriority(disease);
        logger.info("Made new ZFIN Priority: {}", priority);
        return priority;
    }

    public Priority getExomeWalkerPrioritiser(String rwFilePath, String rwIndexPath, String entrezSeedGenes) throws ExomizerInitializationException {
        Priority priority = new GenewandererPriority(rwFilePath, rwIndexPath);
        priority.setParameters(entrezSeedGenes);
        logger.info("Made new ExomeWalker Priority: {}", priority);
        return priority;
    }

    public Priority getDynamicPhenoWandererPrioritiser(String rwFilePath, String rwIndexPath, String hpoids, String candGene, String disease, DataMatrix rwMatrix) throws ExomizerInitializationException {
        Priority priority = new DynamicPhenoWandererPriority(rwFilePath, rwIndexPath, hpoids, candGene, disease, rwMatrix);
        logger.info("Made new DynamicPhenoWanderer Priority: {}", priority);
        return priority;
    }

}
