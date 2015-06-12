/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import static de.charite.compbio.exomiser.core.ExomiserSettings.NUM_GENES_OPTION;
import static de.charite.compbio.exomiser.core.ExomiserSettings.OUTPUT_PASS_VARIANTS_ONLY_OPTION;
import static de.charite.compbio.exomiser.core.ExomiserSettings.OUT_FILE_FORMAT_OPTION;
import static de.charite.compbio.exomiser.core.ExomiserSettings.OUT_FILE_PREFIX_OPTION;
import java.util.Set;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface OutputSettings {
    
    @JsonSetter(OUTPUT_PASS_VARIANTS_ONLY_OPTION)
    public boolean outputPassVariantsOnly();

    @JsonProperty(NUM_GENES_OPTION)
    public int getNumberOfGenesToShow();

    @JsonProperty(OUT_FILE_FORMAT_OPTION)
    public Set<OutputFormat> getOutputFormats();

    @JsonProperty(OUT_FILE_PREFIX_OPTION)
    public String getOutputPrefix();
    
}
