/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

import java.util.ArrayList;
import java.util.List;

public class PrioritiserSettingsImpl implements PrioritiserSettings {

    private final PriorityType priorityType;
    private final String diseaseId;
    private final String candidateGeneSymbol;
    private final String exomiser2Params;
    private final List<String> hpoIds;
    private final List<Integer> seedGenes;

    public static class PrioritiserSettingsBuilder {

        private PriorityType priorityType = PriorityType.NONE;
        private String diseaseId = "";
        private String candidateGeneSymbol = "";
        private String exomiser2Params = "";
        private List<String> hpoIds = new ArrayList<>();
        private List<Integer> seedGenes = new ArrayList<>();

        public PrioritiserSettingsBuilder usePrioritiser(PriorityType priorityType) {
            this.priorityType = priorityType;
            return this;
        }

        public PrioritiserSettingsBuilder hpoIdList(List<String> hpoIds) {
            this.hpoIds = hpoIds;
            return this;
        }

        public PrioritiserSettingsBuilder diseaseId(String diseaseId) {
            this.diseaseId = diseaseId;
            return this;
        }

        public PrioritiserSettingsBuilder candidateGene(String candidateGeneSymbol) {
            this.candidateGeneSymbol = candidateGeneSymbol;
            return this;
        }

        public PrioritiserSettingsBuilder exomiser2Params(String exomiser2Params) {
            this.exomiser2Params = exomiser2Params;
            return this;
        }

        public PrioritiserSettingsBuilder seedGeneList(List<Integer> seedGenes) {
            this.seedGenes = seedGenes;
            return this;
        }

        public PrioritiserSettingsImpl build() {
            return new PrioritiserSettingsImpl(this);
        }
    }

    private PrioritiserSettingsImpl(PrioritiserSettingsBuilder builder) {
        priorityType = builder.priorityType;
        diseaseId = builder.diseaseId;
        candidateGeneSymbol = builder.candidateGeneSymbol;
        exomiser2Params = builder.exomiser2Params;
        hpoIds = builder.hpoIds;
        seedGenes = builder.seedGenes;
    }

    @Override
    public PriorityType getPrioritiserType() {
        return priorityType;
    }

    @Override
    public String getDiseaseId() {
        return diseaseId;
    }

    @Override
    public String getCandidateGene() {
        return candidateGeneSymbol;
    }

    @Override
    public List<String> getHpoIds() {
        return hpoIds;
    }

    @Override
    public List<Integer> getSeedGeneList() {
        return seedGenes;
    }

    @Override
    public String getExomiser2Params() {
        return exomiser2Params;
    }
}
