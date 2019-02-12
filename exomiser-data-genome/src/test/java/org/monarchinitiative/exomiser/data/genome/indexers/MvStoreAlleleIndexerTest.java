/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
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
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class MvStoreAlleleIndexerTest {

    private static Logger logger = LoggerFactory.getLogger(MvStoreAlleleIndexerTest.class);

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
        assertThat(mvStore.getMapNames(), equalTo(Sets.newHashSet("alleles")));
        assertThat(instance.count(), equalTo(0L));
    }

    @Test
    public void writeSingleAlleleNoInfo() throws Exception {
        MVStore mvStore = newMvStore();

        MvStoreAlleleIndexer instance = new MvStoreAlleleIndexer(mvStore);

        Allele allele = new Allele(1, 12345, "A", "T");
        instance.writeAllele(allele);

        assertThat(instance.count(), equalTo(1L));

        MVMap<AlleleKey, AlleleProperties> alleleMap = mvStore.openMap("alleles");

        AlleleKey alleleKey = AlleleConverter.toAlleleKey(allele);
        AlleleProperties alleleProperties = alleleProperties(Collections.emptyMap());

        assertThat(alleleMap.containsKey(alleleKey), is(true));
        assertThat(alleleMap.get(alleleKey), equalTo(alleleProperties));
    }

    @Test
    public void writeSingleAlleleWithJustRsId() throws Exception {
        MVStore mvStore = newMvStore();

        MvStoreAlleleIndexer instance = new MvStoreAlleleIndexer(mvStore);

        Allele allele = new Allele(1, 12345, "A", "T");
        allele.setRsId("rs12345");

        instance.writeAllele(allele);

        assertThat(instance.count(), equalTo(1L));
        MVMap<AlleleKey, AlleleProperties> alleleMap = mvStore.openMap("alleles");

        AlleleKey alleleKey = AlleleConverter.toAlleleKey(allele);
        AlleleProperties alleleProperties = alleleProperties("rs12345", Collections.emptyMap());

        assertThat(alleleMap.containsKey(alleleKey), is(true));
        assertThat(alleleMap.get(alleleKey), equalTo(alleleProperties));
    }

    @Test
    public void writeSingleAlleleWithRsIdAndOtherInfo() throws Exception {
        MVStore mvStore = newMvStore();

        MvStoreAlleleIndexer instance = new MvStoreAlleleIndexer(mvStore);

        Allele allele = new Allele(1, 12345, "A", "T");
        allele.setRsId("rs12345");
        allele.addValue(AlleleProperty.KG, 0.0023f);

        instance.writeAllele(allele);

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
        allele.addValue(AlleleProperty.KG, 0.0023f);

        instance.writeAllele(allele);

        Allele dupAllele = new Allele(1, 12345, "A", "T");
        dupAllele.setRsId("rs12345");
        dupAllele.addValue(AlleleProperty.KG, 0.0023f);

        instance.writeAllele(dupAllele);

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
        allele.addValue(AlleleProperty.KG, 0.0023f);

        Allele other = new Allele(1, 12345, "A", "T");
        other.addValue(AlleleProperty.EXAC_NFE, 0.12345f);

        instance.writeAllele(allele);
        instance.writeAllele(other);

        assertThat(instance.count(), equalTo(1L));

        MVMap<AlleleKey, AlleleProperties> alleleMap = mvStore.openMap("alleles");

        AlleleKey alleleKey = AlleleConverter.toAlleleKey(allele);
        Map<String, Float> properties = new HashMap<>();
        properties.put("KG", 0.0023f);
        properties.put("EXAC_NFE", 0.12345f);
        AlleleProperties alleleProperties = alleleProperties("rs12345", properties);

        assertThat(alleleMap.containsKey(alleleKey), is(true));
        assertThat(alleleMap.get(alleleKey), equalTo(alleleProperties));
    }

    @Test
    public void writeTwoIdenticalAllelesRsIdIsUpdatedWhenEmptyAndInfoFieldIsMerged() throws Exception {
        MVStore mvStore = newMvStore();

        MvStoreAlleleIndexer instance = new MvStoreAlleleIndexer(mvStore);

        Allele allele = new Allele(1, 12345, "A", "T");
        allele.addValue(AlleleProperty.KG, 0.0023f);

        Allele other = new Allele(1, 12345, "A", "T");
        other.setRsId("rs12345");
        other.addValue(AlleleProperty.EXAC_NFE, 0.12345f);

        instance.writeAllele(allele);
        instance.writeAllele(other);

        assertThat(instance.count(), equalTo(1L));

        MVMap<AlleleKey, AlleleProperties> alleleMap = mvStore.openMap("alleles");

        AlleleKey alleleKey = AlleleConverter.toAlleleKey(allele);
        Map<String, Float> properties = new HashMap<>();
        properties.put("KG", 0.0023f);
        properties.put("EXAC_NFE", 0.12345f);
        AlleleProperties alleleProperties = alleleProperties("rs12345", properties);

        assertThat(alleleMap.containsKey(alleleKey), is(true));
        assertThat(alleleMap.get(alleleKey), equalTo(alleleProperties));
    }

    @Test
    public void writeTwoIdenticalAllelesFromDifferentDbSnpReleases() throws Exception {
        MVStore mvStore = newMvStore();

        MvStoreAlleleIndexer instance = new MvStoreAlleleIndexer(mvStore);

        Allele allele = new Allele(1, 12618254, "C", "CAAGAAG");
        allele.setRsId("rs534165942");
        allele.addValue(AlleleProperty.KG, 1.098f);
        //these are from two different releases and DbSNP changed the rsId for some reason.
        Allele other = new Allele(1, 12618254, "C", "CAAGAAG");
        other.setRsId("rs59874722");
        other.addValue(AlleleProperty.KG, 1.098f);

        instance.writeAllele(allele);
        instance.writeAllele(other);

        assertThat(instance.count(), equalTo(1L));

        MVMap<AlleleKey, AlleleProperties> alleleMap = mvStore.openMap("alleles");

        AlleleKey alleleKey = AlleleConverter.toAlleleKey(allele);
        Map<String, Float> properties = new HashMap<>();
        properties.put("KG", 1.098f);
        AlleleProperties alleleProperties = alleleProperties("rs534165942", properties);

        assertThat(alleleMap.containsKey(alleleKey), is(true));
        assertThat(alleleMap.get(alleleKey), equalTo(alleleProperties));
    }

    @Test
    public void writeTwoAlleles() throws Exception {
        MVStore mvStore = newMvStore();

        MvStoreAlleleIndexer instance = new MvStoreAlleleIndexer(mvStore);

        Allele allele = new Allele(1, 12618254, "C", "CAAGAAG");
        allele.setRsId("rs534165942");
        allele.addValue(AlleleProperty.KG, 1.098f);

        Allele other = new Allele(23, 36103454, "A", "G");
        other.addValue(AlleleProperty.EXAC_AFR, 0.012086052f);

        Allele duplicateOther = new Allele(23, 36103454, "A", "G");
        duplicateOther.addValue(AlleleProperty.EXAC_AFR, 0.012086052f);

        instance.writeAllele(allele);
        instance.writeAllele(other);
        instance.writeAllele(duplicateOther);

        assertThat(instance.count(), equalTo(2L));

        MVMap<AlleleKey, AlleleProperties> alleleMap = mvStore.openMap("alleles");

        AlleleKey alleleKey = alleleKey(1, 12618254, "C", "CAAGAAG");
        Map<String, Float> properties = new HashMap<>();
        properties.put("KG", 1.098f);
        AlleleProperties alleleProperties = alleleProperties("rs534165942", properties);

        assertThat(alleleMap.containsKey(alleleKey), is(true));
        assertThat(alleleMap.get(alleleKey), equalTo(alleleProperties));


        AlleleKey otherAlleleKey = alleleKey(23, 36103454, "A", "G");
        Map<String, Float> otherProperties = new HashMap<>();
        otherProperties.put("EXAC_AFR", 0.012086052f);
        AlleleProperties otherAlleleProperties = alleleProperties(otherProperties);

        assertThat(alleleMap.containsKey(otherAlleleKey), is(true));
        assertThat(alleleMap.get(otherAlleleKey), equalTo(otherAlleleProperties));
    }

    @Test
    public void writeAndUpdateAlleles() throws Exception {
        MVStore mvStore = newMvStore();

        MvStoreAlleleIndexer instance = new MvStoreAlleleIndexer(mvStore);

        Allele allele = new Allele(1, 12618254, "C", "CAAGAAG");
        allele.setRsId("rs534165942");
        allele.addValue(AlleleProperty.KG, 1.0f);
        //simulate adding more data from a second datasource
        Allele updateAllele = new Allele(1, 12618254, "C", "CAAGAAG");
        updateAllele.setRsId("rs534165942");
        updateAllele.addValue(AlleleProperty.EXAC_NFE, 2.0f);
        ClinVarData alleleClinVarData = ClinVarData.builder()
                .alleleId("12345")
                .primaryInterpretation(ClinVarData.ClinSig.CONFLICTING_PATHOGENICITY_INTERPRETATIONS)
                .secondaryInterpretations(EnumSet.of(ClinVarData.ClinSig.UNCERTAIN_SIGNIFICANCE, ClinVarData.ClinSig.LIKELY_PATHOGENIC))
                .includedAlleles(ImmutableMap.of("54321", ClinVarData.ClinSig.PATHOGENIC))
                .reviewStatus("conflicting interpretations")
                .build();
        updateAllele.setClinVarData(alleleClinVarData);

        Allele other = new Allele(23, 36103454, "A", "G");
        other.addValue(AlleleProperty.EXAC_AFR, 0.01f);

        Allele updateOther = new Allele(23, 36103454, "A", "G");
        updateOther.addValue(AlleleProperty.ESP_ALL, 0.2f);

        instance.writeAllele(allele);
        instance.writeAllele(other);
        instance.writeAllele(updateOther);
        instance.writeAllele(updateAllele);

        assertThat(instance.count(), equalTo(2L));

        MVMap<AlleleKey, AlleleProperties> alleleMap = mvStore.openMap("alleles");
        assertThat(alleleMap.size(), equalTo(2));

        AlleleKey alleleKey = alleleKey(1, 12618254, "C", "CAAGAAG");
        Map<String, Float> properties = new HashMap<>();
        properties.put("KG", 1.0f);
        properties.put("EXAC_NFE", 2.0f);
        AlleleProperties alleleProperties = alleleProperties("rs534165942", alleleClinVarData, properties);

        assertThat(alleleMap.containsKey(alleleKey), is(true));
        assertThat(alleleMap.get(alleleKey), equalTo(alleleProperties));


        AlleleKey otherAlleleKey = alleleKey(23, 36103454, "A", "G");
        Map<String, Float> otherProperties = new HashMap<>();
        otherProperties.put("EXAC_AFR", 0.01f);
        otherProperties.put("ESP_ALL", 0.2f);
        AlleleProperties otherAlleleProperties = alleleProperties(otherProperties);

        assertThat(alleleMap.containsKey(otherAlleleKey), is(true));
        assertThat(alleleMap.get(otherAlleleKey), equalTo(otherAlleleProperties));
    }


    @Test
    @ExtendWith(TempDirectory.class)
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

        logger.info("Closing map");
        mvStore.close();

        //re-open the store
        logger.info("Re-opening map");
        MVStore reOpened = new MVStore.Builder()
                .fileName(mvTestFile.getAbsolutePath())
                .readOnly()
                .open();

        MVMap<AlleleKey, AlleleProperties> reOpenedAlleleMap = reOpened.openMap("alleles", MvStoreUtil.alleleMapBuilder());

        logger.info("Re-opened map contains {} entries:", reOpenedAlleleMap.size());
        assertThat(reOpenedAlleleMap.size(), equalTo(originalMapSize));
        assertThat(getAlleleProperties(reOpenedAlleleMap, 1, 10019, "TA", "T").getRsId(), equalTo("rs775809821"));
        assertThat(getAlleleProperties(reOpenedAlleleMap, 1, 10039, "A", "C").getRsId(), equalTo("rs978760828"));
        assertThat(getAlleleProperties(reOpenedAlleleMap, 1, 10043, "T", "A").getRsId(), equalTo("rs1008829651"));
        assertThat(getAlleleProperties(reOpenedAlleleMap, 1, 10051, "A", "G").getRsId(), equalTo("rs1052373574"));
        assertThat(getAlleleProperties(reOpenedAlleleMap, 1, 10055, "T", "A").getRsId(), equalTo("rs892501864"));
        assertThat(getAlleleProperties(reOpenedAlleleMap, 1, 10055, "T", "TA").getRsId(), equalTo("rs768019142"));
        assertThat(getAlleleProperties(reOpenedAlleleMap, 1, 10063, "A", "C").getRsId(), equalTo("rs1010989343"));
        assertThat(getAlleleProperties(reOpenedAlleleMap, 1, 10077, "C", "G").getRsId(), equalTo("rs1022805358"));
        assertThat(getAlleleProperties(reOpenedAlleleMap, 1, 10109, "A", "T").getRsId(), equalTo("rs376007522"));
        assertThat(getAlleleProperties(reOpenedAlleleMap, 1, 10108, "C", "T").getRsId(), equalTo("rs62651026"));

        reOpened.close();
    }

    private AlleleProperties getAlleleProperties(MVMap<AlleleKey, AlleleProperties> reOpenedAlleleMap, int chr, int pos, String ref, String alt) {
        AlleleKey last = alleleKey(chr, pos, ref, alt);
        AlleleProperties lastProperties = reOpenedAlleleMap.get(last);
        logger.info("{}-{}-{}-{} {{} {}}", chr, pos, ref, alt, lastProperties.getRsId(), lastProperties.getPropertiesMap());
        return lastProperties;
    }
}