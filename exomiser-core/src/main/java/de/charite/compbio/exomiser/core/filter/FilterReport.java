/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Handles the storage of reports from a filter. The report contains a list of
 * messages from the filter about what happened during the filtering of a
 * particular list of {@code VariantEvaluation}.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FilterReport {

    private final FilterType filterType;

    private final List<String> messages;
    
    private final int passed;
    
    private final int failed;

    public FilterReport(FilterType filterType, int pass, int fail) {
        this.filterType = filterType;
        passed = pass;
        failed = fail;
        messages = new ArrayList<>();
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public boolean addMessage(String message) {
        return messages.add(message);
    }
    
    public List<String> getMessages() {
        return messages;
    }

    public boolean hasMessages() {
        return !messages.isEmpty();
    }
    
    public int getPassed() {
        return passed;
    }

    public int getFailed() {
        return failed;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.filterType);
        hash = 97 * hash + Objects.hashCode(this.messages);
        hash = 97 * hash + this.passed;
        hash = 97 * hash + this.failed;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FilterReport other = (FilterReport) obj;
        if (this.filterType != other.filterType) {
            return false;
        }
        if (!Objects.equals(this.messages, other.messages)) {
            return false;
        }
        if (this.passed != other.passed) {
            return false;
        }
        if (this.failed != other.failed) {
            return false;
        }
        return true;
    }

    
    @Override
    public String toString() {
        return String.format("FilterReport for %s: pass:%d fail:%d %s", filterType, passed, failed, messages);
    }

}
