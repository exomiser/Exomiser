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
    java -jar exomiser-cli-|version|.jar --sample examples/pfeiffer-phenopacket.yml --vcf examples/Pfeiffer.vcf.gz --assembly hg19


This command prioritises variants from the input `VCF <https://samtools.github.io/hts-specs/VCFv4.3.pdf>`_ file, called
against the `GRCh37/hg19 reference assembly <https://www.ncbi.nlm.nih.gov/assembly/GCF_000001405.25/>`_ in the context
of the sample phenotypes encoded using `Human Phenotype Ontology <https://hpo.jax.org>`_ terms contained in the
`Phenopacket <https://phenopacket-schema.readthedocs.io>`_ file.

Running a multi-sample VCF for trios also requires a `PED <https://gatk.broadinstitute.org/hc/en-us/articles/360035531972-PED-Pedigree-format>`_ file. e.g.

.. parsed-literal::

    # run a test exome family analysis
    cd exomiser-cli-|version|
    java -jar exomiser-cli-|version|.jar --sample examples/pfeiffer-family.yml --vcf examples/Pfeiffer-quartet.vcf.gz --assembly hg19 --ped examples/Pfeiffer-quartet.ped


By default there will be two output files written to the ``results`` directory using the same filename as the input VCF file but
with ``_exomiser`` appended before a file extension of ``.json`` or ``.html`` e.g.

.. parsed-literal::

    ls results/Pfeiffer*
    results/Pfeiffer_exomiser.html  results/Pfeiffer_exomiser.json

The HTML file is for human use, whilst the JSON file is better read by machines, for instance by using `jq <https://stedolan.github.io/jq/>`_.
Details on how to interpret the output can be found in the :ref:`result_interpretation` section.

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

If you're running the exomiser from a different directory to the one the exomiser-cli-|version|.jar file is located,
you will need to specify the path to the ``application.properties`` file in the start-up command. For example:

.. parsed-literal::

     java -Xmx4g -jar /full/path/to/your/exomiser-cli/directory/exomiser-cli-|version|.jar --analysis /full/path/to/your/exomiser-cli/directory/examples/test-analysis-exome.yml --spring.config.location=/full/path/to/your/exomiser-cli/directory/application.properties


*n.b.* the ``spring.config.location`` command **must be the last argument in the input commands!**


Troubleshooting
===============

java.lang.UnsupportedClassVersionError:
---------------------------------------
If you get the following error message:

.. code-block:: console

    Exception in thread "main" java.lang.UnsupportedClassVersionError:
    org/monarchinitiative/exomiser/cli/Main : Unsupported major.minor version


or

.. code-block:: console

    Error: A JNI error has occurred, please check your installation and try again
    Exception in thread "main" java.lang.UnsupportedClassVersionError: org/monarchinitiative/exomiser/cli/Main has been
    compiled by a more recent version of the Java Runtime (class file version 55.0), this version of the Java Runtime
    only recognizes class file versions up to 52.0


You are running an older unsupported version of Java. Exomiser requires java version 17 or higher. This can be checked by running:

.. code-block:: console

    $ java -version

You should see something like this in response:

.. code-block:: console

    openjdk version "17.0.9" 2023-10-17
    OpenJDK Runtime Environment (build 17.0.9+9-Ubuntu-122.04)
    OpenJDK 64-Bit Server VM (build 17.0.9+9-Ubuntu-122.04, mixed mode, sharing)


Versions lower than 17 (e.g. 1.5, 1.6, 1.7, 1.8, 9, 10) will not run exomiser, so you will need to install the latest java version.
