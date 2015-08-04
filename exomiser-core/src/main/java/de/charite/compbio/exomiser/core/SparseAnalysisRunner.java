package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.factories.VariantFactory;
import de.charite.compbio.exomiser.core.filters.SimpleGeneFilterRunner;
import de.charite.compbio.exomiser.core.filters.SparseVariantFilterRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Analysis runner
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SparseAnalysisRunner extends AbstractAnalysisRunner {

    private static final Logger logger = LoggerFactory.getLogger(SparseAnalysisRunner.class);

    public SparseAnalysisRunner(VariantFactory variantFactory, VariantDataService variantDataService) {
        super(variantFactory, new SparseVariantFilterRunner(variantDataService), new SimpleGeneFilterRunner());
    }

}
