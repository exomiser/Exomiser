---
layout: page
title: Genomiser manual
subtitle: Instructions how to intall use, and configure Genomiser
---

* TOC
{:toc}

# Run Genomiser

Genomiser uses a config yml file instead of command-line arguments. To run your own samples you have to edit the `test-analysis-genome.yml` file and define

* the location of your VCF,
* the location of your PED-file (only for multiple samples in one VCF),
* your patient's HPO terms (use the [HPO-Browser](http://compbio.charite.de/hpoweb) to find terms)
* the inheritance model if known,
* the outputPrefix for your output files.

We suggest all other options are left in their current state for optimal performance. Then run

```
java -Xms2g -Xmx4g -jar exomiser-cli-7.2.1.jar --analysis test-analysis-genome.yml
```

# Configuration

Genomiser can only be user with a yml configuration file in [yml format](http://yaml.org/). An example cam be found in the downloaded `exomiser-cli-7.2.1-distribution.zip` file or [here](../example/test-analysis-genome). It is structured into two sections

analysis:
: Section include input, run mode, filters, and prioritizers. See [Analysis section](#analysis-section) for more details.

outputOptions:
: Section that defines the output format, output file and number of results that should be printed out. See [output options section](#output-options-section) for more details.

## Analysis section

vcf:
: The variant file in [VCF format](https://github.com/samtools/hts-specs). There can be variants of multiple samples from one family in the file.

ped:
: If you have multiple samples as input you have to define the pedigree using the [ped format](http://pngu.mgh.harvard.edu/~purcell/plink/data.shtml#ped). It is important that you correctly define affected and unaffected individuals. If you use `X_RECESSIVE` as mode of inheritance be sure that the sex is correct (unknown is also fine).

modeOfInheritance:
: Can be `AUTOSOMAL_DOMINANT`, `AUTOSOMAL_RECESSIVE`, `X_RECESSIVE` or `UNDEFINED`. This is a functionality of Jannovar. See its [inheritance documentation](http://jannovar.readthedocs.io/en/master/ped_filters.html) for further information.

analysisMode:
: Can be `FULL`, `SPARSE` or `PASS_ONLY`. We highly recommend `PASS_ONLY` on genomes for because of memory issues. It will only keep variants that passes all filters. `FULL` will keep all variants. `SPARSE` means that **TODO**



geneScoreMode: 
: Can be `RAW_SCORE` or `RANK_BASED`. **TODO**

hpoIds:
: Input of the HPO identifiers/terms. You can select them using the [HPO browser](http://compbio.charite.de/hpoweb). Input must be in array format. So it have to start and end with a square bracket. Terms are comma separated and delimited by single quotes. For example `['HP:0001156', 'HP:0001363', 'HP:0011304', 'HP:0010055']`

frequencySources:
: Here you can specify which variant frequency databases you want to use. You can add multiple databases using the same array format like the hpoIDs. Possible options are [`THOUSAND_GENOMES`](http://www.1000genomes.org), [`ESP_AFRICAN_AMERICAN`, `ESP_EUROPEAN_AMERICAN`, `ESP_ALL`](http://evs.gs.washington.edu/EVS/), [`EXAC_AFRICAN_INC_AFRICAN_AMERICAN`, `EXAC_AMERICAN`, `EXAC_SOUTH_ASIAN`, `EXAC_EAST_ASIAN`,`EXAC_FINNISH`, `EXAC_NON_FINNISH_EUROPEAN`,`EXAC_OTHER`](http://exac.broadinstitute.org/about)
pathogenicitySources:
: Possible pathogenicitySources: `POLYPHEN`, `MUTATION_TASTER`, `SIFT`, `CADD`, `REMM`. `REMM` is trained on non-coding regulatory regions. **WARNING** if you enable `CADD`, ensure that you have downloaded and installed the `CADD` tabix files and updated their location in the application.properties. Exomiser will not run without this. An example is: `[POLYPHEN, MUTATION_TASTER, SIFT, REMM]`

This is the recommended order for a genome-sized analysis.
#all steps are optional
steps: [ 
#intervalFilter: {interval: 'chr10:123256200-123256300'}, 
#geneIdFilter: {geneIds: [12345, 34567, 98765]},
hiPhivePrioritiser: {},
#running the prioritiser followed by a priorityScoreFilter will remove genes
#which are least likely to contribute to the phenotype defined in hpoIds, this will
#dramatically reduce the time and memory required to analyse a genome.
# 0.501 is a good compromise to select good phenotype matches and the best protein-protein interactions hits from hiPhive
priorityScoreFilter: {priorityType: HIPHIVE_PRIORITY, minPriorityScore: 0.501},
#variantEffectFilter: {remove: [SYNONYMOUS_VARIANT]},
#regulatoryFeatureFilter removes all non-regulatory non-coding variants over 20Kb from a known gene.
regulatoryFeatureFilter: {},
#knownVariantFilter: {}, #removes variants represented in the database
frequencyFilter: {maxFrequency: 1.0},
pathogenicityFilter: {keepNonPathogenic: true},
#inheritanceFilter and omimPrioritiser should always run AFTER all other filters have completed
#they will analyse genes according to the specified modeOfInheritance above- UNDEFINED will not be analysed.
inheritanceFilter: {},
#omimPrioritiser isn't mandatory.
omimPrioritiser: {}
#Other prioritisers: Only combine omimPrioritiser with one of these.
#Don't include any if you only want to filter the variants.
#hiPhivePrioritiser: {},
# or run hiPhive in benchmarking mode: 
#hiPhivePrioritiser: {diseaseId: 'OMIM:101600', candidateGeneSymbol: FGFR2, runParams: 'human,mouse,fish,ppi'},
#phenixPrioritiser: {}
#exomeWalkerPrioritiser: {seedGeneIds: [11111, 22222, 33333]}
]

## Output options section





## Choose filters

[filters](../filters)
