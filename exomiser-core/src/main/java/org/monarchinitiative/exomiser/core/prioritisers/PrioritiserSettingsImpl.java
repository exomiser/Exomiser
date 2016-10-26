/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.prioritisers;

import java.util.ArrayList;
import java.util.List;

class PrioritiserSettingsImpl implements PrioritiserSettings {

    private final PriorityType priorityType;
    private final String diseaseId;
    private final String candidateGeneSymbol;
    private final String hiPhiveParams;
    private final List<String> hpoIds;
    private final List<Integer> seedGenes;

    public static PrioritiserSettingsBuilder builder() {
        return new PrioritiserSettingsBuilder();
    }

    public static class PrioritiserSettingsBuilder {

        private PriorityType priorityType = PriorityType.NONE;
        private String diseaseId = "";
        private String candidateGeneSymbol = "";
        private String hiPhiveParams = "";
        private List<String> hpoIds = new ArrayList<>();
        private List<Integer> seedGenes = new ArrayList<>();

        private PrioritiserSettingsBuilder() {}

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

        public PrioritiserSettingsBuilder hiPhiveParams(String hiPhiveParams) {
            this.hiPhiveParams = hiPhiveParams;
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
        hiPhiveParams = builder.hiPhiveParams;
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
    public String getHiPhiveParams() {
        return hiPhiveParams;
    }
}
