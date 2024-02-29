.. _inputandoptions:

=======================
Input Files and Options
=======================

The Exomiser can be run via simply via a yaml analysis file. The extended cli capability was removed in version 10.0.0
as this was less capable than the yaml scripts and only supported hg19 exome analysis. Version 13.0.0 introduced a more
flexible input using a `GA4GH Phenopacket <https://phenopacket-schema.readthedocs.io>`_ (v 1.0) for the sample data with
the ability to specify the input pedigree, VCF and genome assembly independently; user-specified, preset or default
analysis options and a new batch mode.


Sample, vcf, assembly, ped
==========================

It is recommended to provide Exomiser with the input sample as a `Phenopacket <https://phenopacket-schema.readthedocs.io/en/1.0.0/phenopacket.html>`_.
Exomiser will accept this in either JSON or YAML format. The sample is provided using the ``sample`` switch and
the full path to the phenopacket file:

.. parsed-literal::

    java -jar exomiser-cli-|version|.jar --sample path/to/phenopacket.json


Should the phenopacket either not specify a VCF file or specifies a file on another filesystem, the VCF file path can be
provided/overridden using the ``vcf`` option. This option requires that the genome assembly the VCF file was called against
is also specified using the ``assembly`` option:

.. parsed-literal::

    java -jar exomiser-cli-|version|.jar --sample path/to/phenopacket.json --vcf path/to/genome.vcf --assembly hg19


or for hg38/ GRCh38:

.. parsed-literal::

    java -jar exomiser-cli-|version|.jar --sample path/to/phenopacket.json --vcf path/to/genome.vcf --assembly hg38


Lastly, when analysing a multi-sample VCF a pedigree is required. This can be provided using a dedicated PED file. This
uses the ``ped`` switch and a full path to the PED file:

.. parsed-literal::

    java -jar exomiser-cli-|version|.jar --sample path/to/phenopacket.json --vcf path/to/genome.vcf --assembly hg38 --ped path/to/pedigree.ped


or the pedigree, proband and family members can be provided as a phenopacket `family <https://phenopacket-schema.readthedocs.io/en/1.0.0/family.html>`_ message
which can encode the pedigree.

.. parsed-literal::

    java -jar exomiser-cli-|version|.jar --sample path/to/family.json --vcf path/to/genome.vcf --assembly hg38


Whatever the input used it is essential that the sample names used for the proband and other family members are consistent between the
pedigree and the sample identifiers used in the VCF file. Exomiser will exit with an error explaining that they do not match.
Examples of these can be found in the `examples` directory of the installation.


Preset
======

If no ``analysis`` is provided and no preset is specified, Exomiser will default to running the ``exome`` preset analysis.
If you want to run Genomiser, which will analyse non-coding regions of a WGS sample use ``--preset genome``:

.. parsed-literal::

    java -jar exomiser-cli-|version|.jar --sample path/to/phenopacket.json --preset genome


In order to run a ``genome`` preset you need to first ensure that the REMM score data has been downloaded for the relevant
genome assembly and is enabled in the ``application.properties`` see the :ref:`remm` section.


Analysis
========

.. important::

    The exome and genome analyses found in the `test-analysis-exome.yml` and `test-analysis-genome.yml` files are
    recommended for use in most situations, and removing steps from the analysis is likely to negatively impact
    performance. It is *strongly* recommended to test any changes against the standard setup on the example samples and
    your own solved cases to check the impact of any changes you might want to make. If you want to score all variants
    and write failed ones to the output, it is recommended to use `analysisMode: FULL`.


Analysis files contain all possible options for running an analysis including the ability to specify variant frequency
and pathogenicity data sources and the ability to tweak the order that analysis steps are performed.

See the test-analysis-exome.yml and test-analysis-genome.yml files located in the base install directory for examples.
Details can be found in the :ref:`analysis` section.

.. parsed-literal::

    java -jar exomiser-cli-|version|.jar --analysis examples/test-analysis-exome.yml

These files an also be used to run full-genomes, however they will require substantially more RAM to do so. For example
a 4.4 million variant analysis requires approximately 12GB RAM. However, RAM requirements can be greatly reduced by
setting the analysisMode option to PASS_ONLY. This will also aid your ability to evaluate the results.

Analyses can be run in batch mode. Simply put the path to each analysis file in the batch file - one file path per line.

.. parsed-literal::

    java -jar exomiser-cli-|version|.jar --analysis-batch examples/test-analysis-batch.txt


Output
======

By default Exomiser will write out any result files to the exomiser-cli-|version|/results sub-directory of the
Exomiser installation directory. Unless specified in the `output.yml` or `outputOptions` section of the analysis YAML
file, Exomiser will write out a `.json` and a `.html` file. These are for machine (JSON) and human (HTML) use. The
filenames will match the input VCF filename. For example

.. parsed-literal::

    java -jar exomiser-cli-|version|.jar --sample examples/pfeiffer-phenopacket.yml --vcf path/to/manuel.vcf.gz --assembly hg19

Would result in two files being output with the filename 'manuel_exomiser' and the '.json' and '.html' extensions:

.. parsed-literal::

  exomiser-cli-|version|/results/manuel_exomiser.html
  exomiser-cli-|version|/results/manuel_exomiser.json


Users requiring more control over their output can use either the ``outputOptions`` section of an analysis file or a
specific :ref:`outputoptions` yaml file. An example of this can be found in the exomiser-cli-|version|/examples/output-options.yml
file:

.. code-block:: yaml

    ---
    outputContributingVariantsOnly: false
    # numGenes options: 0 = all or specify a limit e.g. 500 for the first 500 results
    numGenes: 10
    minExomiserGeneScore: 0.7
    # outputDirectory: (optional) (default: '{exomiserDir}/results/')
    outputDirectory: results/
    # outputFileName: (optional) (default: 'input-vcf-name-exomiser')
	outputFileName: NA12345-exomiser-results
    # out-format options: HTML, JSON, TSV_GENE, TSV_VARIANT, VCF (default: HTML)
    outputFormats: [HTML, JSON, TSV_GENE]


This file is passed to Exomiser using the ``--output`` switch:

.. parsed-literal::

    java -jar exomiser-cli-|version|.jar --sample examples/pfeiffer-phenopacket.yml --vcf path/to/manuel.vcf.gz --output path/to/output-options.yml


The output filename, directory and format can also be specified directly on the CLI (see the --help command for details).

Batch
=====

The above commands can be added to a batch file for example in the file exomiser-cli-|version|/examples/test-analysis-batch-commands.txt

.. parsed-literal::

    #This is an example analysis batch file to be run using the --analysis-batch command
    #
    #Each line should specify the path of a single analysis file, either relative to the directory the exomiser
    #is being run from or the full system path. It will run any combination of exomiser commands listed using -h or --help.
    #
    # Original format exomiser analysis containing all the sample and analysis information
    --analysis test-analysis-exome.yml
    # New preset exome analysis using a v1 phenopacket to submit the phenotype information and adding/overriding the VCF input
    --preset exome --sample pfeiffer-phenopacket.yml --vcf Pfeiffer.vcf.gz
    # Using the default analysis (exome) with a v1 phenopacket containing the phenotype information and adding/overriding the VCF input
    --sample pfeiffer-phenopacket.yml --vcf Pfeiffer.vcf.gz
    # Using a user-defined analysis with a v1 phenopacket containing the phenotype information and adding/overriding the VCF input
    --analysis preset-exome-analysis.yml --sample pfeiffer-phenopacket.yml --vcf Pfeiffer.vcf.gz
    # Using a user-defined analysis with a v1 phenopacket containing the phenotype information and adding/overriding the VCF input
    --analysis preset-exome-analysis.yml --sample pfeiffer-phenopacket.yml --vcf Pfeiffer.vcf.gz --output output-options.yml


then run using the ``--batch`` command:

.. parsed-literal::

    java -jar exomiser-cli-|version|.jar --batch path/to/exomiser-cli-|version|/examples/test-analysis-batch-commands.txt


The advantage of this is that a single command will be able to analyse many samples in far less time than starting a new
JVM for each as there will be no start-up penalty after the initial start and the Java JIT compiler will be able to take
advantage of a longer-running process to optimise the runtime code. For maximum throughput on a cluster consider splitting
your batch jobs over multiple nodes.