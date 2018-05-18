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

/**
 * Provides an entry point for getting a ResultsWriter for a specific format.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
public class ResultsWriterFactory {

    /**
     * Build {@link ResultsWriter} for the given {@link OutputFormat}.
     * 
     * @param outputFormat
     *            the format to use for the output
     * @return the constructed {@link ResultsWriter} implementation
     */
    public ResultsWriter getResultsWriter(OutputFormat outputFormat) {
        switch (outputFormat){
            case TSV_GENE:
                return new TsvGeneResultsWriter();
            case TSV_VARIANT:
                return new TsvVariantResultsWriter();
            case VCF:
                return new VcfResultsWriter();
            case PHENOGRID:
                return new PhenogridWriter();
            case JSON:
                return new JsonResultsWriter();
            case HTML:
            default:
                return new HtmlResultsWriter();
        }
    }

}
