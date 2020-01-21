/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.rest.prioritiser.config;

import com.google.common.collect.ImmutableMap;
import org.monarchinitiative.exomiser.core.model.GeneIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
public class ControllerConfig {

    private static final Logger logger = LoggerFactory.getLogger(ControllerConfig.class);

    private final DataSource dataSource;

    public ControllerConfig(@Qualifier("phenotypeDataSource") DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    @Bean
    public Map<Integer, GeneIdentifier> getGeneIdentifiers() {
        logger.info("Loading gene identifiers...");
        Map<Integer, GeneIdentifier> geneIdentifierMap = new TreeMap<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select human_gene_symbol as gene_symbol, entrez_id from human2mouse_orthologs");
                ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                int entrezId = rs.getInt("entrez_id");
                String entrezStringId = String.valueOf(entrezId);
                String geneSymbol = rs.getString("gene_symbol");
                GeneIdentifier geneIdentifier = GeneIdentifier.builder()
                        .geneSymbol(geneSymbol)
                        .geneId(entrezStringId)
                        .entrezId(entrezStringId)
                        .build();
                geneIdentifierMap.put(entrezId, geneIdentifier);
            }
            logger.info("Loaded {} gene identifiers", geneIdentifierMap.size());
            return ImmutableMap.copyOf(geneIdentifierMap);
        } catch (SQLException e) {
            logger.error("Error executing getGenes query: ", e);
        }
        throw new RuntimeException("Unable to retrieve gene identifiers");
    }

}
