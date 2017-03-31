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

package org.monarchinitiative.exomiser.core.prioritisers.service;

import com.google.common.collect.Lists;
import org.monarchinitiative.exomiser.core.phenotype.Organism;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatch;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneDiseaseModel;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneModel;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneOrthologModel;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TestPrioritiserDataFileReader {

    private static final String COMMENT_LINE_PREFIX = "#";

    private TestPrioritiserDataFileReader() {
        //static utility class
    }

    public static List<GeneModel> readOrganismData(String organismModelTestDataFile) {
        return readLines(organismModelTestDataFile)
                .filter(line -> !line.startsWith(COMMENT_LINE_PREFIX))
                .map(lineToGeneModel())
                .collect(toList());
    }

    private static Function<String, GeneOrthologModel> lineToGeneModel() {
        //MOUSE	MGI:95523_118	2263	FGFR2	MGI:95523	Fgfr2	MP:0009522,MP:0009525
        //FISH	ZDB-GENE-081119-4_3835	341640	HGNC:25396	ZDB-GENE-081119-4	frem2b	ZP:0004670,ZP:0004671,ZP:0004669
        return line -> {
            String[] fields = line.split("\t");
            return new GeneOrthologModel(fields[1], Organism.valueOf(fields[0]), Integer.valueOf(fields[2]), fields[3], fields[4], fields[5], getOntologyTerms(fields[6]));
        };
    }

    public static List<Disease> readDiseaseData(String diseaseModelTestDataFile) {
        return readLines(diseaseModelTestDataFile)
                .filter(line -> !line.startsWith(COMMENT_LINE_PREFIX))
                .map(lineToDiseaseData())
                .collect(toList());
    }

    private static Function<String, Disease> lineToDiseaseData() {
        //HUMAN	2263	FGFR2	OMIM:613659	Gastric cancer,somatic	D	S	HP:0012126
        return line -> {
            String[] fields = line.split("\t");
            return Disease.builder().diseaseId(fields[3])
                    .diseaseName(fields[4])
                    .associatedGeneId(Integer.valueOf(fields[1]))
                    .associatedGeneSymbol(fields[2])
                    .diseaseTypeCode(fields[5])
                    .inheritanceModeCode(fields[6])
                    .phenotypeIds(getOntologyTerms(fields[7]))
                    .build();
        };
    }

    public static List<GeneModel> readDiseaseModelData(String diseaseModelTestDataFile) {
        return readLines(diseaseModelTestDataFile)
                .filter(line -> !line.startsWith(COMMENT_LINE_PREFIX))
                .map(lineToDiseaseModel())
                .collect(toList());
    }

    private static Function<String, GeneDiseaseModel> lineToDiseaseModel() {
        //HUMAN	2263	FGFR2	OMIM:613659	Gastric cancer,somatic	D	S	HP:0012126
        return line -> {
            String[] fields = line.split("\t");
            String modelId = fields[3] + "_" + fields[1];
            return new GeneDiseaseModel(modelId, Organism.valueOf(fields[0]), Integer.valueOf(fields[1]), fields[2], fields[3], fields[4], getOntologyTerms(fields[7]));
        };
    }

    private static List<String> getOntologyTerms(String field) {
        return Lists.newArrayList(field.split(","));
    }

    public static List<PhenotypeMatch> readOntologyMatchData(String ontologyMatchFile) {
        return readLines(ontologyMatchFile)
                .filter(line -> !line.startsWith(COMMENT_LINE_PREFIX))
                .map(lineToPhenotypeMatch())
                .collect(toList());
    }

    private static Function<String, PhenotypeMatch> lineToPhenotypeMatch() {
        //HP:0010055	Broad hallux	0.000000	ZP:0012192	null	0.000000	MP:0009250	abnormal appendicular skeleton morphology	2.529091	0.188889	0.691171
        return line -> {
            String[] elements = line.split("\t");
            PhenotypeTerm query = PhenotypeTerm.of(elements[0], elements[1]);
            PhenotypeTerm match = PhenotypeTerm.of(elements[3], elements[4]);
            PhenotypeTerm lcs = PhenotypeTerm.of(elements[6], elements[7]);

            return PhenotypeMatch.builder()
                    .query(query)
                    .match(match)
                    .lcs(lcs)
                    .ic(Double.valueOf(elements[8]))
                    .simj(Double.valueOf(elements[9]))
                    .score(Double.valueOf(elements[10]))
                    .build();
        };
    }

    private static Stream<String> readLines(String filePath) {
        try {
            return Files.lines(Paths.get(filePath), Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
