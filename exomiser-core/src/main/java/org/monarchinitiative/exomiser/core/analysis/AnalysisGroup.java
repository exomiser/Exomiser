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

package org.monarchinitiative.exomiser.core.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for grouping a set of {@link AnalysisStep} by {@link org.monarchinitiative.exomiser.core.analysis.AnalysisStep.AnalysisStepType}.
 * It is not part of the public API as is only used internally in the analysis package.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
class AnalysisGroup {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisGroup.class);

    private final AnalysisStep.AnalysisStepType analysisStepType;
    private final List<AnalysisStep> analysisSteps;

    private AnalysisGroup(AnalysisStep.AnalysisStepType analysisStepType, List<AnalysisStep> analysisSteps) {
        this.analysisStepType = analysisStepType;
        this.analysisSteps = analysisSteps;
    }

    /**
     * Groups {@link AnalysisStep} together by function into an {@link AnalysisGroup} to be used by the
     * {@link AnalysisRunner}.
     * <p>
     * The groups are ordered as they are defined in the input analysisSteps, with the steps maintaining
     * the same ordering within a group.
     *
     * @param analysisSteps the {@link AnalysisStep} for the {@link Analysis}
     * @return a list of {@link AnalysisGroup} containing {@link AnalysisStep} grouped by function.
     */
    public static List<AnalysisGroup> groupAnalysisSteps(List<AnalysisStep> analysisSteps) {
        if (analysisSteps.isEmpty()) {
            logger.debug("No AnalysisSteps to group.");
            return Collections.emptyList();
        }
        List<AnalysisGroup> groups = new ArrayList<>();
        AnalysisStep.AnalysisStepType currentGroupType = analysisSteps.get(0).getType();
        logger.debug("First group is for {} steps", currentGroupType);
        List<AnalysisStep> currentGroupSteps = new ArrayList<>();
        for (AnalysisStep step : analysisSteps) {
            if (step.getType() != currentGroupType) {
                logger.debug("Making new group for {} steps", step.getType());
                groups.add(AnalysisGroup.of(currentGroupSteps));
                currentGroupSteps = new ArrayList<>();
                currentGroupType = step.getType();
            }
            currentGroupSteps.add(step);
        }
        //make sure the last group is added too
        groups.add(AnalysisGroup.of(currentGroupSteps));
        return groups;
    }

    public static AnalysisGroup of(AnalysisStep... analysisSteps) {
        return of(List.of(analysisSteps));
    }

    public static AnalysisGroup of(List<AnalysisStep> analysisSteps) {
        if (analysisSteps == null || analysisSteps.isEmpty()) {
            throw new IllegalArgumentException("analysisSteps cannot be null or empty");
        }
        AnalysisStep.AnalysisStepType analysisStepType = analysisSteps.get(0).getType();
        for (AnalysisStep step : analysisSteps) {
            if (analysisStepType != step.getType()) {
                throw new IllegalArgumentException("analysisSteps must all be of same analysisStepType - " + analysisStepType + " != " + step
                        .getType());
            }
        }
        return new AnalysisGroup(analysisStepType, analysisSteps);
    }

    public AnalysisStep.AnalysisStepType getAnalysisStepType() {
        return analysisStepType;
    }

    boolean isVariantFilterGroup() {
        return !analysisSteps.isEmpty() && analysisSteps.get(0).isVariantFilter();
    }

    boolean hasPrioritiserStep() {
        return analysisSteps.stream().anyMatch(AnalysisStep::isGenePrioritiser);
    }

    public List<AnalysisStep> getAnalysisSteps() {
        return analysisSteps;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnalysisGroup)) return false;
        AnalysisGroup that = (AnalysisGroup) o;
        return analysisStepType == that.analysisStepType &&
                analysisSteps.equals(that.analysisSteps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(analysisStepType, analysisSteps);
    }

    @Override
    public String toString() {
        return "AnalysisGroup{" +
                "analysisStepType=" + analysisStepType +
                ", analysisSteps=" + analysisSteps +
                '}';
    }
}
