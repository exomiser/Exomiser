/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2022 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.prioritisers;

import org.monarchinitiative.exomiser.core.phenotype.Model;
import org.monarchinitiative.exomiser.core.phenotype.Organism;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Class for specifying HiPhive options. These can trigger benchmarking mode or allow specification of
 * which organism to run against and whether or not to run against the PPI matrix.
 * <p>
 * Valid run parameters are 'human', 'mouse', 'fish', 'ppi'. Combinations must be comma separated, for example
 * 'human,fish,ppi' will only run the genes against human and fish phenotypes and the ppi matrix.
 * <p>
 * Both the diseaseId and the candidate gene symbol must be valid in order to trigger benchmarking mode. When these are
 * specified the relevant models will be removed from the result set.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public record HiPhiveOptions(
        String diseaseId,
        String candidateGeneSymbol,

        boolean isBenchmarkingEnabled,

        boolean runPpi,
        boolean runHuman,
        boolean runMouse,
        boolean runFish
) {
    private static final Logger logger = LoggerFactory.getLogger(HiPhiveOptions.class);

    private static final HiPhiveOptions DEFAULT = HiPhiveOptions.builder().build();

    public static HiPhiveOptions defaults() {
        return DEFAULT;
    }

    public String getRunParams() {
        StringJoiner stringJoiner = new StringJoiner(", ");
        if (runHuman) {
            stringJoiner.add("human");
        }
        if (runMouse) {
            stringJoiner.add("mouse");
        }
        if (runFish) {
            stringJoiner.add("fish");
        }
        if (runPpi) {
            stringJoiner.add("ppi");
        }
        return stringJoiner.toString();
    }

    /**
     * Tests whether of not the {@link GeneModel} is a benchmarking model. This method takes into account whether or not
     * the instance is isBenchmarkingEnabled so clients do not need to check this first.
     *
     * @param geneModel the {@link GeneModel} to be tested
     * @return true if the {@link GeneModel} matches either the disease or gene specified in the options. Will return
     * false if the instance is not isBenchmarkingEnabled.
     * @since 13.0.0
     */
    public boolean isBenchmarkingModel(GeneModel geneModel) {
        return isBenchmarkingEnabled && isBenchmarkHit(geneModel);
    }

    private boolean isBenchmarkHit(GeneModel model) {
        return matchesDisease(model) && matchesCandidateGeneSymbol(model);
    }

    private boolean matchesCandidateGeneSymbol(GeneModel model) {
        return model.humanGeneSymbol() != null && model.humanGeneSymbol().equals(candidateGeneSymbol);
    }

    private boolean matchesDisease(Model model) {
        // human model ID is now disease plus entrezgene to ensure uniqueness in HiPhive code
        return model.id() != null && model.id().split("_")[0].equals(diseaseId);
    }

    public Set<Organism> getOrganismsToRun() {
        List<Organism> organismsToRun = new ArrayList<>();
        if (runHuman) {
            organismsToRun.add(Organism.HUMAN);
        }
        if (runMouse) {
            organismsToRun.add(Organism.MOUSE);
        }
        if (runFish) {
            organismsToRun.add(Organism.FISH);
        }

        if (organismsToRun.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(organismsToRun));
    }

    static class InvalidRunParameterException extends RuntimeException {
        InvalidRunParameterException(String message) {
            super(message);
        }
    }

    @Override
    public String toString() {
        return "HiPhiveOptions{" +
               "diseaseId='" + diseaseId + '\'' +
               ", candidateGeneSymbol='" + candidateGeneSymbol + '\'' +
               ", isBenchmarkingEnabled=" + isBenchmarkingEnabled +
               ", runPpi=" + runPpi +
               ", runHuman=" + runHuman +
               ", runMouse=" + runMouse +
               ", runFish=" + runFish +
               '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String diseaseId = "";
        private String candidateGeneSymbol = "";

        private boolean benchmarkingEnabled = false;

        private boolean runPpi = true;
        private boolean runHuman = true;
        private boolean runMouse = true;
        private boolean runFish = true;


        private Builder() {
        }

        /**
         * Parses the runParams to determine what needs running. By default all options are enabled. Expects a comma-separated list of parameters. For example
         * 'human,mouse,fish,ppi' is equivalent to the default, 'human,mouse' will only run HiPhive against human and mouse models.
         *
         * @param runParams
         * @return
         */
        public Builder runParams(String runParams) {
            parseRunParams(runParams);
            return this;
        }

        private void parseRunParams(String runParameters) {
            if (runParameters != null && !runParameters.isEmpty()) {
                setAllRunParametersFalse();
                for (String input : runParameters.split(",")) {
                    String param = input.trim();
                    switch (param) {
                        case "ppi":
                            this.runPpi = true;
                            break;
                        case "human":
                            this.runHuman = true;
                            break;
                        case "mouse":
                            this.runMouse = true;
                            break;
                        case "fish":
                            this.runFish = true;
                            break;
                        default:
                            throw new InvalidRunParameterException(String.format("'%s' is not a valid parameter.", param));
                    }
                }
            }
        }

        private void setAllRunParametersFalse() {
            this.runPpi = false;
            this.runHuman = false;
            this.runMouse = false;
            this.runFish = false;
        }

        public Builder candidateGeneSymbol(String candidateGeneSymbol) {
            this.candidateGeneSymbol = candidateGeneSymbol;
            return this;
        }

        public Builder diseaseId(String diseaseId) {
            this.diseaseId = diseaseId;
            return this;
        }

        public HiPhiveOptions build() {
            this.benchmarkingEnabled = assertBenchmarkingStatus(diseaseId, candidateGeneSymbol);
            return new HiPhiveOptions(
                diseaseId,
                candidateGeneSymbol,
                benchmarkingEnabled,
                runPpi,
                runHuman,
                runMouse,
                runFish
            );
        }

        private boolean assertBenchmarkingStatus(String diseaseId, String candidateGeneSymbol) {
            return !(nullOrEmpty(diseaseId) || nullOrEmpty(candidateGeneSymbol));
        }

        private boolean nullOrEmpty(String string) {
            return string == null || string.isEmpty();
        }
    }
}
