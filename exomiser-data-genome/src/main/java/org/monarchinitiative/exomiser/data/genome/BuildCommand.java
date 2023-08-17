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

import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.jannovar.JannovarDataFactory;
import org.monarchinitiative.exomiser.core.genome.jannovar.TranscriptSource;
import org.monarchinitiative.exomiser.data.genome.config.AssemblyResources;
import org.monarchinitiative.exomiser.data.genome.model.AlleleResource;
import org.monarchinitiative.exomiser.data.genome.model.BuildInfo;
import org.monarchinitiative.exomiser.data.genome.model.resource.ClinVarAlleleResource;
import org.monarchinitiative.exomiser.data.genome.model.resource.sv.SvResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

/**
 * Main logic for building the exomiser hg37 genome data distribution.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Component
@Command(name = "build", description = "Command to build the Exomiser genome data bundle.")
public class BuildCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(BuildCommand.class);

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @Option(names = "--build-dir", required = true)
    private Path buildDir;
    private final AssemblyResources hg19Resources;
    private final AssemblyResources hg38Resources;
    private final Path jannovarIniFile;

    @Option(names = "--assembly", required = true, converter = AssemblyConverter.class, description = "Genome assembly to build the data for - one of hg19 or hg38.")
    private GenomeAssembly assembly;
    @Option(names = "--version", required = true, description = "Data version for this build. Typically this would be of the form YYMM i.e. 2308 indicates the data was built in August 2023.")
    private String version;
    @Option(names = "--clinvar", description = "Flag to trigger building of ClinVar data.")
    private boolean buildClinVar;
    @Option(names = "--transcripts", converter = TranscriptSourceConverter.class, split = ",", arity = "0..1", fallbackValue = "ensembl,refseq,ucsc", description = "List of transcript databases to build. If specified without parameter, will build all sources: ${FALLBACK-VALUE}")
    private List<TranscriptSource> transcriptSources;
    @Option(names = "--variants", split = ",", arity = "0..1", fallbackValue = "esp,exac,uk10k,topmed,dbsnp,gnomad-exome,gnomad-genome,dbnsfp", description = "List of variant data sources to build. If specified without parameter, will build all sources: ${FALLBACK-VALUE}")
    private List<String> variantSources;
    @Option(names = "--genome", description = "Flag to trigger building of genome data.")
    private boolean buildGenome;

    public BuildCommand(AssemblyResources hg19AssemblyResources, AssemblyResources hg38AssemblyResources, Path jannovarIniFile) {
//        this.buildDir = buildDir;

        this.hg19Resources = hg19AssemblyResources;
        this.hg38Resources = hg38AssemblyResources;

        this.jannovarIniFile = jannovarIniFile;
    }

    @Override
    public Integer call() throws IOException {
        // --assembly=hg19
        // --version=1711
        // --build-dir=
        // AND OPTIONALLY
        // --variants
        // OR
        // --variants=exac,gnomad-exome,dbsnp...
        // --transcripts
        // OR
        // --transcripts=ensembl,ucsc
        // --genome

        BuildInfo buildInfo = BuildInfo.of(assembly, version);
        String buildString = buildInfo.getBuildString();
        logger.info("Building version {}", buildString);
        Path outPath = buildDir.resolve(buildString);
        logger.info("Build directory set to {}", outPath);
        if (!outPath.toFile().exists()) {
            Files.createDirectories(outPath);
        }

        AssemblyResources assemblyResources = getAssemblyResourcesForAssembly(assembly);
        Map<String, AlleleResource> alleleResources = assemblyResources.getAlleleResources();

        if (shouldBuildAllData()) {
            logger.info("BUILDING ALLL THIe THINGS!");
            buildClinVarData(buildInfo, outPath, assemblyResources.getClinVarResource());
            buildTranscriptData(buildInfo, outPath, List.of(TranscriptSource.values()));
            buildVariantData(buildInfo, outPath, new ArrayList<>(alleleResources.values()));
            buildGenomeData(buildInfo, outPath, assemblyResources);
        }

        if (transcriptSources != null) {
            buildTranscriptData(buildInfo, outPath, transcriptSources);
        }

        if (buildClinVar) {
            ClinVarAlleleResource clinVarResource = assemblyResources.getClinVarResource();
            buildClinVarData(buildInfo, outPath, clinVarResource);
        }

        if (variantSources != null) {
            List<AlleleResource> userDefinedAlleleResources = assemblyResources.getUserDefinedResources(variantSources);
            buildVariantData(buildInfo, outPath, userDefinedAlleleResources);
        }

        if (buildGenome) {
            buildGenomeData(buildInfo, outPath, assemblyResources);
        }

        logger.info("Finished build {}", buildInfo.getBuildString());
        return 0;
    }

    private boolean shouldBuildAllData() {
        return !buildGenome && !buildClinVar && transcriptSources == null && variantSources == null;
    }

    private void buildTranscriptData(BuildInfo buildInfo, Path outPath, List<TranscriptSource> transcriptSources) {
        logger.info("Building Jannovar transcript data sources - {}", transcriptSources);
        Path jannovarBuildDir = buildDir.resolve("jannovar");
        try {
            Files.createDirectories(jannovarBuildDir);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create Jannovar build directory", e);
        }
        JannovarDataFactory jannovarDataFactory = JannovarDataFactory.builder(jannovarIniFile).downloadDir(jannovarBuildDir).build();
        TranscriptDataBuildRunner transcriptDataBuildRunner = new TranscriptDataBuildRunner(buildInfo, jannovarDataFactory, outPath, transcriptSources);
        transcriptDataBuildRunner.run();
    }

    private void buildClinVarData(BuildInfo buildInfo, Path outPath, ClinVarAlleleResource clinVarResource) {
        logger.info("Creating ClinVar database...");
        ResourceDownloader.download(clinVarResource);
        ClinVarBuildRunner clinVarWhiteListBuildRunner = new ClinVarBuildRunner(buildInfo, outPath, clinVarResource);
        clinVarWhiteListBuildRunner.run();
    }

    private void buildVariantData(BuildInfo buildInfo, Path outPath, List<AlleleResource> userDefinedAlleleResources) {
        logger.info("Downloading variant resources - {}", userDefinedAlleleResources.stream()
                .map(AlleleResource::getName)
                .toList());
        userDefinedAlleleResources.parallelStream().forEach(ResourceDownloader::download);
        logger.info("Building variant database...");
        VariantDatabaseBuildRunner variantDatabaseBuildRunner = new VariantDatabaseBuildRunner(buildInfo, outPath, userDefinedAlleleResources);
        variantDatabaseBuildRunner.run();
    }

    private void buildGenomeData(BuildInfo buildInfo, Path outPath, AssemblyResources assemblyResources) {
        logger.info("Building genome database...");
        Path genomePath = assemblyResources.getGenomeDataPath();
        logger.info("Genome Path: {}", genomePath);
        Path genomeProcessedPath = assemblyResources.getGenomeProcessedPath();
        try {
            Files.createDirectories(genomePath);
            Files.createDirectories(genomeProcessedPath);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create genome data and processed directories", e);
        }
        List<SvResource> svResources = assemblyResources.getSvResources();
        GenomeDatabaseBuildRunner genomeDatabaseBuildRunner = new GenomeDatabaseBuildRunner(buildInfo, genomePath, genomeProcessedPath, outPath, svResources);
        genomeDatabaseBuildRunner.run();
    }

    private AssemblyResources getAssemblyResourcesForAssembly(GenomeAssembly genomeAssembly) {
        return genomeAssembly == GenomeAssembly.HG19 ? this.hg19Resources : this.hg38Resources;
    }

    static class AssemblyConverter implements CommandLine.ITypeConverter<GenomeAssembly> {

        @Override
        public GenomeAssembly convert(String value) throws Exception {
            return GenomeAssembly.parseAssembly(value);
        }
    }
    static class TranscriptSourceConverter implements CommandLine.ITypeConverter<TranscriptSource> {

        @Override
        public TranscriptSource convert(String value) throws Exception {
            return TranscriptSource.parseValue(value.trim());
        }
    }
}
