package org.monarchinitiative.exomiser.autoconfigure;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.mvstore.MVStore;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * This class provided methods to dynamically generate the required data stubs for the exomiser data directories. This is
 * to ensure that the tests don't break when updating the H2 database as the file format for this changes with each minor
 * release e.g. 1.4.xxx was version 1, 2.1.xxx was version 2, and 2.2.xxx is version 3.
 * <p>
 * The methods here will create empty
 * databases with the correct schemas with the same Flyway migration mechanism and files used in the production data.
 */
public class TestDataDirectories {

    /**
     * Creates the exomiser data directory tree in the provided directory using the data version argument. For example,
     * if the dataDir pointed to /data with the data version "2402", four sub-directories will be created:
     * <pre>
     *     /data/2402_hg19
     *     /data/2402_hg38
     *     /data/2402_phenotype
     *     /data/remm
     * </pre>
     *
     * @param dataDir     directory in which to write the test data
     * @param dataVersion a version string for the data, typically of the form YYmm e.g. 2402 for February 2024
     */
    public static void setupDataDirectories(Path dataDir, String dataVersion) {
        try {
            setupGenomeReleaseData(dataDir, dataVersion + "_hg19");
            setupGenomeReleaseData(dataDir, dataVersion + "_hg38");
            setupPhenotypeReleaseData(dataDir, dataVersion + "_phenotype");
            setupRemmData(dataDir, "remm");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Path setupGenomeReleaseData(Path dataDir, String version) throws IOException {
        Path genomeDataDir = dataDir.resolve(version);
        Path genomeDbMigrationDir = genomeDataDir.resolve("db-migration");
        Files.createDirectories(genomeDbMigrationDir);

        Path transcriptsStub = Path.of("src/test/resources/data/transcripts_stub.ser");
        List<String> transcriptSources = List.of("ensembl", "refseq", "ucsc");
        for (String transcriptSource : transcriptSources) {
            Files.copy(transcriptsStub, genomeDataDir.resolve(version + "_transcripts_" + transcriptSource + ".ser"), StandardCopyOption.REPLACE_EXISTING);
        }

        MVStore hg19variantsMv = MVStore.open(genomeDataDir.resolve(version + "_variants.mv.db").toString());
        MvStoreUtil.openAlleleMVMap(hg19variantsMv);
        hg19variantsMv.close();

        MVStore clinVarMv = MVStore.open(genomeDataDir.resolve(version + "_clinvar.mv.db").toString());
        MvStoreUtil.openClinVarMVMap(clinVarMv);
        clinVarMv.close();

        // create genome database
        Path dbMigrationFile = genomeDbMigrationDir.resolve("V1.0__Create_exomiser_" + version + "_genome_schema.sql");
        Files.copy(Path.of("../exomiser-data-genome/src/main/resources/db/migration/V1.0.0__create_schema.sql"), dbMigrationFile, StandardCopyOption.REPLACE_EXISTING);

        JdbcDataSource genomeDataSource = new JdbcDataSource();
        genomeDataSource.setURL("jdbc:h2:file:" + genomeDataDir.resolve(version + "_genome;MODE=POSTGRESQL"));
        genomeDataSource.setUser("sa");
        genomeDataSource.setPassword("");

        Flyway flyway = Flyway.configure()
                .dataSource(genomeDataSource)
                .schemas("EXOMISER")
                .locations("filesystem:" + genomeDbMigrationDir.toAbsolutePath())
                .load();
        flyway.migrate();
        // clean-up migration files
        Files.deleteIfExists(dbMigrationFile);
        Files.deleteIfExists(genomeDbMigrationDir);

        return genomeDataDir;
    }

    public static Path setupPhenotypeReleaseData(Path testData, String version) throws IOException {
        Path phenoDataDir = testData.resolve(version);
        Path phenoDbMigrationDir = phenoDataDir.resolve("db-migration");

        Files.createDirectories(phenoDataDir.resolve("db-migration"));

        Path phenoDbMigrationFile = phenoDbMigrationDir.resolve("V1.0__Create_exomiser_" + version + "_schema.sql");
        Files.copy(Path.of("../exomiser-data-phenotype/src/main/resources/db/migration/h2/V1.0__Create_exomiser_schema.sql"), phenoDbMigrationFile, StandardCopyOption.REPLACE_EXISTING);

        JdbcDataSource phenoDataSource = new JdbcDataSource();
        phenoDataSource.setURL("jdbc:h2:file:" + phenoDataDir.resolve(version + ";MODE=POSTGRESQL"));
        phenoDataSource.setUser("sa");
        phenoDataSource.setPassword("");

        Flyway flyway = Flyway.configure()
                .dataSource(phenoDataSource)
                .schemas("EXOMISER")
                .locations("filesystem:" + phenoDbMigrationDir.toAbsolutePath())
                .load();
        flyway.migrate();
        // clean-up migration files
        Files.deleteIfExists(phenoDbMigrationFile);
        Files.deleteIfExists(phenoDbMigrationDir);

        MVStore dataMatrixMvStore = MVStore.open(phenoDataDir.resolve("rw_string_10.mv").toString());
        dataMatrixMvStore.close();
        return phenoDataDir;
    }

    public static Path setupRemmData(Path testData, String remm) throws IOException {
        Path remmDataDir = testData.resolve(remm);
        Files.createDirectories(remmDataDir);
        Files.copy(Path.of("src/test/resources/data/remm/remmData.tsv.gz"), remmDataDir.resolve("remmData.tsv.gz"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(Path.of("src/test/resources/data/remm/remmData.tsv.gz.tbi"), remmDataDir.resolve("remmData.tsv.gz.tbi"), StandardCopyOption.REPLACE_EXISTING);
        return remmDataDir;
    }
}
