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

package org.monarchinitiative.exomiser.core.analysis.util;

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.pedigree.*;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PedigreeConverter {

    private static final Logger logger = LoggerFactory.getLogger(PedigreeConverter.class);

    private PedigreeConverter() {
        //
    }

    public static de.charite.compbio.jannovar.pedigree.Pedigree convertToJannovarPedigree(Pedigree pedigree) {
        logger.debug("Converting pedigree");
        ImmutableList<PedPerson> people = pedigree.getIndividuals()
                .stream()
                .map(toPedPerson())
                .collect(ImmutableList.toImmutableList());

        return buildJannovarPedigree(people);
    }

    private static Function<Pedigree.Individual, PedPerson> toPedPerson() {
        return individual -> {
            logger.debug("Converting individual {}", individual);
            Sex sex = mapSex(individual.getSex());
            Disease disease = mapDisease(individual.getStatus());
            return new PedPerson(individual.getFamilyId(), individual.getId(), mapParentId(individual.getFatherId()), mapParentId(individual.getMotherId()), sex, disease);
        };
    }

    private static String mapParentId(String parentId) {
        return parentId.isEmpty() ? "0" : parentId;
    }

    private static Sex mapSex(Pedigree.Individual.Sex sex) {
        switch (sex) {
            case MALE:
                return Sex.MALE;
            case FEMALE:
                return Sex.FEMALE;
            default:
                return Sex.UNKNOWN;
        }
    }

    private static Disease mapDisease(Pedigree.Individual.Status status) {
        switch (status) {
            case AFFECTED:
                return Disease.AFFECTED;
            case UNAFFECTED:
                return Disease.UNAFFECTED;
            default:
                return Disease.UNKNOWN;
        }
    }

    private static de.charite.compbio.jannovar.pedigree.Pedigree buildJannovarPedigree(ImmutableList<PedPerson> people) {
        PedFileContents pedFileContents = new PedFileContents(ImmutableList.of(), people);

        final String name = pedFileContents.getIndividuals().get(0).getPedigree();
        try {
            logger.debug("Building pedigree for family {}", name);
            return new de.charite.compbio.jannovar.pedigree.Pedigree(name, new PedigreeExtractor(name, pedFileContents).run());
        } catch (PedParseException e) {
            String message = "Problem converting pedigree.";
            logger.error(message);
            throw new PedigreeConversionException(message, e);
        }
    }

    private static class PedigreeConversionException extends RuntimeException {
        private PedigreeConversionException(String message, Exception e) {
            super(message, e);
        }
    }
}
