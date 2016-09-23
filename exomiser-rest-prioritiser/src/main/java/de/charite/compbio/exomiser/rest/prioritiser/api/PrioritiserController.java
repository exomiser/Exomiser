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

package de.charite.compbio.exomiser.rest.prioritiser.api;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.prioritisers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RestController
public class PrioritiserController {

    private static final Logger logger = LoggerFactory.getLogger(PrioritiserController.class);

    private PriorityFactory priorityFactory;

    @Autowired
    public PrioritiserController(PriorityFactory priorityFactory) {
        this.priorityFactory = priorityFactory;
    }

    @RequestMapping(value = "about", method = RequestMethod.GET)
    public String about() {
        return "This service will return a collection of prioritiser results for any given set of:" +
                "\n\t - HPO identifiers e.g. HPO:00001" +
                "\n\t - Entrez gene identifiers e.g. 23364" +
                "\n\t - Specified prioritiser e.g. hiphive along with any prioritiser specific commands e.g. human,mouse,ppi";
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public Collection<PriorityResult> prioritise(@RequestParam(value = "phenotypes") List<String> phenotypes,
                                                 @RequestParam(value = "genes", required = false) List<Integer> genesIds,
                                                 @RequestParam(value = "prioritiser") String prioritiserName,
                                                 @RequestParam(value = "prioritiser-params", required = false, defaultValue = "") String prioritiserParams
    ) {

        logger.info("phenotypes: {}({}) genes: {} prioritiser: {} prioritiser-params: {}", phenotypes, phenotypes.size(), genesIds, prioritiserName, prioritiserParams);
        PriorityType priorityType = parsePrioritserType(prioritiserName.trim());
        PrioritiserSettings prioritiserSettings = new PrioritiserSettingsImpl.PrioritiserSettingsBuilder().hpoIdList(phenotypes).exomiser2Params(prioritiserParams).build();
        Prioritiser prioritiser = priorityFactory.makePrioritiser(priorityType, prioritiserSettings);
        if (genesIds == null) {
            //todo: if not specified, we'll assume they want to use the whole genome. Should save people a lot of typing.
            genesIds = Arrays.asList(2263);
        }
        //this is a hack - really the Prioritiser should only work on GeneIds, but currently this isn't possible as OmimPrioritiser uses some properties of Gene
        List<Gene> genes = genesIds.stream().map(id -> new Gene("GENE:" + id, id)).collect(Collectors.toList());
        prioritiser.prioritizeGenes(genes);
        //in an ideal world this would return prioritiser.prioritize(genesIds)
        return genes.stream().map(gene -> gene.getPriorityResult(prioritiser.getPriorityType())).sorted(Comparator.naturalOrder()).collect(Collectors.toList());
    }

    private PriorityType parsePrioritserType(String prioritiserName) {
        switch(prioritiserName) {
            case "phenix":
                return PriorityType.PHENIX_PRIORITY;
            case "phive":
                return PriorityType.PHIVE_PRIORITY;
            case "hiphive":
            default:
                return PriorityType.HIPHIVE_PRIORITY;
        }


    }

}
