/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.writers;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ResultsWriterFactoryTest {

    private ResultsWriterFactory instance;

    @Before
    public void setUp() {
        instance = new ResultsWriterFactory();
    }

    @Test
    public void testGetHtmlResultsWriter() {
        OutputFormat outputFormat = OutputFormat.HTML;
        ResultsWriter result = instance.getResultsWriter(outputFormat);
        assertThat(result, instanceOf(HtmlResultsWriter.class));

    }

    @Test
    public void testGetTsvGeneResults() {
        OutputFormat outputFormat = OutputFormat.TSV_GENE;
        ResultsWriter result = instance.getResultsWriter(outputFormat);
        assertThat(result, instanceOf(TsvGeneResultsWriter.class));
    }

    @Test
    public void testGetTsvVariantResultsWriter() {
        OutputFormat outputFormat = OutputFormat.TSV_VARIANT;
        ResultsWriter result = instance.getResultsWriter(outputFormat);
        assertThat(result, instanceOf(TsvVariantResultsWriter.class));
    }

    @Test
    public void testGetVcfResultsWriter() {
        OutputFormat outputFormat = OutputFormat.VCF;
        ResultsWriter result = instance.getResultsWriter(outputFormat);
        assertThat(result, instanceOf(VcfResultsWriter.class));
    }

    @Test
    public void testGetPhenoGridResultsWriter() {
        OutputFormat outputFormat = OutputFormat.PHENOGRID;
        ResultsWriter result = instance.getResultsWriter(outputFormat);
        assertThat(result, instanceOf(PhenogridWriter.class));
    }

    @Test
    public void testGetJsonResultsWriter() {
        OutputFormat outputFormat = OutputFormat.JSON;
        ResultsWriter result = instance.getResultsWriter(outputFormat);
        assertThat(result, instanceOf(JsonResultsWriter.class));
    }

}
