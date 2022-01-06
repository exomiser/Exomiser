===============================
Analysis/Job file configuration
===============================

Exomiser analyses are defined using a `yml format <http://yaml.org/>`_ configuration file(s). Examples can be found in
the unpacked exomiser-cli-|version|/examples/ directory. Here you will find a mixture of samples, jobs, analyses,
outputOptions, phenopackets, phenopacket family, vcf and ped files.

.. _job:

Job
~~~

An Exomiser :ref:`job<job>` contains three sections:

 - :ref:`sample<sample>` or `phenopacket <https://phenopacket-schema.readthedocs.io/en/1.0.0/phenopacket.html>`_
 - :ref:`preset<preset>` or :ref:`analysis<analysis>`
 - :ref:`outputOptions<outputoptions>`


These fields define all the input data, analysis steps and output requirements for a complete Exomiser analysis. They can
be used as a record of how a sample was analysed. Alternatively the various sections can all be defined at runtime using
the CLI arguments as explained in the :ref:`inputandoptions` section.

which defines run mode, :ref:`filters<variantfilters>` and :ref:`prioritisers<prioritisers>`, and the :ref:`output options<outputoptions>` section that defines the output format,
output file and number of results that should be returned. Each of these sections can be defined independently on the
command line or provided as a single file as shown in the `job`_ section.

.. literalinclude:: ../exomiser-cli/src/test/resources/pfeiffer-job-phenopacket.yml
    :language: yaml


.. _sample:

Sample
~~~~~~

It is recommended to provide Exomiser the input sample as in `Phenopacket <https://phenopacket-schema.readthedocs.io/>`_ format.
Exomiser will accept this in either JSON or YAML format.

probandId:
----------

hpoIds:
-------
Input of the HPO identifiers/terms. You can select them using the `HPO browser <https://hpo.jax.org/>`_. Input must be in
array format. Terms are comma separated and delimited by single quotes. For example `['HP:0001156', 'HP:0001363', 'HP:0011304', 'HP:0010055']`.
It is *critical* that these are as detailed as possible and describe the observed phenotypes as fully and precisely as
possible. These are used by the phenotype matching algorithms and will heavily influence the outcome of an analysis.

vcf:
----
The variant file in `VCF format <https://github.com/samtools/hts-specs>`_. There can be variants of multiple samples from
one family in the file.

ped:
----
If you have multiple samples as input you have to define the pedigree using the `ped format <http://pngu.mgh.harvard.edu/~purcell/plink/data.shtml#ped>`_.
It is important that you correctly define affected and unaffected individuals. If you use ``X_RECESSIVE`` as mode of inheritance
be sure that the sex is correct (unknown is also fine).


.. _preset:

Preset
~~~~~~

.. code-block:: yaml

    # one of EXOME or GENOME. GENOME will require REMM to be available. Default is EXOME.
    preset: EXOME



.. _analysis:

Analysis
~~~~~~~~~

The analysis shown below is equivalent to the ``--preset exome`` argument or adding

.. code-block:: yaml

    preset: EXOME

to an Exomiser job file. In most cases it is recommended to use the default presets as these have been thoroughly
tested on the `UK 100,000 genome rare-disease cohort <https://www.nejm.org/doi/10.1056/NEJMoa2035790>`_. In case the user
requires anything different, it is possible to manually define the data sources and steps in an Exomiser analysis.


.. code-block:: yaml

    ---
    analysisMode: PASS_ONLY
    inheritanceModes: {
      AUTOSOMAL_DOMINANT: 0.1,
      AUTOSOMAL_RECESSIVE_COMP_HET: 2.0,
      AUTOSOMAL_RECESSIVE_HOM_ALT: 0.1,
      X_DOMINANT: 0.1,
      X_RECESSIVE_COMP_HET: 2.0,
      X_RECESSIVE_HOM_ALT: 0.1,
      MITOCHONDRIAL: 0.2
    }
    frequencySources: [
      THOUSAND_GENOMES,
      TOPMED,
      UK10K,

      ESP_AFRICAN_AMERICAN, ESP_EUROPEAN_AMERICAN, ESP_ALL,

      EXAC_AFRICAN_INC_AFRICAN_AMERICAN, EXAC_AMERICAN,
      EXAC_SOUTH_ASIAN, EXAC_EAST_ASIAN,
      EXAC_FINNISH, EXAC_NON_FINNISH_EUROPEAN,
      EXAC_OTHER,

      GNOMAD_E_AFR,
      GNOMAD_E_AMR,
      #        GNOMAD_E_ASJ,
      GNOMAD_E_EAS,
      GNOMAD_E_FIN,
      GNOMAD_E_NFE,
      GNOMAD_E_OTH,
      GNOMAD_E_SAS,

      GNOMAD_G_AFR,
      GNOMAD_G_AMR,
      #        GNOMAD_G_ASJ,
      GNOMAD_G_EAS,
      GNOMAD_G_FIN,
      GNOMAD_G_NFE,
      GNOMAD_G_OTH,
      GNOMAD_G_SAS
    ]
    pathogenicitySources: [POLYPHEN, MUTATION_TASTER, SIFT]
    #this is the standard exomiser order.
    steps: [
      variantEffectFilter: {
        remove: [
          FIVE_PRIME_UTR_EXON_VARIANT,
          FIVE_PRIME_UTR_INTRON_VARIANT,
          THREE_PRIME_UTR_EXON_VARIANT,
          THREE_PRIME_UTR_INTRON_VARIANT,
          NON_CODING_TRANSCRIPT_EXON_VARIANT,
          UPSTREAM_GENE_VARIANT,
          INTERGENIC_VARIANT,
          REGULATORY_REGION_VARIANT,
          CODING_TRANSCRIPT_INTRON_VARIANT,
          NON_CODING_TRANSCRIPT_INTRON_VARIANT,
          DOWNSTREAM_GENE_VARIANT
        ]
      },
      frequencyFilter: {maxFrequency: 2.0},
      pathogenicityFilter: {keepNonPathogenic: true},
      # inheritanceFilter and omimPrioritiser should always run AFTER all other filters have completed
      # they will analyse genes according to the specified modeOfInheritance above- UNDEFINED will not be analysed.
      inheritanceFilter: {},
      # omimPrioritiser isn't mandatory, but is highly recommended
      omimPrioritiser: {},
      hiPhivePrioritiser: {}
    ]



analysisMode:
-------------
Can be ``FULL`` or ``PASS_ONLY``. We highly recommend ``PASS_ONLY`` on genomes for because of memory issues. It will only
keep variants that passes all filters. ``FULL`` will keep all variants, using the most memory and requiring the most time,
but it can be useful for troubleshooting why a particular variant of interest might not have been returned in the results
of an analysis.

.. code-block:: yaml

    analysisMode: PASS_ONLY


inheritanceModes:
------------------
Can be ``AUTOSOMAL_DOMINANT``, ``AUTOSOMAL_RECESSIVE``, ``X_RECESSIVE`` or ``UNDEFINED``. This is a functionality of Jannovar.
See its `inheritance documentation <http://jannovar.readthedocs.io/en/master/ped_filters.html>`_ for further information.

.. code-block:: yaml

    # These are the default settings, with values representing the maximum minor allele frequency in percent (%) permitted for an
    # allele to be considered as a causative candidate under that mode of inheritance.
    # If you just want to analyse a sample under a single inheritance mode, delete/comment-out the others. For AUTOSOMAL_RECESSIVE
    # or X_RECESSIVE ensure *both* relevant HOM_ALT and COMP_HET modes are present.
    # In cases where you do not want any cut-offs applied an empty map should be used e.g. inheritanceModes: {}
    inheritanceModes: {
            AUTOSOMAL_DOMINANT: 0.1,
            AUTOSOMAL_RECESSIVE_HOM_ALT: 0.1,
            AUTOSOMAL_RECESSIVE_COMP_HET: 2.0,
            X_DOMINANT: 0.1,
            X_RECESSIVE_HOM_ALT: 0.1,
            X_RECESSIVE_COMP_HET: 2.0,
            MITOCHONDRIAL: 0.2
    }


.. _frequencysources:

frequencySources:
-----------------

Here you can specify which variant frequency databases you want to use. You can add multiple databases using the same
array format as the HPO IDs.

The data sources used are from `1000 genomes <http://www.1000genomes.org>`_ (via DBSNP), `DBSNP <https://www.ncbi.nlm.nih.gov/projects/SNP/>`_,
`ESP <https://evs.gs.washington.edu/EVS/>`_, `ExAC, gnomAD exomes and gnomAD genomes <https://gnomad.broadinstitute.org/about>`_,
`UK10K <https://www.uk10k.org/>`_ (via DBSNP), `TOPMed <https://topmed.nhlbi.nih.gov/>`_ (via DBSNP).

DBSNP:
    ``THOUSAND_GENOMES``,
    ``UK10K``,
    ``TOPMED``

ESP:
    ``ESP_AFRICAN_AMERICAN``, ``ESP_EUROPEAN_AMERICAN``, ``ESP_ALL``

ExAC:
    ``EXAC_AFRICAN_INC_AFRICAN_AMERICAN``,
    ``EXAC_AMERICAN``,
    ``EXAC_SOUTH_ASIAN``,
    ``EXAC_EAST_ASIAN``,
    ``EXAC_FINNISH``,
    ``EXAC_NON_FINNISH_EUROPEAN``,
    ``EXAC_OTHER``

gnomAD exomes:
    ``GNOMAD_E_AFR``,
    ``GNOMAD_E_AMR``,
    ``GNOMAD_E_ASJ``,
    ``GNOMAD_E_EAS``,
    ``GNOMAD_E_FIN``,
    ``GNOMAD_E_NFE``,
    ``GNOMAD_E_OTH``,
    ``GNOMAD_E_SAS``,

gnomAD genomes:
    ``GNOMAD_G_AFR``,
    ``GNOMAD_G_AMR``,
    ``GNOMAD_G_ASJ``,
    ``GNOMAD_G_EAS``,
    ``GNOMAD_G_FIN``,
    ``GNOMAD_G_NFE``,
    ``GNOMAD_G_OTH``,
    ``GNOMAD_G_SAS``

We recommend using all databases if the proband population background is unknown, although removing the ``GNOMAD_E_ASJ`` and ``GNOMAD_G_ASJ``, unless your proband is
known to come from an Ashkenazi population e.g.

.. code-block:: yaml

    frequencySources: [
      THOUSAND_GENOMES,
      TOPMED,
      UK10K,

      ESP_AFRICAN_AMERICAN, ESP_EUROPEAN_AMERICAN, ESP_ALL,

      EXAC_AFRICAN_INC_AFRICAN_AMERICAN, EXAC_AMERICAN,
      EXAC_SOUTH_ASIAN, EXAC_EAST_ASIAN,
      EXAC_FINNISH, EXAC_NON_FINNISH_EUROPEAN,
      EXAC_OTHER,

      GNOMAD_E_AFR,
      GNOMAD_E_AMR,
      #        GNOMAD_E_ASJ,
      GNOMAD_E_EAS,
      GNOMAD_E_FIN,
      GNOMAD_E_NFE,
      GNOMAD_E_OTH,
      GNOMAD_E_SAS,

      GNOMAD_G_AFR,
      GNOMAD_G_AMR,
      #        GNOMAD_G_ASJ,
      GNOMAD_G_EAS,
      GNOMAD_G_FIN,
      GNOMAD_G_NFE,
      GNOMAD_G_OTH,
      GNOMAD_G_SAS
    ]


.. _pathogenicitysources:

pathogenicitySources:
---------------------
Possible pathogenicitySources: ``POLYPHEN``, ``MUTATION_TASTER``, ``SIFT``, ``REVEL``, ``MVP``, ``CADD``, ``REMM``. ``REMM`` is trained on
non-coding regulatory regions. **WARNING** if you enable ``CADD``, ensure that you have downloaded and installed the CADD
tabix files and updated their location in the ``application.properties`` (see :ref:`cadd-install`). Exomiser will not run
without this.

We recommend using either  ``[REVEL, MVP]`` **OR** ``[POLYPHEN, MUTATION_TASTER, SIFT]`` as REVEL and MVP are newer
predictors which have been shown to have better performance and are more nuanced. Mixing them with the Polyphen2,
MutationTaster or SIFT will give worse performance.

.. code-block:: yaml

    pathogenicitySources: [REVEL, MVP, REMM]


.. _steps:

steps
-----
This section instructs exomiser which analysis steps should be run and with which criteria. **_n.b._ the order in which the steps are declared is important**
- exomiser will run them in the order declared, although certain optimisations will happen automatically. We recommend
using the [standard settings](../example/test-analysis-genome) for genome wide analysis as these have been optimised for
both speed and memory performance. Nonetheless all steps are optional. Being an array of steps, this section must be
enclosed in square brackets. Steps are comma separated and written in hash format *name: {options}*. **All steps are optional**
- comment them out or delete them if you do not want to use them.

Analysis steps are defined in terms of :ref:`variant filters<variantfilters>`, :ref:`gene filters<genefilters>` or
:ref:`prioritisers<prioritisers>`. The :ref:`inheritanceFilter<inheritancefilter>` and :ref:`omimPrioritiser<omimprioritiser>` are both somewhat anomalous as they
operate on genes but also require the variants to have already been filtered. The optimiser will ensure that these are
run at the correct time if they have been incorrectly placed.

Using these it is possible to create artificial exomes, define gene panels or only examine specific regions, for example.

.. _variantfilters:

Variant filters
...............

These operate on variants and will produce a pass or fail result for each variant run through the filter.

intervalFilter:
...............
Define an interval of interest. Only variants within this interval will be passed. Currently only single intervals are
possible.

.. code-block:: yaml

    intervalFilter: {interval: 'chr10:123256200-123256300'}


geneIdFilter:
.............
You can define `entrez-gene-ids <http://www.ncbi.nlm.nih.gov/gene/>`_ for genes of interest. Only variants associated with
these genes will be analyzed.

.. code-block:: yaml

    geneIdFilter: {geneIds: [12345, 34567, 98765]}


variantEffectFilter:
....................
If you are interested only in specific functional classes of variants you can define a set of classes you want to remove
from the output. Variant effects are generated by `Jannovar <http://charite.github.io/jannovar/>`_. Jannovar uses
`Sequence Ontology (SO) <http://www.sequenceontology.org/>`_ terms and are listed in their `manual <http://jannovar.readthedocs.io/en/master/var_effects.html>`_.

.. code-block:: yaml

    variantEffectFilter: {remove: [SYNONYMOUS_VARIANT]}


regulatoryFeatureFilter:
........................
If included it removes all non-regulatory, non-coding variants over 20Kb from a known gene. Intergenic and upstream
variants in known enhancer regions over 20kb from a known gene are associated with genes in their TAD and not effected
by this filter. This is an important filter to include when analysing whole-genome data.

.. code-block:: yaml

    regulatoryFeatureFilter: {}


knownVariantFilter:
...................
Removes variants represented in the databases set in the :ref:`frequencySources<frequencysources>` section. E.g. if you define

.. code-block:: yaml

    frequencySources: [THOUSAND_GENOMES]


every variant with an RS number will be removed. Variants without an RSid will be removed/failed if they are represented
in any database defined in the :ref:`frequencySources<frequencysources>` section. We do not recommend this option on recessive diseases.

.. code-block:: yaml

    knownVariantFilter: {}


.. _frequencyfilter:

frequencyFilter:
................
Frequency cutoff of a variant **in percent**. Frequencies are derived from the databases defined in the :ref:`frequencySources<frequencysources>`
section. We recommend a value below 5.0 % depending on the disease. Variants will be removed/failed if they have a
frequency higher than the stated percentage in any database defined in the :ref:`frequencySources<frequencysources>` section.
_n.b_ Not defining this filter will result in all variants having no frequency data, even if the :ref:`frequencySources<frequencysources>`
contains values.

.. code-block:: yaml

    frequencyFilter: {maxFrequency: 1.0}


.. _pathogenicityfilter:

pathogenicityFilter:
....................
Will apply the pathogenicity scores defined in the :ref:`pathogenicitySources<pathogenicitysources>` section to variants.
If the ``keepNonPathogenic`` field is set to ``true`` then all variants will be kept. Setting this to ``false`` will set
the filter to fail non-missense variants with pathogenicity scores lower than a score cutoff of 0.5.
This filter is meant to be quite permissive and we recommend it be set to true.

.. code-block:: yaml

    pathogenicityFilter: {keepNonPathogenic: true}


.. _genefilters:

Gene filters
............
These act at the gene-level and therefore may also refer to the variants associated with the gene. As a rule this is
discouraged, although is broken by the inheritanceFiler.

priorityScoreFilter:
....................
Running the prioritizer followed by a priorityScoreFilter will remove genes which are least likely to contribute to the
phenotype defined in hpoIds, this will dramatically reduce the time and memory required to analyze a genome. 0.501
is a good compromise to select good phenotype matches and the best protein-protein interactions hits using the hiPhive
prioritiser. PriorityType can be one of ``HIPHIVE_PRIORITY``, ``PHIVE_PRIORITY``, ``PHENIX_PRIORITY``, ``OMIM_PRIORITY``, ``EXOMEWALKER_PRIORITY``.

.. code-block:: yaml

    priorityScoreFilter: {priorityType: HIPHIVE_PRIORITY, minPriorityScore: 0.501}


.. _inheritancefilter:

inheritanceFilter:
..................
**inheritanceFilter** and **omimPrioritiser** should always run AFTER all other filters have completed. They will analyze
genes according to the specified **modeOfInheritance** above. If set to ``UNDEFINED`` no filtering will be done.
You can read more in the `Jannovar inheritance documentation <http://jannovar.readthedocs.io/en/master/ped_filters.html>`_ how exactly this filter works.

.. code-block:: yaml

    inheritanceFilter: {}


.. _prioritisers:

Prioritisers
............

These work on the gene-level and will produce the semantic-similarity scores for how well a gene matches the sample's HPO
profile. We recommend using a combination of the :ref:`OMIM<omimprioritiser>` and :ref:`hiPHIVE<hiphiveprioritiser>` prioritisers **only**. These have been tested
on the UK's `100,000 genomes pilot project on rare disease diagnoses <https://www.nejm.org/doi/10.1056/NEJMoa2035790>`_.
Using other prioritisers may miss diagnoses as the data could be out of date or the algorithm's effectiveness may have
been superseded. They are retained for historical interest.

.. _omimprioritiser:

omimPrioritiser:
.................
**inheritanceFilter** and **omimPrioritiser** should always run AFTER all other filters have completed. Other prioritisers:
Only combine **omimPrioritiser** with one of the next filters. The OMIM prioritiser adds known disease information from
OMIM to genes including the inheritance mode and then checks this inheritance mode with the compatible inheritance modes
of the gene. Genes with incompatible modes will have their scores halved.

.. code-block:: yaml

    omimPrioritiser: {}


.. _hiphiveprioritiser:

hiPhivePrioritiser:
...................
Scores genes using phenotype comparisons to human, mouse and fish involving disruption of the gene or nearby genes in the
interactome using a random walk.

See the `hiPHIVE publication <https://doi.org/10.1038/gim.2015.137>`_ for details.


.. code-block:: yaml

    # Using the default
    hiPhivePrioritiser: {}


is the same as

.. code-block:: yaml

    hiPhivePrioritiser: {runParams: 'human, mouse, fish, ppi'}


It is possible to only run comparisons agains a given organism/set of organisms ``human,mouse,fish`` and include/exclude
protein-protein interaction proximities ``ppi``. e.g. only using human and mouse data -

.. code-block:: yaml

    hiPhivePrioritiser: {runParams: 'human,mouse'}


phenixPrioritiser:
..................
Scores genes using phenotype comparisons to known human disease genes. See the `PhenIX publication <https://www.science.org/doi/10.1126/scitranslmed.3009262>`_ for details.

.. code-block:: yaml

    phenixPrioritiser: {}


phivePrioritiser:
.................

Scores genes using phenotype comparisons to mice with disruption of the gene. This is equivalent to running ``hiPhivePrioritiser: {runParams: 'mouse'}``.
See the `PHIVE publication <https://doi.org/10.1093/database/bat025>`_ for details.

.. code-block:: yaml

    phivePrioritiser: {}



exomeWalkerPrioritiser:
.......................
Scores genes by proximity in interactome to the seed genes. Gene identifiers are required to be genbank identifiers.
See the `ExomeWalker publication <https://doi.org/10.1093/bioinformatics/btu508>`_ for details.

.. code-block:: yaml

    exomeWalkerPrioritiser: {seedGeneIds: [11111, 22222, 33333]}


.. _outputoptions:

Output options
~~~~~~~~~~~~~~

When specified as part of a command-line argument, the file should be a properly formed YAML object:

.. code-block:: yaml

    # Exomiser outputOptions
    ---
    outputContributingVariantsOnly: true
    numGenes: 20
    minExomiserGeneScore: 0.7
    outputPrefix: /analyses/sample-12345/sample-12345
    outputFormats: [HTML, JSON]


When included as part of an exomiser :ref:`job<job>` the fields are inlined in the ``exomiser-job.yaml`` file like so:

.. code-block:: yaml

    # other analysis and sample options above
    outputOptions:
        outputContributingVariantsOnly: true
        numGenes: 20
        minExomiserGeneScore: 0.7
        outputPrefix: /analyses/sample-12345/sample-12345
        outputFormats: [HTML, JSON]


outputContributingVariantsOnly:
-------------------------------
Can be ``true`` or ``false``. Setting this to ``true`` will make the ``TSV_VARIANT`` and ``VCF`` output file only contain
**PASS** variants which contribute to the gene score. Defaults to ``true``.

numGenes:
---------
Maximum number of genes listed in the results. If set to ``0`` all are listed. In most cases a limit of ``50`` is more
than adequate as typically a good result will be found in the top 5. Not enabled by
default.

minExomiserGeneScore
--------------------
The mimimum gene (combined phenotype and variant) score required to be returned. As a rule of thumb scores >= 0.7 are a
good score however, depending on the proband phenotype and the phenotype of best matching condition although it is not a
hard-and-fast number. In our testing 0.7 gave the best performance in terms or recall and sensitivity. Not enabled by
default.

outputPrefix:
-------------
Specify the path/filename without an extension and this will be added according to the
:ref:`outputFormats<outputformats>` option. If unspecified this will default to the following:
``{exomiserDir}/results/input-vcf-name-exomiser-results.html``. Alternatively, specify a fully qualified path
only. e.g. ``/home/jules/exomes/analysis``.

.. _outputformats:

outputFormats:
--------------
Array to define the output formats. can be ``[TSV-GENE]``, ``[TSV-VARIANT]``, ``[VCF]``, ``JSON`` or ``HTML`` or any
combination like ``[TSV-GENE, TSV-VARIANT, VCF]``. JSON is the most informative output option and suitable for use in
downstream computational analyses or manually queried using something like `jq <https://stedolan.github.io/jq/>`_. The
HTML output is most suitable for manual inspection / human use.
Default is ``[JSON, HTML]``.
