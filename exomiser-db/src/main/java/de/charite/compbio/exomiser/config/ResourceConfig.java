/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.config;

import de.charite.compbio.exomiser.parsers.ClinVarParser;
import de.charite.compbio.exomiser.parsers.DbSnpFrequencyParser;
import de.charite.compbio.exomiser.parsers.DiseaseInheritanceCache;
import de.charite.compbio.exomiser.parsers.EntrezParser;
import de.charite.compbio.exomiser.parsers.EspFrequencyParser;
import de.charite.compbio.exomiser.parsers.HPOOntologyFileParser;
import de.charite.compbio.exomiser.parsers.MetaDataParser;
import de.charite.compbio.exomiser.parsers.MimToGeneParser;
import de.charite.compbio.exomiser.parsers.MorbidMapParser;
import de.charite.compbio.exomiser.parsers.NSFP2SQLDumpParser;
import de.charite.compbio.exomiser.parsers.Omim2GeneParser;
import de.charite.compbio.exomiser.parsers.OmimResourceGroupParser;
import de.charite.compbio.exomiser.parsers.PhenoSeriesParser;
import de.charite.compbio.exomiser.parsers.ResourceGroupParser;
import de.charite.compbio.exomiser.parsers.StringParser;
import de.charite.compbio.exomiser.parsers.StringResourceGroupParser;
import de.charite.compbio.exomiser.parsers.VariantFrequencyResourceGroupParser;
import de.charite.compbio.exomiser.resources.Resource;
import java.util.LinkedHashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Java configuration for providing {@code Resource} definitions for the 
 * application to work on.
 * 
 * If you want to add another data source, this is where you define the resource
 * and the application will pick it up and incorporate the data into the database.
 * Obviously you'll have to write a parser and a flyway migration to get the data 
 * into the database.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
public class ResourceConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(ResourceConfig.class);
    
    private static final String UCSC_URL = "ftp://hgdownload.soe.ucsc.edu/goldenPath/hg19/database/";
    
    private Set<Resource> resources;
    
    @Bean
    public Set<Resource> getResources() {
        if (resources == null) {
            logger.info("Making new set of Resources");
            resources = new LinkedHashSet();
//            resources.add(getExampleResource());
            resources.add(getHpoResource());
            //OMIM group
            resources.add(getOmimMimToGeneResource());
            resources.add(getOmimMorbidMapResource());
            resources.add(getHpoPhenotypeAnnotationsResource());
            //StringDB group
            resources.add(getStringEntrezToSymResource());
            resources.add(getStringProteinLinksResource());
            //Exome Walker
            resources.add(getExomeWalkerPhenotypicSeriesResource());
            resources.add(getExomeWalkerOmimToGeneResource());
            
            resources.add(getClinVarResource());
            resources.add(getMetaDataResource());
            
            //these ones are biggies:
            resources.add(getDbNsfpResource());
            //VariantFrequency group
            resources.add(getDbSnpResource());
            resources.add(getEspResource());
            resources.add(ucscHgResource());

            //UCSC resource files for making the ucsc.ser file - 
            //these are download only
            resources.add(getUcscKnownGeneResource());
            resources.add(getUcscKnownGeneXrefResource());
            resources.add(getUcscKnownGeneMrnaResource());
            resources.add(getUcscKnownToLocusLinkResource());
       
        }
        
        return resources;
        
    }
    
    @Bean
    public Resource getExampleResource() {
        logger.info("Making example resource");
        Resource resource = new Resource();
        resource.setName("Example");
        resource.setUrl("");
        resource.setRemoteFileName("");
        resource.setVersion(null);
        resource.setExtractedFileName("");
        resource.setExtractionScheme("copy");
        //
        resource.setParserClass(null);
        resource.setParsedFileName("");
        //
        resource.setResourceGroupName(null);
        resource.setResourceGroupParserClass(null);
        
        return resource;
    } 

    
    @Bean
    public Resource getHpoResource() {
        logger.info("Making HPO resource");
        Resource resource = new Resource();
        resource.setName("HPO");
        resource.setUrl("http://compbio.charite.de/hudson/job/hpo/lastStableBuild/artifact/ontology/release/");
        resource.setRemoteFileName("hp.obo");
        resource.setVersion(null);
        resource.setExtractedFileName("hpo.obo");
        resource.setExtractionScheme("copy");
        //TODO: this ought to return an actual class or instantiated object?
        resource.setParserClass(HPOOntologyFileParser.class);
        resource.setParsedFileName("hpo.pg");
        //
        resource.setResourceGroupName(null);
        resource.setResourceGroupParserClass(null);
        
        return resource;
    } 
    
    @Bean
    public Resource getDbNsfpResource() {
        logger.info("Making dbNSFP resource");
        Resource resource = new Resource();
        // this is a 4.4GB file so might take a while
        // it is also about 25GB when uncompresssed
        resource.setName("dbNSFP");
        resource.setUrl("http://dbnsfp.houstonbioinformatics.org/dbNSFPzip/");
        resource.setRemoteFileName("dbNSFPv2.4.zip");
        resource.setVersion("2.4");
        resource.setExtractedFileName("dbNSFPv2.4.zip");
        resource.setExtractionScheme("copy");
        //TODO: this ought to return an actual class or instantiated object?
        resource.setParserClass(NSFP2SQLDumpParser.class);
        resource.setParsedFileName("variant.pg");
        //
        resource.setResourceGroupName(null);
        resource.setResourceGroupParserClass(null);
        
        return resource;
    } 
    
    @Bean
    public Resource getDbSnpResource() {
        logger.info("Making dbSNP resource");
        Resource resource = new Resource();
        resource.setName("dbSNP");
        resource.setUrl("ftp://ftp.ncbi.nlm.nih.gov/snp/organisms/human_9606/VCF/");
        resource.setRemoteFileName("00-All.vcf.gz");
        resource.setVersion("00");
        resource.setExtractedFileName("dbSNP_00-All.vcf");
        resource.setExtractionScheme("copy"); //can also do a gz 
        //TODO: this ought to return an actual class or instantiated object?
        resource.setParserClass(DbSnpFrequencyParser.class);
        resource.setParsedFileName("frequency.pg");
        //TODO: define groups seperately using DI
        resource.setResourceGroupName("frequency");
        resource.setResourceGroupParserClass(VariantFrequencyResourceGroupParser.class);
        
        return resource;
    }
    
    @Bean
    public Resource getEspResource() {
        logger.info("Making ESP resource");
        Resource resource = new Resource();
        resource.setName("ESP");
        resource.setUrl("http://evs.gs.washington.edu/evs_bulk_data/");
        resource.setRemoteFileName("ESP6500SI-V2-SSA137.protein-hgvs-update.snps_indels.vcf.tar.gz");
        resource.setVersion(null);
        resource.setExtractedFileName("ESP_snps_indels");
        resource.setExtractionScheme("tgz");
        //TODO: this ought to return an actual class or instantiated object?
        resource.setParserClass(EspFrequencyParser.class);
        resource.setParsedFileName("frequency.pg");
        //TODO: define groups seperately using DI
        resource.setResourceGroupName("frequency");
        resource.setResourceGroupParserClass(VariantFrequencyResourceGroupParser.class);
        
        return resource;
    }
    
    @Bean
    public Resource ucscHgResource() {
        logger.info("Making UCSC_HG19 resource");
        Resource resource = new Resource();
        resource.setName("UCSC_HG19");
        //this file is contained in the resources package as it needs to be made manually with Jannovar
        resource.setUrl(null); 
        resource.setRemoteFileName("ucsc_hg19.ser.gz");
        resource.setVersion("hg19");
        resource.setExtractedFileName("ucsc_hg19.ser");
        resource.setExtractionScheme("gz");
        //
        resource.setParserClass(null);
        resource.setParsedFileName(null);
        //
        resource.setResourceGroupName("frequency");
        resource.setResourceGroupParserClass(VariantFrequencyResourceGroupParser.class);
        
        return resource;
    } 

    @Bean
    public Resource getOmimMimToGeneResource() {
        logger.info("Making OMIM_mim2gene resource");
        Resource resource = new Resource();
        resource.setName("OMIM_mim2gene");
        resource.setUrl("ftp://ftp.omim.org/OMIM/");
        resource.setRemoteFileName("mim2gene.txt");
        resource.setVersion(null);
        resource.setExtractedFileName("OMIM_mim2gene.txt");
        resource.setExtractionScheme("copy");
        //
        resource.setParserClass(MimToGeneParser.class);
        resource.setParsedFileName("omim.pg");
        //
        resource.setResourceGroupName("omim");
        resource.setResourceGroupParserClass(OmimResourceGroupParser.class);
        
        return resource;
    }
    
    @Bean
    public Resource getOmimMorbidMapResource() {
        logger.info("Making OMIM_morbidmap resource");
        Resource resource = new Resource();
        resource.setName("OMIM_morbidmap");
        resource.setUrl("ftp://ftp.omim.org/OMIM/");
        resource.setRemoteFileName("morbidmap");
        resource.setVersion(null);
        resource.setExtractedFileName("OMIM_morbidmap.txt");
        resource.setExtractionScheme("copy");
        //
        resource.setParserClass(MorbidMapParser.class);
        resource.setParsedFileName("omim.pg");
        //
        resource.setResourceGroupName("omim");
        resource.setResourceGroupParserClass(OmimResourceGroupParser.class);
        
        return resource;
    }
    
    @Bean
    public Resource getHpoPhenotypeAnnotationsResource() {
        logger.info("Making HPO_phenotype_annotations resource");
        Resource resource = new Resource();
        resource.setName("HPO_phenotype_annotations");
        resource.setUrl("http://compbio.charite.de/hudson/job/hpo.annotations/lastStableBuild/artifact/misc/");
        resource.setRemoteFileName("phenotype_annotation.tab");
        resource.setVersion(null);
        resource.setExtractedFileName("phenotype_annotation.tab");
        resource.setExtractionScheme("copy");
        //
        resource.setParserClass(DiseaseInheritanceCache.class);
        resource.setParsedFileName("omim.pg");
        //
        resource.setResourceGroupName("omim");
        resource.setResourceGroupParserClass(OmimResourceGroupParser.class);
        
        return resource;
    }
    
    @Bean
    public Resource getStringEntrezToSymResource() {
        logger.info("Making String_entrez2sym resource");
        Resource resource = new Resource();
        resource.setName("String_entrez2sym");
        resource.setUrl("http://www.ensembl.org/biomart/martservice?query=%3C?xml%20version=%221.0%22%20encoding=%22UTF-8%22?%3E%20%3C!DOCTYPE%20Query%3E%20%3CQuery%20%20virtualSchemaName%20=%20%22default%22%20formatter%20=%20%22TSV%22%20header%20=%20%220%22%20uniqueRows%20=%20%220%22%20count%20=%20%22%22%20datasetConfigVersion%20=%20%220.6%22%20%3E%20%20%3CDataset%20name%20=%20%22hsapiens_gene_ensembl%22%20interface%20=%20%22default%22%20%3E%20%3CAttribute%20name%20=%20%22ensembl_peptide_id%22%20/%3E%20%3CAttribute%20name%20=%20%22entrezgene%22%20/%3E%20%3CAttribute%20name%20=%20%22hgnc_symbol%22%20/%3E%20%3C/Dataset%3E%20%3C/");
        resource.setRemoteFileName("Query%3E");
        resource.setVersion(null);
        resource.setExtractedFileName("ensembl_biomart.txt");
        resource.setExtractionScheme("copy");
        //
        resource.setParserClass(EntrezParser.class);
        resource.setParsedFileName("entrez2sym.pg");
        //
        resource.setResourceGroupName("string");
        resource.setResourceGroupParserClass(StringResourceGroupParser.class);
        
        return resource;
    }
    
    @Bean
    public Resource getStringProteinLinksResource() {
        logger.info("Making String_protein_links resource");
        Resource resource = new Resource();
        resource.setName("String_protein_links");
        resource.setUrl("http://string-db.org/newstring_download/protein.links.v9.1/");
        resource.setRemoteFileName("9606.protein.links.v9.1.txt.gz");
        resource.setVersion("9.1");
        resource.setExtractedFileName("9606.protein.links.v9.1.txt");
        resource.setExtractionScheme("gz");
        //
        resource.setParserClass(StringParser.class);
        resource.setParsedFileName("string.pg");
        //
        resource.setResourceGroupName("string");
        resource.setResourceGroupParserClass(StringResourceGroupParser.class);
        
        return resource;
    }
    
    @Bean
    public Resource getExomeWalkerPhenotypicSeriesResource() {
        logger.info("Making ExomeWalker_phenotypic_series resource");
        Resource resource = new Resource();
        resource.setName("ExomeWalker_phenotypic_series");
        resource.setUrl(null); //this file is contained in the resources package
        //pheno2gene.txt is produced from a custom data-dump from OMIM which is then post-processed
        //with some perl scripts using data from the mim2gene_medgen and gene_info files from NCBI
        //(ftp://ftp.ncbi.nlm.nih.gov/gene/DATA) consequently this is treated as static data.
        resource.setRemoteFileName("pheno2gene.txt");
        resource.setVersion(null);
        resource.setExtractedFileName("pheno2gene.txt");
        resource.setExtractionScheme("copy");
        //
        resource.setParserClass(PhenoSeriesParser.class);
        resource.setParsedFileName("phenoseries.pg");
        //
        resource.setResourceGroupName(null);
        resource.setResourceGroupParserClass(null);
        
        return resource;
    }
    
    @Bean
    public Resource getExomeWalkerOmimToGeneResource() {
        logger.info("Making ExomeWalker_omim2gene resource");
        Resource resource = new Resource();
        resource.setName("ExomeWalker_omim2gene");
        //pheno2gene.txt is produced from a custom data-dump from OMIM which is then post-processed
        //with some perl scripts using data from the mim2gene_medgen and gene_info files from NCBI
        //(ftp://ftp.ncbi.nlm.nih.gov/gene/DATA) consequently this is treated as static data.
        resource.setUrl(null);
        resource.setRemoteFileName("pheno2gene.txt");
        resource.setVersion(null);
        resource.setExtractedFileName("pheno2gene.txt");
        resource.setExtractionScheme("copy");
        //
        resource.setParserClass(Omim2GeneParser.class);
        resource.setParsedFileName("omim2gene.pg");
        //
        resource.setResourceGroupName(null);
        resource.setResourceGroupParserClass(null);
        
        return resource;
    }
    
    @Bean
    public Resource getClinVarResource() {
        logger.info("Making ClinVar resource");
        Resource resource = new Resource();
        resource.setName("ClinVar");
        resource.setUrl("ftp://ftp.ncbi.nlm.nih.gov/pub/clinvar/tab_delimited/");
        resource.setRemoteFileName("variant_summary.txt.gz");
        resource.setVersion("Feb 13 20:10");
        resource.setExtractedFileName("ClinVar_variant_summary.txt");
        resource.setExtractionScheme("gz");
        //
        resource.setParserClass(ClinVarParser.class);
        resource.setParsedFileName("clinvar.pg");
        //
        resource.setResourceGroupName(null);
        resource.setResourceGroupParserClass(null);
        
        return resource;
    }
    
    @Bean
    public Resource getMetaDataResource() {
        logger.info("Making MetaData resource");
        Resource resource = new Resource();
        resource.setName("MetaData");
        resource.setUrl(null);
        resource.setRemoteFileName(null);
        resource.setVersion(null);
        resource.setExtractedFileName("");
        resource.setExtractionScheme(null);
        //
        resource.setParserClass(MetaDataParser.class);
        resource.setParsedFileName("metadata.pg");
        //
        resource.setResourceGroupName(null);
        resource.setResourceGroupParserClass(null);
        
        return resource;
    }
    
    /**
     * Only required for creating the ucsc_hg19.ser resource file with jannovar in order 
     * that the {@code de.charite.compbio.exomiser.parsers.DbSnpFrequencyParser}
     * can filter the variants within known gene coding-regions. 
     * @return 
     */
    @Bean
    public Resource getUcscKnownGeneResource() {
        logger.info("Making UCSC_knownGene resource");
        Resource resource = new Resource();
        resource.setName("UCSC_knownGene");
        resource.setUrl(UCSC_URL);
        resource.setRemoteFileName("knownGene.txt.gz");
        resource.setVersion(null);
        resource.setExtractedFileName("UCSC_knownGene.txt");
        resource.setExtractionScheme("gz");
        //
        resource.setParserClass(null);
        resource.setParsedFileName(null);
        //
        resource.setResourceGroupName(null);
        resource.setResourceGroupParserClass(null);
        
        return resource;
    }
    
    /**
     * Only required for creating the ucsc_hg19.ser resource file with jannovar in order 
     * that the {@code de.charite.compbio.exomiser.parsers.DbSnpFrequencyParser}
     * can filter the variants within known gene coding-regions. 
     * @return 
     */
    @Bean
    public Resource getUcscKnownGeneXrefResource() {
        logger.info("Making UCSC_knownGeneXref resource");
        Resource resource = new Resource();
        resource.setName("UCSC_knownGeneXref");
        resource.setUrl(UCSC_URL);
        resource.setRemoteFileName("kgXref.txt.gz");
        resource.setVersion(null);
        resource.setExtractedFileName("UCSC_knownGeneXref.txt");
        resource.setExtractionScheme("gz");
        //
        resource.setParserClass(null);
        resource.setParsedFileName(null);
        //
        resource.setResourceGroupName(null);
        resource.setResourceGroupParserClass(null);
        
        return resource;
    }
    
    /**
     * Only required for creating the ucsc_hg19.ser resource file with jannovar in order 
     * that the {@code de.charite.compbio.exomiser.parsers.DbSnpFrequencyParser}
     * can filter the variants within known gene coding-regions. 
     * @return 
     */
    @Bean
    public Resource getUcscKnownGeneMrnaResource() {
        logger.info("Making UCSC_knownGeneMrna resource");
        Resource resource = new Resource();
        resource.setName("UCSC_knownGeneMrna");
        resource.setUrl(UCSC_URL);
        resource.setRemoteFileName("knownGeneMrna.txt.gz");
        resource.setVersion(null);
        resource.setExtractedFileName("UCSC_knownGeneMrna.txt");
        resource.setExtractionScheme("gz");
        //
        resource.setParserClass(null);
        resource.setParsedFileName(null);
        //
        resource.setResourceGroupName(null);
        resource.setResourceGroupParserClass(null);
        
        return resource;
    }
    
    /**
     * Only required for creating the ucsc_hg19.ser resource file with jannovar in order 
     * that the {@code de.charite.compbio.exomiser.parsers.DbSnpFrequencyParser}
     * can filter the variants within known gene coding-regions. 
     * @return 
     */
    @Bean
    public Resource getUcscKnownToLocusLinkResource() {
        logger.info("Making UCSC_knownGeneToLocusLink resource");
        Resource resource = new Resource();
        resource.setName("UCSC_knownGeneToLocusLink");
        resource.setUrl(UCSC_URL);
        resource.setRemoteFileName("knownToLocusLink.txt.gz");
        resource.setVersion(null);
        resource.setExtractedFileName("UCSC_knownGeneToLocusLink.txt");
        resource.setExtractionScheme("gz");
        //
        resource.setParserClass(null);
        resource.setParsedFileName(null);
        //
        resource.setResourceGroupName(null);
        resource.setResourceGroupParserClass(null);
        
        return resource;
    }
 
}
