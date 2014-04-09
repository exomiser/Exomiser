/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencyData {
    
    private static final Logger logger = LoggerFactory.getLogger(FrequencyData.class);
    
    private MinorAlleleFrequency dbSnpMaf;

    private MinorAlleleFrequency espEaMaf;
    
    private MinorAlleleFrequency espAaMaf;

    private MinorAlleleFrequency espAllMaf;

    public FrequencyData() {
    }

    public MinorAlleleFrequency getDbSnpMaf() {
        return dbSnpMaf;
    }

    public void setDbSnpMaf(MinorAlleleFrequency dbSnpMaf) {
        this.dbSnpMaf = dbSnpMaf;
    }

    public MinorAlleleFrequency getEspEaMaf() {
        return espEaMaf;
    }

    public void setEspEaMaf(MinorAlleleFrequency espEaMaf) {
        this.espEaMaf = espEaMaf;
    }

    public MinorAlleleFrequency getEspAaMaf() {
        return espAaMaf;
    }

    public void setEspAaMaf(MinorAlleleFrequency espAaMaf) {
        this.espAaMaf = espAaMaf;
    }

    public MinorAlleleFrequency getEspAllMaf() {
        return espAllMaf;
    }

    public void setEspAllMaf(MinorAlleleFrequency espAllMaf) {
        this.espAllMaf = espAllMaf;
    }
    
    

}
