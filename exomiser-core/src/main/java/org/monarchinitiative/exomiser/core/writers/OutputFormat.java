/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Enum for representing the desired format of the output.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public enum OutputFormat {

    HTML("html"),
    VCF("vcf"),
    TSV_GENE("genes.tsv"),
    TSV_VARIANT("variants.tsv"),
    JSON("json");

    private static final Logger logger = LoggerFactory.getLogger(OutputFormat.class);

    private final String fileExtension;

    OutputFormat(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public static OutputFormat parseFormat(String value) {
        switch (value.trim().toUpperCase()) {
            case "TSV_GENE", "TAB-GENE", "TSV-GENE":
                return OutputFormat.TSV_GENE;
            case "TSV_VARIANT", "TAB-VARIANT", "TSV-VARIANT":
                return OutputFormat.TSV_VARIANT;
            case "VCF":
                return OutputFormat.VCF;
            case "JSON":
                return OutputFormat.JSON;
            case "HTML":
                return OutputFormat.HTML;
            default:
                logger.info("Unrecognised output format '{}'. Valid options are {} - defaulting to {}", value, List.of(OutputFormat
                        .values()), OutputFormat.HTML);
                return OutputFormat.HTML;
        }
    }
}
