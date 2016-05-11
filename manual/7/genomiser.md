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
: Here you can specify which variant frequency databases you want to use. You can add multiple databases using the same array format like the hpoIDs. Possible options are [`THOUSAND_GENOMES`](http://www.1000genomes.org), [`ESP_AFRICAN_AMERICAN`, `ESP_EUROPEAN_AMERICAN`, `ESP_ALL`](http://evs.gs.washington.edu/EVS/), [`EXAC_AFRICAN_INC_AFRICAN_AMERICAN`, `EXAC_AMERICAN`, `EXAC_SOUTH_ASIAN`, `EXAC_EAST_ASIAN`,`EXAC_FINNISH`, `EXAC_NON_FINNISH_EUROPEAN`,`EXAC_OTHER`](http://exac.broadinstitute.org/about). We recommend to use all databases.

pathogenicitySources:
: Possible pathogenicitySources: `POLYPHEN`, `MUTATION_TASTER`, `SIFT`, `CADD`, `REMM`. `REMM` is trained on non-coding regulatory regions. **WARNING** if you enable `CADD`, ensure that you have downloaded and installed the `CADD` tabix files and updated their location in the application.properties. Exomiser will not run without this. An example is: `[POLYPHEN, MUTATION_TASTER, SIFT, REMM]`

steps:
: in this analysis section the different analysis steps are defined and are **important** the same ordering is used. We recommend the standard settings on genome wide analysis because of performance. But in general all steps are option. Steps must be defined in hash format. So this section has to start and to end with a square bracket. Steps are comma separated and written like *name: {options}*.  See the [steps section](#steps-section) for more details. **All steps are optional.** Uncomment them if you do not want them.

### Steps section

intervalFilter: 
: Define an interval of interest. Only variants of this interval will be analyzed. **TODO Are multiple intervals possible?**  Example: `intervalFilter: {interval: 'chr10:123256200-123256300'}`

geneIdFilter:
: You can define [entrez-gene-ids](http://www.ncbi.nlm.nih.gov/gene/) for genes of interest. Only variants associated with these genes will be analyzed. Example: `geneIdFilter: {geneIds: [12345, 34567, 98765]}`

hiPhivePrioritiser: 
: **TODO describe this** Example `hiPhivePrioritiser: {}`


priorityScoreFilter: 
: Running the prioritizer followed by a priorityScoreFilter will remove genes which are least likely to contribute to the phenotype defined in hpoIds, this will dramatically reduce the time and memory required to analyze a genome. 0.501 is a good compromise to select good phenotype matches and the best protein-protein interactions hits using the hiPhive prioritizer. **TODO add different priority types** Example `priorityScoreFilter: {priorityType: HIPHIVE_PRIORITY, minPriorityScore: 0.501}`

variantEffectFilter: 
: If you are interested only in specific functional classes of variants you can define a set of classes you want to remove from the output. Variant effects are generated by [Jannovar](http://charite.github.io/jannovar/). Jannovar uses [Sequence Ontology (SO)](http://www.sequenceontology.org/) terms and are listed in their [manual](http://jannovar.readthedocs.io/en/master/var_effects.html). Example `variantEffectFilter: {remove: [SYNONYMOUS_VARIANT]}`

regulatoryFeatureFilter:
: If set it removes all non-regulatory non-coding variants over 20Kb from a known gene. **TODO if set with empty {}, then it is active? What happen with variants over 20kb if you remove this filter?Are they are accosiated with genes in their TAD? This will be important to describe because of the TAD model** Example `regulatoryFeatureFilter: {}`

knownVariantFilter:
: Removes variants represented in the databases set by the option **frequencySources**. E.g. if you define `frequencySources: [THOUSAND_GENOMES]` every variant with an RS number will be removed. We do not recommend this option on recessive diseases. Example `knownVariantFilter: {}`


frequencyFilter:
: Frequency cutoff of a variant. Frequencies are derived from the databases defined by option **frequencySources**. The value is in percent! We recommend a value below 5.0% depending on the disease. Example `frequencyFilter: {maxFrequency: 1.0}`

pathogenicityFilter:
: **TODO** Example `pathogenicityFilter: {keepNonPathogenic: true}`

inheritanceFilter:
: **inheritanceFilter** and **omimPrioritiser** should always run AFTER all other filters have completed. They will analyze genes according to the specified **modeOfInheritance** above. If set to `UNDEFINED` no filtering will be done. You can read more in the [Jannovar inheritance documentation](http://jannovar.readthedocs.io/en/master/ped_filters.html) how exactly this filter works. Example: `inheritanceFilter: {}`

omimPrioritiser:
: **inheritanceFilter** and **omimPrioritiser** should always run AFTER all other filters have completed. Other prioritizers: Only combine **omimPrioritiser** with one of the next filters. **TODO What das this filter do? Is it about recessive/dominant genes vs given inheritance? Maybe we should rename it because of OMIM license stuff.** Example `omimPrioritiser: {}`


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

## Choose filters

[filters](../filters)
