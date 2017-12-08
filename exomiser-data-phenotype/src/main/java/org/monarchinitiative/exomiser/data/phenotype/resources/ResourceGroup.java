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
package org.monarchinitiative.exomiser.data.phenotype.resources;

import org.monarchinitiative.exomiser.data.phenotype.parsers.ResourceGroupParser;
import org.monarchinitiative.exomiser.data.phenotype.parsers.ResourceParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents a bundle of resources which need to be parsed as a
 * group due to inter-dependencies of the resources.
 * <p>
 * <p>
 * For example the OMIM resources mordbidmap and mim2gene have related
 * information which needs to be drawn together in order to produce the final
 * dump file for the omim table. The
 * {@code org.monarchinitiative.exomiser.resources.ResourceGroup} provides a means
 * of grouping these together so that they can be parsed in the correct order by
 * a {@code org.monarchinitiative.exomiser.parsers.ResourceGroupParser}.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @see ResourceGroupParser
 */
public class ResourceGroup {

    private static final Logger logger = LoggerFactory.getLogger(ResourceGroup.class);
    private final String groupName;
    private final Class<? extends ResourceGroupParser> resourceGroupParserClass;
    private final Map<Class<? extends ResourceParser>, Resource> resourcesClassMap;
    private final Map<String, Resource> resourcesNameMap;

    public ResourceGroup(String groupName, Class<? extends ResourceGroupParser> resourceGroupParserClass) {
        this.groupName = groupName;
        this.resourceGroupParserClass = resourceGroupParserClass;
        this.resourcesClassMap = new LinkedHashMap<>();
        this.resourcesNameMap = new LinkedHashMap<>();
    }

    public String getName() {
        return groupName;
    }

    public Class<? extends ResourceGroupParser> getParserClass() {
        return resourceGroupParserClass;
    }

    public boolean addResource(Resource resource) {

        if (resource.getParserClass() == null) {
            resourcesNameMap.put(resource.getName(), resource);
            return true;
        } else {
            //this will work for all resources apart from the ucsc_hg19.ser which is
            //used as a cache by other parsers

            Class resourceParserClass = resource.getParserClass();
            if (resourceParserClass != null) {
                resourcesClassMap.put(resourceParserClass, resource);
                //we need to add the resource to the name map in all cases as the
                //resource could be requested by name as well as my class
                resourcesNameMap.put(resource.getName(), resource);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the {@code org.monarchinitiative.exomiser.resources.Resource} which
     * is parsed by the specified
     * {@code org.monarchinitiative.exomiser.parsers.ResourceParser} class or a
     * null.
     * <p>
     * <p>
     * This is the preferred method for getting a resource from the
     * ResourceGroup as opposed to the getResource(String resourceName).
     * Although there is a potential for configuring things incorrectly to start
     * with, there is better type-safety using the full class groupName of the
     * {@code org.monarchinitiative.exomiser.parsers.ResourceParser}
     *
     * @param clazz the class of the
     *              {@code org.monarchinitiative.exomiser.parsers.ResourceParser} used to parse
     *              a {@code org.monarchinitiative.exomiser.resources.Resource}
     * @return The matching
     * {@code org.monarchinitiative.exomiser.resources.Resource} or null if no
     * match is found.
     */
    public Resource getResource(Class<? extends ResourceParser> clazz) {
        return resourcesClassMap.get(clazz);
    }

    /**
     * Returns the {@code org.monarchinitiative.exomiser.resources.Resource} with
     * the same groupName as the provided resourceName or a null.
     * <p>
     * <p>
     * Only use this method when there is no ResourceParserClass available for
     * the resource as this method essentially relies on hard-coded names for
     * resources. In preference to this define a ResourceParserClass and use the
     * getResource(Class clazz) method.
     *
     * @param resourceName the groupName of the required
     *                     {@code org.monarchinitiative.exomiser.resources.Resource}
     * @return The matching
     * {@code org.monarchinitiative.exomiser.resources.Resource} or null if no
     * match is found.
     */
    public Resource getResource(String resourceName) {
        return resourcesNameMap.get(resourceName);
    }

    @Override
    public String toString() {
        return "ResourceGroup{" + "groupName=" + groupName + ", resourceGroupParserClass=" + resourceGroupParserClass + ", resources=" + resourcesClassMap
                .values() + '}';
    }
}
