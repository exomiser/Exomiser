/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.factories.VariantFactory;
import de.charite.compbio.exomiser.core.filters.*;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.prioritisers.Prioritiser;
import de.charite.compbio.exomiser.core.prioritisers.PrioritiserRunner;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.exomiser.core.prioritisers.ScoringMode;
import de.charite.compbio.exomiser.core.util.RawScoreGeneScorer;
import de.charite.compbio.exomiser.core.util.GeneScorer;
import de.charite.compbio.exomiser.core.util.InheritanceModeAnalyser;
import de.charite.compbio.exomiser.core.util.RankBasedGeneScorer;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SimpleAnalysisRunner extends AbstractAnalysisRunner {

    protected static final Logger logger = LoggerFactory.getLogger(SimpleAnalysisRunner.class);

    public SimpleAnalysisRunner(VariantFactory variantFactory, VariantDataService variantDataService) {
        super(variantFactory, new SimpleVariantFilterRunner(variantDataService), new SimpleGeneFilterRunner());
    }

}
