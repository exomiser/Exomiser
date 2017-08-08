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

package org.monarchinitiative.exomiser.core.model;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantRecordTest {

    @Test
    public void testConstructor() {
        String vcfRecord = "chr1\t1\t.\tA\tT\t.\t.\tvariant info here";
        VariantRecord instance = VariantRecord.valueOf(vcfRecord);
        System.out.println(instance);
        assertThat(instance.getChr(), equalTo("chr1"));
        assertThat(instance.getStart(), equalTo(1));
        assertThat(instance.getEnd(), equalTo(1));
        assertThat(instance.getVcfRecord(), equalTo(vcfRecord));
    }


//    @Test
//    public void loadFullGenomeVcf_StringsOnly() throws Exception {
//        Path path = Paths.get("C:/Users/jj8/Documents/exomiser-cli-4.0.0/data/NA19722_601952_AUTOSOMAL_RECESSIVE_POMP_13_29233225_5UTR_38.vcf");
//
//        System.out.printf("Loading file as Strings...%n");
//        //Takes about 1.5GB RAM, 7 secs for 4.4 million lines
//        Instant startReadAllLines = Instant.now();
//        List<String> lines = Files.readAllLines(path);
//        Instant endReadAllLines = Instant.now();
//        System.out.printf("File has %d lines. Took %s ms to read.%n", lines.size(), Duration.between(startReadAllLines, endReadAllLines).toMillis());
//    }
//
//    @Test
//    public void loadFullGenomeVcf_VariantRecord() throws Exception {
//        Path path = Paths.get("C:/Users/jj8/Documents/exomiser-cli-4.0.0/data/NA19722_601952_AUTOSOMAL_RECESSIVE_POMP_13_29233225_5UTR_38.vcf");
//
//        System.out.printf("Loading file as VariantRecords...%n");
//        //sort by chr name (alphabetically), start, end
//        Instant startReadVcf = Instant.now();
//        //Takes about 2GB RAM, 9 secs for 4.4 million lines
//        List<VariantRecord> variantRecords = Files.newBufferedReader(path)
//                .lines()
//                .filter(line -> !line.startsWith("#"))
//                .map(line -> VariantRecord.valueOf(line))
//                .collect(Collectors.toList());
//
//        Instant endReadVcf = Instant.now();
//        System.out.printf("File has %d lines. Took %s ms to read.%n", variantRecords.size(), Duration.between(startReadVcf, endReadVcf).toMillis());
//    }
//
//    @Test
//    public void loadFullGenomeVcf_VariantcontextToVariantRecord() throws Exception {
//        Path path = Paths.get("C:/Users/jj8/Documents/exomiser-cli-4.0.0/data/NA19722_601952_AUTOSOMAL_RECESSIVE_POMP_13_29233225_5UTR_38.vcf");
//
//        System.out.printf("Loading file from VariantContext to VariantRecords...%n");
//        //sort by chr name (alphabetically), start, end
//        Instant startReadVcf = Instant.now();
//       //yuk! This takes over 3GB RAM for 4.4 million variants
//        VCFFileReader vcfFileReader = new VCFFileReader(path.toFile(), false);
//        List<VariantRecord> variantRecords = StreamSupport.stream(vcfFileReader.spliterator(), true).map(variantContext -> new VariantRecord(variantContext.getContig(), variantContext.getStart(), variantContext.getEnd(), "")).collect(Collectors.toList());
//        vcfFileReader.close();
//        Instant endReadVcf = Instant.now();
//        System.out.printf("File has %d lines. Took %s ms to read.%n", variantRecords.size(), Duration.between(startReadVcf, endReadVcf).toMillis());
//    }


}
