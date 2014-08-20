package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class BedFilter implements Filter {

    
    private final FilterType filterType = FilterType.BED_FILTER;

    /**
     * A set of off-target variant types such as Intergenic that we will filter
     * out from further consideration.
     */
    private final Set<String> targetGenes;

    /**
     * The constructor initializes the set of off-target
     * {@link jannovar.common.VariantType VariantType} constants, e.g.,
     * INTERGENIC, that we will filter out using this class.
     *
     * @param genes
     */
    public BedFilter(Set<String> genes) {
        this.targetGenes = genes;
    }
    
    /**
     * @return an integer constant (as defined in exomizer.common.Constants)
     * that will act as a flag to generate the output HTML dynamically depending
     * on the filters that the user has chosen.
     */
    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    /**
     * Take a list of variants and apply the filter to each variant. If a
     * variant does not pass the filter, flag it as failed.
     * @param variantList
     */
    @Override
    public void filterVariants(List<VariantEvaluation> variantList) {

        Set<String> nontargetGenes = new HashSet<>();

        int failed = 0;

        //add a token failed score - this is essentially a boolean pass/fail so we're using 0 here.
        FilterScore failedScore = new BedFilterScore(0f);
        
        for (VariantEvaluation ve : variantList) {
            if (ve.isOffExomeTarget()) {
                ve.addFailedFilter(filterType, failedScore);
                failed++;
                continue;
            }
            String gs = ve.getGeneSymbol();
            if (!targetGenes.contains(gs)) {
                nontargetGenes.add(gs);
                ve.addFailedFilter(filterType, failedScore);
                failed++;
            }
        }
        int passed = variantList.size() - failed;
        
    }

    @Override
    public boolean filterVariant(VariantEvaluation variantEvaluation) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    protected FilterReport makeReport(Set<String> nontargetGenes, int passed, int failed){
        
        FilterReport report = new FilterReport(filterType, passed, failed);
        
        report.addMessage(String.format("Removed a total of %d off-target variants from further consideration", failed));
        report.addMessage("Off target variants are defined as intergenic or intronic but not in splice sequences");

        StringBuilder sb = new StringBuilder();

        if (! nontargetGenes.isEmpty()) {
            sb.append(String.format("Variants were found in %d off target genes: ", nontargetGenes.size()));
            boolean notfirst = false;
            for (String gene : nontargetGenes) {
                if (notfirst) {
                    sb.append(", ");
                }
                notfirst = true;
                sb.append(gene);
            }
            sb.append(". Variants in these off-target genes were not further considered in the analysis.");
            report.addMessage(sb.toString());
        }
        return report;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.filterType);
        hash = 73 * hash + Objects.hashCode(this.targetGenes);
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
        final BedFilter other = (BedFilter) obj;
        if (this.filterType != other.filterType) {
            return false;
        }
        if (!Objects.equals(this.targetGenes, other.targetGenes)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return filterType + " filter targetGenes=" + targetGenes;
    }
}
