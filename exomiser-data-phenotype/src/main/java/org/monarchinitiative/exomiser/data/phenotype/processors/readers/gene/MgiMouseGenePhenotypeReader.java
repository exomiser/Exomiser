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

package org.monarchinitiative.exomiser.data.phenotype.processors.readers.gene;

import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.ResourceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class MgiMouseGenePhenotypeReader implements ResourceReader<List<GenePhenotype>> {

    private static final Logger logger = LoggerFactory.getLogger(MgiMouseGenePhenotypeReader.class);

    private final Resource mgiGenePhenoResource;

    public MgiMouseGenePhenotypeReader(Resource mgiGenePhenoResource) {
        this.mgiGenePhenoResource = mgiGenePhenoResource;
    }

    @Override
    public List<GenePhenotype> read() {
        logger.info("Reading {}", mgiGenePhenoResource.getResourcePath());
        List<GenePhenotype> genePhenotypes = new ArrayList<>();

        //Genotypes and Mammalian Phenotype Annotations for Marker Type Genes excluding conditional mutations (tab-delimited)
        //Allelic Composition	Allele Symbol(s)	Allele ID(s)	Genetic Background	Mammalian Phenotype ID	PubMed ID (pipe-delimited)	MGI Marker Accession ID (pipe-delimited)	MGI Genotype Accession ID (pipe-delimited)
        //Rb1<tm1Tyj>/Rb1<tm1Tyj>	Rb1<tm1Tyj>	MGI:1857242	involves: 129S2/SvPas	MP:0000600	12529408	MGI:97874	MGI:2166359
        //Rb1<tm1Tyj>/Rb1<tm1Tyj>	Rb1<tm1Tyj>	MGI:1857242	involves: 129S2/SvPas	MP:0001716	16449662	MGI:97874	MGI:2166359
        //Rb1<tm1Tyj>/Rb1<tm1Tyj>	Rb1<tm1Tyj>	MGI:1857242	involves: 129S2/SvPas	MP:0001698	16449662	MGI:97874	MGI:2166359
        //Rbpj<tm1Kyo>/Rbpj<tm1Kyo>	Rbpj<tm1Kyo>	MGI:1857411	involves: 129S2/SvPas * C57BL/6	MP:0001614	15466160	MGI:96522	MGI:2166381
        //Rbpj<tm1Kyo>/Rbpj<tm1Kyo>	Rbpj<tm1Kyo>	MGI:1857411	involves: 129S2/SvPas * C57BL/6	MP:0000364	15466160	MGI:96522	MGI:2166381
        //Shank2<em2(IMPC)Rbrc>/Shank2<em2(IMPC)Rbrc>	Shank2<em2(IMPC)Rbrc>	MGI:6156159	Not Specified	MP:0001513		MGI:2671987	MGI:6263466
        //Shank2<em2(IMPC)Rbrc>/Shank2<em2(IMPC)Rbrc>	Shank2<em2(IMPC)Rbrc>	MGI:6156159	Not Specified	MP:0002644		MGI:2671987	MGI:6263466

        Map<String, String> mouse2geneMap = new LinkedHashMap<>();
        SetMultimap<String, String> mouse2PhenotypeMap = TreeMultimap.create();

        try (BufferedReader reader = mgiGenePhenoResource.newBufferedReader()) {
            for (String line; (line = reader.readLine()) != null; ) {
                String[] fields = line.split("\\t");
                String mpId = fields[4];
                String modelId = fields[7];
                String mgiGeneId = fields[6];
                mouse2geneMap.put(modelId, mgiGeneId);
                logger.debug("modelId={} geneId={} mpId={}", modelId, mgiGeneId, mpId);
                // skip multi-gene models
                if (!mgiGeneId.contains("|")) {
                    mouse2PhenotypeMap.put(modelId, mpId);
                }
            }
        } catch (IOException e) {
            logger.error("", e);
        }

        for (Map.Entry<String, Collection<String>> entry : mouse2PhenotypeMap.asMap().entrySet()) {
            String modelId = entry.getKey();
            Collection<String> mpIds = entry.getValue();
            String markerId = mouse2geneMap.get(modelId);
            genePhenotypes.add(new GenePhenotype(modelId, markerId, mpIds));
        }
        genePhenotypes.sort(Comparator.comparing(GenePhenotype::getGeneId).thenComparing(GenePhenotype::getId));

        logger.info("Read {} MGI mouse gene-phenotype models from {}", genePhenotypes.size(), mgiGenePhenoResource.getResourcePath());
        return genePhenotypes;
    }
}
