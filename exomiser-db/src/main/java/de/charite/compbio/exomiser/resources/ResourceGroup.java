/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.resources;

import de.charite.compbio.exomiser.parsers.ResourceGroupParser;
import de.charite.compbio.exomiser.parsers.ResourceParser;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a bundle of resources which need to be parsed as a
 * group due to inter-dependencies of the resources. 
 * 
 * <p>For example the OMIM
 * resources mordbidmap and mim2gene have related information which needs to be
 * drawn together in order to produce the final dump file for the omim table.
 * The {@code de.charite.compbio.exomiser.resources.ResourceGroup} provides a means of
 * grouping these together so that they can be parsed in the correct order by a
 * {@code de.charite.compbio.exomiser.parsers.ResourceGroupParser}.
 *
 * @see de.charite.compbio.exomiser.parsers.ResourceGroupParser
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
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
                
        if (resource.getParserClass() == null ) {
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
     * Returns the {@code de.charite.compbio.exomiser.resources.Resource} which is
     * parsed by the specified {@code de.charite.compbio.exomiser.parsers.ResourceParser}
     * class or a null.
     * 
     * <p>This is the preferred method for getting a resource from the ResourceGroup 
 as opposed to the getResource(String resourceName). Although there is a 
 potential for configuring things incorrectly to start with, there is better
 type-safety using the full class groupName of the 
 {@code de.charite.compbio.exomiser.parsers.ResourceParser}
     * 
     * @param   clazz 
     *          the class of the {@code de.charite.compbio.exomiser.parsers.ResourceParser} 
     *          used to parse a {@code de.charite.compbio.exomiser.resources.Resource}
     * @return  The matching {@code de.charite.compbio.exomiser.resources.Resource}
     *          or null if no match is found.
     */
    public Resource getResource(Class<? extends ResourceParser> clazz) {
        return resourcesClassMap.get(clazz);
    }

    /**
     * Returns the {@code de.charite.compbio.exomiser.resources.Resource} with 
 the same groupName as the provided resourceName or a null.
     * 
     * <p>Only use this method when there is no ResourceParserClass available for the
     * resource as this method essentially relies on hard-coded names for resources.
     * In preference to this define a ResourceParserClass and use the
     * getResource(Class clazz) method.
     * 
     * 
     * @param   resourceName 
     *          the groupName of the required {@code de.charite.compbio.exomiser.resources.Resource} 
     * @return  The matching {@code de.charite.compbio.exomiser.resources.Resource}
     *          or null if no match is found.
     */
    public Resource getResource(String resourceName) {
        return resourcesNameMap.get(resourceName);
    }
    
    @Override
    public String toString() {
        return "ResourceGroup{" + "groupName=" + groupName + ", resourceGroupParserClass=" + resourceGroupParserClass + ", resources=" + resourcesClassMap.values() + '}';
    }
}
