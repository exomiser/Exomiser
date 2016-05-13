---
layout: page
title: Analysis file configuration
subtitle: Instructions on how to configure Exomiser using yaml format analysis files
---

* TOC
{:toc}


# Analysis file configuration

Genomiser analyses are defined using a [yml format](http://yaml.org/) configuration file. An example can be found in the downloaded `exomiser-cli-{{ site.latest_7_version }}-distribution.zip` file or [here](../example/test-analysis-genome). It is structured into two sections, [analysis](#analysis) which defines the input, run mode filters and prioritisers and the [outputOptions](#outputOptions) section that defines the output format, output file and number of results that should be printed out.

## analysis:

### vcf:
The variant file in [VCF format](https://github.com/samtools/hts-specs). There can be variants of multiple samples from one family in the file.

### ped:
If you have multiple samples as input you have to define the pedigree using the [ped format](http://pngu.mgh.harvard.edu/~purcell/plink/data.shtml#ped). It is important that you correctly define affected and unaffected individuals. If you use `X_RECESSIVE` as mode of inheritance be sure that the sex is correct (unknown is also fine).

### modeOfInheritance:
Can be `AUTOSOMAL_DOMINANT`, `AUTOSOMAL_RECESSIVE`, `X_RECESSIVE` or `UNDEFINED`. This is a functionality of Jannovar. See its [inheritance documentation](http://jannovar.readthedocs.io/en/master/ped_filters.html) for further information.

### analysisMode:
Can be `FULL`, `SPARSE` or `PASS_ONLY`. We highly recommend `PASS_ONLY` on genomes for because of memory issues. It will only keep variants that passes all filters. `FULL` will keep all variants. `SPARSE` will keep all variants, but will only run a variant through the variant filters until it fails one.  

### geneScoreMode: 
Can be `RAW_SCORE` or `RANK_BASED`. This effects the scoring mode used by certain prioritisers. We recommend sticking to `RAW_SCORE`. This is likely to be removed in further versions. 

### hpoIds:
Input of the HPO identifiers/terms. You can select them using the [HPO browser](http://compbio.charite.de/hpoweb). Input must be in array format. Terms are comma separated and delimited by single quotes. For example `['HP:0001156', 'HP:0001363', 'HP:0011304', 'HP:0010055']`. It is *critical* that these are as detailed as possible and describe the observed phenotypes as fully and precisely as possible. These are used by the phenotype matching algorithms and will heavily influence the outcome of an analysis.

### frequencySources:
Here you can specify which variant frequency databases you want to use. You can add multiple databases using the same array format like the hpoIDs. Possible options are [`THOUSAND_GENOMES`](http://www.1000genomes.org), [`ESP_AFRICAN_AMERICAN`, `ESP_EUROPEAN_AMERICAN`, `ESP_ALL`](http://evs.gs.washington.edu/EVS/), [`EXAC_AFRICAN_INC_AFRICAN_AMERICAN`, `EXAC_AMERICAN`, `EXAC_SOUTH_ASIAN`, `EXAC_EAST_ASIAN`,`EXAC_FINNISH`, `EXAC_NON_FINNISH_EUROPEAN`,`EXAC_OTHER`](http://exac.broadinstitute.org/about). We recommend using all databases.

### pathogenicitySources:
Possible pathogenicitySources: `POLYPHEN`, `MUTATION_TASTER`, `SIFT`, `CADD`, `REMM`. `REMM` is trained on non-coding regulatory regions. **WARNING** if you enable `CADD`, ensure that you have downloaded and installed the `CADD` tabix files and updated their location in the application.properties. Exomiser will not run without this. An example is: `[POLYPHEN, MUTATION_TASTER, SIFT, REMM]`

### steps:
This section instructs exomiser which analysis steps should be run and with which criteria. **_n.b._ the order in which the steps are declared is important** - exomiser will run them in the order declared, although certain optimisations will happen automatically. We recommend using the [standard settings](../example/test-analysis-genome) for genome wide analysis as these have been optimised for both speed and memory performance. Nonetheless all steps are optional. Being an array of steps, this section must be enclosed in square brackets. Steps are comma separated and written in hash format *name: {options}*. **All steps are optional** - comment them out or delete them if you do not want to use them.

Analysis steps are defined in terms of [variant filters](#variant_filters), [gene filters](#gene_filters) or [prioritisers](#prioritisers). The `inheritanceFilter` and `omimPrioritiser` are both somewhat anomalous as they operate on genes but also require the variants to have already been filtered. The optimiser will ensure that these are run at the correct time if they have been incorrectly placed. 
  
Using these it is possible to create artificial exomes or define gene panels for example.

#### Variant filters {#variant_filters}
These operate on variants and will produce a pass or fail result for each variant run through the filter.

##### intervalFilter: 
Define an interval of interest. Only variants within this interval will be passed. Currently only single intervals are possible.  Example: `intervalFilter: {interval: 'chr10:123256200-123256300'}`

##### geneIdFilter:
You can define [entrez-gene-ids](http://www.ncbi.nlm.nih.gov/gene/) for genes of interest. Only variants associated with these genes will be analyzed. Example: `geneIdFilter: {geneIds: [12345, 34567, 98765]}`

##### variantEffectFilter: 
If you are interested only in specific functional classes of variants you can define a set of classes you want to remove from the output. Variant effects are generated by [Jannovar](http://charite.github.io/jannovar/). Jannovar uses [Sequence Ontology (SO)](http://www.sequenceontology.org/) terms and are listed in their [manual](http://jannovar.readthedocs.io/en/master/var_effects.html). Example `variantEffectFilter: {remove: [SYNONYMOUS_VARIANT]}`

##### regulatoryFeatureFilter:
If included it removes all non-regulatory, non-coding variants over 20Kb from a known gene. Intergenic and upstream variants in known enhancer regions over 20kb from a known gene are associated with genes in their TAD and not effected by this filter. This is an important filter to include when analysing whole-genome data. Example `regulatoryFeatureFilter: {}`

##### knownVariantFilter:
Removes variants represented in the databases set in the **frequencySources** section. E.g. if you define `frequencySources: [THOUSAND_GENOMES]` every variant with an RS number will be removed. Variants without an RSid will be removed/failed if they are represented in any database defined in the **frequencySources** section. We do not recommend this option on recessive diseases. Example `knownVariantFilter: {}`

##### frequencyFilter:
Frequency cutoff of a variant **in percent**. Frequencies are derived from the databases defined in the **frequencySources** section. We recommend a value below 5.0% depending on the disease. Variants will be removed/failed if they have a frequency higher than the stated percentage in any database defined in the **frequencySources** section. _n.b_ Not defining this filter will result in all variants having no frequency data, even if the **frequencySources** contains values. Example `frequencyFilter: {maxFrequency: 1.0}`

##### pathogenicityFilter:
Will apply the pathogenicity scores defined in the **pathogenicitySources** section to variants. If the `keepNonPathogenic` field is set to `true` then all variants wil be kept. Setting this to `false` will set the filter to fail non-missense variants with pathogenicity scores lower than a score cutoff of 0.5. This filter is meant to be quite permissive. Example `pathogenicityFilter: {keepNonPathogenic: true}`

#### Gene filters {#gene_filters}
These act at the gene-level and therefore may also refer to the variants associated with the gene. As a rule this is discouraged, although is broken by the inheritanceFiler. 

##### priorityScoreFilter: 
Running the prioritizer followed by a priorityScoreFilter will remove genes which are least likely to contribute to the phenotype defined in hpoIds, this will dramatically reduce the time and memory required to analyze a genome. 0.501 is a good compromise to select good phenotype matches and the best protein-protein interactions hits using the hiPhive prioritizer. PriorityType can be one of `HIPHIVE_PRIORITY`, `PHIVE_PRIORITY`, `PHENIX_PRIORITY`, `OMIM_PRIORITY`, `EXOMEWALKER_PRIORITY`. Example `priorityScoreFilter: {priorityType: HIPHIVE_PRIORITY, minPriorityScore: 0.501}`

##### inheritanceFilter:
**inheritanceFilter** and **omimPrioritiser** should always run AFTER all other filters have completed. They will analyze genes according to the specified **modeOfInheritance** above. If set to `UNDEFINED` no filtering will be done. You can read more in the [Jannovar inheritance documentation](http://jannovar.readthedocs.io/en/master/ped_filters.html) how exactly this filter works. Example: `inheritanceFilter: {}`

#### Prioritisers {#prioritisers}
These work on the gene-level and will produce the semantic-similarity scores for how well a gene matches the sample's HPO profile.

##### omimPrioritiser:
**inheritanceFilter** and **omimPrioritiser** should always run AFTER all other filters have completed. Other prioritizers: Only combine **omimPrioritiser** with one of the next filters. The OMIM prioritiser adds known disease information from OMIM to genes including the inheritance mode and then checks this inheritance mode with the compatible inheritance modes of the gene. Genes with incompatible modes will have their scores halved. Example `omimPrioritiser: {}`

##### hiPhivePrioritiser:
Scores genes using phenotype comparisons to human, mouse and fish involving disruption of the gene or nearby genes in the interactome using a RandomWalk. Using the default `hiPhivePrioritiser: {}` is the same as `hiPhivePrioritiser: {runParams: 'human,mouse,fish,ppi'}`. It is possible to only run comparisons agains a given organism/set of organisms `human,mouse,fish` and include/exclude protein-protein interaction proximities `ppi`. e.g. only using human and mouse data - `hiPhivePrioritiser: {runParams: 'human,mouse'}` 

##### phenixPrioritiser:
Scores genes using phenotype comparisons to known human disease genes. Example `phenixPrioritiser: {}`

##### phivePrioritiser:
Scores genes using phenotype comparisons to mice with disruption of the gene. Example `phivePrioritiser: {}`

##### exomeWalkerPrioritiser:
Scores genes by proximity in interactome to the seed genes. Example `exomeWalkerPrioritiser: {seedGeneIds: [11111, 22222, 33333]}`

## outputOptions:

### outputPassVariantsOnly:
Can be `true` or `false`. Setting this to `true` will make the `TSV_VARIANT` and `VCF` output file only contain **PASS** variants.

### numGenes: 
Maximum number of genes listed in the results. If set to `0` all are listed. In most cases a limit of `50` is good.

### outputPrefix:
Specify the path/filename without an extension and this will be added according to the **outputFormats** option. If unspecified this will default to the following: `{exomiserDir}/results/input-vcf-name-exomiser-results.html`. Alternatively, specify a fully qualified path only. e.g. `/users/jules/exomes/analysis`.

### outputFormats:
Array to define the output formats. can be `[TSV-GENE]`, `[TSV-VARIANT]`, `[VCF]` or any combination like `[TSV-GENE, TSV-VARIANT, VCF]`. Output formats are described in this publication[^1].

[^1]: Smedley, Damian, et al. "Next-generation diagnostics and disease-gene discovery with the Exomiser." *Nature protocols* 10.12 (2015): 2004-2015.
