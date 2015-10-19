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
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.RegulatoryFeature;
import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantEffect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
    public VariantEffect getRegulatoryFeatureData(Variant variant) {
        VariantEffect variantEffect = variant.getVariantEffect();
        //logger.info("Testing " + variant.getChromosomalVariant() + " with effect " + variantEffect.toString());
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = createPreparedStatement(connection, variant);
                ResultSet rs = preparedStatement.executeQuery()) {
            if (rs.next()) {
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

    public List<RegulatoryFeature> getRegulatoryFeatures() {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement("select distinct CHROMOSOME as chr, \"START\" as start, \"end\" as end, FEATURE_TYPE as feature_type from REGULATORY_FEATURES");
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
            VariantEffect variantEffect = convertFeatureTypeToVariantEffect(featureType);
            RegulatoryFeature regulatoryFeature = new RegulatoryFeature(chr, start, end, variantEffect);
            regulatoryFeatures.add(regulatoryFeature);
        }
        return regulatoryFeatures;
    }

    //TODO: Jannovar VariantEffect doesn't capture these well
    //TODO: should these also be combined with the TADs? CTCF binding sites can act as insulators, enhancers can have long-range effects within a TAD, promoters are shorter range (to my knowledge), open chromatin has what effect on expression?
    private VariantEffect convertFeatureTypeToVariantEffect(String featureType) {
        switch(featureType) {
            case "Enhancer":
                return VariantEffect.REGULATORY_REGION_VARIANT;
            case "TF binding site":
                return VariantEffect.TF_BINDING_SITE_VARIANT;
            case "Promoter":
            case "Promoter Flanking Region":
            case "CTCF Binding Site":
            case "Open chromatin":
            case "FANTOM permissive":
            default:
                return VariantEffect.REGULATORY_REGION_VARIANT;
        }
    }
}
