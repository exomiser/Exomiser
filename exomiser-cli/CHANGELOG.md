# The Exomiser Command Line Executable - Changelog

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