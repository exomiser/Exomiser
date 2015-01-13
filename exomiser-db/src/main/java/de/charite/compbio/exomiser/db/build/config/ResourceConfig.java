/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.db.build.config;

import de.charite.compbio.exomiser.db.build.parsers.ClinVarParser;
import de.charite.compbio.exomiser.db.build.parsers.DbSnpFrequencyParser;
import de.charite.compbio.exomiser.db.build.parsers.DiseaseInheritanceCache;
import de.charite.compbio.exomiser.db.build.parsers.EntrezParser;
import de.charite.compbio.exomiser.db.build.parsers.EspFrequencyParser;
import de.charite.compbio.exomiser.db.build.parsers.HPOOntologyFileParser;
import de.charite.compbio.exomiser.db.build.parsers.MetaDataParser;
import de.charite.compbio.exomiser.db.build.parsers.MimToGeneParser;
import de.charite.compbio.exomiser.db.build.parsers.MorbidMapParser;
import de.charite.compbio.exomiser.db.build.parsers.NSFP2SQLDumpParser;
import de.charite.compbio.exomiser.db.build.parsers.Omim2GeneParser;
import de.charite.compbio.exomiser.db.build.parsers.OmimResourceGroupParser;
import de.charite.compbio.exomiser.db.build.parsers.PhenoSeriesParser;
import de.charite.compbio.exomiser.db.build.parsers.ResourceGroupParser;
import de.charite.compbio.exomiser.db.build.parsers.StringParser;
import de.charite.compbio.exomiser.db.build.parsers.StringResourceGroupParser;
import de.charite.compbio.exomiser.db.build.parsers.VariantFrequencyResourceGroupParser;
import de.charite.compbio.exomiser.db.build.resources.Resource;
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
    public Set<Resource> resources() {
        if (resources == null) {
            logger.info("Making new set of Resources");
            resources = new LinkedHashSet();
//            resources.add(exampleResource());
            resources.add(hpoResource());
            //OMIM group
            resources.add(omimMimToGeneResource());
            resources.add(omimMorbidMapResource());
            resources.add(hpoPhenotypeAnnotationsResource());
            //StringDB group
            resources.add(stringEntrezToSymResource());
            resources.add(stringProteinLinksResource());
            //Exome Walker
            resources.add(exomeWalkerPhenotypicSeriesResource());
            resources.add(exomeWalkerOmimToGeneResource());
            
            resources.add(clinVarResource());
            resources.add(metaDataResource());
            
            //these ones are biggies:
            resources.add(dbNsfpResource());
            //VariantFrequency group
            resources.add(dbSnpResource());
            resources.add(espResource());
            resources.add(hgResource());

            //UCSC resource files for making the ucsc.ser file - 
            //these are download only
            resources.add(ucscKnownGeneResource());
            resources.add(ucscKnownGeneXrefResource());
            resources.add(ucscKnownGeneMrnaResource());
            resources.add(ucscKnownToLocusLinkResource());       
        }
        
        return resources;
        
    }
    
    @Bean
    public Resource exampleResource() {
        logger.info("Making example resource");
        Resource resource = new Resource("Example");
        resource.setUrl("");
        resource.setRemoteFileName("");
        resource.setVersion("");
        resource.setExtractedFileName("");
        resource.setExtractionScheme("copy");
        //
        resource.setParserClass(null);
        resource.setParsedFileName("");
        //
        resource.setResourceGroupName("");
        resource.setResourceGroupParserClass(null);
        
        return resource;
    } 

    
    @Bean
    public Resource hpoResource() {
        logger.info("Making HPO resource");
        Resource resource = new Resource("HPO");
        resource.setUrl("http://compbio.charite.de/hudson/job/hpo/lastStableBuild/artifact/hp/");
        resource.setRemoteFileName("hp.obo");
        resource.setVersion("");
        resource.setExtractedFileName("hpo.obo");
        resource.setExtractionScheme("copy");
        //parsing
        resource.setParserClass(HPOOntologyFileParser.class);
        resource.setParsedFileName("hpo.pg");
        //resource groups
        resource.setResourceGroupName("");
        resource.setResourceGroupParserClass(null);
        
        return resource;
    } 
    
    @Bean
    public Resource dbNsfpResource() {
        logger.info("Making dbNSFP resource");
        Resource resource = new Resource("dbNSFP");
        // this is a 4.4GB file so might take a while
        // it is also about 25GB when uncompresssed
        resource.setUrl("http://dbnsfp.houstonbioinformatics.org/dbNSFPzip/");
        resource.setRemoteFileName("dbNSFPv2.6.zip");
        resource.setVersion("2.6");
        resource.setExtractedFileName("dbNSFPv2.6.zip");
        resource.setExtractionScheme("copy");
        //parsing
        resource.setParserClass(NSFP2SQLDumpParser.class);
        resource.setParsedFileName("variant.pg");
        //
        resource.setResourceGroupName("");
        resource.setResourceGroupParserClass(null);
        
        return resource;
    } 
    
    @Bean
    public Resource dbSnpResource() {
        logger.info("Making dbSNP resource");
        Resource resource = new Resource("dbSNP");
        resource.setUrl("ftp://ftp.ncbi.nlm.nih.gov/snp/organisms/human_9606_b141_GRCh37p13/VCF/");//once all switched to b38 and 1000g frequencies in should set back to human_9606 default
        resource.setRemoteFileName("00-All.vcf.gz");
        resource.setVersion("00");
        resource.setExtractedFileName("dbSNP_00-All.vcf");
        resource.setExtractionScheme("copy"); //can also do a gz 
        //parsing
        resource.setParserClass(DbSnpFrequencyParser.class);
        resource.setParsedFileName("frequency.pg");
        //resource groups
        resource.setResourceGroupName(VariantFrequencyResourceGroupParser.NAME);
        resource.setResourceGroupParserClass(VariantFrequencyResourceGroupParser.class);
        
        return resource;
    }
    
    @Bean
    public Resource espResource() {
        logger.info("Making ESP resource");
        Resource resource = new Resource("ESP");
        resource.setUrl("http://evs.gs.washington.edu/evs_bulk_data/");
        resource.setRemoteFileName("ESP6500SI-V2-SSA137.protein-hgvs-update.snps_indels.vcf.tar.gz");
        resource.setVersion("");
        resource.setExtractedFileName("ESP_snps_indels");
        resource.setExtractionScheme("tgz");
        //parsing
        resource.setParserClass(EspFrequencyParser.class);
        resource.setParsedFileName("frequency.pg");
        //resource groups
        resource.setResourceGroupName(VariantFrequencyResourceGroupParser.NAME);
        resource.setResourceGroupParserClass(VariantFrequencyResourceGroupParser.class);
        
        return resource;
    }
    
    @Bean
    public Resource hgResource() {
        logger.info("Making UCSC_HG19 resource");
        Resource resource = new Resource("UCSC_HG19");
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
        resource.setResourceGroupName(VariantFrequencyResourceGroupParser.NAME);
        resource.setResourceGroupParserClass(VariantFrequencyResourceGroupParser.class);
        
        return resource;
    } 

    @Bean
    public Resource omimMimToGeneResource() {
        logger.info("Making OMIM_mim2gene resource");
        Resource resource = new Resource("OMIM_mim2gene");
        resource.setUrl("ftp://ftp.omim.org/OMIM/");
        resource.setRemoteFileName("mim2gene.txt");
        resource.setVersion(null);
        resource.setExtractedFileName("OMIM_mim2gene.txt");
        resource.setExtractionScheme("copy");
        //
        resource.setParserClass(MimToGeneParser.class);
        resource.setParsedFileName("omim.pg");
        //part of the OMIM ResourceGroup
        resource.setResourceGroupName(OmimResourceGroupParser.NAME);
        resource.setResourceGroupParserClass(OmimResourceGroupParser.class);
        
        return resource;
    }
    
    @Bean
    public Resource omimMorbidMapResource() {
        logger.info("Making OMIM_morbidmap resource");
        Resource resource = new Resource("OMIM_morbidmap");
        resource.setUrl("ftp://ftp.omim.org/OMIM/");
        resource.setRemoteFileName("morbidmap");
        resource.setVersion(null);
        resource.setExtractedFileName("OMIM_morbidmap.txt");
        resource.setExtractionScheme("copy");
        //
        resource.setParserClass(MorbidMapParser.class);
        resource.setParsedFileName("omim.pg");
        //part of the OMIM ResourceGroup
        resource.setResourceGroupName(OmimResourceGroupParser.NAME);
        resource.setResourceGroupParserClass(OmimResourceGroupParser.class);
        
        return resource;
    }
    
    @Bean
    public Resource hpoPhenotypeAnnotationsResource() {
        logger.info("Making HPO_phenotype_annotations resource");
        Resource resource = new Resource("HPO_phenotype_annotations");
        resource.setUrl("http://compbio.charite.de/hudson/job/hpo.annotations/lastStableBuild/artifact/misc/");
        resource.setRemoteFileName("phenotype_annotation.tab");
        resource.setVersion(null);
        resource.setExtractedFileName("phenotype_annotation.tab");
        resource.setExtractionScheme("copy");
        //
        resource.setParserClass(DiseaseInheritanceCache.class);
        resource.setParsedFileName("omim.pg");
        //part of the OMIM ResourceGroup
        resource.setResourceGroupName(OmimResourceGroupParser.NAME);
        resource.setResourceGroupParserClass(OmimResourceGroupParser.class);
        
        return resource;
    }
    
    @Bean
    public Resource stringEntrezToSymResource() {
        logger.info("Making STRING_entrez2sym resource");
        Resource resource = new Resource("STRING_entrez2sym");
        resource.setUrl("http://www.ensembl.org/biomart/martservice?query=%3C?xml%20version=%221.0%22%20encoding=%22UTF-8%22?%3E%20%3C!DOCTYPE%20Query%3E%20%3CQuery%20%20virtualSchemaName%20=%20%22default%22%20formatter%20=%20%22TSV%22%20header%20=%20%220%22%20uniqueRows%20=%20%220%22%20count%20=%20%22%22%20datasetConfigVersion%20=%20%220.6%22%20%3E%20%20%3CDataset%20name%20=%20%22hsapiens_gene_ensembl%22%20interface%20=%20%22default%22%20%3E%20%3CAttribute%20name%20=%20%22ensembl_peptide_id%22%20/%3E%20%3CAttribute%20name%20=%20%22entrezgene%22%20/%3E%20%3CAttribute%20name%20=%20%22hgnc_symbol%22%20/%3E%20%3C/Dataset%3E%20%3C/");
        resource.setRemoteFileName("Query%3E");
        resource.setVersion(null);
        resource.setExtractedFileName("ensembl_biomart.txt");
        resource.setExtractionScheme("copy");
        //
        resource.setParserClass(EntrezParser.class);
        resource.setParsedFileName("entrez2sym.pg");
        //
        resource.setResourceGroupName(StringResourceGroupParser.NAME);
        resource.setResourceGroupParserClass(StringResourceGroupParser.class);
        
        return resource;
    }
    
    @Bean
    public Resource stringProteinLinksResource() {
        logger.info("Making STRING_protein_links resource");
        Resource resource = new Resource("STRING_protein_links");
        resource.setUrl("http://string-db.org/newstring_download/protein.links.v9.1/");
        resource.setRemoteFileName("9606.protein.links.v9.1.txt.gz");
        resource.setVersion("9.1");
        resource.setExtractedFileName("9606.protein.links.v9.1.txt");
        resource.setExtractionScheme("gz");
        //
        resource.setParserClass(StringParser.class);
        resource.setParsedFileName("string.pg");
        //
        resource.setResourceGroupName(StringResourceGroupParser.NAME);
        resource.setResourceGroupParserClass(StringResourceGroupParser.class);
        
        return resource;
    }
    
    @Bean
    public Resource exomeWalkerPhenotypicSeriesResource() {
        logger.info("Making ExomeWalker_phenotypic_series resource");
        Resource resource = new Resource("ExomeWalker_phenotypic_series");
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
    public Resource exomeWalkerOmimToGeneResource() {
        logger.info("Making ExomeWalker_omim2gene resource");
        Resource resource = new Resource("ExomeWalker_omim2gene");
        //pheno2gene.txt is produced from a custom data-dump from OMIM which is then post-processed
        //with some perl scripts using data from the mim2gene_medgen and gene_info files from NCBI
        //(ftp://ftp.ncbi.nlm.nih.gov/gene/DATA) consequently this is treated as static data.
        resource.setUrl("");
        resource.setRemoteFileName("pheno2gene.txt");
        resource.setVersion("");
        resource.setExtractedFileName("pheno2gene.txt");
        resource.setExtractionScheme("copy");
        //
        resource.setParserClass(Omim2GeneParser.class);
        resource.setParsedFileName("omim2gene.pg");
        //
        resource.setResourceGroupName("");
        resource.setResourceGroupParserClass(null);
        
        return resource;
    }
    
    @Bean
    public Resource clinVarResource() {
        logger.info("Making ClinVar resource");
        Resource resource = new Resource("ClinVar");
        resource.setUrl("ftp://ftp.ncbi.nlm.nih.gov/pub/clinvar/tab_delimited/");
        resource.setRemoteFileName("variant_summary.txt.gz");
        resource.setVersion("Feb 13 20:10");
        resource.setExtractedFileName("ClinVar_variant_summary.txt");
        resource.setExtractionScheme("gz");
        //
        resource.setParserClass(ClinVarParser.class);
        resource.setParsedFileName("clinvar.pg");
        //
        resource.setResourceGroupName("");
        resource.setResourceGroupParserClass(null);
        
        return resource;
    }
    
    @Bean
    public Resource metaDataResource() {
        logger.info("Making MetaData resource");
        Resource resource = new Resource("MetaData");
        resource.setUrl("");
        resource.setRemoteFileName("");
        resource.setVersion("");
        resource.setExtractedFileName("");
        resource.setExtractionScheme("");
        //
        resource.setParserClass(MetaDataParser.class);
        resource.setParsedFileName("metadata.pg");
        //
        resource.setResourceGroupName("");
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
    public Resource ucscKnownGeneResource() {
        logger.info("Making UCSC_knownGene resource");
        Resource resource = new Resource("UCSC_knownGene");
        resource.setUrl(UCSC_URL);
        resource.setRemoteFileName("knownGene.txt.gz");
        resource.setVersion("");
        resource.setExtractedFileName("UCSC_knownGene.txt");
        resource.setExtractionScheme("gz");
        //
        resource.setParserClass(null);
        resource.setParsedFileName("");
        //
        resource.setResourceGroupName("");
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
    public Resource ucscKnownGeneXrefResource() {
        logger.info("Making UCSC_knownGeneXref resource");
        Resource resource = new Resource("UCSC_knownGeneXref");
        resource.setUrl(UCSC_URL);
        resource.setRemoteFileName("kgXref.txt.gz");
        resource.setVersion("");
        resource.setExtractedFileName("UCSC_knownGeneXref.txt");
        resource.setExtractionScheme("gz");
        //
        resource.setParserClass(null);
        resource.setParsedFileName("");
        //
        resource.setResourceGroupName("");
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
    public Resource ucscKnownGeneMrnaResource() {
        logger.info("Making UCSC_knownGeneMrna resource");
        Resource resource = new Resource("UCSC_knownGeneMrna");
        resource.setUrl(UCSC_URL);
        resource.setRemoteFileName("knownGeneMrna.txt.gz");
        resource.setVersion(null);
        resource.setExtractedFileName("UCSC_knownGeneMrna.txt");
        resource.setExtractionScheme("gz");
        //
        resource.setParserClass(null);
        resource.setParsedFileName("");
        //
        resource.setResourceGroupName("");
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
    public Resource ucscKnownToLocusLinkResource() {
        logger.info("Making UCSC_knownGeneToLocusLink resource");
        Resource resource = new Resource("UCSC_knownGeneToLocusLink");
        resource.setUrl(UCSC_URL);
        resource.setRemoteFileName("knownToLocusLink.txt.gz");
        resource.setVersion(null);
        resource.setExtractedFileName("UCSC_knownGeneToLocusLink.txt");
        resource.setExtractionScheme("gz");
        //
        resource.setParserClass(null);
        resource.setParsedFileName("");
        //
        resource.setResourceGroupName("");
        resource.setResourceGroupParserClass(null);
        
        return resource;
    }
 
}
