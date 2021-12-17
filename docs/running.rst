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


