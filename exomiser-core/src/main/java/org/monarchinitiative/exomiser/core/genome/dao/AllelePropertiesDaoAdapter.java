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

package org.monarchinitiative.exomiser.core.genome.dao;

import org.monarchinitiative.exomiser.core.model.AlleleProtoAdaptor;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;

/**
 * Adapter class for providing {@link FrequencyDao} and {@link PathogenicityDao} views on the {@link AllelePropertiesDao}.
 * This is a bit of a hack due to the 'proxy' Spring caching implementation where method interception of calls occurs
 * through a proxy. Consequently local calls within the same class cannot get intercepted. This could be solved using
 * aspectj compile/runtime weaving, but this added complications.
 *
 * @since 12.0.0
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AllelePropertiesDaoAdapter implements FrequencyDao, PathogenicityDao {

    private final AllelePropertiesDao allelePropertiesDao;

    public AllelePropertiesDaoAdapter(AllelePropertiesDao allelePropertiesDao) {
        this.allelePropertiesDao = allelePropertiesDao;
    }

    @Override
    public FrequencyData getFrequencyData(Variant variant) {
        AlleleProto.AlleleProperties alleleProperties = allelePropertiesDao.getAlleleProperties(variant);
        return AlleleProtoAdaptor.toFrequencyData(alleleProperties);
    }

    @Override
    public PathogenicityData getPathogenicityData(Variant variant) {
        AlleleProto.AlleleProperties alleleProperties = allelePropertiesDao.getAlleleProperties(variant);
        return AlleleProtoAdaptor.toPathogenicityData(alleleProperties);
    }
}
