---
layout: page
title: Installation
subtitle: Instructions how to intall the Exomiser software suite
---

* TOC
{:toc}


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

## Windows

1. Install [7-Zip](http://www.7-zip.org) for unzipping the archive files. The built-in archiving software has issues extracting the zip files. 
2. Download the data and distribution files from the FTP site.
3. Extract the distribution files by right-clicking `exomiser-cli-{{ site.latest_7_version }}-distribution.zip` and selecting 7-Zip > Extract Here
4. Extract the data files by right-clicking `exomiser-cli-{{ site.latest_7_version }}-data.zip` and selecting 7-Zip > Extract Here
   1. Allow 7-Zip to overwite any empty data files with the full versions if prompted (remmData for example) 
5. Use your cmd to test your installation:

   cd exomiser-cli-{{ site.latest_7_version }}
   java -Xms2g -Xmx4g -jar exomiser-cli-{{ site.latest_7_version }}.jar --analysis NA19722_601952_AUTOSOMAL_RECESSIVE_POMP_13_29233225_5UTR_38.yml
 
## Linux

Use the following commands:

1. Download the distribution (won't take long)

   wget ftp://ftp.sanger.ac.uk/pub/resources/software/exomiser/downloads/exomiser/exomiser-cli-{{ site.latest_7_version }}-distribution.zip

2. Download the data (this is ~20GB and will take a while)

   wget ftp://ftp.sanger.ac.uk/pub/resources/software/exomiser/downloads/exomiser/exomiser-cli-{{ site.latest_7_version }}-data.zip

3. Download the checksums and verify the files (optional)
 
   wget ftp://ftp.sanger.ac.uk/pub/resources/software/exomiser/downloads/exomiser/exomiser-cli-{{ site.latest_7_version }}.sha256
   sha256sum -c exomiser-cli-{{ site.latest_7_version }}.sha256

4. Unzip the distribution and data files - this will create a directory called 'exomiser-cli-{{ site.latest_7_version }}' in the current working directory

   unzip exomiser-cli-{{ site.latest_7_version }}-distribution.zip
   unzip exomiser-cli-{{ site.latest_7_version }}-data.zip
 
 5. Run a test genomiser analysis

   cd exomiser-cli-{{ site.latest_7_version }}
   java -Xms2g -Xmx4g -jar exomiser-cli-{{ site.latest_7_version }}.jar --analysis NA19722_601952_AUTOSOMAL_RECESSIVE_POMP_13_29233225_5UTR_38.yml

These commands will download, verify and extract the exomiser files and then run the analysis contained in the file `NA19722_601952_AUTOSOMAL_RECESSIVE_POMP_13_29233225_5UTR_38.yml`. 
This file is an analysis for an autosomal recessive 5'UTR variant located in POMP gene on chromosome 13. The phenotype HPO terms are taken from the clinical synopsis of
[OMIM #601952](http://www.omim.org/clinicalSynopsis/601952) 

## Mac

1. Install http://p7zip.sourceforge.net/

    brew install p7zip

2. Unzip the data:

    7z l exomiser-cli-7.2.1-data.zip

# Alternative set-up

If you want to run Exomiser using an H2 database from a location of your choosing edit the line in `application.properties`:

    h2Path=

with

    h2Path=/full/path/to/alternative/h2/database/exomiser.h2.db

