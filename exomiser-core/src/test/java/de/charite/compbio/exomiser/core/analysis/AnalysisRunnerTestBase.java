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
package de.charite.compbio.exomiser.core.analysis;

import de.charite.compbio.exomiser.core.factories.*;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.toMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains common methods required by the AnalysisRunnerTest classes.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public abstract class AnalysisRunnerTestBase {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisRunnerTestBase.class);
 
    final Path vcfPath = Paths.get("src/test/resources/smallTest.vcf");
    
    final Path twoAffectedPedPath = Paths.get("src/test/resources/inheritance/twoAffected.ped");
    final Path childAffectedPedPath = Paths.get("src/test/resources/inheritance/childAffected.ped");
    final Path inheritanceFilterVCFPath = Paths.get("src/test/resources/inheritance/inheritanceFilterTest.vcf");

    final SampleDataFactory sampleDataFactory = TestFactory.buildDefaultSampleDataFactory();
    final VariantDataService stubDataService = new VariantDataServiceStub();
    
    
    Analysis makeAnalysis(Path vcfPath, AnalysisStep... analysisSteps) {
        Analysis analysis = new Analysis(vcfPath);
        if (analysisSteps.length != 0) {
            analysis.addAllSteps(Arrays.asList(analysisSteps));
        }
        return analysis;
    }

    Map<String, Gene> makeResults(List<Gene> genes) {
        return genes.stream().collect(toMap(Gene::getGeneSymbol, gene -> gene));
    }

    void printResults(SampleData sampleData) {
        for (Gene gene : sampleData.getGenes()) {
            logger.info("{}", gene);
            for (VariantEvaluation variantEvaluation : gene.getVariantEvaluations()) {
                logger.info("{}", variantEvaluation);
            }
        }
    }

}
