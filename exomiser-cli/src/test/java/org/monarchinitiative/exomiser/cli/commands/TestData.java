package org.monarchinitiative.exomiser.cli.commands;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Timestamp;
import de.charite.compbio.jannovar.mendel.SubModeOfInheritance;
import org.monarchinitiative.exomiser.api.v1.AnalysisProto;
import org.monarchinitiative.exomiser.api.v1.JobProto;
import org.monarchinitiative.exomiser.api.v1.OutputProto;
import org.monarchinitiative.exomiser.api.v1.SampleProto;
import org.monarchinitiative.exomiser.core.analysis.AnalysisMode;
import org.monarchinitiative.exomiser.core.analysis.AnalysisProtoBuilder;
import org.monarchinitiative.exomiser.core.analysis.InheritanceModeOptions;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.phenopackets.schema.v1.Family;
import org.phenopackets.schema.v1.Phenopacket;
import org.phenopackets.schema.v1.core.*;

import java.time.Instant;
import java.util.List;

import static de.charite.compbio.jannovar.annotation.VariantEffect.*;
import static org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource.*;
import static org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource.SPLICE_AI;

public class TestData {
    public static final SampleProto.Sample SAMPLE = SampleProto.Sample.newBuilder()
            .setGenomeAssembly("hg19")
            .setVcf("examples/Pfeiffer.vcf")
            .setPed("examples/Pfeiffer-singleton.ped")
            .setProband("manuel")
            .addAllHpoIds(List.of("HP:0001156", "HP:0001363", "HP:0011304", "HP:0010055"))
            .build();

    public static final Phenopacket PHENOPACKET = Phenopacket.newBuilder()
            .setId("manuel")
            .setSubject(Individual.newBuilder()
                    .setId("manuel")
                    .setSex(Sex.MALE)
                    .build())
            .addPhenotypicFeatures(PhenotypicFeature.newBuilder().setType(OntologyClass.newBuilder()
                    .setId("HP:0001159")
                    .setLabel("Syndactyly")
                    .build()))
            .addPhenotypicFeatures(PhenotypicFeature.newBuilder().setType(OntologyClass.newBuilder()
                    .setId("HP:0000486")
                    .setLabel("Strabismus")
                    .build()))
            .addPhenotypicFeatures(PhenotypicFeature.newBuilder().setType(OntologyClass.newBuilder()
                    .setId("HP:0000327")
                    .setLabel("Hypoplasia of the maxilla")
                    .build()))
            .addPhenotypicFeatures(PhenotypicFeature.newBuilder().setType(OntologyClass.newBuilder()
                    .setId("HP:0000520")
                    .setLabel("Proptosis")
                    .build()))
            .addPhenotypicFeatures(PhenotypicFeature.newBuilder().setType(OntologyClass.newBuilder()
                    .setId("HP:0000316")
                    .setLabel("Hypertelorism")
                    .build()))
            .addPhenotypicFeatures(PhenotypicFeature.newBuilder().setType(OntologyClass.newBuilder()
                    .setId("HP:0000244")
                    .setLabel("Brachyturricephaly")
                    .build()))
            .addHtsFiles(HtsFile.newBuilder()
                    .setUri("examples/Pfeiffer.vcf")
                    .setHtsFormat(HtsFile.HtsFormat.VCF)
                    .setGenomeAssembly("hg19")
            )
            .setMetaData(MetaData.newBuilder()
                    .setCreated(Timestamp.newBuilder()
                            .setSeconds(Instant.parse("2019-11-12T13:47:51.948Z").getEpochSecond())
                            .setNanos(Instant.parse("2019-11-12T13:47:51.948Z").getNano())
                    )
                    .setCreatedBy("julesj")
                    .addResources(Resource.newBuilder()
                            .setId("hp")
                            .setName("human phenotype ontology")
                            .setUrl("http://purl.obolibrary.org/obo/hp.owl")
                            .setVersion("hp/releases/2019-11-08")
                            .setNamespacePrefix("HP")
                            .setIriPrefix("http://purl.obolibrary.org/obo/HP_")
                    )
                    .setPhenopacketSchemaVersion("1.0")
            )
            .build();

    public static final Family FAMILY = Family.newBuilder()
            .setId("ISDBM322017-family")
            .setProband(Phenopacket.newBuilder()
                    .setSubject(Individual.newBuilder()
                            .setId("ISDBM322017")
                            .setSex(Sex.FEMALE)
                            .build())
                    .addPhenotypicFeatures(PhenotypicFeature.newBuilder().setType(OntologyClass.newBuilder()
                            .setId("HP:0001159")
                            .setLabel("Syndactyly")
                            .build()))
                    .addPhenotypicFeatures(PhenotypicFeature.newBuilder().setType(OntologyClass.newBuilder()
                            .setId("HP:0000486")
                            .setLabel("Strabismus")
                            .build()))
                    .addPhenotypicFeatures(PhenotypicFeature.newBuilder().setType(OntologyClass.newBuilder()
                            .setId("HP:0000327")
                            .setLabel("Hypoplasia of the maxilla")
                            .build()))
                    .addPhenotypicFeatures(PhenotypicFeature.newBuilder().setType(OntologyClass.newBuilder()
                            .setId("HP:0000520")
                            .setLabel("Proptosis")
                            .build()))
                    .addPhenotypicFeatures(PhenotypicFeature.newBuilder().setType(OntologyClass.newBuilder()
                            .setId("HP:0000316")
                            .setLabel("Hypertelorism")
                            .build()))
                    .addPhenotypicFeatures(PhenotypicFeature.newBuilder().setType(OntologyClass.newBuilder()
                            .setId("HP:0000244")
                            .setLabel("Brachyturricephaly")
                            .build()))
                    .build())
            .setPedigree(Pedigree.newBuilder()
                    .addPersons(Pedigree.Person.newBuilder()
                            .setIndividualId("ISDBM322017")
                            .setPaternalId("ISDBM322016")
                            .setMaternalId("ISDBM322018")
                            .setSex(Sex.FEMALE)
                            .setAffectedStatus(Pedigree.Person.AffectedStatus.AFFECTED)
                            .build())
                    .addPersons(Pedigree.Person.newBuilder()
                            .setIndividualId("ISDBM322015")
                            .setPaternalId("ISDBM322016")
                            .setMaternalId("ISDBM322018")
                            .setSex(Sex.MALE)
                            .setAffectedStatus(Pedigree.Person.AffectedStatus.UNAFFECTED)
                            .build())
                    .addPersons(Pedigree.Person.newBuilder()
                            .setIndividualId("ISDBM322016")
                            .setSex(Sex.MALE)
                            .setAffectedStatus(Pedigree.Person.AffectedStatus.UNAFFECTED)
                            .build())
                    .addPersons(Pedigree.Person.newBuilder()
                            .setIndividualId("ISDBM322018")
                            .setSex(Sex.FEMALE)
                            .setAffectedStatus(Pedigree.Person.AffectedStatus.UNAFFECTED)
                            .build())
                    .build())
            .addHtsFiles(HtsFile.newBuilder()
                    .setUri("examples/Pfeiffer-quartet.vcf.gz")
                    .setHtsFormat(HtsFile.HtsFormat.VCF)
                    .setGenomeAssembly("GRCh37")
            )
            .setMetaData(MetaData.newBuilder()
                    .setCreated(Timestamp.newBuilder()
                            .setSeconds(Instant.parse("2019-11-12T13:47:51.948Z").getEpochSecond())
                            .setNanos(Instant.parse("2019-11-12T13:47:51.948Z").getNano())
                    )
                    .setCreatedBy("julesj")
                    .addResources(Resource.newBuilder()
                            .setId("hp")
                            .setName("human phenotype ontology")
                            .setUrl("http://purl.obolibrary.org/obo/hp.owl")
                            .setVersion("hp/releases/2019-11-08")
                            .setNamespacePrefix("HP")
                            .setIriPrefix("http://purl.obolibrary.org/obo/HP_")
                    )
                    .setPhenopacketSchemaVersion("1.0")
            )
            .build();

    public static final OutputProto.OutputOptions OUTPUT = OutputProto.OutputOptions.newBuilder()
            .setOutputFileName("Pfeiffer-hiphive-exome")
            .setOutputContributingVariantsOnly(false)
            .setNumGenes(0)
            .addAllOutputFormats(List.of("HTML", "JSON", "TSV_GENE", "TSV_VARIANT", "VCF"))
            .build();

    public static final OutputProto.OutputOptions DEFAULT_OUTPUT_OPTIONS = OutputProto.OutputOptions.newBuilder()
            .setOutputContributingVariantsOnly(false)
            .setNumGenes(0)
            .addAllOutputFormats(List.of("HTML", "JSON", "PARQUET"))
            .build();

    public static final AnalysisProto.Analysis ANALYSIS = AnalysisProtoBuilder.builder()
            .analysisMode(AnalysisMode.PASS_ONLY)
            .inheritanceModes(InheritanceModeOptions.of(
                    new ImmutableMap.Builder<SubModeOfInheritance, Float>()
                            .put(SubModeOfInheritance.AUTOSOMAL_DOMINANT, 0.1f)
                            .put(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, 2.0f)
                            .put(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_HOM_ALT, 0.1f)
                            .put(SubModeOfInheritance.X_DOMINANT, 0.1f)
                            .put(SubModeOfInheritance.X_RECESSIVE_COMP_HET, 2.0f)
                            .put(SubModeOfInheritance.X_RECESSIVE_HOM_ALT, 0.1f)
                            .put(SubModeOfInheritance.MITOCHONDRIAL, 0.2f)
                            .build()
            ))
            .frequencySources(ImmutableSet.of(
                    // commented out values are legacy/ founder populations and not required.
                    // Why keep them? I don't know, for historical purposes perhaps?
//                    FrequencySource.THOUSAND_GENOMES,
//                    FrequencySource.TOPMED,
                    FrequencySource.UK10K,

//                    FrequencySource.ESP_AA,
//                    FrequencySource.ESP_EA,
//                    FrequencySource.ESP_ALL,

//                    FrequencySource.EXAC_AFRICAN_INC_AFRICAN_AMERICAN,
//                    FrequencySource.EXAC_AMERICAN,
//                    FrequencySource.EXAC_SOUTH_ASIAN,
//                    FrequencySource.EXAC_EAST_ASIAN,
//                    FrequencySource.EXAC_FINNISH,
//                    FrequencySource.EXAC_NON_FINNISH_EUROPEAN,
//                    FrequencySource.EXAC_OTHER,

                    FrequencySource.GNOMAD_E_AFR,
                    FrequencySource.GNOMAD_E_AMR,
                    FrequencySource.GNOMAD_E_EAS,
//                    FrequencySource.GNOMAD_E_FIN,
                    FrequencySource.GNOMAD_E_NFE,
//                    FrequencySource.GNOMAD_E_OTH,
                    FrequencySource.GNOMAD_E_SAS,

                    FrequencySource.GNOMAD_G_AFR,
                    FrequencySource.GNOMAD_G_AMR,
                    FrequencySource.GNOMAD_G_EAS,
//                    FrequencySource.GNOMAD_G_FIN,
                    FrequencySource.GNOMAD_G_NFE,
//                    FrequencySource.GNOMAD_G_OTH,
                    FrequencySource.GNOMAD_G_SAS
            ))
            .pathogenicitySources(ImmutableSet.of(REVEL, MVP, ALPHA_MISSENSE, SPLICE_AI))
            .addFailedVariantFilter()
            .addVariantEffectFilter(ImmutableSet.of(
                    FIVE_PRIME_UTR_EXON_VARIANT,
                    FIVE_PRIME_UTR_INTRON_VARIANT,
                    THREE_PRIME_UTR_EXON_VARIANT,
                    THREE_PRIME_UTR_INTRON_VARIANT,
                    NON_CODING_TRANSCRIPT_EXON_VARIANT,
                    UPSTREAM_GENE_VARIANT,
                    INTERGENIC_VARIANT,
                    REGULATORY_REGION_VARIANT,
                    CODING_TRANSCRIPT_INTRON_VARIANT,
                    NON_CODING_TRANSCRIPT_INTRON_VARIANT,
                    DOWNSTREAM_GENE_VARIANT))
            .addFrequencyFilter(2.0f)
            .addPathogenicityFilter(true)
            .addInheritanceFilter()
            .addOmimPrioritiser()
            .addHiPhivePrioritiser()
            .build();

    public static final JobProto.Job PFEIFFER_SAMPLE_JOB = JobProto.Job.newBuilder()
            .setSample(SAMPLE)
            .setAnalysis(ANALYSIS)
            .setOutputOptions(OUTPUT)
            .build();

    public static final JobProto.Job PFEIFFER_PHENOPACKET_JOB = JobProto.Job.newBuilder()
            .setPhenopacket(PHENOPACKET)
            .setAnalysis(ANALYSIS)
            .setOutputOptions(OUTPUT)
            .build();
}
