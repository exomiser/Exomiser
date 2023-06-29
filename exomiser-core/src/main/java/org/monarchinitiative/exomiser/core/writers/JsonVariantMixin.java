/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.writers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.monarchinitiative.svart.GenomicVariant;
import org.monarchinitiative.svart.VariantType;

/**
 * Jackson Mixin (http://www.cowtowncoder.com/blog/archives/2009/08/entry_305.html) class to add JSON serialisation to
 * svart {@link GenomicVariant} fields.
 *
 * @since 13.0.0
 */
public interface JsonVariantMixin extends GenomicVariant {

    @JsonProperty("contigName")
    @Override
    String contigName();

    @JsonProperty("start")
    @Override
    int start();

    @JsonProperty("end")
    @Override
    int end();

    @JsonProperty("length")
    @Override
    int length();

    @JsonProperty("changeLength")
    @Override
    int changeLength();

    @JsonProperty("ref")
    @Override
    String ref();

    @JsonProperty("alt")
    @Override
    String alt();

    @JsonProperty("id")
    @Override
    String id();

    @JsonProperty("type")
    @Override
    VariantType variantType();

    @JsonIgnore
    @Override
    boolean isOneBased();
}
