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

package org.monarchinitiative.exomiser.data.phenotype.processors.readers.ontology;

import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.ontology.OboOntologyTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toMap;

/**
 * Class for reading, processing and writing out OwlSim Phenodigm cache files. This class is a slight departure from the
 * usual ResourceReader/OutputFileWriter pattern used in the rest of the package as the cache files are potentially very
 * large (tens of GB) due to them being all-vs-all ontology comparisons. For this reason we simple stream and process the
 * files line-by-line.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class OwlSimPhenodigmProcessor {

    private static final Logger logger = LoggerFactory.getLogger(OwlSimPhenodigmProcessor.class);

    private final Resource hpMappingsResource;
    private final Path hpMappingsOutFile;

    public OwlSimPhenodigmProcessor(Resource hpMappingsResource, Path hpMappingsOutFile) {
        this.hpMappingsResource = hpMappingsResource;
        this.hpMappingsOutFile = hpMappingsOutFile;
    }

    public void process(List<OboOntologyTerm> hpTerms, List<OboOntologyTerm> otherTerms) {
        logger.info("Processing file: {}", hpMappingsResource.getResourcePath());
        logger.info("Writing out to: {}", hpMappingsOutFile);

        PhenodigmCacheLineProcessor cacheLineProcessor = new PhenodigmCacheLineProcessor(hpTerms, otherTerms);

        try (BufferedReader reader = hpMappingsResource.newBufferedReader();
             BufferedWriter writer = Files.newBufferedWriter(hpMappingsOutFile, Charset.defaultCharset())) {
            for (String line; (line = reader.readLine()) != null; ) {
                String outLine = cacheLineProcessor.processLine(line);
                writer.write(outLine);
                writer.newLine();
            }
        } catch (Exception ex) {
            logger.error("Error processing {}", hpMappingsResource, ex);
        }
        logger.info("Written {} phenotype mappings to {}", cacheLineProcessor.linesProcessed(), hpMappingsOutFile);
    }

    static class PhenodigmCacheLineProcessor {
        private final Map<String, String> hpIdTerms;
        private final Map<String, String> otherIdTerms;

        private final AtomicInteger id = new AtomicInteger();

        public PhenodigmCacheLineProcessor(List<OboOntologyTerm> hpTerms, List<OboOntologyTerm> otherTerms) {
            hpIdTerms = idLabelCache(hpTerms);
            otherIdTerms = idLabelCache(otherTerms);
        }

        private Map<String, String> idLabelCache(List<OboOntologyTerm> ontologyTerms) {
            return ontologyTerms.stream().distinct().collect(toMap(OboOntologyTerm::getId, OboOntologyTerm::getLabel));
        }

        public String processLine(String line) {
            String[] fields = line.split("\t");

            String queryId = reformatCurie(fields[0]);
            String queryTerm = hpIdTerms.getOrDefault(queryId, "");

            String hitId = reformatCurie(fields[1]);
            String hitTerm = otherIdTerms.getOrDefault(hitId, "");

            String simJ = fields[2];
            String ic = fields[3];
            double score = Math.sqrt(Double.parseDouble(simJ) * Double.parseDouble(ic));

            String lcs = reformatCurie(fields[4].split(";")[0]);
            String lcsTerm = hpIdTerms.containsKey(lcs) ? hpIdTerms.get(lcs) : otherIdTerms.getOrDefault(lcs, "");

            return String.format("%d|%s|%s|%s|%s|%s|%s|%s|%s|%s", id.getAndIncrement(), queryId, queryTerm, hitId, hitTerm, simJ, ic, score, lcs, lcsTerm);
        }

        public int linesProcessed() {
            return id.get();
        }

        private String reformatCurie(String field) {
            return field.replace("_", ":");
        }

    }

}
