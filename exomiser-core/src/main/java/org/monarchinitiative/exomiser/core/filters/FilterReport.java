/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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
package org.monarchinitiative.exomiser.core.filters;

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

    public FilterReport(FilterType filterType, int pass, int fail, List<String> messages) {
        this.filterType = filterType;
        this.passed = pass;
        this.failed = fail;
        this.messages = List.copyOf(messages);
    }

    public FilterType getFilterType() {
        return filterType;
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
        return this.failed == other.failed;
    }


    @Override
    public String toString() {
        return String.format("FilterReport for %s: pass:%d fail:%d %s", filterType, passed, failed, messages);
    }

}
