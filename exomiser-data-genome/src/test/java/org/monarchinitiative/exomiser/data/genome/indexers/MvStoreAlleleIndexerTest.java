/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2022 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.genome.indexers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.proto.AlleleData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleKey;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleProperties;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.ClinVar;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.exomiser.data.genome.model.AlleleProperty;
import org.monarchinitiative.exomiser.data.genome.model.AlleleResource;
import org.monarchinitiative.exomiser.data.genome.model.resource.DbSnpAlleleResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.monarchinitiative.exomiser.core.proto.AlleleProto.FrequencySource.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class MvStoreAlleleIndexerTest {

    private static final Logger logger = LoggerFactory.getLogger(MvStoreAlleleIndexerTest.class);

    private MVStore newMvStore() {
        // open the store (in-memory if fileName is null)
        return new MVStore.Builder()
                .compress()
                .open();
    }

    private AlleleKey alleleKey(int chr, int pos, String ref, String alt) {
        return AlleleKey.newBuilder()
                .setChr(chr)
                .setPosition(pos)
                .setRef(ref)
                .setAlt(alt)
                .build();
    }

    private AlleleProperties alleleProperties(Map<String, Float> properties) {
        return AlleleProperties.newBuilder()
                .putAllProperties(properties)
                .build();
    }

    private AlleleProperties alleleProperties(String rsId, Map<String, Float> properties) {
        return AlleleProperties.newBuilder()
                .setRsId(rsId)
                .putAllProperties(properties)
                .build();
    }

    private AlleleProperties alleleProperties(String rsId, ClinVarData clinVarData, Map<String, Float> properties) {
        ClinVar clinVar = AlleleConverter.toProtoClinVar(clinVarData);

        return AlleleProperties.newBuilder()
                .setRsId(rsId)
                .setClinVar(clinVar)
                .putAllProperties(properties)
                .build();
    }

    @Test
    public void createsSingleAllelesMap() throws Exception {
        MVStore mvStore = newMvStore();

        MvStoreAlleleIndexer instance = new MvStoreAlleleIndexer(mvStore);
        assertThat(mvStore.getMapNames(), equalTo(Set.of("alleles")));
        assertThat(instance.count(), equalTo(0L));
    }

    @Test
    public void writeSingleAlleleNoInfo() throws Exception {
        MVStore mvStore = newMvStore();

        MvStoreAlleleIndexer instance = new MvStoreAlleleIndexer(mvStore);

        Allele allele = new Allele(1, 12345, "A", "T");
        instance.write(allele);

        assertThat(instance.count(), equalTo(1L));

        MVMap<AlleleKey, AlleleProperties> alleleMap = mvStore.openMap("alleles");

        AlleleKey alleleKey = AlleleConverter.toAlleleKey(allele);
        AlleleProperties alleleProperties = AlleleProperties.getDefaultInstance();

        assertThat(alleleMap.containsKey(alleleKey), is(true));
        assertThat(alleleMap.get(alleleKey), equalTo(alleleProperties));
    }

    @Test
    public void writeSingleAlleleWithJustRsId() throws Exception {
        MVStore mvStore = newMvStore();

        MvStoreAlleleIndexer instance = new MvStoreAlleleIndexer(mvStore);

        Allele allele = new Allele(1, 12345, "A", "T");
        allele.setRsId("rs12345");

        instance.write(allele);

        assertThat(instance.count(), equalTo(1L));
        MVMap<AlleleKey, AlleleProperties> alleleMap = mvStore.openMap("alleles");

        AlleleKey alleleKey = AlleleConverter.toAlleleKey(allele);
        AlleleProperties alleleProperties = AlleleProperties.newBuilder()
                .setRsId("rs12345")
                .putAllProperties(Collections.emptyMap())
                .build();

        assertThat(alleleMap.containsKey(alleleKey), is(true));
        assertThat(alleleMap.get(alleleKey), equalTo(alleleProperties));
    }

    @Test
    public void writeSingleAlleleWithRsIdAndOtherInfo() throws Exception {
        MVStore mvStore = newMvStore();

        MvStoreAlleleIndexer instance = new MvStoreAlleleIndexer(mvStore);

        Allele allele = new Allele(1, 12345, "A", "T");
        allele.setRsId("rs12345");
        allele.addFrequency(AlleleData.frequencyOf(KG, 2, 1000));

        instance.write(allele);

        assertThat(instance.count(), equalTo(1L));

        MVMap<AlleleKey, AlleleProperties> alleleMap = mvStore.openMap("alleles");

        AlleleKey alleleKey = AlleleConverter.toAlleleKey(allele);
        AlleleProperties alleleProperties = AlleleConverter.toAlleleProperties(allele);

        assertThat(alleleMap.containsKey(alleleKey), is(true));
        assertThat(alleleMap.get(alleleKey), equalTo(alleleProperties));
    }

    @Test
    public void writeDuplicateSingleAllele() throws Exception {
        MVStore mvStore = newMvStore();

        MvStoreAlleleIndexer instance = new MvStoreAlleleIndexer(mvStore);

        Allele allele = new Allele(1, 12345, "A", "T");
        allele.setRsId("rs12345");
        allele.addFrequency(AlleleData.frequencyOf(KG, 2, 1000));

        instance.write(allele);

        Allele dupAllele = new Allele(1, 12345, "A", "T");
        dupAllele.setRsId("rs12345");
        dupAllele.addFrequency(AlleleData.frequencyOf(KG, 2, 1000));

        instance.write(dupAllele);

        assertThat(instance.count(), equalTo(1L));

        MVMap<AlleleKey, AlleleProperties> alleleMap = mvStore.openMap("alleles");

        AlleleKey alleleKey = AlleleConverter.toAlleleKey(allele);
        AlleleProperties alleleProperties = AlleleConverter.toAlleleProperties(allele);

        assertThat(alleleMap.containsKey(alleleKey), is(true));
        assertThat(alleleMap.get(alleleKey), equalTo(alleleProperties));
    }

    @Test
    public void writeTwoIdenticalAllelesWithRsIdAndOtherInfoMergesInfoField() throws Exception {
        MVStore mvStore = newMvStore();

        MvStoreAlleleIndexer instance = new MvStoreAlleleIndexer(mvStore);

        Allele allele = new Allele(1, 12345, "A", "T");
        allele.setRsId("rs12345");
        AlleleProto.Frequency kgFreq = AlleleData.frequencyOf(KG, 2, 1000);
        allele.addFrequency(kgFreq);

        Allele other = new Allele(1, 12345, "A", "T");
        AlleleProto.Frequency nfeFreq = AlleleData.frequencyOf(GNOMAD_E_NFE, 6, 5000);
        other.addFrequency(nfeFreq);

        instance.write(allele);
        instance.write(other);

        assertThat(instance.count(), equalTo(1L));

        MVMap<AlleleKey, AlleleProperties> alleleMap = mvStore.openMap("alleles");

        AlleleKey alleleKey = AlleleConverter.toAlleleKey(allele);
        AlleleProperties alleleProperties = AlleleProperties.newBuilder()
                .setRsId("rs12345")
                .addFrequencies(kgFreq)
                .addFrequencies(nfeFreq)
                .build();

        assertThat(alleleMap.containsKey(alleleKey), is(true));
        assertThat(alleleMap.get(alleleKey), equalTo(alleleProperties));
    }

    @Test
    public void writeTwoIdenticalAllelesRsIdIsUpdatedWhenEmptyAndInfoFieldIsMerged() throws Exception {
        MVStore mvStore = newMvStore();

        MvStoreAlleleIndexer instance = new MvStoreAlleleIndexer(mvStore);

        Allele allele = new Allele(1, 12345, "A", "T");
        AlleleProto.Frequency kgFreq = AlleleData.frequencyOf(KG, 2, 1000);
        allele.addFrequency(kgFreq);

        Allele other = new Allele(1, 12345, "A", "T");
        other.setRsId("rs12345");
        AlleleProto.Frequency nfeFreq = AlleleData.frequencyOf(GNOMAD_E_NFE, 6, 5000);
        other.addFrequency(nfeFreq);

        instance.write(allele);
        instance.write(other);

        assertThat(instance.count(), equalTo(1L));

        MVMap<AlleleKey, AlleleProperties> alleleMap = mvStore.openMap("alleles");

        AlleleKey alleleKey = AlleleConverter.toAlleleKey(allele);
        AlleleProperties alleleProperties = AlleleProperties.newBuilder()
                .setRsId("rs12345")
                .addFrequencies(kgFreq)
                .addFrequencies(nfeFreq)
                .build();

        assertThat(alleleMap.containsKey(alleleKey), is(true));
        assertThat(alleleMap.get(alleleKey), equalTo(alleleProperties));
    }

    @Test
    public void writeTwoIdenticalAllelesFromDifferentDbSnpReleases() throws Exception {
        MVStore mvStore = newMvStore();

        MvStoreAlleleIndexer instance = new MvStoreAlleleIndexer(mvStore);

        Allele allele = new Allele(1, 12618254, "C", "CAAGAAG");
        allele.setRsId("rs534165942");
        AlleleProto.Frequency freq = AlleleData.frequencyOf(KG, 55, 5008);
        allele.addFrequency(freq); // 55/5008
        //these are from two different releases and DbSNP changed the rsId for some reason.
        Allele other = new Allele(1, 12618254, "C", "CAAGAAG");
        other.setRsId("rs59874722");
        other.addFrequency(freq); // 55/5008

        instance.write(allele);
        instance.write(other);

        assertThat(instance.count(), equalTo(1L));

        MVMap<AlleleKey, AlleleProperties> alleleMap = mvStore.openMap("alleles");

        AlleleKey alleleKey = AlleleConverter.toAlleleKey(allele);
        AlleleProperties alleleProperties = AlleleProperties.newBuilder()
                .setRsId("rs534165942")
                .addFrequencies(freq)
                .build();

        assertThat(alleleMap.containsKey(alleleKey), is(true));
        assertThat(alleleMap.get(alleleKey), equalTo(alleleProperties));
    }

    @Test
    public void writeTwoAlleles() throws Exception {
        MVStore mvStore = newMvStore();

        MvStoreAlleleIndexer instance = new MvStoreAlleleIndexer(mvStore);

        Allele allele = new Allele(1, 12618254, "C", "CAAGAAG");
        allele.setRsId("rs534165942");
        AlleleProto.Frequency kgFreq = AlleleData.frequencyOf(KG, 55, 5008);
        allele.addFrequency(kgFreq);

        Allele other = new Allele(23, 36103454, "A", "G");
        AlleleProto.Frequency gnomadFreq = AlleleData.frequencyOf(GNOMAD_E_AFR, 50, 15000);
        other.addFrequency(gnomadFreq);

        Allele duplicateOther = new Allele(23, 36103454, "A", "G");
        duplicateOther.addFrequency(gnomadFreq);

        instance.write(allele);
        instance.write(other);
        instance.write(duplicateOther);

        assertThat(instance.count(), equalTo(2L));

        MVMap<AlleleKey, AlleleProperties> alleleMap = mvStore.openMap("alleles");

        AlleleKey alleleKey = alleleKey(1, 12618254, "C", "CAAGAAG");
        AlleleProperties alleleProperties = AlleleProperties.newBuilder()
                .setRsId("rs534165942")
                .addFrequencies(kgFreq)
                .build();

        assertThat(alleleMap.containsKey(alleleKey), is(true));
        assertThat(alleleMap.get(alleleKey), equalTo(alleleProperties));


        AlleleKey otherAlleleKey = alleleKey(23, 36103454, "A", "G");
        AlleleProperties otherAlleleProperties = AlleleProperties.newBuilder()
                .addFrequencies(gnomadFreq)
                .build();

        assertThat(alleleMap.containsKey(otherAlleleKey), is(true));
        assertThat(alleleMap.get(otherAlleleKey), equalTo(otherAlleleProperties));
    }

    @Test
    public void writeAndUpdateAlleles() throws Exception {
        MVStore mvStore = newMvStore();

        MvStoreAlleleIndexer instance = new MvStoreAlleleIndexer(mvStore);

        Allele allele = new Allele(1, 12618254, "C", "CAAGAAG");
        allele.setRsId("rs534165942");
        AlleleProto.Frequency kgFreq = AlleleData.frequencyOf(KG, 10, 1000);
        allele.addFrequency(kgFreq);
        //simulate adding more data from a second datasource
        Allele updateAllele = new Allele(1, 12618254, "C", "CAAGAAG");
        updateAllele.setRsId("rs534165942");
        AlleleProto.Frequency gnomadFreq = AlleleData.frequencyOf(GNOMAD_E_NFE, 300, 15000);
        updateAllele.addFrequency(gnomadFreq);

        ClinVarData alleleClinVarData = ClinVarData.builder()
                .variationId("12345")
                .primaryInterpretation(ClinVarData.ClinSig.CONFLICTING_PATHOGENICITY_INTERPRETATIONS)
                .secondaryInterpretations(EnumSet.of(ClinVarData.ClinSig.UNCERTAIN_SIGNIFICANCE, ClinVarData.ClinSig.LIKELY_PATHOGENIC))
                .includedAlleles(Map.of("54321", ClinVarData.ClinSig.PATHOGENIC))
                .reviewStatus(ClinVarData.ReviewStatus.CRITERIA_PROVIDED_CONFLICTING_INTERPRETATIONS)
                .build();
        updateAllele.setClinVarData(alleleClinVarData);

        Allele other = new Allele(23, 36103454, "A", "G");
        AlleleProto.Frequency espEaFreq = AlleleData.frequencyOf(ESP_EA, 1, 6500);
        other.addFrequency(espEaFreq);

        Allele updateOther = new Allele(23, 36103454, "A", "G");
        AlleleProto.Frequency espAllFreq = AlleleData.frequencyOf(ESP_ALL, 3, 6500);
        updateOther.addFrequency(espAllFreq);

        instance.write(allele);
        instance.write(other);
        instance.write(updateOther);
        instance.write(updateAllele);

        assertThat(instance.count(), equalTo(2L));

        MVMap<AlleleKey, AlleleProperties> alleleMap = mvStore.openMap("alleles");
        assertThat(alleleMap.size(), equalTo(2));

        AlleleKey alleleKey = alleleKey(1, 12618254, "C", "CAAGAAG");
        ClinVar clinVar = AlleleConverter.toProtoClinVar(alleleClinVarData);

        AlleleProperties alleleProperties = AlleleProperties.newBuilder()
                .setRsId("rs534165942")
                .setClinVar(clinVar)
                .addFrequencies(kgFreq)
                .addFrequencies(gnomadFreq)
                .build();

        assertThat(alleleMap.containsKey(alleleKey), is(true));
        assertThat(alleleMap.get(alleleKey), equalTo(alleleProperties));


        AlleleKey otherAlleleKey = alleleKey(23, 36103454, "A", "G");
        AlleleProperties otherAlleleProperties = AlleleProperties.newBuilder()
                .addFrequencies(espEaFreq)
                .addFrequencies(espAllFreq)
                .build();

        assertThat(alleleMap.containsKey(otherAlleleKey), is(true));
        assertThat(alleleMap.get(otherAlleleKey), equalTo(otherAlleleProperties));
    }


    @Test
    public void processAndWriteToDisk(@TempDir Path tempDir) throws Exception {
        AlleleResource dbSnpResource = new DbSnpAlleleResource("test_first_ten_dbsnp", new URL("http://"), Paths.get("src/test/resources/test_first_ten_dbsnp.vcf.gz"));

        File mvTestFile = tempDir.resolve("test.mv.db").toFile();
        logger.info("Writing allele data to file {}", mvTestFile);
        MVStore mvStore = new MVStore.Builder()
                .fileName(mvTestFile.getAbsolutePath())
                .compress()
                .open();

        MvStoreAlleleIndexer instance = new MvStoreAlleleIndexer(mvStore);
        instance.index(dbSnpResource);

        MVMap<AlleleKey, AlleleProperties> alleleMap = mvStore.openMap("alleles");
        int originalMapSize = alleleMap.size();
        logger.info("Map contains {} entries:", originalMapSize);
        assertThat(originalMapSize, equalTo(10));
        Map<AlleleKey, AlleleProperties> original = Map.copyOf(alleleMap);
        logger.info("Closing map");
        mvStore.close();

        //re-open the store
        logger.info("Re-opening map");
        MVStore reOpened = new MVStore.Builder()
                .fileName(mvTestFile.getAbsolutePath())
                .readOnly()
                .open();

        MVMap<AlleleKey, AlleleProperties> reOpenedAlleleMap = reOpened.openMap("alleles", MvStoreUtil.alleleMapBuilder());
        var reopened = Map.copyOf(reOpenedAlleleMap);
        assertThat(reopened, equalTo(original));
        logger.info("Re-opened map contains {} entries:", reOpenedAlleleMap.size());
        reOpened.close();
    }


//    @Disabled("Just playing about")
//    @Test
//    void chromosomeOneOnly() {
////        MVStore finalStore = new MVStore.Builder()
////                .fileName("C:/Users/hhx640/Documents/exomiser-data/2007_hg19/2007_hg19_variants.mv.db")
////                .readOnly()
////                .open();
//
//        MVStore chromosomeOneStore = new MVStore.Builder()
//                .fileName("C:/Users/hhx640/Documents/exomiser-data/2007_hg19/2007_hg19_chr1_variants.mv.db")
//                .readOnly()
//                .open();
//
//        MVStore chromosomeOneNewAllelePropertiesStore = new MVStore.Builder()
//                .fileName("C:/Users/hhx640/Documents/exomiser-data/2007_hg19/2007_hg19_chr1_new_schema_variants.mv.db")
//                .compress()
//                .open();
//
//        migrateSchema(chromosomeOneStore, chromosomeOneNewAllelePropertiesStore);
//
//        chromosomeOneStore.close();
//
//        chromosomeOneNewAllelePropertiesStore.compactMoveChunks();
//        chromosomeOneNewAllelePropertiesStore.close();
////        finalStore.close();
//    }

//    @Disabled("Just playing about")
//    @Test
//    void copyToNewSchema() {
//
////        MVStore finalStore = new MVStore.Builder()
////                .fileName("C:/Users/hhx640/Documents/exomiser-data/2007_hg19/2007_hg19_variants.mv.db")
////                .readOnly()
////                .open();
//
//        MVStore newSchemaStore = new MVStore.Builder()
//                .fileName("C:/Users/hhx640/Documents/exomiser-data/2007_hg19/2007_hg19_variants_new_schema_temp.mv.db")
//                .readOnly()
//                .open();
//
//        MVStore copied = new MVStore.Builder()
//                .fileName("C:/Users/hhx640/Documents/exomiser-data/2007_hg19/2007_hg19_variants_new_schema.mv.db")
//                .compress()
//                .open();
//
//        copyToNewInstance(newSchemaStore, copied);
//
//        newSchemaStore.close();
//        copied.close();
//    }
//
//    private void copyToNewInstance(MVStore mvStore, MVStore newStore) {
//        MVMap<AlleleKey, AlleleProperties> map = MvStoreUtil.openAlleleMVMap(mvStore);
//
//        MVMap<AlleleKey, AlleleProperties> newMap = MvStoreUtil.openAlleleMVMap(newStore);
//
//        logger.info("Copying {} entries from temp store {} to final store {}", map.size(), mvStore.getFileStore()
//                .getFileName(), newStore.getFileStore().getFileName());
//        int count = 0;
//        for (Map.Entry<AlleleKey, AlleleProperties> entry : map.entrySet()) {
//            newMap.put(entry.getKey(), entry.getValue());
//            count++;
//            if (count % 10000000 == 0) {
//                newStore.compactMoveChunks();
//                newStore.commit();
//                var key = entry.getKey();
//                logger.info("copied {} alleles. Current: {}-{}-{}-{}", count, key.getChr(), key.getPosition(), key.getRef(), key
//                        .getAlt());
//            }
//        }
//        newStore.compactMoveChunks();
//        logger.info("Finished copying {} entries to new map", newMap.size());
//    }
//
//    private void migrateSchema(MVStore mvStore, MVStore newStore) {
//        MVMap<AlleleKey, AlleleProperties> map = MvStoreUtil.openAlleleMVMap(mvStore);
//
//        MVMap<AlleleKey, AlleleProperties> newMap = MvStoreUtil.openAlleleMVMap(newStore);
//
//        logger.info("Copying {} entries from temp store {} to final store {}", map.size(), mvStore.getFileStore()
//                .getFileName(), newStore.getFileStore().getFileName());
//        int count = 0;
//        for (Map.Entry<AlleleKey, AlleleProperties> entry : map.entrySet()) {
//            AlleleProperties original = entry.getValue();
//            AlleleProperties migrated = migrateAlleleProperties(original);
//            newMap.put(entry.getKey(), migrated);
//            count++;
//            if (count % 10000000 == 0) {
//                AlleleKey key = entry.getKey();
//                logger.info("Migrated {} alleles. Current: {}-{}-{}-{}", count, key.getChr(), key.getPosition(), key.getRef(), key
//                        .getAlt());
//            }
//        }
//
//        logger.info("Finished copying {} entries to new map", newMap.size());
//    }
//
//    private AlleleProperties migrateAlleleProperties(AlleleProperties original) {
//        Map<String, Float> propertiesMap = original.getPropertiesMap();
//        if (propertiesMap.isEmpty()) {
//            return original;
//        }
//        List<AlleleProto.Frequency> frequencies = parseFrequencies(propertiesMap);
//        List<AlleleProto.Pathogenicity> pathScores = parsePathScores(propertiesMap);
//        return original.toBuilder()
//                .addAllFrequencies(frequencies)
//                .addAllPathScores(pathScores)
//                .clearProperties()
//                .build();
//    }
//
//    private List<AlleleProto.Frequency> parseFrequencies(Map<String, Float> values) {
//        List<AlleleProto.Frequency> frequencies = new ArrayList<>(values.size());
//        for (Map.Entry<String, Float> field : values.entrySet()) {
//            String key = field.getKey();
//            if (FREQUENCY_SOURCE_MAP.containsKey(key)) {
//                AlleleProto.FrequencySource source = FREQUENCY_SOURCE_MAP.get(key);
//                float value = field.getValue();
//                frequencies.add(AlleleProto.Frequency.newBuilder().setFrequencySource(source).setFreq(value).build());
//            }
//        }
//        return frequencies;
//    }
//
//    private List<AlleleProto.Pathogenicity> parsePathScores(Map<String, Float> values) {
//        List<AlleleProto.Pathogenicity> pathogenicityScores = new ArrayList<>();
//        for (Map.Entry<String, Float> field : values.entrySet()) {
//            String key = field.getKey();
//            if (PATHOGENICITY_SOURCE_MAP.containsKey(key)) {
//                AlleleProto.PathogenicitySource source = PATHOGENICITY_SOURCE_MAP.get(key);
//                float score = field.getValue();
//                pathogenicityScores.add(AlleleProto.Pathogenicity.newBuilder()
//                        .setPathogenicitySource(source)
//                        .setScore(score)
//                        .build());
//            }
//        }
//        return pathogenicityScores;
//    }
//
//
//    // These maps are constant look-ups for keys in the AlleleProto.AlleleProperties propertiesMap which was generated by the
//    // genome-data module. The keys are AlleleProperty string values.
//    private static final Map<String, AlleleProto.FrequencySource> FREQUENCY_SOURCE_MAP = new ImmutableMap.Builder<String, AlleleProto.FrequencySource>()
//            .put("KG", AlleleProto.FrequencySource.THOUSAND_GENOMES)
//            .put("TOPMED", AlleleProto.FrequencySource.TOPMED)
//            .put("UK10K", AlleleProto.FrequencySource.UK10K)
//
//            .put("ESP_AA", AlleleProto.FrequencySource.ESP_AFRICAN_AMERICAN)
//            .put("ESP_EA", AlleleProto.FrequencySource.ESP_EUROPEAN_AMERICAN)
//            .put("ESP_ALL", AlleleProto.FrequencySource.ESP_ALL)
//
//            .put("EXAC_AFR", AlleleProto.FrequencySource.EXAC_AFRICAN_INC_AFRICAN_AMERICAN)
//            .put("EXAC_AMR", AlleleProto.FrequencySource.EXAC_AMERICAN)
//            .put("EXAC_EAS", AlleleProto.FrequencySource.EXAC_EAST_ASIAN)
//            .put("EXAC_FIN", AlleleProto.FrequencySource.EXAC_FINNISH)
//            .put("EXAC_NFE", AlleleProto.FrequencySource.EXAC_NON_FINNISH_EUROPEAN)
//            .put("EXAC_OTH", AlleleProto.FrequencySource.EXAC_OTHER)
//            .put("EXAC_SAS", AlleleProto.FrequencySource.EXAC_SOUTH_ASIAN)
//
//            .put("GNOMAD_E_AFR", AlleleProto.FrequencySource.GNOMAD_E_AFR)
//            .put("GNOMAD_E_AMR", AlleleProto.FrequencySource.GNOMAD_E_AMR)
//            .put("GNOMAD_E_ASJ", AlleleProto.FrequencySource.GNOMAD_E_ASJ)
//            .put("GNOMAD_E_EAS", AlleleProto.FrequencySource.GNOMAD_E_EAS)
//            .put("GNOMAD_E_FIN", AlleleProto.FrequencySource.GNOMAD_E_FIN)
//            .put("GNOMAD_E_NFE", AlleleProto.FrequencySource.GNOMAD_E_NFE)
//            .put("GNOMAD_E_OTH", AlleleProto.FrequencySource.GNOMAD_E_OTH)
//            .put("GNOMAD_E_SAS", AlleleProto.FrequencySource.GNOMAD_E_SAS)
//
//            .put("GNOMAD_G_AFR", AlleleProto.FrequencySource.GNOMAD_G_AFR)
//            .put("GNOMAD_G_AMR", AlleleProto.FrequencySource.GNOMAD_G_AMR)
//            .put("GNOMAD_G_ASJ", AlleleProto.FrequencySource.GNOMAD_G_ASJ)
//            .put("GNOMAD_G_EAS", AlleleProto.FrequencySource.GNOMAD_G_EAS)
//            .put("GNOMAD_G_FIN", AlleleProto.FrequencySource.GNOMAD_G_FIN)
//            .put("GNOMAD_G_NFE", AlleleProto.FrequencySource.GNOMAD_G_NFE)
//            .put("GNOMAD_G_OTH", AlleleProto.FrequencySource.GNOMAD_G_OTH)
//            .put("GNOMAD_G_SAS", AlleleProto.FrequencySource.GNOMAD_G_SAS)
//            .build();
//
//    private static final Map<String, AlleleProto.PathogenicitySource> PATHOGENICITY_SOURCE_MAP = new ImmutableMap.Builder<String, AlleleProto.PathogenicitySource>()
//            .put("POLYPHEN", AlleleProto.PathogenicitySource.POLYPHEN)
//            .put("MUT_TASTER", AlleleProto.PathogenicitySource.MUTATION_TASTER)
//            .put("SIFT", AlleleProto.PathogenicitySource.SIFT)
//            .put("CADD", AlleleProto.PathogenicitySource.CADD)
//            .put("REMM", AlleleProto.PathogenicitySource.REMM)
//            .put("REVEL", AlleleProto.PathogenicitySource.REVEL)
//            .put("MCAP", AlleleProto.PathogenicitySource.M_CAP)
//            .put("MPC", AlleleProto.PathogenicitySource.MPC)
//            .put("MVP", AlleleProto.PathogenicitySource.MVP)
//            .put("PRIMATE_AI", AlleleProto.PathogenicitySource.PRIMATE_AI)
//            .build();

}