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
package de.charite.compbio.exomiser.core.dao;

import com.google.common.collect.ImmutableSet;
import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

/**
 * Common class for processing results sets from ontology tables - these are all
 * of the form id:term for each species.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OntologyDaoResultSetProcessor {

    private OntologyDaoResultSetProcessor() {
    }

    static Set<PhenotypeTerm> processOntologyTermsResultSet(ResultSet rs) throws SQLException {
        ImmutableSet.Builder<PhenotypeTerm> termsCache = ImmutableSet.builder();
        while (rs.next()) {
            String id = rs.getString("id");
            String term = rs.getString("term");
            id = id.trim();
            PhenotypeTerm phenotypeTerm = new PhenotypeTerm(id, term, 0.0d);
            termsCache.add(phenotypeTerm);
        }
        return termsCache.build();
    }

    static Set<PhenotypeMatch> processOntologyTermMatchResultSet(ResultSet rs, PhenotypeTerm queryPhenotype) throws SQLException {
        ImmutableSet.Builder<PhenotypeMatch> phenotypeMatches = ImmutableSet.builder();
        while (rs.next()) {
            //simj, ic, score, hp_id_hit AS hit_id, hp_hit_term AS hit_term, lcs_id, lcs_term 
            String matchId = rs.getString("hit_id");
            String matchTerm = rs.getString("hit_term");
            PhenotypeTerm matchPhenotype = new PhenotypeTerm(matchId, matchTerm, 0.0d);
            
            double lcsIc = rs.getDouble("ic");
            String lcsId = rs.getString("lcs_id");
            String lcsTerm = rs.getString("lcs_term");
            PhenotypeTerm lcsPhenotype = new PhenotypeTerm(lcsId, lcsTerm, lcsIc);
            
            double simj = rs.getDouble("simj");
            double score = rs.getDouble("score");
            PhenotypeMatch match = new PhenotypeMatch(queryPhenotype, matchPhenotype, simj, score, lcsPhenotype);
            phenotypeMatches.add(match);
        }
        return phenotypeMatches.build();
    }

}
