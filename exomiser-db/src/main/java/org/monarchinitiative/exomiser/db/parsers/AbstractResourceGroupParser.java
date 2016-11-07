/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.monarchinitiative.exomiser.db.parsers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public abstract class AbstractResourceGroupParser {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    
    protected void logResourceMissing(String resourceGroupName, Class<? extends ResourceParser> clazz) {
        logger.error("MISSING RESOURCE for {} data required by {} - check this is defined in resource configuration class.", resourceGroupName, clazz);
    }
}
