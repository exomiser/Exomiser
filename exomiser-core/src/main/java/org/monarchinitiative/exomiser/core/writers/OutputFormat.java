/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.writers;

/**
 * Enum for representing the desired format of the output.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public enum OutputFormat {

    HTML("html"),
    VCF("vcf"),
    TSV_GENE("genes.tsv"),
    TSV_VARIANT("variants.tsv"),
    PHENOGRID("phenogrid.json");

    private final String fileExtension;

    OutputFormat(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getFileExtension() {
        return fileExtension;
    }
    
}
