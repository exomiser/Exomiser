/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.common;

import de.charite.compbio.exomiser.exome.VariantEvaluation;
import jannovar.pedigree.Pedigree;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to encapsulate the patient data from a VCF file and their pedigree.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SampleData {
   
    /**
     * Store the path of the file used to create this data.
     */
    Path vcfFilePath;
    
    /**
     * Store lines of header of VCF file, in case we want to print them out
     * again.
     */
    private List<String> vcfHeader;
    /**
     * Total number of variants in original VCF file.
     */
    private int before_NS_SS_I;
    /**
     * Total number of NS/SS/I variants in original VCF file (nonsynonymous,
     * splicing, indel).
     */
    private int after_NS_SS_I;
    /**
     * Total number of samples (sequenced persons) in the input VCF file.
     */
    private int numSamples = 0;    
    /**
     * List of all sample names of VCF file
     */
    private List<String> sampleNames;    
    /**
     * Pedigree of the persons whose samples were sequenced. Created on the
     * basis of a PED file for multisample VCF files, or as a default
     * single-sample Pedigree for single-sample VCF files.
     */
    private Pedigree pedigree;

    private List<VariantEvaluation> variantList;

    public SampleData() {
    
    }

    
    public SampleData(List<String> sampleNames, Pedigree pedigree, List<VariantEvaluation> variantList) {
        this.sampleNames = sampleNames;
        this.pedigree = pedigree;
        this.variantList = variantList;
    }   
    
          
//    /**
//     * Set the VCF path ({@link #vcf_file}) as well as the base name of the VCF
//     * file ({@link #vcf_file_basename}).
//     */
//    public void setVCFfile(String fname) {
//        this.vcf_file = fname;
//        int sep = fname.lastIndexOf("/");
//        int wsep = fname.lastIndexOf("\\");
//        sep = Math.max(sep, wsep);
//        this.vcf_file_basename = fname.substring(sep + 1);
//    }
    
    
    
    /**
     * @return List of Strings representing the sample names in the VCF file.
     */
    public List<String> getSampleNames() {
        return sampleNames;
    }
    
    public void setSampleNames(List<String> sampleNames) {
        this.sampleNames = sampleNames;
    }
    
    /**
     * @return the number of samples represented in the VCF file.
     */
    public int getNumberOfSamples() {
        return numSamples;
    }

    public void setNumberOfSamples(int numSamples) {
        this.numSamples = numSamples;
    }

    public Path getVcfFilePath() {
        return vcfFilePath;
    }

    public void setVcfFilePath(Path vcfFilePath) {
        this.vcfFilePath = vcfFilePath;
    }

    public List<String> getVcfHeader() {
        return vcfHeader;
    }

    public void setVcfHeader(List<String> vcfHeader) {
        this.vcfHeader = vcfHeader;
    }

    public List<VariantEvaluation> getVariantEvaluations() {
        return variantList;
    }

    public void setVariantEvaluations(List<VariantEvaluation> variantList) {
        this.variantList = variantList;
    }

    public Pedigree getPedigree() {
        return pedigree;
    }

    public void setPedigree(Pedigree pedigree) {
        this.pedigree = pedigree;
    }

}
