================
Running Exomiser
================

Usage
=====

In general we recommend running Exomiser from the install directory. A single sample exome analysis can be run using the
following command:

.. parsed-literal::

    # run a test exome analysis
    cd exomiser-cli-|version|
    java -jar exomiser-cli-|version|.jar --analysis examples/test-analysis-exome.yml


The ``examples`` directory contains a selection of single sample exome and genome analysis files, a multisample (family)
analysis with an associated pedigree in `PED <https://gatk.broadinstitute.org/hc/en-us/articles/360035531972-PED-Pedigree-format>`_
format, and the respective `Phenopacket <https://phenopacket-schema.readthedocs.io>`_ representations of the proband or
family.


Want help?
==========

.. parsed-literal::

    java -jar exomiser-cli-|version|.jar --help


Running from alternate directory
================================

If you're running the exomiser from a different directory to the one the exomiser-cli-|version|.jar is located you'll
need to add the command to let Exomiser know where the ``application.properties`` file is located.

.. parsed-literal::

    --spring.config.location=/full/path/to/your/exomiser-cli/directory



If you're running the exomiser from a different directory to the one the exomiser-cli-|version|.jar file is located,
you will need to specify the path to the ``application.properties`` file in the start-up command. For example:

.. parsed-literal::

     java -Xmx4g -jar /full/path/to/your/exomiser-cli/directory/exomiser-cli-|version|.jar --analysis /full/path/to/your/exomiser-cli/directory/examples/test-analysis-exome.yml --spring.config.location=/full/path/to/your/exomiser-cli/directory/application.properties


*n.b.* the ``spring.config.location`` command **must be the last argument in the input commands!**

Input files and arguments
=========================

The Exomiser can be run via simply via a yaml analysis file. The extended cli capability was removed in version 10.0.0
as this was less capable than the yaml scripts and only supported hg19 exome analysis. Version 13.0.0 introduced a more
flexible input using a `GA4GH Phenopacket <https://phenopacket-schema.readthedocs.io>`_ for the sample data with the
ability to specify the input pedigree, VCF and genome assembly independently; user-specified, preset or
default analysis options and a new batch mode.


Analysis file
=============

Analysis files contain all possible options for running an analysis including the ability to specify variant frequency
and pathogenicity data sources and the ability to tweak the order that analysis steps are performed.

See the test-analysis-exome.yml and test-analysis-genome.yml files located in the base install directory for details.

.. parsed-literal::

    java -jar exomiser-cli-|version|.jar --analysis examples/test-analysis-exome.yml

These files an also be used to run full-genomes, however they will require substantially more RAM to do so. For example
a 4.4 million variant analysis requires approximately 12GB RAM. However, RAM requirements can be greatly reduced by
setting the analysisMode option to PASS_ONLY. This will also aid your ability to evaluate the results.

Analyses can be run in batch mode. Simply put the path to each analysis file in the batch file - one file path per line.

.. parsed-literal::

    java -jar exomiser-cli-|version|.jar --analysis-batch examples/test-analysis-batch.txt


Troubleshooting
===============

java.lang.UnsupportedClassVersionError:
---------------------------------------
If you get the following error message:

.. parsed-literal::

    Exception in thread "main" java.lang.UnsupportedClassVersionError:
    org/monarchinitiative/exomiser/cli/Main : Unsupported major.minor version

You are running an older unsupported version of Java. Exomiser requires java version 11 or higher. This can be checked by running:

.. parsed-literal::

  java -version

You should see something like this in response:

.. parsed-literal::

    openjdk version "11.0.11" 2021-04-20
    OpenJDK Runtime Environment (build 11.0.11+9-Ubuntu-0ubuntu2.20.04)
    OpenJDK 64-Bit Server VM (build 11.0.11+9-Ubuntu-0ubuntu2.20.04, mixed mode, sharing)


Versions lower than 11 (e.g. 1.5, 1.6, 1.7, 1.8, 9, 10) will not run exomiser, so you will need to install the latest java version.
