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
package org.monarchinitiative.exomiser.core.phenotype.dao;

import com.google.common.collect.ImmutableSet;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatch;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

/**
 * Common class for processing results sets from ontology tables - these are all
 * of the form id:term for each species.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
class OntologyDaoResultSetProcessor {

    private OntologyDaoResultSetProcessor() {
    }

    static Set<PhenotypeTerm> processOntologyTermsResultSet(ResultSet rs) throws SQLException {
        ImmutableSet.Builder<PhenotypeTerm> termsCache = ImmutableSet.builder();
        while (rs.next()) {
            String id = rs.getString("id");
            String term = rs.getString("term");
            id = id.trim();
            PhenotypeTerm phenotypeTerm = PhenotypeTerm.of(id, term);
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
            PhenotypeTerm matchPhenotype = PhenotypeTerm.of(matchId, matchTerm);
            
            String lcsId = rs.getString("lcs_id");
            String lcsTerm = rs.getString("lcs_term");
            PhenotypeTerm lcsPhenotype = PhenotypeTerm.of(lcsId, lcsTerm);

            double ic = rs.getDouble("ic");
            double simj = rs.getDouble("simj");
            double score = rs.getDouble("score");
            PhenotypeMatch match = PhenotypeMatch.builder()
                    .query(queryPhenotype)
                    .match(matchPhenotype)
                    .lcs(lcsPhenotype)
                    .simj(simj)
                    .ic(ic)
                    .score(score)
                    .build();
            phenotypeMatches.add(match);
        }
        return phenotypeMatches.build();
    }

}
