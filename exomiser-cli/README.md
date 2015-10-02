# The Exomiser - A Tool to Annotate and Prioritize Exome Variants: Command Line Executable

## Software and Hardware requirements
 - For exome analysis of a 30,000 variant sample 4GB RAM should suffice.
 - For genome analysis of a 4,400,000 variant sample 12GB RAM should suffice.
 - Any 64-bit operating system
 - Java 8 or above
 - An internet connection is not required to run the Exomiser, although network access will be required if accessing a
  networked database (optional).
 - By default the Exomiser is completely self-contained and is able to run on standard consumer laptops.

## Installation

1. Download and unzip exomiser-cli-${project.version}-distribution.gz
2. Download exomiser-${project.version}.h2.db.gz from the h2_db_dumps folder 
3. Unzip in the exomiser-cli-${project.version}/data directory
4. mv exomiser-${project.version}.h2.db exomiser.h2.db
5. Run the example commands below from the exomiser-cli-${project.version} directory

## Alternative set-up

If you want to run Exomiser using an H2 database from a location of your choosing edit the line in application.properties:

    h2Path=

with

    h2Path=/full/path/to/alternative/h2/database/exomiser.h2.db

(optional) If you want to run from a Postgres database rather than the default H2 embedded database
  
    (a) download exomiser_dump.pg.gz
    (b) gunzip exomiser_dump.pg.gz
    (c) load into your postgres server: pg_restore -h yourhost -d yourdatabase -U youruser < exomiser_dump.pg
    You can do (b) and (c) at once by using: gunzip -c exomiser_dump.pg.gz | pg_restore -h yourhost -d yourdatabase -U youruser
    (d) edit application.properties with the details of how to connect this new database

## Usage

(a) Exomiser v2 - phenotype comparisons to human, mouse and fish involving disruption of the gene or nearby genes in the interactome using a RandomWalk 

    java -Xms2g -Xmx4g -jar exomiser-cli-${project.version}.jar --prioritiser=hiphive -I AD -F 1 -D OMIM:101600 -v data/Pfeiffer.vcf 

    java -Xms2g -Xmx4g -jar exomiser-cli-${project.version}.jar --prioritiser=hiphive -I AD -F 1 --hpo-ids HP:0000006,HP:0000174,HP:0000194,HP:0000218,HP:0000238,HP:0000244,HP:0000272,HP:0000303,HP:0000316,HP:0000322,HP:0000324,HP:0000327,HP:0000348,HP:0000431,HP:0000452,HP:0000453,HP:0000470,HP:0000486,HP:0000494,HP:0000508,HP:0000586,HP:0000678,HP:0001156,HP:0001249,HP:0002308,HP:0002676,HP:0002780,HP:0003041,HP:0003070,HP:0003196,HP:0003272,HP:0003307,HP:0003795,HP:0004209,HP:0004322,HP:0004440,HP:0005048,HP:0005280,HP:0005347,HP:0006101,HP:0006110,HP:0009602,HP:0009773,HP:0010055,HP:0010669,HP:0011304 -v data/Pfeiffer.vcf

(b) Exomiser v1 - phenotype comparisons to mice with disruption of the gene

    java -Xmx2g -jar exomiser-cli-${project.version}.jar --prioritiser=phive -I AD -F 1 -D OMIM:101600 -v data/Pfeiffer.vcf

(c) Phenix - phenotype comparisons to known human disease genes

    java -Xms2g -Xmx4g -jar exomiser-cli-${project.version}.jar --prioritiser=phenix -v data/Pfeiffer.vcf -I AD -F 1 --hpo-ids HP:0000006,HP:0000174,HP:0000194,HP:0000218,HP:0000238,HP:0000244,HP:0000272,HP:0000303,HP:0000316,HP:0000322,HP:0000324,HP:0000327,HP:0000348,HP:0000431,HP:0000452,HP:0000453,HP:0000470,HP:0000486,HP:0000494,HP:0000508,HP:0000586,HP:0000678,HP:0001156,HP:0001249,HP:0002308,HP:0002676,HP:0002780,HP:0003041,HP:0003070,HP:0003196,HP:0003272,HP:0003307,HP:0003795,HP:0004209,HP:0004322,HP:0004440,HP:0005048,HP:0005280,HP:0005347,HP:0006101,HP:0006110,HP:0009602,HP:0009773,HP:0010055,HP:0010669,HP:0011304

(d) ExomeWalker - prioritisation by proximity in interactome to the seed genes

    java -Xms2g -Xmx4g -jar exomiser-cli-${project.version}.jar --prioritiser exomewalker  -v data/Pfeiffer.vcf -I AD -F 1 -S 2260


# Other useful params:

Multiple output formats:

    --output-format HTML (default)
    --output-format TSV-GENE (TSV summary of genes)
    --output-format TSV-VARIANT (TSV summary of variants)
    --output-format VCF (VCF summary)

Output options can be combined, for example:
    --output-format TSV-GENE,VCF (TSV-GENE and VCF)
    --output-format TSV-GENE, TSV-VARIANT, VCF (TSV-GENE, TSV-VARIANT and VCF)

Analysis file:

Analysis files contain all possible options for running an analysis including the ability to specify variant frequency
and pathogenicity data sources and the ability to tweak the order that analysis steps are performed.

    java -Xms2g -Xmx4g -jar exomiser-cli-${project.version}.jar --analysis test-analysis-exome.yml

These files an also be used to run full-genomes, however they will require substantially more RAM to do so. For example
a 4.4 million variant analysis requires approximately 10GB RAM.

Settings file:
    
Settings files contain all the parameters passed in on the command-line so you can just point exomiser to a file. See example.settings and test.settings.

    java -Xms2g -Xmx4g -jar exomiser-cli-${project.version}.jar --settings-file test.settings

    
Alternatively you can mix up a settings file and override settings by specifying them on the command line:

    java -Xms2g -Xmx4g -jar exomiser-cli-${project.version}.jar --settings-file test.settings --prioritiser=phenix


Batch mode analysis:
    
Batch mode will run through a list of settings files. Simple put the path to each settings file in the batch file - one file path per line.

    java -Xms2g -Xmx4g -jar exomiser-cli-${project.version}.jar --batch-file batch.txt

    -T leave in off-target variants

Want help? 

    java -jar exomiser-cli-${project.version}.jar --help

