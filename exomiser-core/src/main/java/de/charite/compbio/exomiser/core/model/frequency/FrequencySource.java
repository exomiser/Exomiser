/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model.frequency;

/**
 * Enum describing where the frequency data has originated.
 * 
 * @author Damian Smedley <damian.smedley@sanger.ac.uk>
 * @author Jules  Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public enum FrequencySource {
    
    UNKNOWN("unknown"),
    //Frequencies from a local datasource
    LOCAL("local"),
    
    //Thousand genomes http://www.1000genomes.org/
    THOUSAND_GENOMES("1000genomes"),
    
    //ESP project http://evs.gs.washington.edu/EVS/
    ESP_AFRICAN_AMERICAN("espAAmaf"),
    ESP_EUROPEAN_AMERICAN("espEAmaf"),
    ESP_ALL("espAllmaf"),
    
    //EXAC project http://exac.broadinstitute.org/about
    EXAC_AFRICAN_INC_AFRICAN_AMERICAN("exacAFRmaf"),
    EXAC_AMERICAN("exacAMRmaf"),
    EXAC_EAST_ASIAN("exacEASmaf"),
    EXAC_FINISH("exacFINmaf"),
    EXAC_NON_FINISH_EUROPEAN("exacNFEmaf"),
    EXAC_SOUTH_ASIAN("exacSASmaf"),
    EXAC_OTHER("exacOTHmaf");
    
    private final String source;
            
    private FrequencySource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }
    
}
