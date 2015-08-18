/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.filters.InheritanceFilter;
import de.charite.compbio.exomiser.core.filters.PriorityScoreFilter;
import de.charite.compbio.exomiser.core.filters.VariantFilter;
import de.charite.compbio.exomiser.core.prioritisers.OMIMPriority;
import de.charite.compbio.exomiser.core.prioritisers.Prioritiser;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks a given list of analysis steps for correct ordering of steps. This is
 * to prevent users from trying to do things which will result in erroneous
 * results, yet will allow users the freedom to change things within these
 * constraints.
 * 
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
        analysisSteps.addAll(lastVariantFilterPos + 1 , inheritanceModeDependentSteps);
        
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
        return analysisSteps.stream().anyMatch(step -> (isVariantFilter(step)));
    }
    
    private boolean containsInheritanceModeDependentStep(List<AnalysisStep> analysisSteps) {
        return analysisSteps.stream().anyMatch(step -> (isInheritanceModeDependent(step)));
    }
    
    private List<AnalysisStep> moveInheritanceModeStepsIntoList(List<AnalysisStep> analysisSteps) {
        List<AnalysisStep> inheritanceModeDependentSteps = new ArrayList<>();

        Iterator<AnalysisStep> stepIterator = analysisSteps.iterator();
        while (stepIterator.hasNext()) {
            AnalysisStep step = stepIterator.next();
            if (isInheritanceModeDependent(step)) {
                inheritanceModeDependentSteps.add(step);
                stepIterator.remove();
            }
        }
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
                .filter(step -> (Prioritiser.class.isInstance(step)))
                .map(step -> {
                    Prioritiser prioritiser = (Prioritiser) step;
                    return prioritiser.getPriorityType();
                })
                .collect(toSet());
    }

    private void removePriorityScoreFiltersNotOfType(List<AnalysisStep> analysisSteps, Set<PriorityType> prioritiserTypes) {
        Iterator<AnalysisStep> stepIterator = analysisSteps.iterator();
        while (stepIterator.hasNext()) {
            AnalysisStep step = stepIterator.next();
            if (PriorityScoreFilter.class.isInstance(step)) {
                PriorityScoreFilter filter = (PriorityScoreFilter) step;
                if (!prioritiserTypes.contains(filter.getPriorityType())) {
                    logger.info("WARNING: Removing {} as the corresponding Prioritiser is not present. AnalysisSteps have been changed.", filter);
                    stepIterator.remove();
                }
            }
        }
    }

    private List<AnalysisStep> movePriorityScoreFiltersNextToMatchingPrioritiser(List<AnalysisStep> analysisSteps) {
        List<PriorityScoreFilter> priorityScoreFilters = movePriorityScoreFiltersIntoList(analysisSteps);
        
        for (PriorityScoreFilter priorityScoreFilter : priorityScoreFilters) {
            addPriorityScoreFilterNextToMatchingPrioritiser(priorityScoreFilter, analysisSteps);
        }
        return analysisSteps;
    }
    
    private List<PriorityScoreFilter> movePriorityScoreFiltersIntoList(List<AnalysisStep> analysisSteps) {
        List<PriorityScoreFilter> priorityScoreFilters = new ArrayList<>();

        Iterator<AnalysisStep> stepIterator = analysisSteps.iterator();
        while (stepIterator.hasNext()) {
            AnalysisStep step = stepIterator.next();
            if (PriorityScoreFilter.class.isInstance(step)) {
                PriorityScoreFilter priorityScoreFilter = (PriorityScoreFilter) step;
                priorityScoreFilters.add(priorityScoreFilter);
                stepIterator.remove();
            }
        }

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

    private static boolean isInheritanceModeDependent(AnalysisStep analysisStep) {
        return InheritanceFilter.class.isInstance(analysisStep) || OMIMPriority.class.isInstance(analysisStep);
    }
   
    private static boolean isVariantFilter(AnalysisStep step) {
        return VariantFilter.class.isInstance(step);
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

    private class AnalysisStepComparator implements Comparator<AnalysisStep> {

        private static final int BEFORE = -1;
        private static final int EQUAL = 0;
        private static final int AFTER = 1;

        @Override
        public int compare(AnalysisStep o1, AnalysisStep o2) {

            if (isVariantFilter(o1) && isVariantFilter(o2)) {
                return EQUAL;
            }
            if (Prioritiser.class.isInstance(o1) && Prioritiser.class.isInstance(o2)) {
                return EQUAL;
            }

            //InheritanceMode dependent steps must run after last VariantFilter.
            if (isVariantFilter(o1) && isInheritanceModeDependent(o2)) {
                return BEFORE;
            }
            if (isInheritanceModeDependent(o1) && isVariantFilter(o2)) {
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
