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

package org.monarchinitiative.exomiser.data.phenotype.processors.steps.gene;

import com.google.common.collect.Multimap;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.gene.GeneModel;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.gene.GeneOrtholog;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.gene.MonarchFishGeneLabelReader;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.gene.MonarchFishGenePhenotypeReader;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.gene.ZfinGeneOrthologReader;
import org.monarchinitiative.exomiser.data.phenotype.processors.steps.ProcessingStep;
import org.monarchinitiative.exomiser.data.phenotype.processors.writers.OutputLineWriter;

import java.util.List;
import java.util.Map;


/**
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class FishGeneModelStep implements ProcessingStep {
    // Fish-Human Orthologs
    private final ZfinGeneOrthologReader zfinGeneOrthologReader;
    private final OutputLineWriter<GeneOrtholog> fishGeneOrthologOutputLineWriter;

    // Gene-Phenotype models
    private final MonarchFishGeneLabelReader monarchFishGeneLabelReader;
    private final MonarchFishGenePhenotypeReader monarchFishGenePhenotypeReader;
    private final OutputLineWriter<GeneModel> fishGeneModelOutputLineWriter;

    public FishGeneModelStep(ZfinGeneOrthologReader zfinGeneOrthologReader, OutputLineWriter<GeneOrtholog> fishGeneOrthologOutputLineWriter, MonarchFishGeneLabelReader monarchFishGeneLabelReader, MonarchFishGenePhenotypeReader monarchFishGenePhenotypeReader, OutputLineWriter<GeneModel> fishGeneModelOutputLineWriter) {
        this.zfinGeneOrthologReader = zfinGeneOrthologReader;
        this.fishGeneOrthologOutputLineWriter = fishGeneOrthologOutputLineWriter;
        this.monarchFishGeneLabelReader = monarchFishGeneLabelReader;
        this.monarchFishGenePhenotypeReader = monarchFishGenePhenotypeReader;
        this.fishGeneModelOutputLineWriter = fishGeneModelOutputLineWriter;
    }

    @Override
    public void run() {
        List<GeneOrtholog> fishGeneOrthologs = zfinGeneOrthologReader.read();
        fishGeneOrthologOutputLineWriter.write(fishGeneOrthologs);

        Map<String, String> fishGeneLabels = monarchFishGeneLabelReader.read();
        Multimap<String, String> fishGenePhenotypes = monarchFishGenePhenotypeReader.read();

        List<GeneModel> fishGeneModels = new FishGeneModelFactory(fishGeneLabels, fishGenePhenotypes).buildGeneModels();
        fishGeneModelOutputLineWriter.write(fishGeneModels);
    }
}
