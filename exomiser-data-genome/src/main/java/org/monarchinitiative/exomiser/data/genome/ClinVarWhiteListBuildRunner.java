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

package org.monarchinitiative.exomiser.data.genome;

import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.tribble.AsciiFeatureCodec;
import htsjdk.tribble.Feature;
import htsjdk.tribble.index.IndexFactory;
import htsjdk.tribble.index.tabix.TabixFormat;
import htsjdk.tribble.index.tabix.TabixIndex;
import htsjdk.tribble.readers.LineIterator;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.data.genome.indexers.ClinVarWhiteListFileAlleleIndexer;
import org.monarchinitiative.exomiser.data.genome.indexers.Indexer;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.exomiser.data.genome.model.AlleleResource;
import org.monarchinitiative.exomiser.data.genome.model.BuildInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.Set;

/**
 * Creates the standard variant whitelist from ClinVar data
 *
 * @deprecated This class has been replaced by the {@link ClinVarBuildRunner} which builds a complete version of ClinVar
 * from the CLinVar VCF file. The {@link org.monarchinitiative.exomiser.core.genome.dao.VariantWhiteList} this was used
 * to build the data for is now
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Deprecated
public class ClinVarWhiteListBuildRunner {

    private static final Logger logger = LoggerFactory.getLogger(ClinVarWhiteListBuildRunner.class);

    // These are two mitochondrial alleles which are probably wrongly assigned in ClinVar and should be benign
    private static final Set<Allele> HG19_BLACKLIST = Set.of(
            new Allele(25, 11467, "A", "G"),
            new Allele(25, 12372, "G", "A")
    );
    private static final Set<Allele> HG38_BLACKLIST = Set.of(
            new Allele(25, 11467, "A", "G"),
            new Allele(25, 12372, "G", "A")
    );

    private final Path outPath;
    private final BuildInfo buildInfo;
    private final AlleleResource clinVarAlleleResource;

    public ClinVarWhiteListBuildRunner(BuildInfo buildInfo, Path outPath, AlleleResource clinVarAlleleResource) {
        this.outPath = outPath.toAbsolutePath();
        this.buildInfo = buildInfo;
        this.clinVarAlleleResource = clinVarAlleleResource;
    }

    public void run() {
        // This is a nacent utility class right here...
        // create clinvar whitelist output file
        String whitelistFileName = buildInfo.getBuildString() + "_clinvar_whitelist.tsv.gz";
        Path whiteListBgZipPath = outPath.resolve(whitelistFileName);

        BlockCompressedOutputStream blockCompressedOutputStream = new BlockCompressedOutputStream(whiteListBgZipPath.toFile(), 6);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(blockCompressedOutputStream);

        Set<Allele> blacklist = buildInfo.getAssembly() == GenomeAssembly.HG19 ? HG19_BLACKLIST : HG38_BLACKLIST;
        try (Indexer<Allele> alleleIndexer = new ClinVarWhiteListFileAlleleIndexer(new BufferedWriter(outputStreamWriter), blacklist)) {
            alleleIndexer.index(clinVarAlleleResource);
        } catch (IOException e) {
            logger.error("Unable to write whitelist to bgzip.", e);
        }

        // use HTSJDK to create tabix index...
        Path whiteListIndexPath = outPath.resolve(whitelistFileName + ".tbi");
        try {
            TabixIndex index = IndexFactory.createTabixIndex(whiteListBgZipPath.toFile(), new ExomiserTabixCodec(), null);
            index.write(whiteListIndexPath);
        } catch (IOException e) {
            logger.error("Unable to write whitelist tabix index.", e);
        }
    }

    private static class ExomiserTabixCodec extends AsciiFeatureCodec<ExomiserTabixFeature> {

        public ExomiserTabixCodec() {
            super(ExomiserTabixFeature.class);
        }

        @Override
        public ExomiserTabixFeature decode(String s) {
            if (s.startsWith("#")) {
                return null;
            }
            String[] fields = s.split("\t");
            if (fields.length < 4) {
                // must have at least chr, pos, ref, alt
                return null;
            }
            return new ExomiserTabixFeature(fields[0], Integer.parseInt(fields[1]));
        }

        @Override
        public Object readActualHeader(LineIterator reader) {
            return null;
        }

        @Override
        public TabixFormat getTabixFormat() {
            // `-c#` isn't a typo/formatting issue its the comment character command and can't have a space between the -c and the character
            // equivalent to `tabix -s 1 -b 2 -e 2 -c# outfile.tsv.gz`
            return new TabixFormat(TabixFormat.GENERIC_FLAGS, 1, 2, 2, '#', 0);
        }

        @Override
        public boolean canDecode(String path) {
            return true;
        }
    }

    private static class ExomiserTabixFeature implements Feature {
        private final String contig;
        private final int pos;

        private ExomiserTabixFeature(String contig, int pos) {
            this.contig = contig;
            this.pos = pos;
        }

        @Override
        public String getContig() {
            return contig;
        }

        @Override
        public int getStart() {
            return pos;
        }

        @Override
        public int getEnd() {
            return pos;
        }
    }
}
