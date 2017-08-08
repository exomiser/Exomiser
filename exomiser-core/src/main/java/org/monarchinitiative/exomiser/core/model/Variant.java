/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.model;

import com.google.common.collect.Sets;
import de.charite.compbio.jannovar.annotation.VariantEffect;

import java.util.List;
import java.util.Set;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface Variant extends VariantCoordinates {

    Set<VariantEffect> codingVariantEffects = Sets.immutableEnumSet(
            VariantEffect.STOP_LOST,
            VariantEffect.STOP_RETAINED_VARIANT,
            VariantEffect.STOP_GAINED,
            VariantEffect.START_LOST,
            VariantEffect.SYNONYMOUS_VARIANT,
            VariantEffect.SPLICE_REGION_VARIANT,
            VariantEffect.SPLICE_ACCEPTOR_VARIANT,
            VariantEffect.SPLICE_DONOR_VARIANT,
            VariantEffect.FRAMESHIFT_ELONGATION,
            VariantEffect.FRAMESHIFT_TRUNCATION,
            VariantEffect.FRAMESHIFT_VARIANT,
            VariantEffect.MISSENSE_VARIANT,
            VariantEffect.MNV,
            VariantEffect.FEATURE_TRUNCATION,
            VariantEffect.DISRUPTIVE_INFRAME_DELETION,
            VariantEffect.DISRUPTIVE_INFRAME_INSERTION,
            VariantEffect.INFRAME_DELETION,
            VariantEffect.INFRAME_INSERTION,
            VariantEffect.INTERNAL_FEATURE_ELONGATION,
            VariantEffect.COMPLEX_SUBSTITUTION
    );

    Set<VariantEffect> regulatoryNonCodingVariantEffects = Sets.immutableEnumSet(
            VariantEffect.MIRNA,
            VariantEffect.REGULATORY_REGION_VARIANT,
            VariantEffect.FIVE_PRIME_UTR_PREMATURE_START_CODON_GAIN_VARIANT,
            VariantEffect.FIVE_PRIME_UTR_TRUNCATION,
            VariantEffect.FIVE_PRIME_UTR_INTRON_VARIANT,
            VariantEffect.FIVE_PRIME_UTR_EXON_VARIANT,
            VariantEffect.TF_BINDING_SITE_VARIANT,
            VariantEffect.THREE_PRIME_UTR_TRUNCATION,
            VariantEffect.THREE_PRIME_UTR_INTRON_VARIANT,
            VariantEffect.THREE_PRIME_UTR_EXON_VARIANT

    );

    default boolean isNonCodingVariant() {
        return !codingVariantEffects.contains(getVariantEffect());
    }
    
    double getPhredScore();

    boolean isXChromosomal();

    boolean isYChromosomal();

    boolean isOffExome();

    VariantEffect getVariantEffect();
    
    void setVariantEffect(VariantEffect ve);
    
    List<TranscriptAnnotation> getAnnotations();

    void setAnnotations(List<TranscriptAnnotation> transcriptAnnotations);

    String getGeneSymbol();

    void setGeneSymbol(String symbol);

    int getEntrezGeneId();

    void setEntrezGeneId(int id);

    /**
     * @return a String such as chr6:g.29911092G>T
     */
    String getHgvsGenome();

}
