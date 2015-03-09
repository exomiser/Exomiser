/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Common class for processing results sets from ontology tables - these are all of the
 * form id:term for each species.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OntologyDaoResultSetProcessor {

    public Set<PhenotypeTerm> processOntologyTermsResultSet(ResultSet rs) throws SQLException {
        Set<PhenotypeTerm> termsCache = new HashSet();
        while (rs.next()) {
            String id = rs.getString(1);
            String term = rs.getString(2);
            id = id.trim();
            PhenotypeTerm phenotypeTerm = new PhenotypeTerm(id, term, 0.0d);
            termsCache.add(phenotypeTerm);
        }
        return termsCache;
    }

}
