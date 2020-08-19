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

package org.monarchinitiative.exomiser.data.phenotype.processors;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class ResourceCheckerTest {

    @Test
    void throwsExceptionWhenProvidedEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> ResourceChecker.check(List.of()), "Must supply at least one resource");
    }

    @Test
    void allPresent() {
        List<Resource> resources = List.of(
                Resource.of("src/test/resources/data/hp_test.obo")
        );
        ResourceChecker instance = ResourceChecker.check(resources);
        assertTrue(instance.resourcesPresent());
    }

    @Test
    void allAbsent() {
        List<Resource> resources = List.of(
                Resource.of("src/test/resources/wibble")
        );
        ResourceChecker instance = ResourceChecker.check(resources);
        assertFalse(instance.resourcesPresent());
    }

    @Test
    void getMissingResources() {
        List<Resource> resources = List.of(
                Resource.of("src/test/resources/data/hp_test.obo"),
                Resource.of("src/test/resources/data/hoopy"),
                Resource.of("src/test/resources/data/frood")
        );
        ResourceChecker instance = ResourceChecker.check(resources);
        assertThat(instance.getMissingResources(),
                equalTo(List.of(
                        Resource.of("src/test/resources/data/hoopy"),
                        Resource.of("src/test/resources/data/frood")))
        );
    }
}