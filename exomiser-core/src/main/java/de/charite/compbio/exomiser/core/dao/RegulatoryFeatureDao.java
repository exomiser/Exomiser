/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantEffect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Repository
public class RegulatoryFeatureDao {

    private final Logger logger = LoggerFactory.getLogger(RegulatoryFeatureDao.class);

    @Autowired
    private DataSource dataSource;

    @Cacheable(value = "regulatory", key = "#variant.chromosomalVariant")
    public VariantEffect getRegulatoryFeatureData(Variant variant, Map<String, Gene> allGenes) {
        VariantEffect variantEffect = variant.getVariantEffect();
        //logger.info("Testing " + variant.getChromosomalVariant() + " with effect " + variantEffect.toString());
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = createPreparedStatement(connection, variant);
                ResultSet rs = preparedStatement.executeQuery()) {
            if (rs.next()) {
                /* TODO
                1. Need to pass correct PriorityType through as well
                2. Proper checks for null genes etc
                3. Should this TAD check be only run for intergenic/up/downstream variants in reg features table? 
                */
                logger.info("Found reg variant - time to see if gene needs reassigning from current closest gene, " + variant.getGeneSymbol() + ", to best pheno gene in TAD");
                PreparedStatement tadPreparedStatement = createTadPreparedStatement(connection,variant);
                ResultSet rs2 = tadPreparedStatement.executeQuery();
                float score = 0;
                while (rs2.next()){
                    int entrezId = rs2.getInt(1);
                    String geneSymbol = rs2.getString(2);
                    Gene gene = allGenes.get(geneSymbol);
                    float geneScore = gene.getPriorityResult(PriorityType.HIPHIVE_PRIORITY).getScore();
                    logger.info("Gene " + geneSymbol + " in TAD " + "has score " + geneScore);
                    if (geneScore > score){
                        logger.info("Changing gene to " + geneSymbol);
                        variant.setEntrezGeneId(entrezId);
                        variant.setGeneSymbol(geneSymbol);
                        List<Annotation> alist = Collections.emptyList();;
                        variant.setAnnotations(alist);
                        score = geneScore;
                    }
                }        
                
                return VariantEffect.REGULATORY_REGION_VARIANT;
                // later may set variant object with the type of regulatory feature, associated gene and tissue involved
            }
        } catch (SQLException e) {
            logger.error("Error executing regulatory feature query: ", e);
        }
        return variantEffect;
    }

    private PreparedStatement createPreparedStatement(Connection connection, Variant variant) throws SQLException {
        String enhancerQuery = "SELECT feature_type, tissue FROM regulatory_features WHERE chromosome = ? AND start <  ? AND \"end\" > ?";
        PreparedStatement ps = connection.prepareStatement(enhancerQuery);
        ps.setInt(1, variant.getChromosome());
        ps.setInt(2, variant.getPosition());
        ps.setInt(3, variant.getPosition());
        return ps;
    }
    
    private PreparedStatement createTadPreparedStatement(Connection connection, Variant variant) throws SQLException {
        String tadQuery = "select entrezid, symbol from tad where chromosome = ? and start < ? and \"end\" > ?";
        PreparedStatement ps = connection.prepareStatement(tadQuery);
        ps.setInt(1, variant.getChromosome());
        ps.setInt(2, variant.getPosition());
        ps.setInt(3, variant.getPosition());
        return ps;
    }
}
