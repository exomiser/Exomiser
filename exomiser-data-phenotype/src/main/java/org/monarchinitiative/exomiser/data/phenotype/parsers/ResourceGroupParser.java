/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.phenotype.parsers;

import org.monarchinitiative.exomiser.data.phenotype.resources.ResourceGroup;

import java.nio.file.Path;

/**
 * Interface defining the functionality for how a group of parsers should work in concert
 * in order to produce the output file(s).
 *
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface ResourceGroupParser {

    /**
     * Parses the {@code org.monarchinitiative.exomiser.resources.Resource} contained in the
     * {@code org.monarchinitiative.exomiser.resources.ResourceGroup} according to
     * the internal rules of the {@cadede.charite.compbio.exomiser.parsers.ResourceGroupParser}
     * implementation.
     *
     * @param resourceGroup
     * @param inDir
     * @param outDir
     */
    void parseResources(ResourceGroup resourceGroup, Path inDir, Path outDir);

    /**
     * Checks that all the required resources for the ResourceGroupParser are present. 
     * @param resourceGroup
     * @return false if any resource is missing.
     */
    boolean requiredResourcesPresent(ResourceGroup resourceGroup);
}
