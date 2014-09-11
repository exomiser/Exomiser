/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.model.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.ExomiserSettings.SettingsBuilder;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for FilterReportFactory.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FilterReportFactoryTest {

    private FilterReportFactory instance;

    public FilterReportFactoryTest() {
    }

    @Before
    public void setUp() {
        instance = new FilterReportFactory();
    }

    @Test
    public void testMakeFilterReportsNoTypesSpecifiedReturnsEmptyList() {
        ExomiserSettings settings = new SettingsBuilder().build();
        List<VariantEvaluation> variantEvaluations = new ArrayList<>();
        List<FilterType> filterTypes = new ArrayList<>();
        List<FilterReport> expResult = new ArrayList<>();

        List<FilterReport> result = instance.makeFilterReports(filterTypes, settings, variantEvaluations);

        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testMakeFilterReportsFrequencyPathogenicityTypesSpecifiedReturnsListWithTwoReports() {
        ExomiserSettings settings = new SettingsBuilder().build();
        List<VariantEvaluation> variantEvaluations = new ArrayList<>();
        List<FilterType> filterTypes = new ArrayList<>();
        filterTypes.add(FilterType.FREQUENCY_FILTER);
        filterTypes.add(FilterType.PATHOGENICITY_FILTER);

        List<FilterReport> result = instance.makeFilterReports(filterTypes, settings, variantEvaluations);

        assertThat(result.size(), equalTo(filterTypes.size()));
    }

    @Test
    public void testMakeDefaultFilterReport() {
        FilterType filterType = FilterType.BED_FILTER;
        ExomiserSettings settings = new SettingsBuilder().build();
        List<VariantEvaluation> variantEvaluations = new ArrayList<>();

        FilterReport expResult = new FilterReport(filterType, 0, 0);
        FilterReport result = instance.makeFilterReport(filterType, settings, variantEvaluations);

        assertThat(result, equalTo(expResult));
    }

}
