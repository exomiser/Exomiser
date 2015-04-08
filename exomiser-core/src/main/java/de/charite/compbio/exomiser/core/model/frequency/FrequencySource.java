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
    LOCAL("Local"),
    
    //Thousand genomes http://www.1000genomes.org/ 
    THOUSAND_GENOMES("1000Genomes"),
    
    //ESP project http://evs.gs.washington.edu/EVS/
    ESP_AFRICAN_AMERICAN("ESP AA"),
    ESP_EUROPEAN_AMERICAN("ESP EA"),
    ESP_ALL("ESP All"),
    
    //ExAC project http://exac.broadinstitute.org/about
    EXAC_AFRICAN_INC_AFRICAN_AMERICAN("ExAC AFR"),
    EXAC_AMERICAN("ExAC AMR"),
    EXAC_EAST_ASIAN("ExAC EAS"),
    EXAC_FINISH("ExAC FIN"),
    EXAC_NON_FINISH_EUROPEAN("ExAC NFE"),
    EXAC_SOUTH_ASIAN("ExAC SAS"),
    EXAC_OTHER("ExAC OTH");
    
    private final String source;
            
    private FrequencySource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }
    
}
