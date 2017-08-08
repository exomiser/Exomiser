# The Exomiser - A Tool to Annotate and Prioritize Disease Variants: Command Line Executable

The Exomiser is a tool to perform genome-wide prioritisation of genomic variants including non-coding and regulatory variants using patient phenotypes as a means of differentiating candidate genes.
 
To perform ana analysis, Exomiser requires the patient's genome/exome in VCF format and their phenotype encoded in HPO terms. The exomiser is also capable of analysing trios/small family genomes, so long as a pedigree in PED format is also provided. 
See [Usage](#usage) section for info on running an analysis.

Further information can be found in the [online documentation](http://exomiser.github.io/Exomiser/).

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

Windows:
    
 1. Install 7-Zip (http://www.7-zip.org) for unzipping the archive files. The built-in archiving software has issues extracting the zip files. 
 2. Download the data and distribution files from the FTP site.
 3. Extract the distribution files by right-clicking exomiser-cli-${project.version}-distribution.zip and selecting 7-Zip > Extract Here
 4. Extract the data files by right-clicking exomiser-cli-${project.version}-data.zip and selecting 7-Zip > Extract Here
   4.1 Allow 7-Zip to overwite any empty data files with the full versions if prompted (remmData for example) 
 5. cd exomiser-cli-${project.version}
 6. java -Xms2g -Xmx4g -jar exomiser-cli-${project.version}.jar --analysis examples/test-analysis-exome.yml
 
Linux: 
The following shell script should work-
    
    #download the distribution (won't take long)
    wget https://data.monarchinitiative.org/exomiser/exomiser-cli-${project.version}-distribution.zip
    #download the data (this is ~20GB and will take a while)
    wget https://data.monarchinitiative.org/exomiser/exomiser-cli-${project.version}-data.zip

    #download the checksums and verify the files (optional)
    wget https://data.monarchinitiative.org/exomiser/exomiser-cli-${project.version}.sha256    
    sha256sum -c exomiser-cli-${project.version}.sha256
    
    #unzip the distribution and data files - this will create a directory called 'exomiser-cli-${project.version}' in the current working directory
    unzip exomiser-cli-${project.version}-distribution.zip
    unzip exomiser-cli-${project.version}-data.zip

    #run a test genomiser analysis
    cd exomiser-cli-${project.version}
    java -Xms2g -Xmx2g -jar exomiser-cli-${project.version}.jar --analysis examples/test-analysis-exome.yml

This script will download, verify and extract the exomiser files and then run the analysis contained in the file 'test-analysis-exome.yml' from the examples sub-directory. This contains a known pathogenic missense variant in the FGFR2 gene.

## Genomiser data files

In order to run the Genomiser you will also need to download the REMM data file from [here](https://charite.github.io/software-remm-score.html). Once downloaded you'll need to add the path to the ReMM.v0.3.1.tsv.gz file to the ```application.properties``` file. For example if you downloaded the file to the exomiser data directory you could add the entry like this:

    exomiser.remm-path=${exomiser.data-directory}/ReMM.v0.3.1.tsv.gz
 
If this step is omitted, the application will throw and error and stop any analysis which defines ```REMM``` in the ```pathogenicitySources``` section of an analysis yml file. 

Having done this, run the analysis like this:
 
    java -Xms4g -Xmx6g -jar exomiser-cli-${project.version}.jar --analysis examples/NA19722_601952_AUTOSOMAL_RECESSIVE_POMP_13_29233225_5UTR_38.yml 

This is an analysis for an autosomal recessive 5'UTR variant located in POMP gene on chromosome 13. The phenotype HPO terms are taken from the clinical synopsis of
OMIM #601952 (http://www.omim.org/clinicalSynopsis/601952) 

## Alternative set-up

If you want to run Exomiser using data from a different release directory edit the line in ```application.properties```:

    exomiser.data-directory=

with

    exomiser.data-directory=/full/path/to/alternative/data/directory

## <a name="usage"></a>Usage

The Exomiser can be run via simply via command line switches (see cli only) or via a yaml analysis file. We strongly recommended using the yaml option as it provides full control over the application. The cli only options are currently a legacy hangover *only* capable of exome analysis.    

### Analysis file (recommended)

Analysis files contain all possible options for running an analysis including the ability to specify variant frequency
and pathogenicity data sources and the ability to tweak the order that analysis steps are performed. 

See the test-analysis-exome.yml and test-analysis-genome.yml files located in the base install directory for details.

    java -Xms2g -Xmx4g -jar exomiser-cli-${project.version}.jar --analysis examples/test-analysis-exome.yml

These files an also be used to run full-genomes, however they will require substantially more RAM to do so. For example
a 4.4 million variant analysis requires approximately 12GB RAM. However, RAM requirements can be greatly reduced by 
setting the analysisMode option to PASS_ONLY. This will also aid your ability to evaluate the results.

Analyses can be run in batch mode. Simply put the path to each analysis file in the batch file - one file path per line.

    java -Xms2g -Xmx4g -jar exomiser-cli-${project.version}.jar --analysis-batch examples/test-analysis-batch.txt

### CLI only (limited to exome analysis only)

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

### Settings file (deprecated)

This feature is now deprecated and may be subject to removal at a later time. We recommend switching to using an analysis yml file instead.
    
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