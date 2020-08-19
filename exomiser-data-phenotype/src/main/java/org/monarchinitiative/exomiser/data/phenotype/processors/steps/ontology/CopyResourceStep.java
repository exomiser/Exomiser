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

package org.monarchinitiative.exomiser.data.phenotype.processors.steps.ontology;

import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.steps.ProcessingStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Copies a resource to the specified target directory.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class CopyResourceStep implements ProcessingStep {

    private static final Logger logger = LoggerFactory.getLogger(CopyResourceStep.class);

    private final Resource resource;
    private final Path releaseDir;

    public CopyResourceStep(Resource resource, Path targetDir) {
        this.resource = resource;
        this.releaseDir = targetDir;
    }

    @Override
    public void run() {
        Path source = resource.getResourcePath();
        Path target = releaseDir.resolve(resource.getFileName());
        try {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Copied {} to {}", source, target);
        } catch (IOException e) {
           logger.error("Unable to copy {} to {}", source, target, e);
        }
    }
}
