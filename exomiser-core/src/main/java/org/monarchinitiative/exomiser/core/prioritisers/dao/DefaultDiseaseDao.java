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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.prioritisers.dao;

import com.google.common.collect.ImmutableList;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Repository
public class DefaultDiseaseDao implements DiseaseDao {

    private final Logger logger = LoggerFactory.getLogger(DefaultDiseaseDao.class);

    private final DataSource dataSource;

    @Autowired
    public DefaultDiseaseDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Cacheable(value = "diseaseHp")
    @Override
    public Set<String> getHpoIdsForDiseaseId(String diseaseId) {
        String hpoListString = "";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement hpoIdsStatement = connection.prepareStatement("SELECT hp_id FROM disease_hp WHERE disease_id = ?")
        ) {
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

    @Cacheable(value="diseases")
    @Override
    public List<Disease> getDiseaseDataAssociatedWithGeneId(int geneId) {
        String query = "SELECT gene_id as entrez_id, symbol as human_gene_symbol, d.disease_id as disease_id, d.diseasename as disease_name, d.TYPE AS disease_type, d.INHERITANCE as inheritance_code, hp_id as pheno_ids FROM entrez2sym e, disease_hp dhp, disease d  WHERE dhp.disease_id = d.DISEASE_ID and e.entrezid = d.GENE_ID and d.GENE_ID = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = setQueryGeneId(connection, query, geneId);
             ResultSet rs = statement.executeQuery()){

            return processDiseaseResults(rs);

        } catch (SQLException e) {
            logger.error("Unable to execute query '{}' for geneId: '{}'", query, geneId, e);
        }
        return Collections.emptyList();
    }

    private PreparedStatement setQueryGeneId(Connection connection, String query, int geneId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, geneId);
        return ps;
    }

    private List<Disease> processDiseaseResults(ResultSet rs) throws SQLException{
        ImmutableList.Builder<Disease> listBuilder = ImmutableList.builder();
        while(rs.next()) {
            List<String> phenotypes = ImmutableList.copyOf(rs.getString("pheno_ids").split(","));
            Disease disease = Disease.builder()
                    .diseaseId(rs.getString("disease_id"))
                    .diseaseName(rs.getString("disease_name"))
                    .associatedGeneId(rs.getInt("entrez_id"))
                    .associatedGeneSymbol(rs.getString("human_gene_symbol"))
                    .inheritanceModeCode(rs.getString("inheritance_code"))
                    .diseaseTypeCode(rs.getString("disease_type"))
                    .phenotypeIds(phenotypes)
                    .build();
            listBuilder.add(disease);
        }
        return listBuilder.build();
    }
}
