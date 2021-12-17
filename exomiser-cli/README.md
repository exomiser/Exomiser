# The Exomiser - A Tool to Annotate and Prioritize Disease Variants: Command Line Executable

The Exomiser is a tool to perform genome-wide prioritisation of genomic variants including non-coding and regulatory variants using patient phenotypes as a means of differentiating candidate genes.
 
To perform an analysis, Exomiser requires the patient's genome/exome in VCF format and their phenotype encoded in HPO
terms. The exomiser is also capable of analysing trios/small family genomes, so long as a pedigree in PED format is also
provided. See [Usage](#usage) section for info on running an analysis.

Further information can be found in the [online documentation](https://exomiser.readthedocs.io/en/latest/).

## Software and Hardware requirements
 - For exome analysis of a 30,000 variant sample 4GB RAM should suffice.
 - For genome analysis of a 4,400,000 variant sample 12GB RAM should suffice.
 - Any 64-bit operating system
 - Java 11 or above
 - At least 50GB free disk space (SSD preferred for best performance)
 - An internet connection is not required to run the Exomiser, although network access will be required if accessing a
  networked database (optional).
 - By default the Exomiser is completely self-contained and is able to run on standard consumer laptops.

## Installation

### Windows

1. Install 7-Zip (http://www.7-zip.org) for unzipping the archive files. The built-in archiving software has issues
   extracting the zip files.
2. Download the data and distribution files from ```https://data.monarchinitiative.org/exomiser/latest```
3. Extract the distribution files by right-clicking exomiser-cli-${project.version}-distribution.zip and selecting
   7-Zip > Extract Here
4. Extract the data files (e.g. 1711_phenotype.zip, 1711_hg19.zip) by right-clicking the archive and selecting 7-Zip >
   Extract files... 4.1 Extract the files to the exomiser data directory. By default exomiser expects this to
   be ```exomiser-cli-${project.version}/data```, but this can be changed in the ```application.properties```
5. cd exomiser-cli-${project.version}
6. java -Xms2g -Xmx4g -jar exomiser-cli-${project.version}.jar --analysis examples/test-analysis-exome.yml

### Linux

The following shell script should work-

    # download the distribution (won't take long)
    wget https://data.monarchinitiative.org/exomiser/latest/exomiser-cli-${project.version}-distribution.zip
    # download the data (this is ~20GB and will take a while)
    wget https://data.monarchinitiative.org/exomiser/latest/2109_hg19.zip
    wget https://data.monarchinitiative.org/exomiser/latest/2109_hg38.zip
    wget https://data.monarchinitiative.org/exomiser/latest/2109_phenotype.zip

    # unzip the distribution and data files - this will create a directory called 'exomiser-cli-${project.version}' in the current working directory
    unzip exomiser-cli-${project.version}-distribution.zip
    unzip 2109_*.zip -d exomiser-cli-${project.version}/data

    # Check the application.properties are pointing to the correct versions:
    #  exomiser.hg19.data-version=2109
    #  exomiser.hg38.data-version=2109
    #  exomiser.phenotype.data-version=2109
    
    # run a test exome analysis
    cd exomiser-cli-${project.version}
    java -jar exomiser-cli-${project.version}.jar --analysis examples/test-analysis-exome.yml

This script will download, verify and extract the exomiser files and then run the analysis contained in the file 'test-analysis-exome.yml' from the examples sub-directory. This contains a known pathogenic missense variant in the FGFR2 gene.

## Genomiser data files

In order to run the Genomiser you will also need to download the REMM data file
from [here](https://zenodo.org/record/4768448). Once downloaded you'll need to add the path to the ReMM.v0.3.1.tsv.gz
file to the ```application.properties``` file. For example if you downloaded the file to the exomiser data directory you
could add the entry like this:

    exomiser.hg19.remm-path=${exomiser.hg19.data-directory}/ReMM.v0.3.1.tsv.gz
 
If this step is omitted, the application will throw and error and stop any analysis which defines ```REMM``` in the ```pathogenicitySources``` section of an analysis yml file. 

Having done this, run the analysis like this:

    java -Xmx6g -jar exomiser-cli-${project.version}.jar --analysis examples/NA19722_601952_AUTOSOMAL_RECESSIVE_POMP_13_29233225_5UTR_38.yml 

This is an analysis for an autosomal recessive 5'UTR variant located in POMP gene on chromosome 13. The phenotype HPO terms are taken from the clinical synopsis of
OMIM #601952 (http://www.omim.org/clinicalSynopsis/601952) 

## Alternative set-up

If you want to run Exomiser using data from a different release directory edit the line in ```application.properties```:

    exomiser.data-directory=

with

    exomiser.data-directory=/full/path/to/alternative/data/directory


## Running from alternate directory

If you're running the exomiser from a different directory to the one the ```exomiser-cli-${project.version}.jar``` is located you'll need to add the command 

    --spring.config.location=/full/path/to/your/exomiser-cli/directory
    
to the end of your command-line arguments. *n.b.* the ```spring.config.location``` command *must be the last argument in the input commands*  
## <a name="usage"></a>Usage

The Exomiser can be run via simply via a yaml analysis file. The extended cli capability was removed in version 10.0.0 as this was less capable than the yaml scripts and only supported hg19 exome analysis.

### Analysis file

Analysis files contain all possible options for running an analysis including the ability to specify variant frequency
and pathogenicity data sources and the ability to tweak the order that analysis steps are performed. 

See the test-analysis-exome.yml and test-analysis-genome.yml files located in the base install directory for details.

    java -Xmx4g -jar exomiser-cli-${project.version}.jar --analysis examples/test-analysis-exome.yml

These files an also be used to run full-genomes, however they will require substantially more RAM to do so. For example
a 4.4 million variant analysis requires approximately 12GB RAM. However, RAM requirements can be greatly reduced by 
setting the analysisMode option to PASS_ONLY. This will also aid your ability to evaluate the results.

Analyses can be run in batch mode. Simply put the path to each analysis file in the batch file - one file path per line.

    java -Xmx4g -jar exomiser-cli-${project.version}.jar --analysis-batch examples/test-analysis-batch.txt
    
If you're running the exomiser from a different directory to the one the jar file is located in, you will need to specify the path to the ```application.properties``` file in the start-up command. For example:

     java -Xmx4g -jar $path_to_exomiser/exomiser-cli-${project.version}.jar --analysis $path_to_exomiser/examples/test-analysis-exome.yml --spring.config.location=$path_to_exomiser/application.properties

    
### Want help?

    java -jar exomiser-cli-${project.version}.jar --help

## Troubleshooting

### java.lang.UnsupportedClassVersionError:

If you get the following error message:

    Exception in thread "main" java.lang.UnsupportedClassVersionError:
    org/monarchinitiative/exomiser/cli/Main : Unsupported major.minor version

You are running an older unsupported version of Java. Exomiser requires java version 11 or higher. This can be checked
by running:

    java -version

You should see something like this in response:

    openjdk version "11.0.11" 2021-04-20
    OpenJDK Runtime Environment (build 11.0.11+9-Ubuntu-0ubuntu2.20.04)
    OpenJDK 64-Bit Server VM (build 11.0.11+9-Ubuntu-0ubuntu2.20.04, mixed mode, sharing)

versions lower than 11 (e.g. 1.5, 1.6, 1.7, 1.8, 9, 10) will not run exomiser, so you will need to install the latest
java version.

### Zip file reported as too big or corrupted

If, when running 'unzip exomiser-cli-${project.version}-distribution.zip', you see the following:

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