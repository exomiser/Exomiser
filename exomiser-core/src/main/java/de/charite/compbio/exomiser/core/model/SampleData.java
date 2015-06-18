/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.model;

import de.charite.compbio.jannovar.pedigree.Pedigree;
import htsjdk.variant.vcf.VCFHeader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class to encapsulate the patient data from a VCF file and their pedigree.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SampleData {
   
    /**
     * Store the path of the file used to create this data.
     */
    private Path vcfPath;
    private Path pedPath;
    
    /**
     * Header of the VCF file.
     */
    private VCFHeader vcfHeader;

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

    private List<VariantEvaluation> variantEvaluations;

    private List<Gene> genes;

    public SampleData() {
        this.sampleNames = new ArrayList<>();
        this.variantEvaluations = new ArrayList<>();
        this.genes = new ArrayList<>();
    }
        
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

    public Path getVcfPath() {
        return vcfPath;
    }

    public void setVcfPath(Path vcfPath) {
        this.vcfPath = vcfPath;
    }

    public void setPedPath(Path pedPath) {
        this.pedPath = pedPath;
    }
    
    public Path getPedPath() {
        return pedPath;
    }
    
    public VCFHeader getVcfHeader() {
        return vcfHeader;
    }

    public void setVcfHeader(VCFHeader vcfHeader) {
        this.vcfHeader = vcfHeader;
    }

    public List<VariantEvaluation> getVariantEvaluations() {
        return variantEvaluations;
    }

    public void setVariantEvaluations(List<VariantEvaluation> variantList) {
        this.variantEvaluations = variantList;
    }

    public Pedigree getPedigree() {
        return pedigree;
    }

    public void setPedigree(Pedigree pedigree) {
        this.pedigree = pedigree;
    }

    public List<Gene> getGenes() {
        return genes;
    }

    public void setGenes(List<Gene> geneList) {
        this.genes = geneList;
    }    

    public List<VariantEvaluation> getUnAnnotatedVariantEvaluations() {
        List<VariantEvaluation> unannotatedList = new ArrayList<>();
        for (VariantEvaluation  varEval : variantEvaluations) {
            if (!varEval.hasAnnotations()) {
                unannotatedList.add(varEval);
            }
        }  
        return unannotatedList;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.vcfPath);
        hash = 59 * hash + Objects.hashCode(this.pedPath);
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
        final SampleData other = (SampleData) obj;
        if (!Objects.equals(this.vcfPath, other.vcfPath)) {
            return false;
        }
        if (!Objects.equals(this.pedPath, other.pedPath)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SampleData{" + "vcfPath=" + vcfPath + ", pedPath=" + pedPath + '}';
    }
    
}
