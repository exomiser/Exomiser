package de.charite.compbio.exomiser.core.analysis;

import de.charite.compbio.exomiser.core.analysis.util.GeneReassigner;
import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.filters.SimpleGeneFilterRunner;
import de.charite.compbio.exomiser.core.filters.SparseVariantFilterRunner;
import de.charite.compbio.exomiser.core.filters.VariantFilter;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import java.util.Iterator;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
class PassOnlyAnalysisRunner extends AbstractAnalysisRunner {

    private static final Logger logger = LoggerFactory.getLogger(PassOnlyAnalysisRunner.class);
    
    PassOnlyAnalysisRunner(SampleDataFactory sampleDataFactory, VariantDataService variantDataService) {
        super(sampleDataFactory, variantDataService, new SparseVariantFilterRunner(), new SimpleGeneFilterRunner());
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
    protected Predicate<VariantEvaluation> geneFilterPredicate(Map<String, Gene> genes, GeneReassigner geneReassigner) {
        return variantEvaluation -> {
            //Only load the variant if the gene has passed the other filters
            //this should drastically reduce the number of collected variants
            //logger.info("Testing " + variantEvaluation.getGeneSymbol() + " with variant effect " + variantEvaluation.getVariantEffect());
           
            // ? call TAD reassigner here
            //GeneReassigner geneReassigner = new GeneReassigner(this.variantDataService, this.mainPriorityType);
            //geneReassigner.reassignGeneToMostPhenotypicallySimilarGeneInTad(variantEvaluation, genes);
            
            if(genes.containsKey(variantEvaluation.getGeneSymbol())) {
                //logger.info("Testing if gene had already passed " + variantEvaluation.getGeneSymbol());
                Gene gene = genes.get(variantEvaluation.getGeneSymbol());
                //logger.info("Gene passed: " + gene.passedFilters());
                if (gene.passedFilters()){
                    return true;
                }
                else{
                    // give TAD code a chance to reassign intergenic variant to a diff gene before failing it
                    //GeneReassigner geneReassigner = new GeneReassigner(this.variantDataService, this.mainPriorityType);
                    geneReassigner.reassignGeneToMostPhenotypicallySimilarGeneInTad(variantEvaluation, genes);
                    gene = genes.get(variantEvaluation.getGeneSymbol());
                    return gene.passedFilters();
                }
                //return gene.passedFilters();
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
                .map(gene -> {
                    removeFailedVariants(gene);
                    return gene;
                })
                .collect(toList());
    }

    private void removeFailedVariants(Gene gene) {
        Iterator<VariantEvaluation> variantIterator = gene.getVariantEvaluations().iterator();
        while (variantIterator.hasNext()) {
            VariantEvaluation variant = variantIterator.next();
            if (failedFilters(variant)) {
                variantIterator.remove();
            }
        }
    }

    private static boolean failedFilters(VariantEvaluation variant) {
        return !variant.passedFilters();
    }
    
    @Override
    protected List<VariantEvaluation> getFinalVariantList(List<VariantEvaluation> variants) {
        return variants
                .stream()
                .filter(VariantEvaluation::passedFilters)
                .collect(toList());
    }
}
