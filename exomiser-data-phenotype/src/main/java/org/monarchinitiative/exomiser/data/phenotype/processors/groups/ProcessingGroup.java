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

package org.monarchinitiative.exomiser.data.phenotype.processors.groups;


import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.ResourceDownloader;
import org.monarchinitiative.exomiser.data.phenotype.processors.steps.ProcessingStep;

import java.util.List;

/**
 * A {@link ProcessingGroup} is a group of {@link ProcessingStep} which read and process data from a common group of
 * {@link Resource}. Once the downloadResources method has been called the required {@link Resource}
 * will be available for the {@link ProcessingGroup} or its {@link ProcessingStep}s to process.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public interface ProcessingGroup {

    String getName();

    List<Resource> getResources();

    default void downloadResources() {
        getResources().forEach(ResourceDownloader::downloadResource);
    }

    /**
     * Handles the parsing and output of the Resource(s) associated with the ProcessingGroup.
     */
    void processResources();

}
