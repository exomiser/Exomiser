/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.Disease;
import de.charite.compbio.exomiser.core.model.DiseaseIdentifier;
import de.charite.compbio.exomiser.core.model.GeneIdentifier;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private PreparedStatement createPreparedStatement(Connection connection, DiseaseIdentifier diseaseId) throws SQLException {
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
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Set<Disease> getKnownDiseasesForGene(GeneIdentifier geneId) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Set<String> getHpoIdsForDiseaseId(String diseaseId) {
    
        String hpoListString = "";
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement hpoIdsStatement = connection.prepareStatement("SELECT hp_id FROM disease_hp WHERE disease_id = ?");
            hpoIdsStatement.setString(1, diseaseId);
            ResultSet rs = hpoIdsStatement.executeQuery();
            rs.next();
            hpoListString = rs.getString(1);
        } catch (SQLException e) {
            logger.error("Unable to retrieve HPO terms for disease {}", diseaseId, e);
        }
        List<String> diseaseHpoIds = parseHpoIdListFromString(hpoListString);
        logger.info("{} HPO ids retrieved for disease {} - {}", diseaseHpoIds.size(), diseaseId, diseaseHpoIds);
        return new TreeSet<>(diseaseHpoIds);
    }

    private List<String> parseHpoIdListFromString(String hpoIdsString) {
        String[] hpoArray = hpoIdsString.split(",");
        List<String> hpoIdList = new ArrayList<>();
        for (String string : hpoArray) {
            hpoIdList.add(string.trim());
        }
        return hpoIdList;
    }    
    
}
