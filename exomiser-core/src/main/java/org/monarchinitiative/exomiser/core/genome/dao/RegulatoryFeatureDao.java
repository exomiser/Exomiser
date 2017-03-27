/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2015  Charite Universitätsmedizin Berlin and Genome Research Ltd.
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
package org.monarchinitiative.exomiser.core.genome.dao;

import org.monarchinitiative.exomiser.core.model.RegulatoryFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Repository
public class RegulatoryFeatureDao {

    private final Logger logger = LoggerFactory.getLogger(RegulatoryFeatureDao.class);

    private final DataSource dataSource;

    @Autowired
    public RegulatoryFeatureDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<RegulatoryFeature> getRegulatoryFeatures() {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement("select CHROMOSOME as chr, START as start, \"end\" as end, FEATURE_TYPE as feature_type from REGULATORY_REGIONS");
                ResultSet rs = preparedStatement.executeQuery()) {

                return processRegulatoryFeatureResults(rs);

        } catch (SQLException e) {
            logger.error("Error executing regulatory feature query: ", e);
        }
        return Collections.emptyList();
    }

    private List<RegulatoryFeature> processRegulatoryFeatureResults(ResultSet rs) throws SQLException {
        List<RegulatoryFeature> regulatoryFeatures = new ArrayList<>();
        while (rs.next()) {
            int chr = rs.getInt("chr");
            int start = rs.getInt("start");
            int end = rs.getInt("end");
            String featureType = rs.getString("feature_type");
            RegulatoryFeature.FeatureType type = convertToFeatureType(featureType);
            if (type != RegulatoryFeature.FeatureType.UNKNOWN) {
                RegulatoryFeature regulatoryFeature = new RegulatoryFeature(chr, start, end, type);
                regulatoryFeatures.add(regulatoryFeature);
            }
        }
        return regulatoryFeatures;
    }

    //TODO: Jannovar VariantEffect doesn't capture these well
    //TODO: should these also be combined with the TADs? CTCF binding sites can act as insulators, enhancers can have long-range effects within a TAD, promoters are shorter range (to my knowledge), open chromatin has what effect on expression?
    private RegulatoryFeature.FeatureType convertToFeatureType(String featureType) {
        switch(featureType) {
            case "Enhancer":
                return RegulatoryFeature.FeatureType.ENHANCER;
            case "TF binding site":
                return RegulatoryFeature.FeatureType.TF_BINDING_SITE;
            case "Promoter":
                return RegulatoryFeature.FeatureType.PROMOTER;
            case "Promoter Flanking Region":
                return RegulatoryFeature.FeatureType.PROMOTER_FLANKING_REGION;
            case "CTCF Binding Site":
                return RegulatoryFeature.FeatureType.CTCF_BINDING_SITE;
            case "Open chromatin":
                return RegulatoryFeature.FeatureType.OPEN_CHROMATIN;
            case "FANTOM permissive":
                return RegulatoryFeature.FeatureType.FANTOM_PERMISSIVE;
            default:
                return RegulatoryFeature.FeatureType.UNKNOWN;
        }
    }
}
