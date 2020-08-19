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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ResourceChecker {

    private enum State {
        PRESENT, ABSENT
    }

    private final Map<Resource, State> resourceStates;

    public static ResourceChecker check(List<Resource> resources) {
        if (resources == null || resources.isEmpty()) {
            throw new IllegalArgumentException("Must supply at least one resource");
        }

        Map<Resource, State> resourceStates = resources.stream()
                .distinct()
                .collect(ImmutableMap.toImmutableMap(Function.identity(), checkState()));

        return new ResourceChecker(resourceStates);
    }

    private static Function<Resource, State> checkState() {
        return externalResource -> {
            try {
                Path resourcePath = externalResource.getResourcePath();
                if (Files.exists(resourcePath) && Files.size(resourcePath) > 0) {
                    return State.PRESENT;
                }
            } catch (IOException e) {
                // not bothered its not accessible so return default absent state
            }
            return State.ABSENT;
        };
    }

    private ResourceChecker(Map<Resource, State> resourceStates) {
        this.resourceStates = resourceStates;
    }

    public boolean resourcesPresent() {
        return !resourceStates.containsValue(State.ABSENT);
    }

    public List<Resource> getMissingResources() {
        return resourceStates.entrySet().stream()
                .filter(entry -> entry.getValue().equals(State.ABSENT))
                .map(Map.Entry::getKey)
                .collect(ImmutableList.toImmutableList());
    }
}
