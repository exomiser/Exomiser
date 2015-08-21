package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.filters.SimpleGeneFilterRunner;
import de.charite.compbio.exomiser.core.filters.SparseVariantFilterRunner;
import de.charite.compbio.exomiser.core.filters.VariantFilter;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PassOnlyAnalysisRunner extends AbstractAnalysisRunner {

    private static final Logger logger = LoggerFactory.getLogger(PassOnlyAnalysisRunner.class);

    public PassOnlyAnalysisRunner(SampleDataFactory sampleDataFactory, VariantDataService variantDataService) {
        super(sampleDataFactory, new SparseVariantFilterRunner(variantDataService), new SimpleGeneFilterRunner());
    }

    @Override
    protected Predicate<VariantEvaluation> variantFilterPredicate(List<VariantFilter> variantFilters) {
        return variantEvaluation -> {
            //loop through the filters and only run if the variantEvaluation has passed all prior filters
            variantFilters.stream()
                    .filter(filter -> variantEvaluation.passedFilters())
                    .forEach(filter -> variantFilterRunner.run(filter, variantEvaluation));

            return variantEvaluation.passedFilters();
        };
    }

    @Override
    protected Predicate<VariantEvaluation> geneFilterPredicate(Map<String, Gene> genes) {
        return variantEvaluation -> {
            //Only load the variant if the gene has passed the other filters
            //this should drastically reduce the number of collected variants
            if(genes.containsKey(variantEvaluation.getGeneSymbol())) {
                Gene gene = genes.get(variantEvaluation.getGeneSymbol());
                return gene.passedFilters();
            }
            return false;
        };
    }

    @Override
    protected List<Gene> getFinalGeneList(Map<String, Gene> passedGenes) {
        return passedGenes.values()
                .stream()
                .filter(gene -> !gene.getVariantEvaluations().isEmpty())
                .filter(Gene::passedFilters)
                .collect(toList());
    }

}
