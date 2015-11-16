/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2015  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantEffect;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface Variant extends VariantCoordinates {

    static final Set<VariantEffect> nonRegulatoryNonCodingVariantEffects = EnumSet.of(
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
            VariantEffect.MNV,
            VariantEffect.FEATURE_TRUNCATION,
            VariantEffect.DISRUPTIVE_INFRAME_DELETION,
            VariantEffect.DISRUPTIVE_INFRAME_INSERTION,
            VariantEffect.INFRAME_DELETION,
            VariantEffect.INFRAME_INSERTION,
            VariantEffect.INTERNAL_FEATURE_ELONGATION,
            VariantEffect.COMPLEX_SUBSTITUTION
    );

    static final Set<VariantEffect> regulatoryNonCodingVariantEffects = EnumSet.of(
            VariantEffect.MIRNA,
            VariantEffect.REGULATORY_REGION_VARIANT,
            VariantEffect.FIVE_PRIME_UTR_PREMATURE_START_CODON_GAIN_VARIANT,
            VariantEffect.FIVE_PRIME_UTR_TRUNCATION,
            VariantEffect.FIVE_PRIME_UTR_VARIANT,
            VariantEffect.TF_BINDING_SITE_VARIANT,
            VariantEffect.THREE_PRIME_UTR_TRUNCATION,
            VariantEffect.THREE_PRIME_UTR_VARIANT
    );

    default public boolean isRegulatoryNonCodingVariant() {
        //TODO: this is broken - MISSENSE and other coding variant effects are not in the set of nonRegulatoryNonCodingVariantEffects
        //check existing isFrameshiftVariant(), isStructural(), isSplicing(), isIntronic(), isOffExome() and isOffTranscript()
        //VariantEffect.INTERGENIC_VARIANT || VariantEffect.UPSTREAM_GENE_VARIANT || VariantEffect.REGULATORY_REGION_VARIANT
//        return !nonRegulatoryNonCodingVariantEffects.contains(getVariantEffect());
        return regulatoryNonCodingVariantEffects.contains(getVariantEffect());
    }
    
    public double getPhredScore();

    public boolean isXChromosomal();

    public boolean isYChromosomal();

    public boolean isOffExome();

    public VariantEffect getVariantEffect();
    
    public void setVariantEffect(VariantEffect ve);
    
    public List<Annotation> getAnnotations();

    public String getGeneSymbol();

    public int getEntrezGeneId();
    
    public void setEntrezGeneId(int id);
    
    public void setGeneSymbol(String symbol);
    
    public void setAnnotations(List<Annotation> alist);
    
    /**
     * @return a String such as chr6:g.29911092G>T
     */
    public String getChromosomalVariant();

}
