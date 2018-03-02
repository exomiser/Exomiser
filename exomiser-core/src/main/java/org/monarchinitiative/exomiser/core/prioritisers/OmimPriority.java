/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.prioritisers;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.core.prioritisers.service.PriorityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

/**
 * This class will add annotations to genes based on their annotations to OMIM or Orphanet disease entries in the
 * exomiser database.
 *
 * @author Peter N Robinson
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @version 0.16 (28 January,2014)
 */
public class OmimPriority implements Prioritiser<OmimPriorityResult> {

    private static final Logger logger = LoggerFactory.getLogger(OmimPriority.class);

    private final PriorityService priorityService;

    public OmimPriority(PriorityService priorityService) {
        this.priorityService = priorityService;
    }

    /**
     * Flag for output field representing OMIM.
     */
    @Override
    public PriorityType getPriorityType() {
        return PriorityType.OMIM_PRIORITY;
    }

    /**
     * This method just annotates each gene with OMIM/Orphanet data, if
     * available, and shows a link in the HTML output.
     *
     * @param genes A list of the {@link Gene} objects that
     * have survived the filtering (i.e., have rare, potentially pathogenic
     * variants).
     */
    @Override
    public void prioritizeGenes(List<String> hpoIds, List<Gene> genes) {
        for (Gene gene : genes) {
            OmimPriorityResult result = prioritiseGene().apply(gene);
            gene.addPriorityResult(result);
        }
    }

    @Override
    public Stream<OmimPriorityResult> prioritise(List<String> hpoIds, List<Gene> genes) {
        return genes.stream().map(prioritiseGene());
    }

    /**
     * If the gene is not contained in the database, we return an empty
     * but initialized RelevanceScore object. Otherwise, we retrieve a list of
     * all OMIM and Orphanet diseases associated with the entrez Gene.
     *
     **/
    private Function<Gene, OmimPriorityResult> prioritiseGene() {
        return gene -> {
            List<Disease> diseases = priorityService.getDiseaseDataAssociatedWithGeneId(gene.getEntrezGeneID());
            // This is a non-punitive prioritiser. We're relying on the other prioritisers to do the main ranking
            // and this class to add in the known diseases associated with the gene.
            // Arguably this shouldn't even exist as a prioritiser any more.
            Map<ModeOfInheritance, Double> scoresByMode = calculateScoresForModes(gene, diseases);
            double score = scoresByMode.values().stream().max(Comparator.naturalOrder()).orElse(0d);

            return new OmimPriorityResult(gene.getEntrezGeneID(), gene.getGeneSymbol(), score, diseases, scoresByMode);
        };
    }

    private Map<ModeOfInheritance, Double> calculateScoresForModes(Gene gene, List<Disease> knownAssociatedDiseases) {
        EnumMap<ModeOfInheritance, Double> scoresForModes = new EnumMap<>(ModeOfInheritance.class);
        for (ModeOfInheritance modeOfInheritance : ModeOfInheritance.values()) {
            if (modeOfInheritance != ModeOfInheritance.ANY) {
                double score = calculateknownDiseaseInheritanceModeModifier(gene, modeOfInheritance, knownAssociatedDiseases);
                scoresForModes.put(modeOfInheritance, score);
            }
        }
        return scoresForModes;
    }

    private double calculateknownDiseaseInheritanceModeModifier(Gene gene, ModeOfInheritance modeOfInheritance, List<Disease> knownAssociatedDiseases) {
        if (gene.getCompatibleInheritanceModes().isEmpty() || modeOfInheritance == ModeOfInheritance.ANY) {
            return 1;
        }

        if (!gene.isCompatibleWith(modeOfInheritance)) {
            return 0.5;
        }

        // if we're still here check the compatibility of the gene against the known modes for the disease
        // under the current mode of inheritance
        return knownAssociatedDiseases.stream()
                .filter(disease -> disease.getInheritanceMode() != InheritanceMode.UNKNOWN)
                .map(Disease::getInheritanceMode)
                .mapToDouble(scoreInheritanceMode(gene, modeOfInheritance))
                .max()
                .orElse(1);
    }

    /**
     * This function checks whether the mode of inheritance of the disease
     * matches the observed pattern of variants. That is, if the disease is
     * autosomal recessive and we have just one heterozygous mutation, then the
     * disease is probably not the correct diagnosis, and we assign it a factor
     * of 0.5. Note that hemizygous X chromosomal variants are usually called as
     * homozygous ALT in VCF files, and thus it is not reliable to distinguish
     * between X-linked recessive and dominant inheritance. Therefore, we return
     * 1 for any gene with X-linked inheritance if the disease in question is
     * listed as X chromosomal.
     */
    private ToDoubleFunction<InheritanceMode> scoreInheritanceMode(Gene gene, ModeOfInheritance currentMode) {
        return inheritanceMode -> {
            // not likely a rare-disease
            // gene only associated with somatic mutations or is polygenic
            if (inheritanceMode == InheritanceMode.SOMATIC || inheritanceMode == InheritanceMode.POLYGENIC) {
                return 0.5;
            }

            // Y chromosomal, rare.
            if (inheritanceMode == InheritanceMode.Y_LINKED) {
                return 1;
            }

            // Gene compatible with any known mode of inheritance for this disease?
            // If yes, we're good, otherwise down-rank this gene-disease-inheritance mode association.
            return geneCompatibleWithInheritanceMode(gene, inheritanceMode, currentMode) ? 1 : 0.5;
        };
    }

    private boolean geneCompatibleWithInheritanceMode(Gene gene, InheritanceMode inheritanceMode, ModeOfInheritance currentMode) {
        /* inheritance unknown (not mentioned in OMIM or not annotated correctly in HPO */
        if (gene.getCompatibleInheritanceModes().isEmpty() || inheritanceMode == InheritanceMode.UNKNOWN) {
            return true;
        }
        Set<ModeOfInheritance> compatibleDiseaseModes = inheritanceMode.toModeOfInheritance();
        //as long as the gene is compatible with at least one of the known modes for the disease we'll return the
        //default score
        for (ModeOfInheritance mode : compatibleDiseaseModes) {
            if (gene.isCompatibleWith(mode) && mode == currentMode) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(OmimPriority.class.getName());
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && getClass() == obj.getClass();
    }

    @Override
    public String toString() {
        return "OmimPrioritiser{}";
    } 
}
