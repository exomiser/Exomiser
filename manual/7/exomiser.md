---
layout: page
title: Exomiser manual
subtitle: Instructions how to intall use, and configure Exomiser
---

* TOC
{:toc}


This page will be avaiable in the future.

# Confirguration

## Command-line parameter


### Multiple output formats

    --output-format HTML (default)
    --output-format TSV-GENE (TSV summary of genes)
    --output-format TSV-VARIANT (TSV summary of variants)
    --output-format VCF (VCF summary)

Output options can be combined, for example:

    --output-format TSV-GENE,VCF (TSV-GENE and VCF)
    --output-format TSV-GENE, TSV-VARIANT, VCF (TSV-GENE, TSV-VARIANT and VCF)


## Analysis file

Instead of adding all command in the command-line you can use an analysis file. 
Analysis files contain all possible options for running an analysis including the ability to specify variant frequency
and pathogenicity data sources and the ability to tweak the order that analysis steps are performed.

```
java -Xms2g -Xmx4g -jar exomiser-cli-{{ site.latest_7_version }}.jar --analysis test-analysis-exome.yml
```

These files an also be used to run full-genomes, however they will require substantially more RAM to do so. For example
a 4.4 million variant analysis requires approximately 12GB RAM. However, RAM requirements can be substantially reduced by 
setting the analysisMode option to PASS_ONLY.  

Analyses can be run in batch mode. Simply put the path to each analysis file in the batch file - one file path per line.

```
java -Xms2g -Xmx4g -jar exomiser-cli-{{ site.latest_7_version }}.jar --analysis-batch test-analysis-batch.txt
```

## Settings file
    
Settings files contain all the parameters passed in on the command-line so you can just point exomiser to a file. See example.settings and test.settings.

```
java -Xms2g -Xmx4g -jar exomiser-cli-{{ site.latest_7_version }}.jar --settings-file test.settings
```
    
Alternatively you can mix up a settings file and override settings by specifying them on the command line:

```
java -Xms2g -Xmx4g -jar exomiser-cli-{{ site.latest_7_version }}.jar --settings-file test.settings --prioritiser=phenix
```
    
Settings can also be run in batch mode. Simply put the path to each settings file in the batch file - one file path per line.

```
java -Xms2g -Xmx4g -jar exomiser-cli-{{ site.latest_7_version }}.jar --batch-file batch.txt
```
