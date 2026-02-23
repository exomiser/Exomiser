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
package org.monarchinitiative.exomiser.core.filters;

/**
 * A FilterResult object gets attached to each Variant object as a result of the
 * filtering of the variants in the VCF file according to various criteria.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public record FilterResult(FilterType filterType, Status status) {

    enum Status {
        PASS, FAIL, NOT_RUN
    }

    public static FilterResult pass(FilterType filterType) {
        return new FilterResult(filterType, Status.PASS);
    }

    public static FilterResult fail(FilterType filterType) {
        return new FilterResult(filterType, Status.FAIL);
    }

    public static FilterResult notRun(FilterType filterType) {
        return new FilterResult(filterType, Status.NOT_RUN);
    }

    public boolean passed() {
        return status == Status.PASS;
    }

    public boolean failed() {
        return status == Status.FAIL;
    }

    public boolean wasRun() {
        return status != Status.NOT_RUN;
    }

    @Override
    public String toString() {
        return "Filter=" + filterType + " status=" + status;
    }

}
