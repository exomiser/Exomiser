---
layout: page
title: Quickstart
subtitle: Quickstart for Exomiser and Genomiser
---

# Software and Hardware requirements
 - For exome analysis of a 30,000 variant sample 4GB RAM should suffice.
 - For genome analysis of a 4,400,000 variant sample 12GB RAM should suffice.
 - Any 64-bit operating system
 - Java 8 or above
 - At least 50GB free disk space (SSD preferred for best performance)
 - An internet connection is not required to run the Exomiser, although network access will be required if accessing a
  networked database (optional).
 - By default the Exomiser is completely self-contained and is able to run on standard consumer laptops.

# Installation

## Window

 1. Install 7-Zip (http://www.7-zip.org) for unzipping the archive files. The built-in archiving software has issues extracting the zip files. 
 2. Download the data and distribution files from the FTP site.
 3. Extract the distribution files by right-clicking exomiser-cli-7.2.1-distribution.zip and selecting 7-Zip > Extract Here
 4. Extract the data files by right-clicking exomiser-cli-7.2.1-data.zip and selecting 7-Zip > Extract Here
   4.1 Allow 7-Zip to overwite any empty data files with the full versions if prompted (remmData for example) 
 5. use your cmd to test your installation:
 ```bash
 cd exomiser-cli-7.2.1
 java -Xms2g -Xmx4g -jar exomiser-cli-7.2.1.jar --analysis NA19722_601952_AUTOSOMAL_RECESSIVE_POMP_13_29233225_5UTR_38.yml
 ```
 
## Linux

Use the following commands:

 1. Download the distribution (won't take long)
 
 ```bash
  wget ftp://ftp.sanger.ac.uk/pub/resources/software/exomiser/downloads/exomiser/exomiser-cli-7.2.1-distribution.zip
 ```
 2. Download the data (this is ~20GB and will take a while)
 
```bash
 wget ftp://ftp.sanger.ac.uk/pub/resources/software/exomiser/downloads/exomiser/exomiser-cli-7.2.1-data.zip
 ```
 3. Download the checksums and verify the files (optional)
 ```bash
 wget ftp://ftp.sanger.ac.uk/pub/resources/software/exomiser/downloads/exomiser/exomiser-cli-7.2.1.sha256    
 sha256sum -c exomiser-cli-7.2.1.sha256
 ```
 4. unzip the distribution and data files - this will create a directory called 'exomiser-cli-7.2.1' in the current working directory
 ```bash
    unzip exomiser-cli-7.2.1-distribution.zip
    unzip exomiser-cli-7.2.1-data.zip
 ```
 5. Run a test genomiser analysis
 ```bash
    cd exomiser-cli-7.2.1
    java -Xms2g -Xmx4g -jar exomiser-cli-7.2.1.jar --analysis NA19722_601952_AUTOSOMAL_RECESSIVE_POMP_13_29233225_5UTR_38.yml
 ```

This commands will download, verify and extract the exomiser files and then run the analysis contained in the file 'NA19722_601952_AUTOSOMAL_RECESSIVE_POMP_13_29233225_5UTR_38.yml'. 
This file is an analysis for an autosomal recessive 5'UTR variant located in POMP gene on chromosome 13. The phenotype HPO terms are taken from the clinical synopsis of
OMIM #601952 (http://www.omim.org/clinicalSynopsis/601952) 

# Alternative set-up

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

# Exomiser

# Genomiser

