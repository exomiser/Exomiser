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

import org.monarchinitiative.exomiser.core.model.TopologicalDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Repository
public class TadDao {

    private final Logger logger = LoggerFactory.getLogger(TadDao.class);

    private final DataSource dataSource;

    @Autowired
    public TadDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public List<TopologicalDomain> getAllTads() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("select CHROMOSOME as chr, START as start, \"end\" as end, ENTREZID as geneId, SYMBOL as geneSymbol from tad");
             ResultSet rs = preparedStatement.executeQuery()) {
            return createTads(rs);

        } catch (SQLException e) {
            logger.error("Error executing topologically associated domain query: ", e);
        }
        return Collections.emptyList();
    }

    private List<TopologicalDomain> createTads(ResultSet rs) throws SQLException {
        List<TopologicalDomain> tads = new ArrayList<>();
        int chr = 0;
        int start = 0;
        int end = 0;
        Map<String, Integer> geneIdentifiers = new LinkedHashMap<>();
        while (rs.next()) {
            int currentChr = rs.getInt("chr");
            int currentStart = rs.getInt("start");
            int currentEnd = rs.getInt("end");
            String geneSymbol = rs.getString("geneSymbol");
            Integer geneId = rs.getInt("geneId");

            //first row
            if (chr == 0 && start == 0 && end == 0) {
                chr = currentChr;
                start = currentStart;
                end = currentEnd;
                geneIdentifiers.put(geneSymbol, geneId);
                continue;
            }
            //TADs probably don't overlap, but this is biology so at least one probably will.
            if (currentChr != chr || currentStart != start || currentEnd != end) {
                TopologicalDomain previousTad = new TopologicalDomain(chr, start, end, geneIdentifiers);
                tads.add(previousTad);
                geneIdentifiers = new LinkedHashMap<>();
            }
            chr = currentChr;
            start = currentStart;
            end = currentEnd;
            geneIdentifiers.put(geneSymbol, geneId);
        }
        TopologicalDomain finalTad = new TopologicalDomain(chr, start, end, geneIdentifiers);
        tads.add(finalTad);

        return tads;
    }

}
