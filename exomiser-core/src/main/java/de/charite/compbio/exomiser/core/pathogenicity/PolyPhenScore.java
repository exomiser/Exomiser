/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.pathogenicity;

/**
 * PolyPhen (polymorphism phenotyping) score.
 * 
 * @link http://www.ncbi.nlm.nih.gov/pmc/articles/PMC2855889/?report=classic
 * @link http://genetics.bwh.harvard.edu/pph2/
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PolyPhenScore extends AbstractPathogenicityScore {

    /**
     * Possibly damaging is > 0.446 with Polyphen2 (this is an intermediate
     * category, thus, we are not being extremely strict with the polyphen
     * filter).
     */
    public static final float POLYPHEN_THRESHOLD = 0.446f;

    /**
     * A polyphen2 score above this threshold is probably damaging
     */
    public static final float POLYPHEN_PROB_DAMAGING_THRESHOLD = 0.956f;

    public PolyPhenScore(float score) {
        super(score);
    }

    @Override
    public String toString() {
        if (score > POLYPHEN_PROB_DAMAGING_THRESHOLD) {
            return String.format("Polyphen2: %.3f (D)", score);
        } else if (score > POLYPHEN_THRESHOLD) {
            return String.format("Polyphen2: %.3f (P)", score);
        } else {
            return String.format("Polyphen2: %.3f (B)", score);
        }
    }

}
