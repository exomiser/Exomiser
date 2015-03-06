/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Service
public class DefaultPrioritiserService implements PrioritiserService {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultPrioritiserService.class);
    
    @Autowired
    DataSource dataSource;
    
    /**
     * Set hpo_ids variable based on the entered disease
     */
    public List<String> getHpoIdsForDiseaseId(String diseaseId) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = createPreparedStatement(connection, diseaseId);
                ResultSet rs = preparedStatement.executeQuery()) {
            
                List<String> diseaseHpoIds = processResults(rs);
                logger.info("{} HPO ids retrieved for disease {} - {}", diseaseHpoIds.size(), diseaseId, diseaseHpoIds);
                return diseaseHpoIds;
        } catch (SQLException e) {
            logger.error("Error executing pathogenicity query: ", e);
        }
        return Collections.EMPTY_LIST;
    }
    

    private PreparedStatement createPreparedStatement(Connection connection, String diseaseId) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT hp_id FROM disease_hp WHERE disease_id = ?");
        preparedStatement.setString(1, diseaseId);
        return preparedStatement;
    }

    private List<String> processResults(ResultSet rs) throws SQLException {
        if (rs.next()){
            String hpoListString = rs.getString(1);
            return parseHpoIdListFromString(hpoListString);
        }
        return new ArrayList<>();
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
