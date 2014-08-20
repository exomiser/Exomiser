/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.model.ExomiserSettings;
import de.charite.compbio.exomiser.core.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.ArrayList;
import java.util.List;
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

    /**
     * Makes a List of {@code FilterReport} for the {@code FilterType} specified.
     * @param filterTypes
     * @param settings
     * @param variantEvaluations
     * @return a List of {@code FilterReport} 
     */
    public List<FilterReport> makeFilterReports(List<FilterType> filterTypes, ExomiserSettings settings, List<VariantEvaluation> variantEvaluations) {

        List<FilterReport> filterReports = new ArrayList<>();

        for (FilterType filterType : filterTypes) {
            filterReports.add(makeFilterReport(filterType, settings, variantEvaluations));
        }

        return filterReports;
    }

    /**
     * Returns a FilterReport for the VariantEvaluation and the specified
     * FilterType. If the FilterType is not recognised or supported then this
     * method will return a default report with no messages.
     *
     * @param filterType
     * @param settings
     * @param variantEvaluations
     * @return
     */
    public FilterReport makeFilterReport(FilterType filterType, ExomiserSettings settings, List<VariantEvaluation> variantEvaluations) {
        switch (filterType) {
            case TARGET_FILTER:
                return makeTargetFilterReport(settings, variantEvaluations);
            case FREQUENCY_FILTER:
                return makeFrequencyFilterReport(settings, variantEvaluations);
            case QUALITY_FILTER:
                return makeQualityFilterReport(settings, variantEvaluations);
            case PATHOGENICITY_FILTER:
                return makePathogenicityFilterReport(settings, variantEvaluations);
            case INTERVAL_FILTER:
                return makeIntervalFilterReport(settings, variantEvaluations);
            case INHERITANCE_FILTER:
                return makeInheritanceFilterReport(settings, variantEvaluations);
            default:
                return makeDefaultFilterReport(filterType, variantEvaluations);
        }
    }

    private FilterReport makeTargetFilterReport(ExomiserSettings settings, List<VariantEvaluation> variantEvaluations) {
        FilterReport report = makeDefaultFilterReport(FilterType.TARGET_FILTER, variantEvaluations);
        
        report.addMessage(String.format("Removed a total of %d off-target variants from further consideration", report.getFailed()));
        report.addMessage("Off target variants are defined as synonymous, intergenic, intronic but not in splice sequences");

        return report;
    }

    private FilterReport makeFrequencyFilterReport(ExomiserSettings settings, List<VariantEvaluation> variantEvaluations) {
        FilterReport report = makeDefaultFilterReport(FilterType.FREQUENCY_FILTER, variantEvaluations);
        
        int numDbSnpFreqData = 0;
        int numDbSnpRsId = 0;
        int numEspFreqData = 0;

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
        }
        
        int before = report.getPassed() + report.getFailed();
        
        report.addMessage(String.format("Allele frequency < %.2f %%", settings.getMaximumFrequency()));
        report.addMessage(String.format("Frequency Data available in dbSNP (for 1000 Genomes Phase I) for %d variants (%.1f%%)", numDbSnpFreqData, 100f * (double) numDbSnpFreqData / before));
        report.addMessage(String.format("dbSNP \"rs\" id available for %d variants (%.1f%%)", numDbSnpRsId, 100 * (double) numDbSnpRsId / before));
        report.addMessage(String.format("Data available in Exome Server Project for %d variants (%.1f%%)", numEspFreqData, 100f * (double) numEspFreqData / before));
        
        return report;
    }

    private FilterReport makeQualityFilterReport(ExomiserSettings settings, List<VariantEvaluation> variantEvaluations) {
        FilterReport report = makeDefaultFilterReport(FilterType.QUALITY_FILTER, variantEvaluations);

        report.addMessage(String.format("PHRED quality %.1f", settings.getMinimumQuality()));
        return report;
    }

    private FilterReport makePathogenicityFilterReport(ExomiserSettings settings, List<VariantEvaluation> variantEvaluations) {
        FilterReport report = makeDefaultFilterReport(FilterType.PATHOGENICITY_FILTER, variantEvaluations);
        
        if (settings.keepNonPathogenicMissense()) {
            report.addMessage("Retained all non-pathogenic missense variants");
        } else {
            // Set up the message - these scores ought to belong to the score itself thather than being hard-coded here...
            report.addMessage("Pathogenicity predictions are based on the dbNSFP-normalized values");
            report.addMessage("Mutation Taster: >0.95 assumed pathogenic, prediction categories not shown");
            report.addMessage("Polyphen2 (HVAR): \"D\" (> 0.956,probably damaging), \"P\": [0.447-0.955], "
                    + "possibly damaging, and \"B\", <0.447, benign.");
            report.addMessage("SIFT: \"D\"<0.05, damaging and \"T\"&ge;0.05, tolerated");
        }
        return report;
    }

    private FilterReport makeIntervalFilterReport(ExomiserSettings settings, List<VariantEvaluation> variantEvaluations) {
        FilterReport report = makeDefaultFilterReport(FilterType.INTERVAL_FILTER, variantEvaluations);

        report.addMessage(String.format("Restricted variants to interval: %s", settings.getGeneticInterval()));
        
        return report;
    }

    private FilterReport makeInheritanceFilterReport(ExomiserSettings settings, List<VariantEvaluation> variantEvaluations) {
        FilterReport report = makeDefaultFilterReport(FilterType.INHERITANCE_FILTER, variantEvaluations);
        
        report.addMessage(String.format("Total of %d variants were analyzed. %d had variants with distribution compatible with %s inheritance.",
                variantEvaluations.size(), report.getPassed(), settings.getModeOfInheritance()));
        
        return report;
    }

    /**
     * 
     * @param filterType
     * @param variantEvaluations
     * @return 
     */
    private FilterReport makeDefaultFilterReport(FilterType filterType, List<VariantEvaluation> variantEvaluations) {
        int passed = 0;

        for (VariantEvaluation ve : variantEvaluations) {
            if (ve.passedFilter(filterType)) {
                passed++;
            }
        }
        
        int failed = variantEvaluations.size() - passed;
        return new FilterReport(filterType, passed, failed);
    }
}
