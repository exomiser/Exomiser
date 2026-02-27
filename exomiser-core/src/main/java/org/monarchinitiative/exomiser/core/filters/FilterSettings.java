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
package org.monarchinitiative.exomiser.core.filters;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.model.GeneticInterval;

import java.util.Collections;
import java.util.Set;

/**
 * Settings parameters required by the filters.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
record FilterSettings(
        float maximumFrequency,
        float minimumQuality,
        GeneticInterval geneticInterval,
        boolean keepNonPathogenicVariants,
        boolean removeFailedVariants,
        boolean removeKnownVariants,
        boolean keepOffTargetVariants,
        Set<String> genesToKeep,
        ModeOfInheritance modeOfInheritance) {


    public static FilterSettingsBuilder builder() {
        return new FilterSettingsBuilder();
    }

    public static class FilterSettingsBuilder {

        private float maximumFrequency = 100.00f;
        private float minimumQuality = 0;
        private GeneticInterval geneticInterval = null;
        private boolean keepNonPathogenicVariants = false;
        private boolean removeFailedVariants = false;
        private boolean removeKnownVariants = false;
        private boolean keepOffTargetVariants = false;
        private Set<String> geneIdsToKeep = Collections.emptySet();
        private ModeOfInheritance modeOfInheritance = ModeOfInheritance.ANY;

        private FilterSettingsBuilder() {
        }

        FilterSettings build() {
            return new FilterSettings(
                    maximumFrequency,
                    minimumQuality,
                    geneticInterval,
                    keepNonPathogenicVariants,
                    removeFailedVariants,
                    removeKnownVariants,
                    keepOffTargetVariants,
                    geneIdsToKeep,
                    modeOfInheritance
            );
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

        public FilterSettingsBuilder removeFailedVariants(boolean removeFailedVariants) {
            this.removeFailedVariants = removeFailedVariants;
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

        public FilterSettingsBuilder genesToKeep(Set<String> geneIds) {
            this.geneIdsToKeep = geneIds;
            return this;
        }

        public FilterSettingsBuilder modeOfInheritance(ModeOfInheritance modeOfInheritance) {
            this.modeOfInheritance = modeOfInheritance;
            return this;
        }
    }

    @Override
    public String toString() {
        return "FilterSettings{" + "maximumFrequency=" + maximumFrequency + ", minimumQuality=" + minimumQuality + ", geneticInterval=" + geneticInterval + ", keepNonPathogenicVariants=" + keepNonPathogenicVariants + ", removeKnownVariants=" + removeKnownVariants + ", keepOffTargetVariants=" + keepOffTargetVariants + ", genesToKeep=" + genesToKeep + ", modeOfInheritance=" + modeOfInheritance + '}';
    }

}
