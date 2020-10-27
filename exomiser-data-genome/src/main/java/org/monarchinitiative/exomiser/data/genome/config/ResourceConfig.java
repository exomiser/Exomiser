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

package org.monarchinitiative.exomiser.data.genome.config;

import org.monarchinitiative.exomiser.data.genome.model.AlleleResource;
import org.springframework.core.env.Environment;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ResourceConfig {

    private final Environment environment;

    public ResourceConfig(Environment environment) {
        this.environment = environment;
    }

    protected <T extends AlleleResource> T alleleResource(Class<T> clazz, String namespacePrefix) {
        ResourceProperties resourceProperties = getResourceProperties(namespacePrefix);
        try {
            return clazz.getConstructor(String.class, URL.class, Path.class)
                    .newInstance(namespacePrefix, resourceProperties.getResourceUrl(), resourceProperties.getResourcePath());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException();
        }
    }

    protected ResourceProperties getResourceProperties(String namespacePrefix) {
        String fileName = environment.getProperty(namespacePrefix + ".file-name");
        Path fileDir = Paths.get(environment.getProperty(namespacePrefix + ".file-dir"));
        String fileUrl = environment.getProperty(namespacePrefix + ".file-url");
        return new ResourceProperties(fileName, fileDir, fileUrl);
    }
}
