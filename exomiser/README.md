The Exomiser - Main Exomiser Executable 
===============================================================

# Installation

Unpack the exomiser-2.0.0.tar.gz or exomiser-2.0.0.zip and it's ready to go.

# Alternative set-up

If you want to run Exomiser from outside this install directory then replace the following line in jdbc.properties:

    exomiser.url=jdbc:h2:file:data/exomiser;MODE=PostgreSQL;SCHEMA=EXOMISER;DATABASE_TO_UPPER=FALSE;IFEXISTS=TRUE;

with

    exomiser.url=jdbc:h2:file:/my/full/path/to/my/exomiser/install/data/exomiser;MODE=PostgreSQL;SCHEMA=EXOMISER;DATABASE_TO_UPPER=FALSE;IFEXISTS=TRUE;

(optional) If you want to run from a Postgres database rather than the default H2 embedded database
  (a) download exomiser_dump.pg.gz
  (b) gunzip exomiser_dump.pg.gz
  (c) load into your postgres server: psql -h yourhost -U yourusername yourdatabase < exomiser_dump.pg
  (d) edit jdbc.properties to point to this new database

# Usage

(a) Exomiser v2 - phenotype comparisons to human, mouse and fish involving disruption of the gene or nearby genes in the interactome using a RandomWalk 

    java -Xms5g -Xmx5g -jar exomiser-2.0.0.jar -D data/ucsc_hg19.ser -I AD -F 1 -W data/rw_string_9_05.gz -X data/rw_string_9_05_id2index.gz -A OMIM:101600 -v data/Pfeiffer.vcf

    java -Xms5g -Xmx5g -jar exomiser-2.0.0.jar -D data/ucsc_hg19.ser -I AD -F 1 -W data/rw_string_9_05.gz -X data/rw_string_9_05_id2index.gz --hpo_ids HP:0000006,HP:0000174,HP:0000194,HP:0000218,HP:0000238,HP:0000244,HP:0000272,HP:0000303,HP:0000316,HP:0000322,HP:0000324,HP:0000327,HP:0000348,HP:0000431,HP:0000452,HP:0000453,HP:0000470,HP:0000486,HP:0000494,HP:0000508,HP:0000586,HP:0000678,HP:0001156,HP:0001249,HP:0002308,HP:0002676,HP:0002780,HP:0003041,HP:0003070,HP:0003196,HP:0003272,HP:0003307,HP:0003795,HP:0004209,HP:0004322,HP:0004440,HP:0005048,HP:0005280,HP:0005347,HP:0006101,HP:0006110,HP:0009602,HP:0009773,HP:0010055,HP:0010669,HP:0011304 -v data/Pfeiffer.vcf

(b) Exomiser v1 - phenotype comparisons to mice with disruption of the gene

    java -Xmx5g -jar exomiser-2.0.0.jar -D data/ucsc_hg19.ser -I AD -F 1 -M -A OMIM:101600 -v data/Pfeiffer.vcf

Phenix - phenotype comparisons to known human disease genes

    java -Xms5g -Xmx5g -jar exomiser-2.0.0.jar -D data/ucsc_hg19.ser -I AD -F 1 --hpo data/phenix/hp.obo --hpoannot data/phenix/phenotype_annotation.tab --phenomizerData data/phenix --hpo_ids HP:0000006,HP:0000174,HP:0000194,HP:0000218,HP:0000238,HP:0000244,HP:0000272,HP:0000303,HP:0000316,HP:0000322,HP:0000324,HP:0000327,HP:0000348,HP:0000431,HP:0000452,HP:0000453,HP:0000470,HP:0000486,HP:0000494,HP:0000508,HP:0000586,HP:0000678,HP:0001156,HP:0001249,HP:0002308,HP:0002676,HP:0002780,HP:0003041,HP:0003070,HP:0003196,HP:0003272,HP:0003307,HP:0003795,HP:0004209,HP:0004322,HP:0004440,HP:0005048,HP:0005280,HP:0005347,HP:0006101,HP:0006110,HP:0009602,HP:0009773,HP:0010055,HP:0010669,HP:0011304 -v data/Pfeiffer.vcf

(c) ExomeWalker - prioritisation by proximity in interactome to the seed genes

    java -Xms5g -Xmx5g -jar exomiser-2.0.0.jar -D data/ucsc_hg19.ser -I AD -F 1 -W data/rw_string_9_05.gz -X data/rw_string_9_05_id2index.gz -S 2260  -v data/Pfeiffer.vcf

Other useful params

    --tsv output TSV summary instead of HTML
    --vcf_output VCF summary instead of HTML
    -T leave in off-target and non-pathogenic variants

# Project Build

This maven project is used to build the main exomiser jar for distribution. The 
assembly plugin will produce a zip and tar.gz with an internal structure defined 
in src/assemble/distribution.xml. This automates most of the following steps, 
although the maven resources plugin will copy all resources and data files to 
target/resources and the assembly plugin will copy from here to the directories 
defined in the outputDirectory fields for each fileSet of distribution.xml:

    cd target
    mkdir data
    # copy in the data made from the db build or run this now if you haven't already
    cp ../../exomiser-db/data/exomiser.h2.db data/.
    cp ../../exomiser-db/data/extracted/ucsc_hg19.ser data/.
    mv Pfeiffer.vcf data/.
    # copy in the rw_string_9_05* data to data/
    ... from somewhere
    # copy in the extra phenix data to data/
    ... from somewhere
    # make the archive.
    tar -cvzf exomiser.tgz exomiser-2.0.0.jar jdbc.properties log4j2.xml lib data 
    # copy to the ftp site
    scp exomiser.tgz gen1:/nfs/disk69/ftp/pub/resources/software/exomiser/downloads/exomiser/ 