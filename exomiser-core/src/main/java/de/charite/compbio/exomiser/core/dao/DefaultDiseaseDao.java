/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.Disease;
import de.charite.compbio.exomiser.core.model.DiseaseIdentifier;
import de.charite.compbio.exomiser.core.model.GeneIdentifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
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
public class DefaultDiseaseDao implements DiseaseDao {

    private final Logger logger = LoggerFactory.getLogger(DefaultDiseaseDao.class);

    @Autowired
    private DataSource dataSource;  
    
    @Cacheable()
    @Override
    public Disease getDisease(DiseaseIdentifier diseaseId) {
            try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedFrequencyQuery = createPreparedStatement(connection, diseaseId);
                ResultSet rs = preparedFrequencyQuery.executeQuery()) {
            return processResults(rs);

        } catch (SQLException e) {
            logger.error("Error executing disease query: ", e);
        }
        return null;
    }

    private PreparedStatement createPreparedStatement(Connection connection, DiseaseIdentifier diseaseId) throws SQLException {
        // Added order by clause as sometimes have multiple rows for the same position, ref and alt and first row may have no freq data
        // Can remove if future versions of database remove these duplicated rows

        //TODO: optimise this query to remove the order by 
        String query = "";
        PreparedStatement ps = connection.prepareStatement(query);

        

        return ps;
    }

    private Disease processResults(ResultSet rs) throws SQLException {
        

        if (rs.next()) { 
        }
        
        return new Disease();
    }

    @Override
    public Set<Disease> getAllDiseases() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Disease> getKnownDiseasesForGene(GeneIdentifier geneId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
