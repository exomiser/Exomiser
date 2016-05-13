---
layout: page
title: Exomiser manual
subtitle: Instructions for configuring and running the Exomiser
---

* TOC
{:toc}

The Exomiser program was originally written purely for exome sequence analysis, however in version 7.0.0 it was extended to be able to analyse whole genome sequences too. The underlying framework and phenotype matching algorthims are identical to the previous version, although major architectural changes were introduced which enabled users to configure the exomiser to run analyses in a user-defined manner. The original command-line parameters and settings file inputs are still maintained and when used will run the original exomiser algorithm. Using the new analysis file input it is still possible to run the original exomiser algorithm (see test-analysis-exome.yml). The advantage here is that it is now possible to run an analysis over a whole-genome sample, yet treat it as an artificial exome for example. We would encourage users to use the new yml format for all their analyses as it also provides a record of exactly how a sample was analysed. This can also be easily shared with collaborators or embedded in publications. The Genomiser algorithm is contained in the test-analysis-genome.yml which has been optimised for both speed and memory usage in analysing whole genomes. Deviating from this default could *significantly* increase both the time and RAM used for an analysis.  

# Configuration

# Command-line parameter



### Multiple output formats

    --output-format HTML (default)
    --output-format TSV-GENE (TSV summary of genes)
    --output-format TSV-VARIANT (TSV summary of variants)
    --output-format VCF (VCF summary)

Output options can be combined, for example:

    --output-format TSV-GENE,VCF (TSV-GENE and VCF)
    --output-format TSV-GENE, TSV-VARIANT, VCF (TSV-GENE, TSV-VARIANT and VCF)

# Settings file
    
Settings files contain all the parameters passed in on the command-line so you can just point exomiser to a settings file uysing the `--settings-file` argument. See example.settings and test.settings.

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

# Analysis file

Instead of specifying all commands on the command-line you can specify exomiser to use the configuration contained an [analysis file](#analysis_file_config) using the `--analysis` argument. 
Analysis files contain all possible options for running an analysis including the ability to specify variant frequency
and pathogenicity data sources and the ability to tweak the order that analysis steps are performed.

```
java -Xms2g -Xmx4g -jar exomiser-cli-{{ site.latest_7_version }}.jar --analysis test-analysis-exome.yml
```

These files can also be used to run full-genomes, however they will require substantially more RAM to do so. For example
a 4.4 million variant analysis requires approximately 12GB RAM. However, RAM requirements can be substantially reduced by 
setting the analysisMode option to PASS_ONLY.  

Analyses can also be run in batches using the `--analysis-batch` command. This requires a file containing the paths to each analysis with one path per line as input.
 
```
java -Xms4g -Xmx8g -jar exomiser-cli-{{ site.latest_7_version }}.jar --analysis-batch test-batch-analysis.txt
```

If you have several genomes/exomes to analyse this is highly recommended as it will remove the start-time overhead and also allow the user to make use of caches as described in the `application.properties` file. Used correctly, this can save a lot of time at the expense of RAM as variant frequency and pathogenicity data will be cached for the most common variants cutting down on calls to the database. 
