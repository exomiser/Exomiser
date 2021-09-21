# The Exomiser Command Line Executable - Changelog

## 13.0.0 2021-09-21

- Minimum Java version is now set to __Java 11__
- New structural variant interpretation alongside small variants - requires data version __2109__ or higher. This has
  been tested using Manta and Canvas short-read callers and Sniffles long-read caller.
- New command line options for more flexible input: --sample --output, --vcf, --batch, --preset --assembly --ped . Run
  --help for details
- Phenopackets v1.0 can be used to input sample phenotype data
- Added ability to specify proband age and sex in input options either via a phenopacket or the 'sample' format
- Improved MOI disease - phenotype matching with added Orphanet MOIs
- Improved incomplete penetrance calculation when using the ANY mode of inheritance option
- Added a `minExomiserGeneScore` option for limiting the output genes to have a mimimum Exomiser combined score. This is
  disabled by default. If enabling it, we recommend using a minimum score of 0.7
- __BREAKING CHANGE__ - JSON output changes `pos` renamed as `start`, `chrmosomeName` renamed as `contigName`.
  Deleted `chromosome` field (use `contigName`). New fields: `end`, `length`, `changeLength` and `variantType`

## 12.1.0 2019-09-25

- The JSON output now shows the id of the variantEvaluation taken from the VCF file.

## 12.0.0 2019-02-28

This release contains significant diagnostic performance improvements due to the inclusion of a high-quality ClinVar
whitelist and 'second generation' pathogenicity scores.

- Added new ```PathogenicitySource``` sources - ```M_CAP, MPC, MVP, PRIMATE_AI```. Be aware that these may not be free
  for commercial use. _Check the licencing before use!_
- Added new variant whitelist feature which enables flagging of variants on a whitelist and bypassing
  of ```FrequencyFilter``` and ```VariantEffectFilter```. By default this will use ClinVar variants listed
  as ```Pathogenic``` or ```Likely_pathogenic``` and with a review status of ```criteria provided, single submitter```
  or better. See https://www.ncbi.nlm.nih.gov/clinvar/docs/review_status/ for an explanation of the ClinVar review
  status.

## 11.0.0 2018-09-21
- Removed ```analysisMode: SPARSE``` option - this will default to ```PASS_ONLY```
- Removed ```phenixPrioritiser: {}``` option - we recommend using ```hiPhivePrioritiser: {runParams: 'human'}``` for human-only model comparisons
- Changed ```outputPassVariantsOnly``` to ```outputContributingVariantsOnly``` in ```outputOptions```. Enabling this will only report the variants marked as ```CONTRIBUTING_VARIANT```, _i.e._ those variants which contribute to the ```EXOMISER_GENE_VARIANT_SCORE``` and ```EXOMISER_GENE_COMBINED_SCORE``` score. This will default to ```false```.   
    ```yaml
    outputOptions:
         outputContributingVariantsOnly: false
    ```

## 10.1.0 2018-05-09
- Added support for filtering multiple intervals in the ```intervalFilter``` 
    ```yaml
    # single interval
    intervalFilter: {interval: 'chr10:123256200-123256300'},
    # or for multiple intervals:
    intervalFilter: {intervals: ['chr10:123256200-123256300', 'chr10:123256290-123256350']},
    # or using a BED file - NOTE this should be 0-based, Exomiser otherwise uses 1-based coordinates in line with VCF
    intervalFilter: {bed: /full/path/to/bed_file.bed}
    ```
- Added support for ClinVar annotations - available in the 1805 variant data release. These will appear automatically and are reported for information only. 
- Added ```JSON``` output format
    ```yaml
    outputFormats: [HTML, JSON, TSV_GENE, TSV_VARIANT, VCF]
    ```

## 10.0.1 2018-03-20
- Updated HTSJDK library to fix ```TribbleException``` being thrown when trying to parse bgzipped VCF files

## 10.0.0 2018-03-07
- Deprecated extended cli options as these were less capable than the analysis file. Options are now ```--analysis``` or ```--analysis-batch``` only. See the ```.yml``` files in the ```examples``` directory for recommended scripts.
- Exomiser can now analyse samples against multiple inheritance modes in one run using the new ```inheritanceModes``` field. This also allows variants to be considered under a model with a maximum frequency (%) cut-off. See example ```.yml``` files for more details. 
     ```yaml
        inheritanceModes: {
             AUTOSOMAL_DOMINANT: 0.1,
             AUTOSOMAL_RECESSIVE_HOM_ALT: 0.1,
             AUTOSOMAL_RECESSIVE_COMP_HET: 2.0,
             X_DOMINANT: 0.1,
             X_RECESSIVE_HOM_ALT: 0.1,
             X_RECESSIVE_COMP_HET: 2.0,
             MITOCHONDRIAL: 0.2
        }
     ```
- The old ```modeOfInheritance``` option will still work, although it will only run with default frequency cut-offs and may be removed in a later release, so please update your analyses.
- The new ```1802_phenotype``` data release will not work on older exomiser versions as the PPI data is now shipped in a much more efficient storage format. This reduces the startup time to zero and reduces the memory footprint by approx 1 GB. We *highly* recommend you update older releases to the latest version in order to benefit from more recent phenotype data.
- Default variant scores for ```FRAMESHIFT```, ```NONSENSE```, ```SPLICING```, ```STOPLOSS``` and ```STARTLOSS``` have been increased from 0.95 to the maximum score of 1.0 to reflect clinical interpretation of these variant consequences.

## 9.0.1 2018-01-15
- Now able to analyse ```MITOCHONDRIAL``` inheritance mode.

## 9.0.0 2017-12-12
- Exomiser can now analyse hg19 or hg38 samples - see ```application.properties``` for setup details.
- Analysis file has new ```genomeAssembly:``` field - see example ```.yml``` files. Will default to hg19 if not specified.
- Genomic and phenotypic data are now separated to allow for more frequent and smaller updates - see README.md for details
- Variant alleles are now stored in a new highly-compressed data format enabling much smaller on-disk footprint with minimal loss of read performance.
- New variant frequency data-sets: TOPMed, UK10K, gnomAD - see example ```.yml``` files.
- New caching mechanism - see ```application.properties``` for setup details.

## 8.0.0 2017-08-08
- See https://github.com/exomiser/Exomiser/projects/2 for a complete list of changes.
- ```application.properties``` file has changed to use ```exomiser``` namespace prefix. Will allow property placeholder substitution - e.g. ```exomiser.property=foo``` can be used elsewhere in the file as ```${exomiser.property}```. Will support user-defined property values too. 
- Analysis file now requires ```proband``` id to be specified. Bug-fix for multi-sample VCF files where the proband sample is not the first sample in the genotypes section leading to occasional scores of 0 for the exomiser_gene_variant_score in cases where the variants are heterozygous and consistent with autosomal recessive.
- Analysis file ```scoringMode``` option has now been removed as it was never used.
- Analysis now supports a new ```failedVariantFilter: {}``` to remove variants without a ```PASS``` or ```.``` in the FILTER field.
- Can now filter variants by LOCAL frequency source.
- It is now possible to use UCSC, ENSEMBL or REFSEQ transcript identifiers.
- REMM data is no longer bundled with the distribution. If you want to use this for non-coding variant pathogenicity scoring you'll need to manually download and install it.
- Memory requirements are now reduced.
- Fixed AR comp-het scoring bug.
- Now partly normalises incoming variant data enabling better performance for multi-allelic sites.
- Variants contributing to the exomiser score are now flagged in output files.
- VCF output now has valid headers for info fields and more informative information.
- VCF output no longer contain invalid values in FILTER field for failed variants.
- VCF lines containing multiple alleles now contain the field ```ExContribAltAllele``` with an zero-based integer indicating the ALT allele contributing to the score.
- HTML output now shows individual variant scores and flags contributing variants along with displaying them first.
- HTML output tweaked to display data more clearly in the genes section.


## 7.2.3 2016-11-02 
- Partial bug-fix for multi-sample VCF files where the proband sample is not the first sample in the genotypes section leading to occasional scores of 0 for the exomiser_gene_variant_score in cases where the variants are heterozygous and consistent with autosomal recessive.

*IMPORTANT!* As a workaround for this issue ensure the proband sample is the first sample in the VCF file. This will be properly fixed in the next major release.

## 7.2.2 2016-07-01 
- Fix for issue when using OmimPrioritiser with UNDEFINED inheritance mode which led to gene phenotype scores being halved.
- Fix for VCF output multiple allele line duplications. VCF output will now have alternate alleles written out on the same line if they were originally like that in the input VCF. The variant scores will be concatenated to correspond with the alleles. VCFs containing alleles split onto seperate lines in the input file will continue to have them like this in the output file.

## 7.2.1 2016-01-05
- Fix for incorrect inheritance mode calculations where the variant chromosome number is prefixed with 'chr' in VCF file.

## 7.2.0 2015-11-25
- Performance in identification of causal regulatory variants as the top candidate of simulated whole genomes now improved to over 80%.
- Enhancer variants are assigned to TADs
- Variant gene assignment improvements and bug-fixes.

## 7.1.0 2015-10-21
- Variants in FANTOM5 enhancer and ENSEMBLE regulatory regions are now all marked REGULATORY_REGION_VARIANT even without
 the regulatoryFeatureFilter being run.
- Massive performance increase when running regulatoryFeatureFilter.
- Running Exomiser in exome analysis mode now requires REGULATORY_FEATURE to be included in the variantEffectFilter.
See test-analysis-exome.yml
- Added missing regulatoryFeatureFilter step from the analysis steps in test-analysis-genome.yml

## 7.0.0 2015-10-01
Now requires Java 8 or higher to run.
- The Exomiser is now somewhat inaccurately named as it can now analyse whole-genome samples.
- Exomiser is now much more customisable with the new analysis YAML configuration files. See test-analysis-exome.yml and
 test-analysis-genome.yml for examples
- Added new --analysis and --analysis-batch commands to be used with the new analysis format.
- New PASS_ONLY analysis mode. Exomiser will only keep variants which passed filters. This allows for dramatically
reduced memory requirements which is especially handy for genome-sized analyses.
- Exomiser now ships with a new pathogenicity score for predicting the deleteriousness of non-coding regulatory mutations,
 the REMM score.
- It is now possible to specify which pathogenicity score or scores you wish to be run out of, polyphen, SIFT, Mutation Taster,
 CADD and REMM
- We now include the variant frequencies from the ExAC dataset and allow for a subset of all frequency sources to be
specified in an analysis. For example, this allows only frequencies from a particular population to be taken into account.

## 6.0.0 2015-01-12
- Added 'none' type prioritiser for when you really don't want to run any prioritiser.
- Exomiser will now show the help options when no parameters are supplied.
- New test settings files for different prioritisers and the batch file.
- Changed input parameters these are optional switches:
    --remove-path-filter-cutoff to --keep-non-pathogenic 
    --remove-off-target-syn to --keep-off-target
- Renamed somewhat misleading example.settings to template.settings to reflect it's intended use.
- TSV output now comes in TSV_GENE and TSV_VARIANT flavours.
- Added missing ehcache.xml to the distribution.
- Switched PostgreSQL driver to use pgjdbc-ng which allegedly has better performance.
- Consolidated JDBC Connection pool to use HikariCP. 
 
## 5.2.0 2014-12-18
- No changes to exomiser-cli

## 5.1.0 2014-12-12
- No changes to exomiser-cli

## 5.0.1 2014-11-14
- No changes to exomiser-cli

## 5.0.0 2014-11-14
- New filter option --genes-to-keep  Allows filtering by Entrez gene ID to keep only those genes specified.
- Added caching options which may significantly increase performance at the expense of memory - see application.properties.
- Changed 'username' in application.properties to 'dbuser' to prevent the current user's username from being used to authenticate against the PostgreSQL database on Windows.
- Added missing full-analysis option to test and example.settings
- Updated external dependencies
- Lots of under the hood changes and increased test coverage in exomiser-core.

## 4.0.1 2014-10-23
- Fixed bug where OMIM prioritiser did not work when inheritance model was not specified
- Adjustment of the exomiser-allspecies algorithm for PPI hits

## 4.0.0 2014-09-19
- Changed FilterScores to FilterResults to encapsulate the pass/fail , score and filterTypes returned from the filters in various ways previously.
- Changed Filter behaviour to simply return a FilterResult instead of altering the VariantEvaluation in inconsistent ways.
- VariantEvaluation now hides a bit more of its inner workings regarding FilterResults.
- PathogenicityData will now return its own most pathogenic score instead of relying on something else to figure this out.

- Major changes to PathogenicityFilter behaviour - Missense variants will always pass the filter regardless of their predicted pathogenicity. Other variant types can now be filtered according to the cutoff score or allowed to pass irrespective of the score.
- Command line option changes:
    -P --remove-path-filter-cutoff command line option added to allow all variant types through pathogenicity filter.
    -P --keep-non-pathogenic-missense command line option removed.
    -P option default changed from true to false! Sorry. Check your existing settings carefully!

- Added GeneFilter functionality
- Renamed Scorable interface to Score
- Renamed VariantEvaluation variables and accessor methods:
    getFilterScoreMap to getFilterResults to match how it is referred to in the code output.
    getFailedFilters to getFailedFilterTypes
    passesFilters to passedFilters

- Bug-fixes
    - Prioritisers now release database connections when finished (affects batch-mode performance)
    - Inheritance filter now performs correctly in all cases.

## 3.0.2 2014-09-08
- VCF output now contains original VCF INFO field with exomiser info appended onto this.
- Bug-fix for crash when Jannovar found no annotations for a variant.

## 3.0.1 2014-09-04
- Bug-fix for duplicate variants in Frequency table where the RSID was different.

## 3.0.0 2014-08-22
- Completely re-worked under the hood code
- New extensible API
- Simplified command-line usage
- Multiple output formats
- Batch mode analysis
- Settings file input
- Zero-config installation

## 2.1.0 2014-05-06
- Embedded H2 database or PostgreSQL
- Simplified set-up/installation