/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.db.build.reference;

/**
 * Class representing the chrom/ref/position/alt and calculated pathogenicity 
 * scores.
 *
 * @author Jules Jacobsen <jules.jacoobsen@sanger.ac.uk>
 */
public class VariantPathogenicity {

    //TODO: this class needs refactoring to split out the variant coordinates 
    //from the pathogenicity scores.
    private final int chromosome;
    private final int position;
    private final char ref;
    private final char alt;
    private final char aaref;
    private final char aaalt;
    private final int aapos;
    private final float phylopScore;
    
    //TODO: possibly make thes guys Floats or PathogenicityScores so that they
    //can be nulls and do away with the -5.0 constant which leads to weird manipulations
    //like in the maxPathogenicity method.
    private final float siftScore;
    private final float polyphenScore;
    private final float muttasterScore;
    private final float caddRawRankScore;
    private final float caddRawScore;
    
    /**
     * This class encapsulates the information in a single dbNSFP line. In some
     * cases, there are multiple lines for the same chromosomal position. We
     * choose the line that is predicted to be most pathogenic by using the
     * function {@link #maxPathogenicity maxPathogenicity()} of this class. The
     * function {@link #toDumpLine toDumpLine()} can then be called to get the
     * corresponding SQL dump line.
     *
     * @param chromosome An integer representation of the chromosome (23:X,
     * 24:Y, 25:M)
     * @param position Position of the variant on the chromosome
     * @param ref reference nucleotide
     * @param alt variant (alternate) nucleotide
     * @param aaref reference amino acid
     * @param aaalt variant (alternate) amino acid
     * @param aapos Position of the variant amino acid in the protein.
     * @param siftScore SIFT score for the variant
     * @param polyphen2HVAR Polyphen2 score for the variant
     * @param muttasterScore Mutation Taster score for the variant
     * @param phyloP phyloP conservation score for the variant
     * @param caddRawRankScore
     * @param caddRawScore
     */
    public VariantPathogenicity(int chromosome, int position, char ref, char alt,
            char aaref, char aaalt, int aapos,
            float siftScore, float polyphen2HVAR, float muttasterScore,
            float phyloP, float caddRawRankScore, float caddRawScore) {
        this.chromosome = chromosome;
        this.position = position;
        this.ref = ref;
        this.alt = alt;
        this.aaref = aaref;
        this.aaalt = aaalt;
        this.aapos = aapos;
        this.siftScore = siftScore;
        this.polyphenScore = polyphen2HVAR;
        this.muttasterScore = muttasterScore;
        this.phylopScore = phyloP;
        this.caddRawRankScore = caddRawRankScore;
        this.caddRawScore = caddRawScore;
    }

    public int getChromosome() {
        return chromosome;
    }

    public int getPosition() {
        return position;
    }

    public char getRef() {
        return ref;
    }

    public char getAlt() {
        return alt;
    }

    public char getAminoAcidRef() {
        return aaref;
    }

    public char getAminoAcidAlt() {
        return aaalt;
    }

    public int getAminoAcidPosition() {
        return aapos;
    }

    public float getSiftScore() {
        return siftScore;
    }

    public float getPolyphenScore() {
        return polyphenScore;
    }

    public float getMutationTasterScore() {
        return muttasterScore;
    }

    public float getPhylopScore() {
        return phylopScore;
    }

    /**
     * @return a float between 0 (not pathogenic) and 1 (maximally pathogenic)
     * that represents the most pathogenic prediction for siftScore, polyphen,
     * and mutation taster.
     */
    public float maxPathogenicity() {
        
        //polyPhen and mutTaster scores range from 0-1 with 1 being the most pathogenic                                
        float maxPath = Math.max(polyphenScore, muttasterScore);
        //sift scores range from 0-1 with 0 being the most pathogenic                        
        if ((1 - siftScore) != 6 ) {
            maxPath = Math.max(maxPath, (1 - siftScore));
        }
//        System.out.printf("Scored mutTaster (%f) polyPhen (%f) sift (1 - %f) maxpath: %f%n", muttasterScore, polyphenScore, siftScore, maxPath);
        
        if (maxPath < 0) {
            maxPath = 0; /* This can occur because the flags for no data are less than zero. */

        }
        return maxPath;
    }

    /**
     * This returns a line that will form part of the import file for
     * postgreSQL.
     *
     * @return
     */
    public String toDumpLine() {
        return String.format("%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s%n",
                chromosome, position, ref, alt, aaref, aaalt, aapos,
                siftScore, polyphenScore, muttasterScore, phylopScore, caddRawRankScore, caddRawScore);
    }

    @Override
    public String toString() {
        return "VariantPathogenicity{" + "chromosome=" + chromosome + 
                ", position=" + position + ", ref=" + ref + ", alt=" + alt + 
                ", aaref=" + aaref + ", aaalt=" + aaalt + ", aapos=" + aapos + 
                ", sift=" + siftScore + ", polyphen=" + polyphenScore + 
                ", muttaster=" + muttasterScore + ", phyloP=" + phylopScore + 
                ", caddRawRank=" + caddRawRankScore + ", caddRaw=" + caddRawScore + '}';
    }
}
