/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for producing {@code FilterReport} lists from the list of
 * filtered {@code VariantEvaluation}. 
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FilterReportFactory {

    private static final Logger logger = LoggerFactory.getLogger(FilterReportFactory.class);

    public FilterReportFactory() {
        Locale.setDefault(Locale.UK);
    }

    /**
     * Makes a List of {@code FilterReport} for the {@code FilterType} specified.
     * @param filterTypes
     * @param settings
     * @param sampleData
     * @return a List of {@code FilterReport} 
     */
    public List<FilterReport> makeFilterReports(List<FilterType> filterTypes, ExomiserSettings settings, SampleData sampleData) {

        List<FilterReport> filterReports = new ArrayList<>();

        for (FilterType filterType : filterTypes) {
            filterReports.add(makeFilterReport(filterType, settings, sampleData));
        }

        return filterReports;
    }

    /**
     * Returns a FilterReport for the SampleData and the specified
     * FilterType. If the FilterType is not recognised or supported then this
     * method will return a default report with no messages.
     *
     * @param filterType
     * @param settings
     * @param sampleData
     * @return
     */
    public FilterReport makeFilterReport(FilterType filterType, ExomiserSettings settings, SampleData sampleData) {
        switch (filterType) {
            case TARGET_FILTER:
                return makeTargetFilterReport(settings, sampleData.getVariantEvaluations());
            case FREQUENCY_FILTER:
                return makeFrequencyFilterReport(settings, sampleData.getVariantEvaluations());
            case QUALITY_FILTER:
                return makeQualityFilterReport(settings, sampleData.getVariantEvaluations());
            case PATHOGENICITY_FILTER:
                return makePathogenicityFilterReport(settings, sampleData.getVariantEvaluations());
            case INTERVAL_FILTER:
                return makeIntervalFilterReport(settings, sampleData.getVariantEvaluations());
            case INHERITANCE_FILTER:
                return makeInheritanceFilterReport(settings, sampleData.getGenes());
            default:
                return makeDefaultVariantFilterReport(filterType, sampleData.getVariantEvaluations());
        }
    }

    private FilterReport makeTargetFilterReport(ExomiserSettings settings, List<VariantEvaluation> variantEvaluations) {
        FilterReport report = makeDefaultVariantFilterReport(FilterType.TARGET_FILTER, variantEvaluations);
        
        report.addMessage(String.format("Removed a total of %d off-target variants from further consideration", report.getFailed()));
        report.addMessage("Off target variants are defined as synonymous, intergenic, intronic but not in splice sequences");

        return report;
    }

    private FilterReport makeFrequencyFilterReport(ExomiserSettings settings, List<VariantEvaluation> variantEvaluations) {
        FilterReport report = makeDefaultVariantFilterReport(FilterType.FREQUENCY_FILTER, variantEvaluations);
        
        int numDbSnpFreqData = 0;
        int numDbSnpRsId = 0;
        int numEspFreqData = 0;
        int numExaCFreqData = 0;

        for (VariantEvaluation ve : variantEvaluations) {
            FrequencyData frequencyData = ve.getFrequencyData();

            if (frequencyData == null) {
                continue;
            }
            if (frequencyData.hasDbSnpData()) {
                numDbSnpFreqData++;
            }
            if (frequencyData.hasDbSnpRsID()) {
                numDbSnpRsId++;
            }
            if (frequencyData.hasEspData()) {
                numEspFreqData++;
            }
            if (frequencyData.hasExacData()) {
                numExaCFreqData++;
            }
        }
        
        int before = report.getPassed() + report.getFailed();
        
        report.addMessage(String.format("Allele frequency < %.2f %%", settings.getMaximumFrequency()));
        report.addMessage(String.format("Frequency Data available in dbSNP (for 1000 Genomes Phase I) for %d variants (%.1f%%)", numDbSnpFreqData, 100f * (double) numDbSnpFreqData / before));
        report.addMessage(String.format("dbSNP \"rs\" id available for %d variants (%.1f%%)", numDbSnpRsId, 100 * (double) numDbSnpRsId / before));
        report.addMessage(String.format("Data available in Exome Server Project for %d variants (%.1f%%)", numEspFreqData, 100f * (double) numEspFreqData / before));
        report.addMessage(String.format("Data available from ExAC Project for %d variants (%.1f%%)", numExaCFreqData, 100f * (double) numExaCFreqData / before));
        return report;
    }

    private FilterReport makeQualityFilterReport(ExomiserSettings settings, List<VariantEvaluation> variantEvaluations) {
        FilterReport report = makeDefaultVariantFilterReport(FilterType.QUALITY_FILTER, variantEvaluations);

        report.addMessage(String.format("PHRED quality %.1f", settings.getMinimumQuality()));
        return report;
    }

    private FilterReport makePathogenicityFilterReport(ExomiserSettings settings, List<VariantEvaluation> variantEvaluations) {
        FilterReport report = makeDefaultVariantFilterReport(FilterType.PATHOGENICITY_FILTER, variantEvaluations);
        
        if (settings.removePathFilterCutOff()) {
            report.addMessage("Retained all non-pathogenic variants of all types. Scoring was applied, but the filter passed all variants.");
        } else {
            report.addMessage("Retained all non-pathogenic missense variants");
            //this is redundant as the defaut now is to keep all these anyway, but maybe somone will be interested in the cutoffs used for the categories?
            // Set up the message - these scores ought to belong to the score itself thather than being hard-coded here...
//            report.addMessage("Pathogenicity predictions are based on the dbNSFP-normalized values");
//            report.addMessage("Mutation Taster: >0.95 assumed pathogenic, prediction categories not shown");
//            report.addMessage("Polyphen2 (HVAR): \"D\" (> 0.956,probably damaging), \"P\": [0.447-0.955], "
//                    + "possibly damaging, and \"B\", <0.447, benign.");
//            report.addMessage("SIFT: \"D\"<0.05, damaging and \"T\"&ge;0.05, tolerated");
        }
        return report;
    }

    private FilterReport makeIntervalFilterReport(ExomiserSettings settings, List<VariantEvaluation> variantEvaluations) {
        FilterReport report = makeDefaultVariantFilterReport(FilterType.INTERVAL_FILTER, variantEvaluations);

        report.addMessage(String.format("Restricted variants to interval: %s", settings.getGeneticInterval()));
        
        return report;
    }

    private FilterReport makeInheritanceFilterReport(ExomiserSettings settings, List<Gene> genes) {
        FilterReport report = makeDefaultGeneFilterReport(FilterType.INHERITANCE_FILTER, genes);
        
        report.addMessage(String.format("Total of %d genes were analyzed. %d had genes with distribution compatible with %s inheritance.",
                genes.size(), report.getPassed(), settings.getModeOfInheritance()));
        
        return report;
    }

    /**
     * 
     * @param filterType
     * @param variantEvaluations
     * @return 
     */
    private FilterReport makeDefaultVariantFilterReport(FilterType filterType, List<VariantEvaluation> variantEvaluations) {
        int passed = 0;

        for (VariantEvaluation ve : variantEvaluations) {
            if (ve.passedFilter(filterType)) {
                passed++;
            }
        }
        
        int failed = variantEvaluations.size() - passed;
        return new FilterReport(filterType, passed, failed);
    }
    
    private FilterReport makeDefaultGeneFilterReport(FilterType filterType, List<Gene> genes) {
        int passed = 0;

        for (Gene gene : genes) {
            if (gene.passedFilter(filterType)) {
                passed++;
            }
        }
        
        int failed = genes.size() - passed;
        return new FilterReport(filterType, passed, failed);
    }
}
