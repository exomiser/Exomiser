/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers.phenogrid;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenoGridMatchScore {
    
    private final String metric;
    private final int score;
    private final int rank;

    public PhenoGridMatchScore(String metric, int score, int rank) {
        this.metric = metric;
        this.score = score;
        this.rank = rank;
    }

    public String getMetric() {
        return metric;
    }

    public int getScore() {
        return score;
    }

    public int getRank() {
        return rank;
    }
    
    
    
}
