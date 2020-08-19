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

package org.monarchinitiative.exomiser.data.phenotype.processors.steps.disease;

import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.DiseasePhenotype;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.disease.DiseasePhenotypeReader;
import org.monarchinitiative.exomiser.data.phenotype.processors.steps.ProcessingStep;
import org.monarchinitiative.exomiser.data.phenotype.processors.writers.OutputLineWriter;

import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class DiseasePhenotypeStep implements ProcessingStep {

    private final DiseasePhenotypeReader diseasePhenotypeReader;
    private final OutputLineWriter<DiseasePhenotype> diseasePhenotypeWriter;

    private DiseasePhenotypeStep(DiseasePhenotypeReader diseasePhenotypeReader, OutputLineWriter<DiseasePhenotype> diseasePhenotypeWriter) {
        this.diseasePhenotypeReader = diseasePhenotypeReader;
        this.diseasePhenotypeWriter = diseasePhenotypeWriter;
    }

    public static DiseasePhenotypeStep create(Resource phenotypeAnnotationsResource, OutputLineWriter<DiseasePhenotype> diseasePhenotypeWriter) {
        DiseasePhenotypeReader diseasePhenotypeReader = new DiseasePhenotypeReader(phenotypeAnnotationsResource);
        return new DiseasePhenotypeStep(diseasePhenotypeReader, diseasePhenotypeWriter);
    }

    @Override
    public void run() {
        List<DiseasePhenotype> diseasePhenotypes = diseasePhenotypeReader.read();
        diseasePhenotypeWriter.write(diseasePhenotypes);
    }

}
