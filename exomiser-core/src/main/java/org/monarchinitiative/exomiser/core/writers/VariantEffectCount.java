/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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
package org.monarchinitiative.exomiser.core.writers;

import de.charite.compbio.jannovar.annotation.VariantEffect;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantEffectCount {
    private final VariantEffect variantEffect;
    private final List<Integer> sampleVariantEffectCounts;

    public VariantEffectCount(VariantEffect variantType, List<Integer> sampleVariantTypeCounts) {
        this.variantEffect = variantType;
        this.sampleVariantEffectCounts = sampleVariantTypeCounts;
    }

    public VariantEffect getVariantType() {
        return variantEffect;
    }

    public List<Integer> getSampleVariantTypeCounts() {
        return sampleVariantEffectCounts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariantEffectCount that = (VariantEffectCount) o;
        return variantEffect == that.variantEffect &&
                Objects.equals(sampleVariantEffectCounts, that.sampleVariantEffectCounts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variantEffect, sampleVariantEffectCounts);
    }

    @Override
    public String toString() {
        return variantEffect + "=" + sampleVariantEffectCounts;
    }
}
