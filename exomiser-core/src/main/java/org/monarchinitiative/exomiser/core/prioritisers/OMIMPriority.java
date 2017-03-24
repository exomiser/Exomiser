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

package org.monarchinitiative.exomiser.core.prioritisers;

import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.core.prioritisers.service.PriorityService;

import java.util.List;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

/**
 * This class is designed to do two things. First, it will add annotations to
 * genes based on their annotations to OMIM or Orphanet disease entries in the
 * exomiser database (Note that the app PopulateExomiserDatabase.jar, from this
 * software package is used to put the data into the database; see there for
 * more information). The tables <b>omim</b> and <b>orphanet</b> are used to
 * store/retrieve this information. The second purpose of this class is to check
 * whether the variants found in the VCF file match with the mode of inheritance
 * listed for the disease (column "inheritance" of the omim table; TODO-add
 * similar functionality for Orphanet). Thus, if we find a heterozygous mutation
 * but the disease is autosomal recessive, then it the corresponding
 * disease/gene is not a good candidate, and its OMIM relevance score is reduced
 * by a factor of 50%. See the function {@link #scoreInheritanceMode} for
 * details on this weighting scheme.
 *
 * @author Peter N Robinson
 * @version 0.16 (28 January,2014)
 */
public class OMIMPriority implements Prioritiser {

    private static final double DEFAULT_SCORE = 1d;

    private final PriorityService priorityService;

    public OMIMPriority(PriorityService priorityService) {
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
     * For now, this method just annotates each gene with OMIM data, if
     * available, and shows a link in the HTML output. However, we can use this
     * method to implement a Phenomizer-type prioritization at a later time
     * point.
     *
     * @param genes A list of the {@link Gene} objects that
     * have survived the filtering (i.e., have rare, potentially pathogenic
     * variants).
     */
    @Override
    public void prioritizeGenes(List<Gene> genes) {
        for (Gene gene : genes) {
            OMIMPriorityResult result = prioritiseGene().apply(gene);
            gene.addPriorityResult(result);
        }
    }

    @Override
    public Stream<OMIMPriorityResult> prioritise(List<Gene> genes) {
        return genes.stream().map(prioritiseGene());
    }

    /**
     * If the gene is not contained in the database, we return an empty
     * but initialized RelevanceScore object. Otherwise, we retrieve a list of
     * all OMIM and Orphanet diseases associated with the entrez Gene.
     *
     **/
    private Function<Gene, OMIMPriorityResult> prioritiseGene() {
        return gene -> {
            List<Disease> diseases = priorityService.getDiseaseDataAssociatedWithGeneId(gene.getEntrezGeneID());
            //this is a pretty non-punitive prioritiser. We're relying on the other prioritisers to do the main ranking
            double score = diseases.stream().map(Disease::getInheritanceMode).mapToDouble(scoreInheritanceMode(gene)).max().orElse(DEFAULT_SCORE);
            return new OMIMPriorityResult(gene.getEntrezGeneID(), gene.getGeneSymbol(), score, diseases);
        };
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
    private ToDoubleFunction<InheritanceMode> scoreInheritanceMode(Gene gene) {
        return inheritanceMode -> {
            /* inheritance unknown (not mentioned in OMIM or not annotated correctly in HPO */
            if (inheritanceMode == InheritanceMode.UNKNOWN) {
                return DEFAULT_SCORE;
            /* Y chromosomal, rare. */
            } else if (inheritanceMode == InheritanceMode.Y_LINKED) {
                return DEFAULT_SCORE;
            /* mitochondrial. */
            } else if (inheritanceMode == InheritanceMode.MITOCHONDRIAL) {
                return DEFAULT_SCORE;
            /* gene only associated with somatic mutations */
            } else if (inheritanceMode == InheritanceMode.SOMATIC) {
                return 0.5d;
            /* gene only associated with polygenic */
            } else if (inheritanceMode == InheritanceMode.POLYGENIC) {
                return 0.5d;
            /* No mode of inheritance is defined (UNDEFINED) */
            } else if (gene.getInheritanceModes().isEmpty()) {
                return DEFAULT_SCORE;
            /* inheritance of disease is dominant or both (dominant/recessive) */
            } else if (gene.isCompatibleWithDominant() && inheritanceMode.isCompatibleWithDominant()) {
                return DEFAULT_SCORE;
            /* inheritance of disease is recessive or both (dominant/recessive) */
            } else if (gene.isCompatibleWithRecessive() && inheritanceMode.isCompatibleWithRecessive()) {
                return DEFAULT_SCORE;
            } else if (gene.isXChromosomal() && inheritanceMode.isXlinked()) {
                return DEFAULT_SCORE;
            } else {
                return 0.5d;
            }
        };
    }

    @Override
    public int hashCode() {
        int hash = 5;
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
        final OMIMPriority other = (OMIMPriority) obj;
        return true;
    }

    @Override
    public String toString() {
        return "OmimPrioritiser{}";
    } 
}
