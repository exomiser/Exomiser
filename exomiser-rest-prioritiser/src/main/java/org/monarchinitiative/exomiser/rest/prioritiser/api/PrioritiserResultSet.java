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

package org.monarchinitiative.exomiser.rest.prioritiser.api;

import org.monarchinitiative.exomiser.core.prioritisers.PriorityResult;

import java.util.List;
import java.util.Map;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PrioritiserResultSet {

    final Map<String, String> params;
    final long queryTime;
    final List<PriorityResult> results;

    public PrioritiserResultSet(Map<String, String> params, long queryTime, List<PriorityResult> results) {
        this.params = params;
        this.queryTime = queryTime;
        this.results = results;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public long getQueryTime() {
        return queryTime;
    }

    public List<PriorityResult> getResults() {
        return results;
    }
    
}
