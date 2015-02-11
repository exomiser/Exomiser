/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model.pathogenicity;

/**
 * The score is the normalized probability that the amino acid change is
 * tolerated. SIFT predicts substitutions with scores less than 0.05 as
 * deleterious. Some SIFT users have found that substitutions with scores less
 * than 0.1 provide better sensitivity for detecting deleterious SNPs.
 *
 * Single nucleotide polymorphism (SNP) studies and random mutagenesis projects
 * identify amino acid substitutions in protein-coding regions. Each
 * substitution has the potential to affect protein function. SIFT (Sorting
 * Intolerant From Tolerant) is a program that predicts whether an amino acid
 * substitution affects protein function so that users can prioritize
 * substitutions for further study. It has been shown that SIFT can distinguish
 * between functionally neutral and deleterious amino acid changes in
 * mutagenesis studies and on human polymorphisms. SIFT is available at
 * {@link http://blocks.fhcrc.org/sift/SIFT.html}
 *
 * {@link http://www.ncbi.nlm.nih.gov/pmc/articles/PMC168916/}
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SiftScore extends AbstractPathogenicityScore {

    /**
     * A SIFT score below this threshold is considered to be pathogenic
     */
    public static final float SIFT_THRESHOLD = 0.06f;

    public SiftScore(float score) {
        super(score);
    }

    @Override
    public String toString() {
        if (score < SiftScore.SIFT_THRESHOLD) {
            return String.format("SIFT: %.3f (D)", score);
        } else {
            return String.format("SIFT: %.3f (T)", score);
        }
    }

}
