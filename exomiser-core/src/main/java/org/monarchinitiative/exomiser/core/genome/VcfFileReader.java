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

package org.monarchinitiative.exomiser.core.genome;

import htsjdk.variant.variantcontext.VariantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Reads a VCF file - wrapper for HTSJDK VCFFileReader
 */
public class VcfFileReader implements VcfReader {

    private static final Logger logger = LoggerFactory.getLogger(VcfFileReader.class);

    private final Path vcfPath;

    public VcfFileReader(Path vcfPath) {
        this.vcfPath = Objects.requireNonNull(vcfPath, "Cannot read from null vcfPath");
    }

    @Override
    public List<String> readSampleIdentifiers() {
        return VcfFiles.readSampleIdentifiers(vcfPath);
    }

    @Override
    public Stream<VariantContext> readVariantContexts() {
        return VcfFiles.readVariantContexts(vcfPath);
    }
}
