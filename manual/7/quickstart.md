---
layout: page
title: Quickstart
subtitle: Quickstart for Exomiser and Genomiser
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

   ```
   cd exomiser-cli-{{ site.latest_7_version }}
   java -Xms2g -Xmx4g -jar exomiser-cli-{{ site.latest_7_version }}.jar --analysis NA19722_601952_AUTOSOMAL_RECESSIVE_POMP_13_29233225_5UTR_38.yml
   ```
 
## Linux

Use the following commands:

1. Download the distribution (won't take long)

   ```
   wget ftp://ftp.sanger.ac.uk/pub/resources/software/exomiser/downloads/exomiser/exomiser-cli-{{ site.latest_7_version }}-distribution.zip
   ```

2. Download the data (this is ~20GB and will take a while)

   ```
   wget ftp://ftp.sanger.ac.uk/pub/resources/software/exomiser/downloads/exomiser/exomiser-cli-{{ site.latest_7_version }}-data.zip
   ```

3. Download the checksums and verify the files (optional)
 
   ```
   wget ftp://ftp.sanger.ac.uk/pub/resources/software/exomiser/downloads/exomiser/exomiser-cli-{{ site.latest_7_version }}.sha256
   sha256sum -c exomiser-cli-{{ site.latest_7_version }}.sha256
   ```

4. Unzip the distribution and data files - this will create a directory called 'exomiser-cli-{{ site.latest_7_version }}' in the current working directory

   ```
   unzip exomiser-cli-{{ site.latest_7_version }}-distribution.zip
   unzip exomiser-cli-{{ site.latest_7_version }}-data.zip
   ```
 
 5. Run a test genomiser analysis

   ```
   cd exomiser-cli-{{ site.latest_7_version }}
   java -Xms2g -Xmx4g -jar exomiser-cli-{{ site.latest_7_version }}.jar --analysis NA19722_601952_AUTOSOMAL_RECESSIVE_POMP_13_29233225_5UTR_38.yml
   ```

This commands will download, verify and extract the exomiser files and then run the analysis contained in the file `NA19722_601952_AUTOSOMAL_RECESSIVE_POMP_13_29233225_5UTR_38.yml`. 
This file is an analysis for an autosomal recessive 5'UTR variant located in POMP gene on chromosome 13. The phenotype HPO terms are taken from the clinical synopsis of
[OMIM #601952](http://www.omim.org/clinicalSynopsis/601952) 

# Exomiser

You can run exomiser using different phenotype similarity algorithms.

## hiPHIVE

Phenotype comparisons to human, mouse and fish involving disruption of the gene or nearby genes in the interactome using a RandomWalk:

Using an OMIM disease:

  ```
  java -Xms2g -Xmx4g -jar exomiser-cli-{{ site.latest_7_version }}.jar --prioritiser=hiphive -I AD -F 1 -D OMIM:101600 -v data/Pfeiffer.vcf
  ```

The same command using HPO-Terms:

  ```
  java -Xms2g -Xmx4g -jar exomiser-cli-{{ site.latest_7_version }}.jar --prioritiser=hiphive -I AD -F 1 --hpo-ids HP:0000006,HP:0000174,HP:0000194,HP:0000218,HP:0000238,HP:0000244,HP:0000272,HP:0000303,HP:0000316,HP:0000322,HP:0000324,HP:0000327,HP:0000348,HP:0000431,HP:0000452,HP:0000453,HP:0000470,HP:0000486,HP:0000494,HP:0000508,HP:0000586,HP:0000678,HP:0001156,HP:0001249,HP:0002308,HP:0002676,HP:0002780,HP:0003041,HP:0003070,HP:0003196,HP:0003272,HP:0003307,HP:0003795,HP:0004209,HP:0004322,HP:0004440,HP:0005048,HP:0005280,HP:0005347,HP:0006101,HP:0006110,HP:0009602,HP:0009773,HP:0010055,HP:0010669,HP:0011304 -v data/Pfeiffer.vcf
  ```


## PHIVE

Phenotype comparisons to mice with disruption of the gene:

  ```
  java -Xmx2g -jar exomiser-cli-{{ site.latest_7_version }}.jar --prioritiser=phive -I AD -F 1 -D OMIM:101600 -v data/Pfeiffer.vcf
  ```

## Phenix

Phenotype comparisons to known human disease genes:

  ```
  java -Xms2g -Xmx4g -jar exomiser-cli-{{ site.latest_7_version }}.jar --prioritiser=phenix -v data/Pfeiffer.vcf -I AD -F 1 --hpo-ids HP:0000006,HP:0000174,HP:0000194,HP:0000218,HP:0000238,HP:0000244,HP:0000272,HP:0000303,HP:0000316,HP:0000322,HP:0000324,HP:0000327,HP:0000348,HP:0000431,HP:0000452,HP:0000453,HP:0000470,HP:0000486,HP:0000494,HP:0000508,HP:0000586,HP:0000678,HP:0001156,HP:0001249,HP:0002308,HP:0002676,HP:0002780,HP:0003041,HP:0003070,HP:0003196,HP:0003272,HP:0003307,HP:0003795,HP:0004209,HP:0004322,HP:0004440,HP:0005048,HP:0005280,HP:0005347,HP:0006101,HP:0006110,HP:0009602,HP:0009773,HP:0010055,HP:0010669,HP:0011304
  ```

## ExomeWalker

Prioritisation by proximity in interactome to the seed genes:

  ```
  java -Xms2g -Xmx4g -jar exomiser-cli-{{ site.latest_7_version }}.jar --prioritiser exomewalker  -v data/Pfeiffer.vcf -I AD -F 1 -S 2260
  ```

# Genomiser

Genomiser uses a config yml file instead of command-line arguments. To run your own samples you have to edit the `test-analysis-genome.yml` file and define

* the location of your VCF,
* the location of your PED-file (only for multiple samples in one VCF),
* your patient's HPO terms (use the [HPO-Browser](http://compbio.charite.de/hpoweb) to find terms)
* the inheritance model if known,
* the outputPrefix for your output files.

We suggest all other options are left in their current state for optimal performance and until you have viewed the README.md file. Then run

  ```
  java -Xms4g -Xmx8g -jar exomiser-cli-{{ site.latest_7_version }}.jar --analysis test-analysis-genome.yml
  ```

