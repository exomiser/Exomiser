/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.analysis;

/**
 * Specifies how the {@link Gene} and {@link Variant} in an {@link Analysis}
 * should be retained.
 *
 * @since 7.0.0
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public enum AnalysisMode {

    FULL, SPARSE, PASS_ONLY;
}
