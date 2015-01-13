/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.prioritisers;

/**
 * Enum used to describe how the genes should be sorted after they have been prioritised.
 * The results of different prioritisers require different sorting.
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public enum ScoringMode {
    RANK_BASED, RAW_SCORE;
}
