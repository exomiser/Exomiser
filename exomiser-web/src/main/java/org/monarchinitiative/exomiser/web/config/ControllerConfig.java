/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
public class ControllerConfig extends WebMvcConfigurerAdapter {

    Logger logger = LoggerFactory.getLogger(ControllerConfig.class);

    @Autowired
    private Environment env;

    @Bean
    public Integer maxVariants() {
        Integer maxVariants = Integer.valueOf(env.getProperty("exomiser.web.max-variants"));
        logger.info("Set max variants to {}", maxVariants);
        return maxVariants;
    }

    @Bean
    public Integer maxGenes() {
        Integer maxGenes = Integer.valueOf(env.getProperty("exomiser.web.max-genes"));
        logger.info("Set max genes to {}", maxGenes);
        return maxGenes;
    }

    @Bean
    public Boolean clinicalInstance() {
        Boolean clinicalInstance = Boolean.valueOf(env.getProperty("exomiser.web.clinical-instance"));
        if (clinicalInstance) {
            logger.info("Instance is running in a CLINICAL setting.");
        } else {
            logger.info("Instance is running in a NON-CLINICAL setting.");
        }
        return clinicalInstance;
    }
}
