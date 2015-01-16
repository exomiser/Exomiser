/*
 * Copyright © 2011-2013 EMBL - European Bioinformatics Institute
 * and Genome Research Limited
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.  
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.charite.compbio.exomiser.core.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class describing the attributes of an external reference. In PhenoDigm
 * this is anouther resource, e.g. MGI, HGNC, OMIM, ORPHANET, DECIPHER.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ExternalIdentifier {
    
    private static final Logger logger = LoggerFactory.getLogger(ExternalIdentifier.class);
    
    private final String databaseCode;
    private final String databaseAcc;

    private static enum ExternalLink {
        //genes
        MGI("MGI", "http://www.informatics.jax.org/accession/MGI:"),
        HGNC("HGNC", "http://www.genenames.org/data/hgnc_data.php?hgnc_id="),
        ZFIN("ZFIN","http://zfin.org/"),
        //diseases
        OMIM("OMIM", "http://omim.org/entry/"),
        ORPHANET("ORPHANET", "http://www.orpha.net/consor/cgi-bin/OC_Exp.php?Lng=GB&Expert="),
        DECIPHER("DECIPHER", "https://decipher.sanger.ac.uk/syndrome/"),
        
        UNKNOWN("UNKNOWN", "");
        
        private final String identifier;
        private final String link;
        
        
        private ExternalLink(String identifier, String link){
            this.identifier = identifier;
            this.link = link;
        }

        public String getLink() {
            return link;
        }
        
        public static ExternalLink getExternalLink(String identifier) {
            
            if (identifier != null) {           
                for (ExternalLink externalLink : ExternalLink.values()) {
                    if (externalLink.identifier.equals(identifier)) {
                        return externalLink;
                    }
                }
            }
            return UNKNOWN;
        }
    }
    /**
     * Creates a new ExternalIdentifier from a compound identifier.
     * Compound identifiers must follow the format:
     * databaseCode:databaseAcc
     * 
     * Examples: OMIM:101600, ORPHANET:1040, MGI:88452
     * @param compoundIdentifier 
     */
    protected ExternalIdentifier(String compoundIdentifier) {
        
        if (compoundIdentifier == null || !compoundIdentifier.contains(":")) {
            //there are quite a lot of null or empty compound identifiers so let's just handle them and not talk about it...
//            logger.debug("'{}' is not of the format DBCODE:ACCESSION - creating an empty external identifier", compoundIdentifier);
            this.databaseAcc = "-";
            this.databaseCode = "-";
        } else {
            String[] database = compoundIdentifier.split(":");
            String dbCode = database[0];
            String dbAc = database[1];
            this.databaseCode = dbCode;
            this.databaseAcc = dbAc;
        }
    }
    
    /**
     * Creates a new ExternalIdentifier from a databaseCode and a databaseAcc.
     * 
     * Examples: OMIM:101600, ORPHANET:1040, MGI:88452
     * @param databaseCode OMIM:
     * @param databaseAcc 101600
     */
    protected ExternalIdentifier(String databaseCode, String databaseAcc) {
        this.databaseCode = databaseCode;
        this.databaseAcc = databaseAcc;
    }
    
    public String getDatabaseCode() {
        return databaseCode;
    }

    public String getDatabaseAcc() {
        return databaseAcc;
    }

    public String getExternalUri() {
        ExternalLink externalLink = ExternalLink.getExternalLink(this.databaseCode);
        if (externalLink == ExternalLink.UNKNOWN) {
            logger.info("{} is of resource type {} - Returning empty external URI.", this.databaseCode, ExternalLink.UNKNOWN);
            return "";
        }
        return String.format("%s%s", externalLink.getLink(), this.databaseAcc);
    }

    /**
     * Returns the 'full' identifier of a gene Id. In the case of an MGI gene Id
     * this will be 'MGI:95522'. 
     * OMIM ids will be in the same format, for example 'OMIM:101600'
     * @return String of the database identifier
     */
    public String getCompoundIdentifier() {
        return databaseCode + ":" + databaseAcc;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + (this.databaseCode != null ? this.databaseCode.hashCode() : 0);
        hash = 79 * hash + (this.databaseAcc != null ? this.databaseAcc.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExternalIdentifier other = (ExternalIdentifier) obj;
        if ((this.databaseCode == null) ? (other.databaseCode != null) : !this.databaseCode.equals(other.databaseCode)) {
            return false;
        }
        if ((this.databaseAcc == null) ? (other.databaseAcc != null) : !this.databaseAcc.equals(other.databaseAcc)) {
            return false;
        }
        return true;
    }
    

    @Override
    public String toString() {
        return getCompoundIdentifier();
    }
        
}
