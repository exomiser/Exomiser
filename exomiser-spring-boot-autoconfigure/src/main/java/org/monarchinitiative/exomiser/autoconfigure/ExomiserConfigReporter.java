/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2022 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ExomiserConfigReporter {

    private static final Logger logger = LoggerFactory.getLogger(ExomiserConfigReporter.class);

    public ExomiserConfigReporter(ExomiserProperties exomiserProperties, Environment environment) {

        var dataDir = propertyOrDefault(exomiserProperties.getDataDirectory(), "-");
        logger.info("exomiser.data-directory: {}", dataDir);

        var hg19Properties = exomiserProperties.getHg19();
        var hg19DataVersion = propertyOrDefault(hg19Properties.getDataVersion(), "-");
        logger.info("exomiser.hg19.data-version: {}", hg19DataVersion);

        var hg19ClinVarVersion = propertyOrDefault(hg19Properties.getClinVarDataVersion(), "-");
        // this is the default placeholder which will be replaced by the actual version if enabled by the user
        if (!hg19ClinVarVersion.equals("${exomiser.hg19.data-version}")) {
            logger.info("exomiser.hg19.clinvar-data-version: {}", hg19ClinVarVersion);
        }

        var hg38Properties = exomiserProperties.getHg38();
        var hg38DataVersion = propertyOrDefault(hg38Properties.getDataVersion(), "-");
        logger.info("exomiser.hg38.data-version: {}", hg38DataVersion);

        var hg38ClinVarVersion = propertyOrDefault(hg38Properties.getClinVarDataVersion(), "-");
        // this is the default placeholder which will be replaced by the actual version if enabled by the user
        if (!hg38ClinVarVersion.equals("${exomiser.hg38.data-version}")) {
            logger.info("exomiser.hg38.clinvar-data-version: {}", hg38ClinVarVersion);
        }

        var phenoProperties = exomiserProperties.getPhenotype();
        var phenoDataVersion = propertyOrDefault(phenoProperties.getDataVersion(), "-");
        logger.info("exomiser.phenotype.data-version: {}", phenoDataVersion);

        String cacheType = environment.getProperty("spring.cache.type");
        if (cacheType != null && !cacheType.equals("none")) {
            logger.info("spring.cache.type: {}", cacheType);
        }
    }

    private String propertyOrDefault(String property, String defaultValue) {
        return property.isEmpty() ? defaultValue : property;
    }

}
