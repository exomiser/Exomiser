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

package org.monarchinitiative.exomiser.autoconfigure.genome;

import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKey;

import java.lang.reflect.Method;
import java.util.StringJoiner;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class VariantKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object o, Method method, Object... params) {
        return generateKey(params);
    }

    /**
     * Generate a key based on the specified parameters.
     */
    public static Object generateKey(Object... params) {
        if (params.length == 0) {
            return SimpleKey.EMPTY;
        }
        if (params.length == 1) {
            Object param = params[0];
            if (param instanceof Variant) {
                return MvStoreUtil.generateAlleleKey((Variant) param);
            }
        }
        return new SimpleKey(params);
    }

    private static String generateVariantKey(Variant variant) {
        StringJoiner stringJoiner = new StringJoiner("-");
        stringJoiner.add(variant.getGenomeAssembly().toString());
        stringJoiner.add(variant.getChromosomeName());
        stringJoiner.add(String.valueOf(variant.getPosition()));
        stringJoiner.add(variant.getRef());
        stringJoiner.add(variant.getAlt());
        return stringJoiner.toString();
    }

}
