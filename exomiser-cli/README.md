# The Exomiser - A Tool to Annotate and Prioritize Disease Variants: Command Line Executable

## *New!* Can now perform genome-wide prioritisation including non-coding, regulatory variants (aka. Genomiser).
See [Analysis file](#analysis_file) section on running analysis yml files for more information and the
test-analysis-genome.yml file located in the base install directory.

## Software and Hardware requirements
 - For exome analysis of a 30,000 variant sample 4GB RAM should suffice.
 - For genome analysis of a 4,400,000 variant sample 12GB RAM should suffice.
 - Any 64-bit operating system
 - Java 8 or above
 - At least 50GB free disk space (SSD preferred for best performance)
 - An internet connection is not required to run the Exomiser, although network access will be required if accessing a
  networked database (optional).
 - By default the Exomiser is completely self-contained and is able to run on standard consumer laptops.

## Installation

1. Download and unzip exomiser-cli-${project.version}-distribution.gz
2. Download exomiser-${project.version}.h2.db.gz from the h2_db_dumps folder
3. Unzip in the exomiser-cli-${project.version}/data directory
4. mv exomiser-${project.version}.h2.db exomiser.h2.db
5. Run the example commands below from the exomiser-cli-${project.version} directory

Windows users should consider using 7-Zip for unzipping .gz files. 

The following shell script should work:
    
    #download, verify and extract the distribution 
    wget ftp://ftp.sanger.ac.uk/pub/resources/software/exomiser/downloads/exomiser/exomiser-cli-${project.version}-distribution.zip
    wget ftp://ftp.sanger.ac.uk/pub/resources/software/exomiser/downloads/exomiser/exomiser-cli-${project.version}-distribution.zip.sha256    
    sha256sum -c exomiser-cli-${project.version}-distribution.sha256
    unzip exomiser-cli-${project.version}-distribution.zip
    
    #dowload and unzip the H2 database, then move it to the installation data directory
    wget ftp://ftp.sanger.ac.uk/pub/resources/software/exomiser/downloads/exomiser/h2_db_dumps/exomiser-${project.version}.h2.db.gz
    gunzip exomiser-${project.version}.h2.db.gz
    mv exomiser-${project.version}.h2.db exomiser-cli-${project.version}/data/exomiser.h2.db
    
    #run a test genomiser analysis
    cd exomiser-cli-${project.version}
    java -Xms2g -Xmx4g -jar exomiser-cli-${project.version}.jar --analysis NA19722_601952_AUTOSOMAL_RECESSIVE_POMP_13_29233225_5UTR_38.yml

This script will download, verify and extract the exomiser files to a directory called 'exomiser-cli-${project.version}' in the users current directory.
It will then change to the the exomiser directory and run the analysis contained in the file 'NA19722_601952_AUTOSOMAL_RECESSIVE_POMP_13_29233225_5UTR_38.yml'. 
This file is an analysis for an autosomal recessive 5'UTR variant located in POMP gene on chromosome 13. The phenotype HPO terms are taken from the clinical synopsis of
OMIM #601952 (http://www.omim.org/clinicalSynopsis/601952) 

## Alternative set-up

If you want to run Exomiser using an H2 database from a location of your choosing edit the line in application.properties:

    h2Path=

with

    h2Path=/full/path/to/alternative/h2/database/exomiser.h2.db

If you want to run from a Postgres database rather than the default H2 embedded database *Optional*
  
1. download exomiser_dump.pg.gz
2. gunzip exomiser_dump.pg.gz
3. load into your postgres server: pg_restore -h yourhost -d yourdatabase -U youruser < exomiser_dump.pg
    You can do 2 and 3 at once by using: gunzip -c exomiser_dump.pg.gz | pg_restore -h yourhost -d yourdatabase -U youruser
4. edit application.properties with the details of how to connect this new database

## Usage

(a) Exomiser hiPHIVE algorithm - phenotype comparisons to human, mouse and fish involving disruption of the gene or nearby genes in the interactome using a RandomWalk

    java -Xms2g -Xmx4g -jar exomiser-cli-${project.version}.jar --prioritiser=hiphive -I AD -F 1 -D OMIM:101600 -v data/Pfeiffer.vcf

    java -Xms2g -Xmx4g -jar exomiser-cli-${project.version}.jar --prioritiser=hiphive -I AD -F 1 --hpo-ids HP:0000006,HP:0000174,HP:0000194,HP:0000218,HP:0000238,HP:0000244,HP:0000272,HP:0000303,HP:0000316,HP:0000322,HP:0000324,HP:0000327,HP:0000348,HP:0000431,HP:0000452,HP:0000453,HP:0000470,HP:0000486,HP:0000494,HP:0000508,HP:0000586,HP:0000678,HP:0001156,HP:0001249,HP:0002308,HP:0002676,HP:0002780,HP:0003041,HP:0003070,HP:0003196,HP:0003272,HP:0003307,HP:0003795,HP:0004209,HP:0004322,HP:0004440,HP:0005048,HP:0005280,HP:0005347,HP:0006101,HP:0006110,HP:0009602,HP:0009773,HP:0010055,HP:0010669,HP:0011304 -v data/Pfeiffer.vcf

(b) Exomiser PHIVE algorithm - phenotype comparisons to mice with disruption of the gene

    java -Xmx2g -jar exomiser-cli-${project.version}.jar --prioritiser=phive -I AD -F 1 -D OMIM:101600 -v data/Pfeiffer.vcf

(c) Exomiser Phenix algorithm - phenotype comparisons to known human disease genes

    java -Xms2g -Xmx4g -jar exomiser-cli-${project.version}.jar --prioritiser=phenix -v data/Pfeiffer.vcf -I AD -F 1 --hpo-ids HP:0000006,HP:0000174,HP:0000194,HP:0000218,HP:0000238,HP:0000244,HP:0000272,HP:0000303,HP:0000316,HP:0000322,HP:0000324,HP:0000327,HP:0000348,HP:0000431,HP:0000452,HP:0000453,HP:0000470,HP:0000486,HP:0000494,HP:0000508,HP:0000586,HP:0000678,HP:0001156,HP:0001249,HP:0002308,HP:0002676,HP:0002780,HP:0003041,HP:0003070,HP:0003196,HP:0003272,HP:0003307,HP:0003795,HP:0004209,HP:0004322,HP:0004440,HP:0005048,HP:0005280,HP:0005347,HP:0006101,HP:0006110,HP:0009602,HP:0009773,HP:0010055,HP:0010669,HP:0011304

(d) Exomiser ExomeWalker algorithm - prioritisation by proximity in interactome to the seed genes

    java -Xms2g -Xmx4g -jar exomiser-cli-${project.version}.jar --prioritiser exomewalker  -v data/Pfeiffer.vcf -I AD -F 1 -S 2260


## Other useful params:

### Multiple output formats:

    --output-format HTML (default)
    --output-format TSV-GENE (TSV summary of genes)
    --output-format TSV-VARIANT (TSV summary of variants)
    --output-format VCF (VCF summary)

Output options can be combined, for example:

    --output-format TSV-GENE,VCF (TSV-GENE and VCF)
    --output-format TSV-GENE, TSV-VARIANT, VCF (TSV-GENE, TSV-VARIANT and VCF)

### <a name="analysis_file"></a>Analysis file:

Analysis files contain all possible options for running an analysis including the ability to specify variant frequency
and pathogenicity data sources and the ability to tweak the order that analysis steps are performed.

    java -Xms2g -Xmx4g -jar exomiser-cli-${project.version}.jar --analysis test-analysis-exome.yml

These files an also be used to run full-genomes, however they will require substantially more RAM to do so. For example
a 4.4 million variant analysis requires approximately 12GB RAM. However, RAM requirements can be substantially reduced by 
setting the analysisMode option to PASS_ONLY.  

Analyses can be run in batch mode. Simply put the path to each analysis file in the batch file - one file path per line.

    java -Xms2g -Xmx4g -jar exomiser-cli-${project.version}.jar --analysis-batch test-analysis-batch.txt

### Settings file:
    
Settings files contain all the parameters passed in on the command-line so you can just point exomiser to a file. See example.settings and test.settings.

    java -Xms2g -Xmx4g -jar exomiser-cli-${project.version}.jar --settings-file test.settings

    
Alternatively you can mix up a settings file and override settings by specifying them on the command line:

    java -Xms2g -Xmx4g -jar exomiser-cli-${project.version}.jar --settings-file test.settings --prioritiser=phenix

    
Settings can also be run in batch mode. Simply put the path to each settings file in the batch file - one file path per line.

    java -Xms2g -Xmx4g -jar exomiser-cli-${project.version}.jar --batch-file batch.txt

### Want help?

    java -jar exomiser-cli-${project.version}.jar --help

## Troubleshooting

### java.lang.UnsupportedClassVersionError:
  If you get the following error message:
  
    Exception in thread "main" java.lang.UnsupportedClassVersionError:
    de/charite/compbio/exomiser/cli/Main : Unsupported major.minor version

  You are running an older unsupported version of Java. Exomiser requires java version 8 or higher. This can be checked by running:
    
    java -version
  
  You should see something like this in response:
    
    java version "1.8.0_65"
    
  versions lower than 1.8 (e.g. 1.5, 1.6 or 1.7) will not run exomiser so you will need to install the latest java version.
  
### Zip file reported as too big or corrupted
  If, when running unzip 'exomiser-cli-${project.version}-distribution.zip', you see the following:
     
    error:  Zip file too big (greater than 4294959102 bytes)
    Archive:  exomiser-cli-${project.version}-distribution.zip
    warning [exomiser-cli-${project.version}-distribution.zip]:  9940454202 extra bytes at beginning or within zipfile
      (attempting to process anyway)
    error [exomiser-cli-${project.version}-distribution.zip]:  start of central directory not found;
      zipfile corrupt.
      (please check that you have transferred or created the zipfile in the
      appropriate BINARY mode and that you have compiled UnZip properly)

  Check that your unzip version was compiled with LARGE_FILE_SUPPORT and ZIP64_SUPPORT. This is standard with UnZip 6.00 and can be checked by typing:
     
    unzip -version
    
  This shouldn't be an issue with more recent linux distributions. 