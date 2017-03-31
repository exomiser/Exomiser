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

package org.monarchinitiative.exomiser.core.prioritisers;

import com.google.common.collect.Sets;
import org.monarchinitiative.exomiser.core.phenotype.Model;
import org.monarchinitiative.exomiser.core.phenotype.Organism;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Class for specifying HiPhive options. These can trigger benchmarking mode or allow specification of
 * which organism to run against and whether or not to run against the PPI matrix.
 *
 * Valid run parameters are 'human', 'mouse', 'fish', 'ppi'. Combinations must be comma separated, for example
 * 'human,fish,ppi' will only run the genes against human and fish phenotypes and the ppi matrix.
 *
 * Both the diseaseId and the candidate gene symbol must be valid in order to trigger benchmarking mode. When these are
 * specified the relevant models will be removed from the result set.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HiPhiveOptions {
    private static final Logger logger = LoggerFactory.getLogger(HiPhiveOptions.class);

    public static final HiPhiveOptions DEFAULT = HiPhiveOptions.builder().build();

    private final String diseaseId;
    private final String candidateGeneSymbol;

    private final boolean benchmarkingEnabled;

    private final boolean runPpi;
    private final boolean runHuman;
    private final boolean runMouse;
    private final boolean runFish;

    private HiPhiveOptions(Builder builder) {
        diseaseId = builder.diseaseId;
        candidateGeneSymbol = builder.candidateGeneSymbol;
        benchmarkingEnabled = builder.benchmarkingEnabled;
        runPpi = builder.runPpi;
        runHuman = builder.runHuman;
        runMouse = builder.runMouse;
        runFish = builder.runFish;
    }

    public String getDiseaseId() {
        return diseaseId;
    }

    public String getCandidateGeneSymbol() {
        return candidateGeneSymbol;
    }

    public boolean isBenchmarkingEnabled() {
        return benchmarkingEnabled;
    }

    public boolean runPpi() {
        return runPpi;
    }

    public boolean runFish() {
        return runFish;
    }

    public boolean runMouse() {
        return runMouse;
    }

    public boolean runHuman() {
        return runHuman;
    }

    public boolean isBenchmarkHit(GeneModel model) {
        return matchesDisease(model) && matchesCandidateGeneSymbol(model);
    }

    private boolean matchesCandidateGeneSymbol(GeneModel model) {
        return model.getHumanGeneSymbol() == null ? candidateGeneSymbol == null : model.getHumanGeneSymbol().equals(candidateGeneSymbol);
    }

    private boolean matchesDisease(Model model) {
        // human model ID is now disease plus entrezgene to ensure uniqueness in HiPhive code
        return model.getId() == null ? diseaseId  == null : model.getId().split("_")[0].equals(diseaseId);
//        return model.getId() == null ? diseaseId  == null : model.getId().equals(diseaseId + "_" + model.getEntrezGeneId());
    }

    public Set<Organism> getOrganismsToRun() {
        List<Organism> organismsToRun = new ArrayList<>();
        if (runHuman){
            organismsToRun.add(Organism.HUMAN);
        }
        if(runMouse) {
            organismsToRun.add(Organism.MOUSE);
        }
        if (runFish) {
            organismsToRun.add(Organism.FISH);
        }

        if(organismsToRun.isEmpty()) {
            return Collections.emptySet();
        }
        return Sets.immutableEnumSet(organismsToRun);
    }

    static class InvalidRunParameterException extends RuntimeException {
        InvalidRunParameterException(String message) {
            super(message);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HiPhiveOptions that = (HiPhiveOptions) o;

        if (benchmarkingEnabled != that.benchmarkingEnabled) {
            return false;
        }
        if (runPpi != that.runPpi) {
            return false;
        }
        if (runHuman != that.runHuman) {
            return false;
        }
        if (runMouse != that.runMouse) {
            return false;
        }
        if (runFish != that.runFish) {
            return false;
        }
        if (diseaseId != null ? !diseaseId.equals(that.diseaseId) : that.diseaseId != null) {
            return false;
        }
        return !(candidateGeneSymbol != null ? !candidateGeneSymbol.equals(that.candidateGeneSymbol) : that.candidateGeneSymbol != null);

    }

    @Override
    public int hashCode() {
        int result = diseaseId != null ? diseaseId.hashCode() : 0;
        result = 31 * result + (candidateGeneSymbol != null ? candidateGeneSymbol.hashCode() : 0);
        result = 31 * result + (benchmarkingEnabled ? 1 : 0);
        result = 31 * result + (runPpi ? 1 : 0);
        result = 31 * result + (runHuman ? 1 : 0);
        result = 31 * result + (runMouse ? 1 : 0);
        result = 31 * result + (runFish ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HiPhiveOptions{" +
                "diseaseId='" + diseaseId + '\'' +
                ", candidateGeneSymbol='" + candidateGeneSymbol + '\'' +
                ", benchmarkingEnabled=" + benchmarkingEnabled +
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


        private Builder() {}

        /**
         * Parses the runParams to determine what needs running. By default all options are enabled. Expects a comma-separated list of parameters. For example
         * 'human,mouse,fish,ppi' is equivalent to the default, 'human,mouse' will only run HiPhive against human and mouse models.
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
            return new HiPhiveOptions(this);
        }

        private boolean assertBenchmarkingStatus(String diseaseId, String candidateGeneSymbol) {
            return !(nullOrEmpty(diseaseId) || nullOrEmpty(candidateGeneSymbol));
        }

        private boolean nullOrEmpty(String string) {
            return string == null || string.isEmpty();
        }
    }
}
