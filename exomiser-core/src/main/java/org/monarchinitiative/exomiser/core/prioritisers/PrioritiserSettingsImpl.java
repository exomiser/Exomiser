/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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
