/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Common class for processing results sets from ontology tables - these are all
 * of the form id:term for each species.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OntologyDaoResultSetProcessor {

    public Set<PhenotypeTerm> processOntologyTermsResultSet(ResultSet rs) throws SQLException {
        Set<PhenotypeTerm> termsCache = new HashSet();
        while (rs.next()) {
            String id = rs.getString("id");
            String term = rs.getString("term");
            id = id.trim();
            PhenotypeTerm phenotypeTerm = new PhenotypeTerm(id, term, 0.0d);
            termsCache.add(phenotypeTerm);
        }
        return termsCache;
    }

    Set<PhenotypeMatch> processOntologyTermMatchResultSet(ResultSet rs, PhenotypeTerm queryPhenotype) throws SQLException {
        Set<PhenotypeMatch> phenotypeMatchs = new LinkedHashSet();
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
            phenotypeMatchs.add(match);
        }
        return phenotypeMatchs;
    }

}
