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

import org.monarchinitiative.exomiser.data.phenotype.processors.model.gene.GeneModel;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.gene.GeneOrtholog;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.gene.*;
import org.monarchinitiative.exomiser.data.phenotype.processors.steps.ProcessingStep;
import org.monarchinitiative.exomiser.data.phenotype.processors.writers.OutputLineWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class MouseGeneModelStep implements ProcessingStep {

    private static final Logger logger = LoggerFactory.getLogger(MouseGeneModelStep.class);

    // Mouse-Human Orthologs
    private final MgiMouseGeneOrthologReader mgiMouseGeneOrthologReader;
    private final EnsemblMouseGeneOrthologReader ensemblMouseGeneOrthologReader;
    private final OutputLineWriter<GeneOrtholog> mouseGeneOrthologOutputLineWriter;

    // Gene-Phenotype models
    private final MgiMouseGenePhenotypeReader mgiMouseGenePhenotypeReader;
    private final ImpcMouseGenePhenotypeReader impcMouseGenePhenotypeReader;
    private final OutputLineWriter<GeneModel> mouseGeneModelOutputLineWriter;

    public MouseGeneModelStep(MgiMouseGeneOrthologReader mgiMouseGeneOrthologReader, EnsemblMouseGeneOrthologReader ensemblMouseGeneOrthologReader, OutputLineWriter<GeneOrtholog> mouseGeneOrthologOutputLineWriter, MgiMouseGenePhenotypeReader mgiMouseGenePhenotypeReader, ImpcMouseGenePhenotypeReader impcMouseGenePhenotypeReader, OutputLineWriter<GeneModel> mouseGeneModelOutputLineWriter) {
        this.mgiMouseGeneOrthologReader = mgiMouseGeneOrthologReader;
        this.ensemblMouseGeneOrthologReader = ensemblMouseGeneOrthologReader;
        this.mouseGeneOrthologOutputLineWriter = mouseGeneOrthologOutputLineWriter;

        this.mgiMouseGenePhenotypeReader = mgiMouseGenePhenotypeReader;
        this.impcMouseGenePhenotypeReader = impcMouseGenePhenotypeReader;
        this.mouseGeneModelOutputLineWriter = mouseGeneModelOutputLineWriter;
    }

    @Override
    public void run() {
        logger.info("Reading mouse gene ortholog resources..");
        List<GeneOrtholog> mgiMouseGeneOrthologs = mgiMouseGeneOrthologReader.read();
        logger.info("Read {} MGI mouse gene orthologs", mgiMouseGeneOrthologs.size());

        List<GeneOrtholog> ensemblMouseGeneOrthologs = ensemblMouseGeneOrthologReader.read();
        logger.info("Read {} Ensembl mouse gene orthologs", ensemblMouseGeneOrthologs.size());

        // there will be overlaps, so create a unique set of the two
        // TODO EXTRACT MouseGeneOrthologFactory for testing
        //  List<GeneOrtholog> uniqueOrthologs = mouseGeneOrthologFactory.buildMouseGeneOrthologs();
        List<GeneOrtholog> uniqueOrthologs = getUniqueGeneOrthologs(mgiMouseGeneOrthologs, ensemblMouseGeneOrthologs);
        logger.info("Read {} unique mouse gene orthologs", uniqueOrthologs.size());

        mouseGeneOrthologOutputLineWriter.write(uniqueOrthologs);

        // read MGI mouse models (these have no gene symbols)
        List<GenePhenotype> mgiModels = mgiMouseGenePhenotypeReader.read();
        // read IMPC mouse models (these also have no gene symbols)
        List<GenePhenotype> impcModels = impcMouseGenePhenotypeReader.read();

        // merge all GeneModels, add gene symbols from GeneOrthologs
        MouseGeneModelFactory mouseGeneModelFactory = new MouseGeneModelFactory(uniqueOrthologs, mgiModels, impcModels);
        List<GeneModel> mouseModels = mouseGeneModelFactory.buildGeneModels();

        // write final GeneModels to outfile.
        mouseGeneModelOutputLineWriter.write(mouseModels);
    }

    /**
     * protected access for testing purposed only
     */
    protected static List<GeneOrtholog> getUniqueGeneOrthologs(List<GeneOrtholog> mgiMouseGeneOrthologs, List<GeneOrtholog> ensemblMouseGeneOrthologs) {
        Map<String, GeneOrtholog> uniqueOrthologMap = new TreeMap<>();
        // n.b. the gene symbol can be different we only want one. For example:
        // MGI:1921958|Dnaaf6|DNAAF6|139212
        // MGI:1921958|Pih1d3|PIH1D3|139212
        // Will take it from MGI first
        collectUniqueOrthologs(mgiMouseGeneOrthologs, uniqueOrthologMap);
        collectUniqueOrthologs(ensemblMouseGeneOrthologs, uniqueOrthologMap);

        List<GeneOrtholog> uniqueOrthologs = new ArrayList<>(uniqueOrthologMap.values());
        uniqueOrthologs.sort(Comparator.comparing(GeneOrtholog::getOrthologGeneId).thenComparing(GeneOrtholog::getHumanGeneSymbol));
        return uniqueOrthologs;
    }

    private static void collectUniqueOrthologs(List<GeneOrtholog> mgiMouseGeneOrthologs, Map<String, GeneOrtholog> uniqueOrthologs) {
        for (GeneOrtholog geneOrtholog : mgiMouseGeneOrthologs) {
            String key = geneOrtholog.getOrthologGeneId() + "_" + geneOrtholog.getEntrezGeneId();
            uniqueOrthologs.put(key, geneOrtholog);
        }
    }
}
