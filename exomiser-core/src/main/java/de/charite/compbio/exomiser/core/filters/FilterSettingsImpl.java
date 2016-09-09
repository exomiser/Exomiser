/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
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
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FilterSettingsImpl implements FilterSettings {

    //FILTER variables
    private final float maximumFrequency;
    private final float minimumQuality;
    private final GeneticInterval geneticInterval;
    private final boolean keepNonPathogenicVariants;
    private final boolean removeKnownVariants;
    private final boolean keepOffTargetVariants;
    private final Set<Integer> genesToKeep;
    private final ModeOfInheritance modeOfInheritance;
    
    private FilterSettingsImpl(FilterSettingsBuilder builder) {
        maximumFrequency = builder.maximumFrequency;
        minimumQuality = builder.minimumQuality;
        geneticInterval = builder.geneticInterval;
        keepNonPathogenicVariants = builder.keepNonPathogenicVariants;
        removeKnownVariants = builder.removeKnownVariants;
        keepOffTargetVariants = builder.keepOffTargetVariants;
        genesToKeep = builder.geneIdsToKeep;
        modeOfInheritance = builder.modeOfInheritance;
    }

    public static class FilterSettingsBuilder {

        private float maximumFrequency = 100.00f;
        private float minimumQuality = 0;
        private GeneticInterval geneticInterval = null;
        private boolean keepNonPathogenicVariants = false;
        private boolean removeKnownVariants = false;
        private boolean keepOffTargetVariants = false;
        private Set<Integer> geneIdsToKeep = Collections.emptySet();
        private ModeOfInheritance modeOfInheritance = ModeOfInheritance.UNINITIALIZED;
        
        public FilterSettings build() {
            return new FilterSettingsImpl(this);
        }

        public FilterSettingsBuilder maximumFrequency(float maximumFrequency) {
            this.maximumFrequency = maximumFrequency;
            return this;
        }

        public FilterSettingsBuilder minimumQuality(float minimumQuality) {
            this.minimumQuality = minimumQuality;
            return this;
        }

        public FilterSettingsBuilder geneticInterval(GeneticInterval geneticInterval) {
            this.geneticInterval = geneticInterval;
            return this;
        }

        public FilterSettingsBuilder keepNonPathogenic(boolean keepNonPathogenic) {
            this.keepNonPathogenicVariants = keepNonPathogenic;
            return this;
        }

        public FilterSettingsBuilder removeKnownVariants(boolean removeKnownVariants) {
            this.removeKnownVariants = removeKnownVariants;
            return this;
        }
        
        public FilterSettingsBuilder keepOffTargetVariants(boolean keepOffTargetVariants) {
            this.keepOffTargetVariants = keepOffTargetVariants;
            return this;
        }
        
        public FilterSettingsBuilder genesToKeep(Set<Integer> geneIds) {
            this.geneIdsToKeep = geneIds;
            return this;
        }
        
        public FilterSettingsBuilder modeOfInheritance(ModeOfInheritance modeOfInheritance) {
            this.modeOfInheritance = modeOfInheritance;
            return this;
        }
    }

    @Override
    public float getMaximumFrequency() {
        return maximumFrequency;
    }

    @Override
    public float getMinimumQuality() {
        return minimumQuality;
    }

    @Override
    public GeneticInterval getGeneticInterval() {
        return geneticInterval;
    }

    @Override
    public boolean keepOffTargetVariants() {
        return keepOffTargetVariants;
    }

    @Override
    public boolean removeKnownVariants() {
        return removeKnownVariants;
    }

    @Override
    public boolean keepNonPathogenicVariants() {
        return keepNonPathogenicVariants;
    }

    @Override
    public Set<Integer> getGenesToKeep() {
        return genesToKeep;
    }

    @Override
    public ModeOfInheritance getModeOfInheritance() {
        return modeOfInheritance;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + Float.floatToIntBits(this.maximumFrequency);
        hash = 31 * hash + Float.floatToIntBits(this.minimumQuality);
        hash = 31 * hash + Objects.hashCode(this.geneticInterval);
        hash = 31 * hash + (this.keepNonPathogenicVariants ? 1 : 0);
        hash = 31 * hash + (this.removeKnownVariants ? 1 : 0);
        hash = 31 * hash + (this.keepOffTargetVariants ? 1 : 0);
        hash = 31 * hash + Objects.hashCode(this.genesToKeep);
        hash = 31 * hash + Objects.hashCode(this.modeOfInheritance);
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
        final FilterSettingsImpl other = (FilterSettingsImpl) obj;
        if (Float.floatToIntBits(this.maximumFrequency) != Float.floatToIntBits(other.maximumFrequency)) {
            return false;
        }
        if (Float.floatToIntBits(this.minimumQuality) != Float.floatToIntBits(other.minimumQuality)) {
            return false;
        }
        if (!Objects.equals(this.geneticInterval, other.geneticInterval)) {
            return false;
        }
        if (this.keepNonPathogenicVariants != other.keepNonPathogenicVariants) {
            return false;
        }
        if (this.removeKnownVariants != other.removeKnownVariants) {
            return false;
        }
        if (this.keepOffTargetVariants != other.keepOffTargetVariants) {
            return false;
        }
        if (!Objects.equals(this.genesToKeep, other.genesToKeep)) {
            return false;
        }
        if (this.modeOfInheritance != other.modeOfInheritance) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FilterSettingsImpl{" + "maximumFrequency=" + maximumFrequency + ", minimumQuality=" + minimumQuality + ", geneticInterval=" + geneticInterval + ", keepNonPathogenicVariants=" + keepNonPathogenicVariants + ", removeKnownVariants=" + removeKnownVariants + ", keepOffTargetVariants=" + keepOffTargetVariants + ", genesToKeep=" + genesToKeep + ", modeOfInheritance=" + modeOfInheritance + '}';
    }
    
}
