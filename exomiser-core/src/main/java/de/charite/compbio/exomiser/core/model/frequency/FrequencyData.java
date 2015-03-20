/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model.frequency;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frequency data for the variant from the Thousand Genomes and the Exome Server
 * Project.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencyData {

    private static final Logger logger = LoggerFactory.getLogger(FrequencyData.class);
    
    /**
     * Thousand Genomes allele count (all samples).
     */
    private final RsId rsId;

    /**
     * dbSNP GMAF (often from thousand genomes project).
     */
    private final Frequency dbSnpMaf;

    /**
     * Exome Server Project (ESP) European American MAF.
     */
    private final Frequency espEaMaf;

    /**
     * Exome Server Project (ESP) African American MAF.
     */
    private final Frequency espAaMaf;

    /**
     * Exome Server Project (ESP) all comers MAF.
     */
    private final Frequency espAllMaf;
    
    /**
     * ExAC project MAFs.
     */
    private final Frequency exacAFRMaf;
    private final Frequency exacAMRMaf;
    private final Frequency exacEASMaf;
    private final Frequency exacFINMaf;
    private final Frequency exacNFEMaf;
    private final Frequency exacOTHMaf;
    private final Frequency exacSASMaf;

    private final List<Frequency> knownFrequencies;

    //builder here like with Pathogenicity?
    public FrequencyData(RsId rsid, Frequency dbSnp, Frequency espAll, Frequency espAA, Frequency espEA, Frequency exacAFR, Frequency exacAMR, Frequency exacEAS, Frequency exacFIN, Frequency exacNFE, Frequency exacOTH, Frequency exacSAS) {
        
        this.rsId = rsid;
        this.dbSnpMaf = dbSnp;
        this.espAaMaf = espAA;
        this.espAllMaf = espAll;
        this.espEaMaf = espEA;
        this.exacAFRMaf = exacAFR;
        this.exacAMRMaf = exacAMR;
        this.exacEASMaf = exacEAS;
        this.exacFINMaf = exacFIN;
        this.exacNFEMaf = exacNFE;
        this.exacOTHMaf = exacOTH;
        this.exacSASMaf = exacSAS;
        
        knownFrequencies = new ArrayList<>();
        
        if (dbSnpMaf != null) {
            knownFrequencies.add(dbSnpMaf);
        }
        if (espAaMaf != null) {
            knownFrequencies.add(espAaMaf);
        }
        if (espAllMaf != null) {
            knownFrequencies.add(espAllMaf);
        }
        if (espEaMaf != null) {
            knownFrequencies.add(espEaMaf);
        }
        if (exacAFRMaf != null) {
            knownFrequencies.add(exacAFRMaf);
        }
        if (exacAMRMaf != null) {
            knownFrequencies.add(exacAMRMaf);
        }
        if (exacEASMaf != null) {
            knownFrequencies.add(exacEASMaf);
        }
        if (exacFINMaf != null) {
            knownFrequencies.add(exacFINMaf);
        }
        if (exacNFEMaf != null) {
            knownFrequencies.add(exacNFEMaf);
        }
        if (exacOTHMaf != null) {
            knownFrequencies.add(exacOTHMaf);
        }
        if (exacSASMaf != null) {
            knownFrequencies.add(exacSASMaf);
        }        
    }

    public RsId getRsId() {
        return rsId;
    }

    public Frequency getDbSnpMaf() {
        return dbSnpMaf;
    }

    public Frequency getEspEaMaf() {
        return espEaMaf;
    }

    public Frequency getEspAaMaf() {
        return espAaMaf;
    }

    public Frequency getEspAllMaf() {
        return espAllMaf;
    }
    
    /**
     * @return true if this variant is at all represented in dbSNP or ESP data,
     * regardless of frequency. That is, if the variant has an RS id in dbSNP or
     * any frequency data at all, return true, otherwise false.
     */
    public boolean representedInDatabase() {
        if (rsId != null) {
            return true;
        }
        return !knownFrequencies.isEmpty();

    }

    public boolean hasDbSnpData() {
        return dbSnpMaf != null;
    }

    public boolean hasDbSnpRsID() {
        return rsId != null;
    }

    public boolean hasEspData() {
        return espAllMaf != null;
    }
    
    public boolean hasExacData() {
        return exacAFRMaf != null || exacAMRMaf != null || exacEASMaf != null || exacFINMaf != null || exacNFEMaf != null || exacOTHMaf != null || exacSASMaf != null;
    }

    /**
     * Returns a list of Frequency objects. If there is no known frequency data
     * then an empty list will be returned.
     *
     * @return a List of Frequency data
     */
    public List<Frequency> getKnownFrequencies() {
        return new ArrayList(knownFrequencies);
    }

    /**
     * Returns a the maximum frequency - if there are no known frequencies/ no
     * frequency data it will return 0.
     *
     * @return
     */
    public float getMaxFreq() {
        //TODO this is analagous to PathogenicityData.getMostPathogenicScore()
        //TODO so should really return a Frequency object...
        float maxFreq = 0f;
        for (Frequency freq : knownFrequencies) {
            //TODO ...but frequency needs to implement comparable first
            maxFreq = Math.max(maxFreq, freq.getFrequency());
        }
        return maxFreq;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.rsId);
        hash = 79 * hash + Objects.hashCode(this.dbSnpMaf);
        hash = 79 * hash + Objects.hashCode(this.espEaMaf);
        hash = 79 * hash + Objects.hashCode(this.espAaMaf);
        hash = 79 * hash + Objects.hashCode(this.espAllMaf);
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
        final FrequencyData other = (FrequencyData) obj;
        if (!Objects.equals(this.rsId, other.rsId)) {
            return false;
        }
        if (!Objects.equals(this.dbSnpMaf, other.dbSnpMaf)) {
            return false;
        }
        if (!Objects.equals(this.espEaMaf, other.espEaMaf)) {
            return false;
        }
        if (!Objects.equals(this.espAaMaf, other.espAaMaf)) {
            return false;
        }
        if (!Objects.equals(this.espAllMaf, other.espAllMaf)) {
            return false;
        }
        return true;
    }

    
    @Override
    public String toString() {
        return "FrequencyData{" + rsId + ", dbSnpMaf=" + dbSnpMaf + ", espEaMaf=" + espEaMaf + ", espAaMaf=" + espAaMaf + ", espAllMaf=" + espAllMaf + '}';
    }

}
