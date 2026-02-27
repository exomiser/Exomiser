================
Running Exomiser
================

Want help?
==========

.. parsed-literal::

    java -jar exomiser-cli-|version|.jar --help


Usage
=====

In general we recommend running Exomiser from the install directory. A single sample exome analysis can be run using the
following command:

.. parsed-literal::

    # run a test exome analysis
    cd exomiser-cli-|version|
    java -jar exomiser-cli-|version|.jar analyse --sample examples/pfeiffer-phenopacket.yml --vcf examples/Pfeiffer.vcf.gz --assembly hg19


This command prioritises variants from the input `VCF <https://samtools.github.io/hts-specs/VCFv4.3.pdf>`_ file, called
against the `GRCh37/hg19 reference assembly <https://www.ncbi.nlm.nih.gov/assembly/GCF_000001405.25/>`_ in the context
of the sample phenotypes encoded using `Human Phenotype Ontology <https://hpo.jax.org>`_ terms contained in the
`Phenopacket <https://phenopacket-schema.readthedocs.io>`_ file.

Running a multi-sample VCF for trios also requires a `PED <https://gatk.broadinstitute.org/hc/en-us/articles/360035531972-PED-Pedigree-format>`_ file. e.g.

.. parsed-literal::

    # run a test exome family analysis
    cd exomiser-cli-|version|
    java -jar exomiser-cli-|version|.jar analyse --sample examples/pfeiffer-family.yml --vcf examples/Pfeiffer-quartet.vcf.gz --assembly hg19 --ped examples/Pfeiffer-quartet.ped


By default there will be three output files written to the ``results`` directory using the same filename as the input VCF file but
with ``_exomiser`` appended before a file extension of ``.jsonl``, ``.parquet`` or ``.html`` e.g.

.. parsed-literal::

    ls results/Pfeiffer*
    results/Pfeiffer_exomiser.html  results/Pfeiffer_exomiser.jsonl results/Pfeiffer_exomiser.parquet

The HTML file is for human use, whilst the JSON file is better read by machines, for instance by using `jq <https://stedolan.github.io/jq/>`_.
The parquet file is a highly efficient binary file format to storing columnar data. It can be accessed using standard
data tools like `pandas <https://pandas.pydata.org/>`_, `Polars <https://pola.rs/>`_ or `DuckDB <https://duckdb.org/>`_.
Details on how to interpret the output can be found in the :ref:`result_interpretation` section.

The ``examples`` directory contains a selection of single sample exome and genome analysis files, a multisample (family)
analysis with an associated pedigree in `PED <https://gatk.broadinstitute.org/hc/en-us/articles/360035531972-PED-Pedigree-format>`_
format, and the respective `Phenopacket <https://phenopacket-schema.readthedocs.io>`_ representations of the proband or
family.


Running from alternate directory
================================

If you're running the exomiser from a different directory to the one the exomiser-cli-|version|.jar file is located,
you will need to specify the path to the ``application.properties`` file in the start-up command. For example:

.. parsed-literal::

     java -Dspring.config.location=/full/path/to/your/exomiser-cli/directory/application.properties \
     -jar /full/path/to/your/exomiser-cli/directory/exomiser-cli-|version|.jar analyse \
     --sample /full/path/to/your/exomiser-cli/directory/examples/pfeiffer-phenopacket.yml \
     --vcf /full/path/to/your/exomiser-cli/directory/examples/Pfeiffer.vcf.gz --assembly hg19 \
     --output-directory full/path/to/your/results/directory


*n.b.* the ``spring.config.location`` command **must be provided using `-D` JVM system property before the -jar commands!**
