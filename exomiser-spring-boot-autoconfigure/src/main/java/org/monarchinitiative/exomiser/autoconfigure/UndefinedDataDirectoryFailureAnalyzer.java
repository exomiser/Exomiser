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

package org.monarchinitiative.exomiser.autoconfigure;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class UndefinedDataDirectoryFailureAnalyzer extends AbstractFailureAnalyzer<UndefinedDataDirectoryException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, UndefinedDataDirectoryException cause) {

        return new FailureAnalysis(
                String.format("The Exomiser could not be auto-configured properly: '%s' is not a valid path", cause),
                "You need to define a valid path for the exomiser data directory. " +
                        "Try defining your own exomiserDataDirectory bean " +
                        "or " +
                        "include the 'exomiser.data-directory' property in your application.properties " +
                        "or " +
                        "supply your application with '--exomiser.data-directory=' as a startup argument.", cause);
    }
}
