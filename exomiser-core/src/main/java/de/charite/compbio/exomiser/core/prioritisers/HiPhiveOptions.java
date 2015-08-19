package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HiPhiveOptions {
    private static final Logger logger = LoggerFactory.getLogger(HiPhiveOptions.class);
    private final String diseaseId;
    private final String candidateGeneSymbol;

    private final boolean benchmarkingEnabled;

    private boolean runPpi = true;
    private boolean runHuman = true;
    private boolean runMouse = true;
    private boolean runFish = true;


    /**
     * Default constructor with safe empty return values. Use this if you don't want to run HiPhive in benchmarking mode.
     */
    public HiPhiveOptions() {
        diseaseId = "";
        candidateGeneSymbol = "";
        benchmarkingEnabled = false;
    }

    /**
     * Constructor for use when running HiPhive in benchmarking mode. Both the diseaseId and the candidate gene symbol
     * must be valid in order to trigger benchmarking mode.
     *
     * @param diseaseId
     * @param candidateGeneSymbol
     */
    public HiPhiveOptions(String diseaseId, String candidateGeneSymbol) {
        this.diseaseId = diseaseId;
        this.candidateGeneSymbol = candidateGeneSymbol;
        if (nullOrEmpty(diseaseId) || nullOrEmpty(candidateGeneSymbol)) {
            benchmarkingEnabled = false;
        } else {
            benchmarkingEnabled = true;
        }
    }

    private boolean nullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    /**
     * Constructor for use when running HiPhive in benchmarking mode with optional parameters to allow specification of
     * which organism to run against and wether or not to run against the PPI matrix.
     *
     * Valid parameters are 'human', 'mouse', 'fish', 'ppi'. Combinations must be comma separated, for example
     * 'human,fish,ppi' will only run the genes against human and fish phenotypes and the ppi matrix.
     *
     * Both the diseaseId and the candidate gene symbol must be valid in order to trigger benchmarking mode.
     *
     * @param diseaseId
     * @param candidateGeneSymbol
     */
    public HiPhiveOptions(String diseaseId, String candidateGeneSymbol, String runParameters) {
        this(diseaseId, candidateGeneSymbol);

        if (!runParameters.isEmpty()) {
            setAllRunParametersFalse();
            for (String param : runParameters.split(",")) {
                if (param.equals("ppi")) {
                    runPpi = true;
                } else if (param.equals("human")) {
                    runHuman = true;
                } else if (param.equals("mouse")) {
                    runMouse = true;
                } else if (param.equals("fish")) {
                    runFish = true;
                } else {
                    throw new InvalidRunParameterException(String.format("'%s' is not a valid parameter.", param));
                }
            }
        }
    }

    private void setAllRunParametersFalse() {
        runPpi = false;
        runHuman = false;
        runMouse = false;
        runFish = false;
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

    public boolean isBenchmarkHit(Model model) {
        return matchesDisease(model) && matchesCandidateGeneSymbol(model);
    }

    private boolean matchesCandidateGeneSymbol(Model model) {
        return model.getHumanGeneSymbol() == null ? candidateGeneSymbol == null : model.getHumanGeneSymbol().equals(candidateGeneSymbol);
    }

    private boolean matchesDisease(Model model) {
        // human model ID is now disease plus entrezgene to ensure uniqueness in HiPhive code
        return model.getModelId() == null ? diseaseId  == null : model.getModelId().split("_")[0].equals(diseaseId);
        //return model.getModelId() == null ? diseaseId  == null : model.getModelId().equals(diseaseId + "_" + model.getEntrezGeneId());
    }

    public class InvalidRunParameterException extends RuntimeException {
        public InvalidRunParameterException(String message) {
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
}
