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
public record FilterReport(FilterType filterType, int passed, int failed, List<String> messages) {

    public FilterReport {
        Objects.requireNonNull(filterType);
        Objects.requireNonNull(messages);
        messages = List.copyOf(messages);
    }

    public boolean hasMessages() {
        return !messages.isEmpty();
    }

    public double percentageFilteredFromBeginning(double originalSize) {
        return ((originalSize - (double) passed) / originalSize) * 100;
    }

    public double percentageFilteredFromReport(double originalSize) {
        return (failed / originalSize) * 100;
    }

    public int totalEvaluationCount() {
        return passed + failed;
    }

    @Override
    public String toString() {
        return String.format("FilterReport for %s: pass:%d fail:%d %s", filterType, passed, failed, messages);
    }

}
