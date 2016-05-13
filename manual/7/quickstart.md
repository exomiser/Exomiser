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
 
Once the software is [setup](setup) according to the instructions the exomiser can be run in a number of ways.
 
# Exomiser

The original exomiser algorithm is easily run using the command-line arguments. Here we are using exomiser to prioritise variants from an exome containing a spiked in causative mutation for Pfeiffer syndrome:
 
```
10      123256215       .       T       G       100     PASS    GENE=FGFR2;INHERITANCE=AD;MIM=101600    GT:DS:GL        1|0:2.000:-5.00,-1.10,-0.04
```
The input parameters tell exomiser to read in in the VCF file (`-v data/Pfeiffer.vcf`) filter out variants using a frequency cuf-off of 1% (`-F 1`) and apply an autosomal dominant inheritance mode filter (`-I AD`). The patient phenotypes are described using the `--hpo-ids` argument and the exomiser is instructed to use the similarity matching algorithm containd in the hiPhive prioritiser `--prioritiser hiphive`. Other prioritisers can be specified as described in the [prioritisers section](#Prioritisers).

```
java -Xms2g -Xmx4g -jar exomiser-cli-{{ site.latest_7_version }}.jar --prioritiser hiphive -I AD -F 1 --hpo-ids HP:0000006,HP:0000174,HP:0000194,HP:0000218,HP:0000238,HP:0000244,HP:0000272,HP:0000303,HP:0000316,HP:0000322,HP:0000324,HP:0000327,HP:0000348,HP:0000431,HP:0000452,HP:0000453,HP:0000470,HP:0000486,HP:0000494,HP:0000508,HP:0000586,HP:0000678,HP:0001156,HP:0001249,HP:0002308,HP:0002676,HP:0002780,HP:0003041,HP:0003070,HP:0003196,HP:0003272,HP:0003307,HP:0003795,HP:0004209,HP:0004322,HP:0004440,HP:0005048,HP:0005280,HP:0005347,HP:0006101,HP:0006110,HP:0009602,HP:0009773,HP:0010055,HP:0010669,HP:0011304 -v data/Pfeiffer.vcf
```

# Genomiser

The Genomiser uses the Exomiser framework to be able to analyse whole-genome data and has been especially tailored to include non-coding variants. It uses a config yml file instead of command-line arguments. To run your own samples you have to edit the `test-analysis-genome.yml` file and define

* the location of your VCF,
* the location of your PED-file (only for multiple samples in one VCF),
* your patient's HPO terms (use the [HPO-Browser](http://compbio.charite.de/hpoweb) to find terms)
* the inheritance model if known,
* the outputPrefix for your output files.

We suggest all other options are left in their current state for optimal performance and until you have viewed the README.md file. Then run

```
java -Xms4g -Xmx8g -jar exomiser-cli-{{ site.latest_7_version }}.jar --analysis test-analysis-genome.yml
```

An example analysis containing a 5' UTR variant can be run using this command:

```
java -Xms4g -Xmx8g -jar exomiser-cli-{{ site.latest_7_version }}.jar --analysis NA19722_601952_AUTOSOMAL_RECESSIVE_POMP_13_29233225_5UTR_38.yml
```

The `NA19722_601952_AUTOSOMAL_RECESSIVE_POMP_13_29233225_5UTR_38.yml` file is an exomiser analysis file for an autosomal recessive 5'UTR variant located in POMP gene on chromosome 13. The phenotype HPO terms are taken from the clinical synopsis of
[OMIM #601952](http://www.omim.org/clinicalSynopsis/601952)


# Prioritisers

The exomiser contains several different phenotype similarity algorithms  specified using the `--prioritiser` argument. The sections below briefly describe the prioritisers and any special parameters they may require. The algorithms are described in detail in their respective publication. 

## hiPHIVE

Phenotype comparisons to human, mouse and fish involving disruption of the gene or nearby genes in the interactome using a RandomWalk:

Using an OMIM disease:

```
java -Xms2g -Xmx4g -jar exomiser-cli-{{ site.latest_7_version }}.jar --prioritiser=hiphive -I AD -F 1 -D OMIM:101600 -v data/Pfeiffer.vcf
```

The same command using HPO-Terms _(recommended)_:

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


