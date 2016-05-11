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
java -Xms4g -Xmx8g -jar exomiser-cli-{{ site.latest_7_version }}.jar --analysis test-analysis-genome.yml
```

Analyses can also be run in batches using the `--analysis-batch` command. This requires a file containing the paths to each analysis with one path per line as input.
 
```
java -Xms4g -Xmx8g -jar exomiser-cli-{{ site.latest_7_version }}.jar --analysis-batch test-batch-analysis.txt
```

If you have several genomes/exomes to analyse this is highly recommended as it will remove the start-time overhead and also allow the user to make use of caches as described in the `application.properties` file. Used correctly, this can save a lot of time at the expense of RAM as variant frequency and pathogenicity data will be cached for the most common variants cutting down on calls to the database. 

# Configuration

Genomiser can only be used with a configuration file in [yml format](http://yaml.org/). An example can be found in the downloaded `exomiser-cli-{{ site.latest_7_version }}-distribution.zip` file or [here](../example/test-analysis-genome). It is structured into two sections

**analysis:**
Section include input, run mode, filters, and prioritizers. See [Analysis section](#analysis-section) for more details.

**outputOptions:**
Section that defines the output format, output file and number of results that should be printed out. See [output options section](#output-options-section) for more details.

## Analysis section

**vcf:**
The variant file in [VCF format](https://github.com/samtools/hts-specs). There can be variants of multiple samples from one family in the file.

**ped:**
If you have multiple samples as input you have to define the pedigree using the [ped format](http://pngu.mgh.harvard.edu/~purcell/plink/data.shtml#ped). It is important that you correctly define affected and unaffected individuals. If you use `X_RECESSIVE` as mode of inheritance be sure that the sex is correct (unknown is also fine).

modeOfInheritance:
: Can be `AUTOSOMAL_DOMINANT`, `AUTOSOMAL_RECESSIVE`, `X_RECESSIVE` or `UNDEFINED`. This is a functionality of Jannovar. See its [inheritance documentation](http://jannovar.readthedocs.io/en/master/ped_filters.html) for further information.

analysisMode:
: Can be `FULL`, `SPARSE` or `PASS_ONLY`. We highly recommend `PASS_ONLY` on genomes for because of memory issues. It will only keep variants that passes all filters. `FULL` will keep all variants. `SPARSE` will keep all variants, but will only run a variant through the variant filters until it fails one.  

geneScoreMode: 
: Can be `RAW_SCORE` or `RANK_BASED`. This effects the scoring mode used by certain prioritisers. We recommend sticking to `RAW_SCORE`. This is likely to be removed in further versions. 

hpoIds:
: Input of the HPO identifiers/terms. You can select them using the [HPO browser](http://compbio.charite.de/hpoweb). Input must be in array format. Terms are comma separated and delimited by single quotes. For example `['HP:0001156', 'HP:0001363', 'HP:0011304', 'HP:0010055']`. It is *critical* that these are as detailed as possible and describe the observed phenotypes as fully and precisely as possible. These are used by the phenotype matching algorithms and will heavily influence the outcome of an analysis.

frequencySources:
: Here you can specify which variant frequency databases you want to use. You can add multiple databases using the same array format like the hpoIDs. Possible options are [`THOUSAND_GENOMES`](http://www.1000genomes.org), [`ESP_AFRICAN_AMERICAN`, `ESP_EUROPEAN_AMERICAN`, `ESP_ALL`](http://evs.gs.washington.edu/EVS/), [`EXAC_AFRICAN_INC_AFRICAN_AMERICAN`, `EXAC_AMERICAN`, `EXAC_SOUTH_ASIAN`, `EXAC_EAST_ASIAN`,`EXAC_FINNISH`, `EXAC_NON_FINNISH_EUROPEAN`,`EXAC_OTHER`](http://exac.broadinstitute.org/about). We recommend using all databases.

pathogenicitySources:
: Possible pathogenicitySources: `POLYPHEN`, `MUTATION_TASTER`, `SIFT`, `CADD`, `REMM`. `REMM` is trained on non-coding regulatory regions. **WARNING** if you enable `CADD`, ensure that you have downloaded and installed the `CADD` tabix files and updated their location in the application.properties. Exomiser will not run without this. An example is: `[POLYPHEN, MUTATION_TASTER, SIFT, REMM]`

steps:
: This section instructs exomiser which analysis steps should be run and with which criteria. **_n.b._ the order in which the steps are declared is important** - exomiser will run them in the order declared, although certain optimisations will happen automatically. We recommend using the [standard settings](../example/test-analysis-genome) for genome wide analysis as these have been optimised for both speed and memory performance. Nonetheless all steps are optional. Being an array of steps, this section must be enclosed in square brackets. Steps are comma separated and written in hash format *name: {options}*.  See the [steps section](#steps-section) for more details. **All steps are optional.** Uncomment them if you do not want them.

: Analysis steps are defined in terms of variant filters, gene filters or prioritisers. The `inheritanceFilter` and `omimPrioritiser` are both somewhat anomalous as they operate on genes but also require the variants to have already been filtered. The optimiser will ensure that these are run at the correct time if they have been incorrectly placed. 
  
 Using these it is possible to create artificial exomes or define gene panels for example.

### Steps section

#### Variant filters
These operate on variants and will produce a pass or fail result for each variant run through the filter.

intervalFilter: 
: Define an interval of interest. Only variants within this interval will be passed. Currently only single intervals are possible.  Example: `intervalFilter: {interval: 'chr10:123256200-123256300'}`

geneIdFilter:
: You can define [entrez-gene-ids](http://www.ncbi.nlm.nih.gov/gene/) for genes of interest. Only variants associated with these genes will be analyzed. Example: `geneIdFilter: {geneIds: [12345, 34567, 98765]}`

variantEffectFilter: 
: If you are interested only in specific functional classes of variants you can define a set of classes you want to remove from the output. Variant effects are generated by [Jannovar](http://charite.github.io/jannovar/). Jannovar uses [Sequence Ontology (SO)](http://www.sequenceontology.org/) terms and are listed in their [manual](http://jannovar.readthedocs.io/en/master/var_effects.html). Example `variantEffectFilter: {remove: [SYNONYMOUS_VARIANT]}`

regulatoryFeatureFilter:
: If set it removes all non-regulatory non-coding variants over 20Kb from a known gene. **TODO if set with empty {}, then it is active? What happen with variants over 20kb if you remove this filter?Are they are associated with genes in their TAD? This will be important to describe because of the TAD model** Example `regulatoryFeatureFilter: {}`

knownVariantFilter:
: Removes variants represented in the databases set in the **frequencySources** section. E.g. if you define `frequencySources: [THOUSAND_GENOMES]` every variant with an RS number will be removed. Variants without an RSid will be removed/failed if they are represented in any database defined in the **frequencySources** section. We do not recommend this option on recessive diseases. Example `knownVariantFilter: {}`

frequencyFilter:
: Frequency cutoff of a variant **in percent**. Frequencies are derived from the databases defined in the **frequencySources** section. We recommend a value below 5.0% depending on the disease. Variants will be removed/failed if they have a frequency higher than the stated percentage in any database defined in the **frequencySources** section. _n.b_ Not defining this filter will result in all variants having no frequency data, even if the **frequencySources** contains values. Example `frequencyFilter: {maxFrequency: 1.0}`

pathogenicityFilter:
: Will apply the pathogenicity scores defined in the **pathogenicitySources** section to variants. If the `keepNonPathogenic` field is set to `true` then all variants wil be kept. Setting this to `false` will set the filter to fail non-missense variants with pathogenicity scores lower than a score cutoff of 0.5. This filter is meant to be quite permissive. Example `pathogenicityFilter: {keepNonPathogenic: true}`

#### Gene filters
priorityScoreFilter: 
: Running the prioritizer followed by a priorityScoreFilter will remove genes which are least likely to contribute to the phenotype defined in hpoIds, this will dramatically reduce the time and memory required to analyze a genome. 0.501 is a good compromise to select good phenotype matches and the best protein-protein interactions hits using the hiPhive prioritizer. **TODO add different priority types** Example `priorityScoreFilter: {priorityType: HIPHIVE_PRIORITY, minPriorityScore: 0.501}`

inheritanceFilter:
: **inheritanceFilter** and **omimPrioritiser** should always run AFTER all other filters have completed. They will analyze genes according to the specified **modeOfInheritance** above. If set to `UNDEFINED` no filtering will be done. You can read more in the [Jannovar inheritance documentation](http://jannovar.readthedocs.io/en/master/ped_filters.html) how exactly this filter works. Example: `inheritanceFilter: {}`

#### Prioritisers
These work on the gene-level and will produce the semantic-similarity scores for how well a gene matches the sample's HPO profile.

omimPrioritiser:
: **inheritanceFilter** and **omimPrioritiser** should always run AFTER all other filters have completed. Other prioritizers: Only combine **omimPrioritiser** with one of the next filters. **TODO What does this filter do? Is it about recessive/dominant genes vs given inheritance? Maybe we should rename it because of OMIM license stuff.** Example `omimPrioritiser: {}`

hiPhivePrioritiser:
: **TODO describe** Don't include **hiPhivePrioritiser** if you only want to filter the variants or run hiPhive in benchmarking mode. **TODO what is the benchmark mode?**. Using the default `hiPhivePrioritiser: {}` is the same as `hiPhivePrioritiser: {runParams: 'human,mouse,fish,ppi'}`. Example `hiPhivePrioritiser: {diseaseId: 'OMIM:101600', candidateGeneSymbol: FGFR2, runParams: 'human,mouse,fish,ppi'}`

phenixPrioritiser:
: **TODO describe this** Example `phenixPrioritiser: {}`

exomeWalkerPrioritiser:
: **TODO describe this** Example `exomeWalkerPrioritiser: {seedGeneIds: [11111, 22222, 33333]}`

## Output options section

outputPassVariantsOnly:
: Can be `true` or `false`. **TODO**

numGenes: 
: Maximum number of genes listed in the results. If set to `0` all are listed. In most cases a limit of `50` is good.

outputPrefix:
: Specify the path/filename without an extension and this will be added according to the **outputFormats** option. If unspecified this will default to the following: `{exomiserDir}/results/input-vcf-name-exomiser-results.html`. Alternatively, specify a fully qualified path only. e.g. `/users/jules/exomes/analysis`.

outputFormats:
: Array to define the output formats. can be `[TSV-GENE]`, `[TSV-VARIANT]`, `[VCF]` or any combination like `[TSV-GENE, TSV-VARIANT, VCF]`. Output formats are described in this publication[^1].

[^1]: Smedley, Damian, et al. "Next-generation diagnostics and disease-gene discovery with the Exomiser." *Nature protocols* 10.12 (2015): 2004-2015.
