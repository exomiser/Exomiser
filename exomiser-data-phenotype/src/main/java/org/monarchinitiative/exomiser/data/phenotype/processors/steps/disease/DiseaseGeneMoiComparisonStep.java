/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.phenotype.processors.steps.disease;

import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.DiseaseGene;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.DiseaseGeneMoiComparison;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.disease.DiseaseInheritanceCacheReader;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.disease.OmimGeneMap2Reader;
import org.monarchinitiative.exomiser.data.phenotype.processors.steps.ProcessingStep;
import org.monarchinitiative.exomiser.data.phenotype.processors.writers.OutputLineWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Compares the OMIM MOI annotations against the HPO MOI annotations and writes the differences out to some Markdown files
 * to but used in the HPO annotation GitHub tracker.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class DiseaseGeneMoiComparisonStep implements ProcessingStep {

    private static final Logger logger = LoggerFactory.getLogger(DiseaseGeneMoiComparisonStep.class);

    private final DiseaseInheritanceCacheReader diseaseInheritanceCacheReader;
    private final OmimGeneMap2Reader omimGeneMap2Reader;
    private final OutputLineWriter<DiseaseGeneMoiComparison> missingInHpoMoiWriter;
    private final OutputLineWriter<DiseaseGeneMoiComparison> missingInOmimMoiWriter;
    private final OutputLineWriter<DiseaseGeneMoiComparison> mismatchedMoiWriter;

    private DiseaseGeneMoiComparisonStep(DiseaseInheritanceCacheReader diseaseInheritanceCacheReader, OmimGeneMap2Reader omimGeneMap2Reader, OutputLineWriter<DiseaseGeneMoiComparison> missingInHpoMoiWriter, OutputLineWriter<DiseaseGeneMoiComparison> missingInOmimMoiWriter, OutputLineWriter<DiseaseGeneMoiComparison> mismatchedMoiWriter) {
        this.diseaseInheritanceCacheReader = diseaseInheritanceCacheReader;
        this.omimGeneMap2Reader = omimGeneMap2Reader;
        this.missingInHpoMoiWriter = missingInHpoMoiWriter;
        this.missingInOmimMoiWriter = missingInOmimMoiWriter;
        this.mismatchedMoiWriter = mismatchedMoiWriter;
    }

    public static DiseaseGeneMoiComparisonStep create(Resource hpoPhenotypeAnnotations, Resource omimGeneMap2, OutputLineWriter<DiseaseGeneMoiComparison> missingInHpoMoiWriter, OutputLineWriter<DiseaseGeneMoiComparison> missingInOmimMoiWriter, OutputLineWriter<DiseaseGeneMoiComparison> mismatchedMoiWriter) {
        DiseaseInheritanceCacheReader diseaseInheritanceCacheReader = new DiseaseInheritanceCacheReader(hpoPhenotypeAnnotations);
        OmimGeneMap2Reader omimGeneMap2Reader = new OmimGeneMap2Reader(diseaseInheritanceCacheReader, omimGeneMap2);
        return new DiseaseGeneMoiComparisonStep(diseaseInheritanceCacheReader, omimGeneMap2Reader, missingInHpoMoiWriter,  missingInOmimMoiWriter, mismatchedMoiWriter);
    }

    @Override
    public void run() {
        logger.info("Starting OMIM-HPO Disease-Gene-MOI comparisons");

        List<DiseaseGene> omimDiseaseGenes = omimGeneMap2Reader.readRaw();
        Map<String, InheritanceMode> inheritanceModeMap = diseaseInheritanceCacheReader.read();

        DiseaseGeneMoiComparisonFactory diseaseGeneMoiChecker = new DiseaseGeneMoiComparisonFactory(omimDiseaseGenes, inheritanceModeMap);
        List<DiseaseGeneMoiComparison> diseaseGeneMoiComparisons = diseaseGeneMoiChecker.buildComparisons();
        logger.info("Ran {} OMIM-HPO Disease-Gene-MOI comparisons", diseaseGeneMoiComparisons.size());

        long numMoiMatches = diseaseGeneMoiComparisons.stream().filter(DiseaseGeneMoiComparison::hasMatchingMoi).count();
        logger.info("{} MOI annotations in agreement between HPO and OMIM.", numMoiMatches);

        long numMoiMisMatches = diseaseGeneMoiComparisons.stream()
                .filter(diseaseGeneMoiComparison -> !diseaseGeneMoiComparison.hasMatchingMoi())
                .count();
        logger.info("{} MOI annotations in disagreement between HPO and OMIM.", numMoiMisMatches);

        logger.info("Of these, there were: ");
        List<DiseaseGeneMoiComparison> missingOmimMoiAnnotations = diseaseGeneMoiComparisons.stream()
                .filter(DiseaseGeneMoiComparison::isMissingOmimMoi)
                .collect(Collectors.toList());
        logger.info("  {} annotations with UNKNOWN MOI from OMIM (annotation present in HPO)", missingOmimMoiAnnotations.size());
        missingInOmimMoiWriter.write(missingOmimMoiAnnotations);

        List<DiseaseGeneMoiComparison> missingHpoMoiAnnotations = diseaseGeneMoiComparisons.stream()
                .filter(DiseaseGeneMoiComparison::isMissingHpoMoi)
                .collect(Collectors.toList());
        logger.info("  {} annotations with UNKNOWN MOI from HPO (annotation present in OMIM)", missingHpoMoiAnnotations.size());
        missingInHpoMoiWriter.write(missingHpoMoiAnnotations);

        List<DiseaseGeneMoiComparison> mismatchedMoiAnnotations = diseaseGeneMoiComparisons.stream()
                .filter(DiseaseGeneMoiComparison::hasMismatchedMoi)
                .collect(Collectors.toList());
        logger.info("  {} gene-disease annotations with KNOWN mismatching MOI", mismatchedMoiAnnotations.size());
        mismatchedMoiWriter.write(mismatchedMoiAnnotations);
    }
}
