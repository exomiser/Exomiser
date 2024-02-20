package org.monarchinitiative.exomiser.data.phenotype.processors.readers.disease;

import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;

import java.util.Collection;

class InheritanceModeWrangler {

    private InheritanceModeWrangler() {
        // static utility class
    }

    static InheritanceMode wrangleInheritanceMode(Collection<InheritanceMode> inheritanceModes) {
        if (inheritanceModes.isEmpty()) {
            return InheritanceMode.UNKNOWN;
        } else if (inheritanceModes.size() == 1) {
            return inheritanceModes.iterator().next();
        } else if (inheritanceModes.contains(InheritanceMode.AUTOSOMAL_DOMINANT) && inheritanceModes.contains(InheritanceMode.AUTOSOMAL_RECESSIVE)) {
            return InheritanceMode.AUTOSOMAL_DOMINANT_AND_RECESSIVE;
        } else if (inheritanceModes.contains(InheritanceMode.X_DOMINANT) && inheritanceModes.contains(InheritanceMode.X_RECESSIVE)) {
            return InheritanceMode.X_LINKED;
        }
        // here we're assuming there is a mix of somatic/polygenic, multifactorial and AD/AR/XD/XR and are returning the A or X in preference of the somatic
        else if (inheritanceModes.contains(InheritanceMode.AUTOSOMAL_DOMINANT)) {
            return InheritanceMode.AUTOSOMAL_DOMINANT;
        } else if (inheritanceModes.contains(InheritanceMode.AUTOSOMAL_RECESSIVE)) {
            return InheritanceMode.AUTOSOMAL_RECESSIVE;
        } else if (inheritanceModes.contains(InheritanceMode.X_DOMINANT)) {
            return InheritanceMode.X_DOMINANT;
        } else if (inheritanceModes.contains(InheritanceMode.X_RECESSIVE)) {
            return InheritanceMode.X_RECESSIVE;
        } else if (inheritanceModes.contains(InheritanceMode.X_LINKED)) {
            return InheritanceMode.X_LINKED;
        } else if (inheritanceModes.contains(InheritanceMode.MITOCHONDRIAL)) {
            return InheritanceMode.MITOCHONDRIAL;
        }
        else if (inheritanceModes.contains(InheritanceMode.SOMATIC)) {
            return InheritanceMode.SOMATIC;
        } else if (inheritanceModes.contains(InheritanceMode.POLYGENIC)) {
            return InheritanceMode.POLYGENIC;
        }
        return InheritanceMode.UNKNOWN;
    }
}
