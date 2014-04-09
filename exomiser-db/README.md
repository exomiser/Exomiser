UCSC known genes:
    http://hgdownload.soe.ucsc.edu/goldenPath/hg19/database/knownGene.txt.gz
    http://hgdownload.soe.ucsc.edu/goldenPath/hg19/database/kgXref.txt.gz
    http://hgdownload.soe.ucsc.edu/goldenPath/hg19/database/knownGeneMrna.txt.gz
    http://hgdownload.soe.ucsc.edu/goldenPath/hg19/database/knownToLocusLink.txt.gz

-- convert to ucsc.ser with jannovar:

ant annotator

java -Xmx2G -jar Annotator.jar -U knownGene.txt -M knownGeneMrna.txt -X kgXref.txt -L knownToLocusLink.txt -S ucsc.ser
or...
java -Xms1G -Xmx2G -jar Jannovar.jar --create-ucsc


Exomiser tables:

dbSNP
    ftp://ftp.ncbi.nlm.nih.gov/snp/organisms/human_9606/VCF/00-All.vcf.gz

ESP
    http://evs.gs.washington.edu/EVS/
    http://evs.gs.washington.edu/evs_bulk_data/ESP6500SI-V2-SSA137.protein-hgvs-update.snps_indels.vcf.tar.gz

PopulateExomiserDatabase(ESP, dbSNP, ucsc.ser) -> frequency.pg


HPO
   http://compbio.charite.de/hudson/job/hpo/lastStableBuild/artifact/ontology/release/hp.obo 

dbNSFP
    http://dbnsfp.houstonbioinformatics.org/dbNSFPzip/dbNSFPv2.3.zip (4.4GB)

OMIM

PhenoDigm dependencies:
    mouse_gene_ortholog
    mouse_gene_level_summary
    fish_gene_ortholog
    fish_gene_level_summmary
    mgi_mp (model_gene_id, model_gene_symbol, model_id, mp_id)
    hp_mp_mapping

    
PhenIX + Panelizer extra tables
'clinvar' 
    http://www.ncbi.nlm.nih.gov/clinvar ftp://ftp.ncbi.nlm.nih.gov/pub/clinvar/tab_delimited/variant_summary.txt.gz ()

HGMDdisease
HGMDpro 
