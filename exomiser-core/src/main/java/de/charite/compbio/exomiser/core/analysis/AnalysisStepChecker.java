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

package de.charite.compbio.exomiser.core.analysis;

import de.charite.compbio.exomiser.core.filters.InheritanceFilter;
import de.charite.compbio.exomiser.core.filters.PriorityScoreFilter;
import de.charite.compbio.exomiser.core.filters.VariantFilter;
import de.charite.compbio.exomiser.core.prioritisers.OMIMPriority;
import de.charite.compbio.exomiser.core.prioritisers.Prioritiser;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Checks a given list of analysis steps for correct ordering of steps. This is
 * to prevent users from trying to do things which will result in erroneous
 * results, yet will allow users the freedom to change things within these
 * constraints.
 *
 * @since 7.0.0
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AnalysisStepChecker {

    Logger logger = LoggerFactory.getLogger(AnalysisStepChecker.class);

    public List<AnalysisStep> check(List<AnalysisStep> analysisSteps) {
        //0 or 1 steps cannot be in the wrong order
        if (analysisSteps.size() < 2) {
            return analysisSteps;
        }

        //**IMPORTANT** DO NOT CHANGE THE ORDER OF THESE STEPS 
        //really important otherwise the inheritance modes and scores are FUBARed
        moveInheritanceModeDependentStepsAfterLastVariantFilter(analysisSteps);
        //totally necessary otherwise nothing passes
        removePriorityScoreFiltersWithoutMatchingPrioritiser(analysisSteps);
        //optimisation to save any potentially expensive filtering which may be unecessary
        movePriorityScoreFiltersNextToMatchingPrioritiser(analysisSteps);

        analysisSteps.sort(new AnalysisStepComparator());

        return analysisSteps;

    }

    private List<AnalysisStep> moveInheritanceModeDependentStepsAfterLastVariantFilter(List<AnalysisStep> analysisSteps) {
        if (!containsVariantFilter(analysisSteps)) {
            //this is likely a pretty silly analysis, but there you go.
            logger.info("CAUTION: Analysis contains no variant filtering steps. This will not perform well.");
            return analysisSteps;
        }

        if (!containsInheritanceModeDependentStep(analysisSteps)) {
            //Carry on, these aren't the steps we're looking for.
            return analysisSteps;
        }

        int originalInheritanceFilterPosition = getLastPositionOfClass(analysisSteps, InheritanceFilter.class);
        int originalOmimPrioritiserPosition = getLastPositionOfClass(analysisSteps, OMIMPriority.class);

        List<AnalysisStep> inheritanceModeDependentSteps = moveInheritanceModeStepsIntoList(analysisSteps);
        int lastVariantFilterPos = getLastPositionOfClass(analysisSteps, VariantFilter.class);
        analysisSteps.addAll(lastVariantFilterPos + 1, inheritanceModeDependentSteps);

        int sortedInheritanceFilterPosition = getLastPositionOfClass(analysisSteps, InheritanceFilter.class);
        int sortedOmimPrioritiserPosition = getLastPositionOfClass(analysisSteps, OMIMPriority.class);

        if (sortedInheritanceFilterPosition != originalInheritanceFilterPosition) {
            logger.info("WARNING: Moved InheritanceFilter. This must run after all variant filter steps. AnalysisSteps have been changed.");
        }
        if (sortedOmimPrioritiserPosition != originalOmimPrioritiserPosition) {
            logger.info("WARNING: Moved OMIM prioritiser. This must run after all variant and inheritance filter steps. AnalysisSteps have been changed.");
        }
        return analysisSteps;
    }

    private boolean containsVariantFilter(List<AnalysisStep> analysisSteps) {
        return analysisSteps.stream().anyMatch(AnalysisStep::isVariantFilter);
    }

    private boolean containsInheritanceModeDependentStep(List<AnalysisStep> analysisSteps) {
        return analysisSteps.stream().anyMatch(AnalysisStep::isInheritanceModeDependent);
    }

    private int getLastPositionOfClass(List<AnalysisStep> analysisSteps, Class clazz) {
        int lastVariantFilterPos = 0;
        for (int i = 0; i < analysisSteps.size(); i++) {
            AnalysisStep step = analysisSteps.get(i);
            if (clazz.isInstance(step)) {
                lastVariantFilterPos = i;
            }
        }
        return lastVariantFilterPos;
    }

    private List<AnalysisStep> moveInheritanceModeStepsIntoList(List<AnalysisStep> analysisSteps) {
        List<AnalysisStep> inheritanceModeDependentSteps = analysisSteps.stream()
                .filter(AnalysisStep::isInheritanceModeDependent)
                .collect(toList());

        analysisSteps.removeIf(AnalysisStep::isInheritanceModeDependent);

        inheritanceModeDependentSteps.sort(new AnalysisStepComparator());

        return inheritanceModeDependentSteps;
    }

    private List<AnalysisStep> removePriorityScoreFiltersWithoutMatchingPrioritiser(List<AnalysisStep> analysisSteps) {
        Set<PriorityType> prioritiserTypes = getPrioritiserTypes(analysisSteps);
        removePriorityScoreFiltersNotOfType(analysisSteps, prioritiserTypes);
        return analysisSteps;
    }

    private Set<PriorityType> getPrioritiserTypes(List<AnalysisStep> analysisSteps) {
        //get all prioritiser PriorityTypes
        return analysisSteps.stream()
                .filter(Prioritiser.class::isInstance)
                .map(step -> {
                    Prioritiser prioritiser = (Prioritiser) step;
                    return prioritiser.getPriorityType();
                })
                .collect(toSet());
    }

    private void removePriorityScoreFiltersNotOfType(List<AnalysisStep> analysisSteps, Set<PriorityType> prioritiserTypes) {
        analysisSteps.removeIf(step -> {
            if (PriorityScoreFilter.class.isInstance(step)) {
                PriorityScoreFilter filter = (PriorityScoreFilter) step;
                if (!prioritiserTypes.contains(filter.getPriorityType())) {
                    logger.info("WARNING: Removing {} as the corresponding Prioritiser is not present. AnalysisSteps have been changed.", filter);
                    return true;
                }
            }
            return false;
        });
    }

    private List<AnalysisStep> movePriorityScoreFiltersNextToMatchingPrioritiser(List<AnalysisStep> analysisSteps) {
        List<PriorityScoreFilter> priorityScoreFilters = movePriorityScoreFiltersIntoList(analysisSteps);

        for (PriorityScoreFilter priorityScoreFilter : priorityScoreFilters) {
            addPriorityScoreFilterNextToMatchingPrioritiser(priorityScoreFilter, analysisSteps);
        }
        return analysisSteps;
    }

    private List<PriorityScoreFilter> movePriorityScoreFiltersIntoList(List<AnalysisStep> analysisSteps) {
        List<PriorityScoreFilter> priorityScoreFilters = analysisSteps.stream()
                .filter(PriorityScoreFilter.class::isInstance)
                .map(step -> (PriorityScoreFilter) step)
                .collect(toList());

        analysisSteps.removeIf(PriorityScoreFilter.class::isInstance);

        return priorityScoreFilters;
    }

    private void addPriorityScoreFilterNextToMatchingPrioritiser(PriorityScoreFilter priorityScoreFilter, List<AnalysisStep> analysisSteps) {
        int positionOfMatchingPrioritiser = 0;
        boolean containsMatchingPrioritiser = false;

        for (int i = 0; i < analysisSteps.size(); i++) {
            AnalysisStep step = analysisSteps.get(i);
            if (Prioritiser.class.isInstance(step)) {
                Prioritiser prioritiser = (Prioritiser) step;
                if (prioritiser.getPriorityType() == priorityScoreFilter.getPriorityType()) {
                    containsMatchingPrioritiser = true;
                    positionOfMatchingPrioritiser = i;
                    break;
                }
            }
        }

        if (containsMatchingPrioritiser) {
            analysisSteps.add(positionOfMatchingPrioritiser + 1, priorityScoreFilter);
        }
    }

    /**
     * Note - this comparator only works with immediate pairs of AnalysisSteps - the parent class needs to be able to
     * correct ordering over longer ranges so purely using the Comparator isn't enough to be able to sort the
     * AnalysisSteps.
     */
    private class AnalysisStepComparator implements Comparator<AnalysisStep> {

        private static final int BEFORE = -1;
        private static final int EQUAL = 0;
        private static final int AFTER = 1;

        @Override
        public int compare(AnalysisStep o1, AnalysisStep o2) {

            if (o1.isVariantFilter() && o2.isVariantFilter()) {
                return EQUAL;
            }
            if (Prioritiser.class.isInstance(o1) && Prioritiser.class.isInstance(o2)) {
                return EQUAL;
            }

            //InheritanceMode dependent steps must run after last VariantFilter.
            if (o1.isVariantFilter() && o2.isInheritanceModeDependent()) {
                return BEFORE;
            }
            if (o1.isInheritanceModeDependent() && o2.isVariantFilter()) {
                return AFTER;
            }

            //OmimPrioritiser must run after InheritanceFilter.
            if (InheritanceFilter.class.isInstance(o1) && OMIMPriority.class.isInstance(o2)) {
                return BEFORE;
            }
            if (OMIMPriority.class.isInstance(o1) && InheritanceFilter.class.isInstance(o2)) {
                return AFTER;
            }

            //PriorityScoreFilter must run after corresponding Prioritiser.
            if (Prioritiser.class.isInstance(o1) && PriorityScoreFilter.class.isInstance(o2)) {
                Prioritiser prioritiser = (Prioritiser) o1;
                PriorityScoreFilter priorityScoreFilter = (PriorityScoreFilter) o2;
                if (prioritiser.getPriorityType() == priorityScoreFilter.getPriorityType()) {
                    return BEFORE;
                }
                return EQUAL;
            }
            if (PriorityScoreFilter.class.isInstance(o1) && Prioritiser.class.isInstance(o2)) {
                PriorityScoreFilter priorityScoreFilter = (PriorityScoreFilter) o1;
                Prioritiser prioritiser = (Prioritiser) o2;
                if (prioritiser.getPriorityType() == priorityScoreFilter.getPriorityType()) {
                    return AFTER;
                }
                return EQUAL;
            }

            return EQUAL;
        }
    }
}
