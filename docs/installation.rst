============
Installation
============

Software and Hardware requirements
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

- Minimum 8/16GB RAM (For an exome analysis of a 30,000 variant sample 4GB RAM should suffice. For a genome analysis of a 4,400,000 variant sample 8GB RAM should suffice.)
- Any 64-bit operating system
- Java 17 or above
- At least 100GB free disk space (SSD preferred for best performance)
- An internet connection is not required to run the Exomiser, although network access will be required if accessing a networked database (optional).
- By default the Exomiser is completely self-contained and is able to run on standard consumer laptops.


Pre-built Binaries
~~~~~~~~~~~~~~~~~~

.. note::

    This is the recommended way of installing for normal users.

Pre-built binaries are available from `The Monarch Initiative <https://monarchinitiative.org>`_ or from the Exomiser repository on `GitHub <https://github.com/exomiser/Exomiser/releases/>`_.

Exomiser requires 2-3 data files to be available as well - one for the phenotype data and one for each genome assembly required.

Windows install
~~~~~~~~~~~~~~~

1. Install `7-Zip <http://www.7-zip.org>`_ for unzipping the archive files. The built-in archiving software has issues extracting the zip files.
2. Download the data and distribution files from https://data.monarchinitiative.org/exomiser/latest
3. Extract the distribution files by right-clicking exomiser-cli-|version|-distribution.zip and selecting 7-Zip > Extract Here
4. Extract the data files (e.g. 2402_phenotype.zip, 2402_hg19.zip) by right-clicking the archive and selecting 7-Zip > Extract files... into the exomiser data directory. By default exomiser expects this to be 'exomiser-cli-\ |version|\/data', but this can be changed in the ``application.properties``
5. cd exomiser-cli-|version|
6. java -Xmx4g -jar exomiser-cli-|version|.jar --analysis examples/test-analysis-exome.yml

Linux install
~~~~~~~~~~~~~

The following shell script should work-

.. parsed-literal::

    # download the distribution (won't take long)
    wget https://data.monarchinitiative.org/exomiser/latest/exomiser-cli-\ |version|\-distribution.zip
    # download the data (this is ~80GB and will take a while). If you only require a single assembly, only download the relevant file.
    wget https://data.monarchinitiative.org/exomiser/latest/2402_hg19.zip
    wget https://data.monarchinitiative.org/exomiser/latest/2402_hg38.zip
    wget https://data.monarchinitiative.org/exomiser/latest/2402_phenotype.zip

    # unzip the distribution and data files - this will create a directory called 'exomiser-cli-|version|' in the current working directory
    unzip exomiser-cli-|version|-distribution.zip
    unzip 2402_*.zip -d exomiser-cli-|version|/data

    # Check the application.properties are pointing to the correct versions
    # exomiser.hg19.data-version=2402
    # exomiser.hg38.data-version=2402
    # exomiser.phenotype.data-version=2402

    # run a test exome analysis
    cd exomiser-cli-|version|
    java -Xmx4g -jar exomiser-cli-|version|.jar --analysis examples/test-analysis-exome.yml


This script will download, verify and extract the exomiser files and then run the analysis contained in the file 'test-analysis-exome.yml' from the examples sub-directory. This contains a known pathogenic missense variant in the FGFR2 gene.

.. _remm:

Genomiser / REMM data
~~~~~~~~~~~~~~~~~~~~~

In order to run the Genomiser you will also need to download the REMM data file
from [here](https://zenodo.org/record/4768448). Once downloaded you'll need to add the path to the ReMM.v0.3.1.tsv.gz
file to the ``application.properties`` file. For example if you downloaded the file to the exomiser data directory you
could add the entry like this:

.. parsed-literal::

    exomiser.hg19.remm-path=${exomiser.hg19.data-directory}/ReMM.v0.3.1.tsv.gz

If this step is omitted, the application will throw and error and stop any analysis which defines ``REMM`` in the ``pathogenicitySources`` section of an analysis yml file.

Having done this, run the analysis like this:

.. parsed-literal::

    java -Xmx6g -jar exomiser-cli-|version|.jar --analysis examples/NA19722_601952_AUTOSOMAL_RECESSIVE_POMP_13_29233225_5UTR_38.yml

This is an analysis for an autosomal recessive 5'UTR variant located in POMP gene on chromosome 13. The phenotype HPO terms are taken from the clinical synopsis of
OMIM #601952 (http://www.omim.org/clinicalSynopsis/601952)

.. _cadd-install:

CADD data
~~~~~~~~~
In order to use CADD you will need to download the CADD data files separately. These can be accessed from https://cadd.gs.washington.edu/download. Exomiser only
requires the file with the score in, not the full annotations. For example, in release v1.4 Exomiser requires both the files `All possible SNVs of GRCh38/hg38`
and `80M InDels to initiate a local setup`. Each genome assembly will require the relevant files. The direct links from the US site are shown below and are correct at the time
of writing.

.. parsed-literal::

  wget https://krishna.gs.washington.edu/download/CADD/v1.4/GRCh38/whole_genome_SNVs.tsv.gz
  wget https://krishna.gs.washington.edu/download/CADD/v1.4/GRCh38/whole_genome_SNVs.tsv.gz.tbi
  wget https://krishna.gs.washington.edu/download/CADD/v1.4/GRCh38/InDels.tsv.gz
  wget https://krishna.gs.washington.edu/download/CADD/v1.4/GRCh38/InDels.tsv.gz.tbi

  wget https://krishna.gs.washington.edu/download/CADD/v1.4/GRCh37/whole_genome_SNVs.tsv.gz
  wget https://krishna.gs.washington.edu/download/CADD/v1.4/GRCh37/whole_genome_SNVs.tsv.gz.tbi
  wget https://krishna.gs.washington.edu/download/CADD/v1.4/GRCh37/InDels.tsv.gz
  wget https://krishna.gs.washington.edu/download/CADD/v1.4/GRCh37/InDels.tsv.gz.tbi

Enable Exomiser to use CADD by altering the ``application.properties`` file to enable these lines and ensure the
``cadd.version`` property matches the version you downloaded.

.. parsed-literal::

    cadd.version=1.4

    exomiser.hg19.cadd-snv-path=${exomiser.data-directory}/cadd/${cadd.version}/hg19/whole_genome_SNVs.tsv.gz
    exomiser.hg19.cadd-in-del-path=${exomiser.data-directory}/cadd/${cadd.version}/hg19/InDels.tsv.gz

    # and/or for hg38
    exomiser.hg38.cadd-snv-path=${exomiser.data-directory}/cadd/${cadd.version}/whole_genome_SNVs.tsv.gz
    exomiser.hg38.cadd-in-del-path=${exomiser.data-directory}/cadd/${cadd.version}/InDels.tsv.gz


Exomiser will expect the tabix index ``.tbi`` file to be present in the same directory as the ``.tsv.gz`` files. To use
CADD scores in an analysis, the ``pathogenicitySources`` should contain the ``CADD`` property

.. code-block:: yaml

    #Possible pathogenicitySources: POLYPHEN, MUTATION_TASTER, SIFT, CADD, REMM
    #REMM is trained on non-coding regulatory regions
    #*WARNING* if you enable CADD or REMM ensure that you have downloaded and installed the CADD/REMM tabix files
    #and updated their location in the application.properties. Exomiser will not run without this.
    pathogenicitySources: [POLYPHEN, MUTATION_TASTER, SIFT, CADD]


Configuring the application.properties
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Once you have downloaded and unzipped all the data, you will need to edit the exomiser-cli-\ |version|\/application.properties
file located in the main exomiser-cli directory. This file contains a lot of comments for optional data and assemblies.

If you want to run Exomiser using data from a different release directory edit the line in ``application.properties``:

.. parsed-literal::

    exomiser.data-directory=

with

.. parsed-literal::

    exomiser.data-directory=/full/path/to/alternative/data/directory

For example, assuming you unzipped the contents of the `2402_hg38.zip` data file into `/data/exomiser-data`:

.. parsed-literal::

    exomiser.data-directory=/data/exomiser-data

where the contents of `exomiser-data` looks something like this:

.. code-block:: bash

    $ tree -L 1 /data/exomiser-data/
        /data/exomiser-data/
        ├── 2402_hg19
        ├── 2402_hg38
        ├── 2402_phenotype
        ├── cadd
        └── remm


By default Exomiser will look for data located in the exomiser-cli-\ |version|\/data directory.

After defining the a `exomiser.data-directory`, a minimal setup for exome analysis using GRCh37/hg19 would only require
the ``application.properties`` to contain this:

.. code-block:: yaml

    ### hg19 assembly ###
    exomiser.hg19.data-version=2402

    ### phenotypes ###
    exomiser.phenotype.data-version=2402


For a GRCh38/hg38 only setup:

.. code-block:: yaml

    ### hg38 assembly ###
    exomiser.hg38.data-version=2402

    ### phenotypes ###
    exomiser.phenotype.data-version=2402


Or an install supporting both assemblies:

.. code-block:: yaml

    ### hg19 assembly ###
    exomiser.hg19.data-version=2402

    ### hg38 assembly ###
    exomiser.hg38.data-version=2402

    ### phenotypes ###
    exomiser.phenotype.data-version=2402


*n.b.* each assembly will require approximately 1GB RAM to load. Attempting to analyse a VCF called using an
unsupported/unloaded assembly data will result in an unrecoverable error being thrown.

By default, Exomiser uses a whitelist created from ClinVar data. Exomiser will consider any variant on the whitelist
to be maximally pathogenic, regardless of the underlying data (*e.g.* variant effect, allele frequency, predicted pathogenicity)
and always included these in the results.


Troubleshooting
~~~~~~~~~~~~~~~


Zip file reported as too big or corrupted
-----------------------------------------
If, when running 'unzip exomiser-cli-|version|-distribution.zip', you see the following:

.. parsed-literal::

    error:  Zip file too big (greater than 4294959102 bytes)
    Archive:  exomiser-cli-|version|-distribution.zip
    warning [exomiser-cli-|version|-distribution.zip]:  9940454202 extra bytes at beginning or within zipfile
      (attempting to process anyway)
    error [exomiser-cli-|version|-distribution.zip]:  start of central directory not found;
      zipfile corrupt.
      (please check that you have transferred or created the zipfile in the
      appropriate BINARY mode and that you have compiled UnZip properly)


Check that your unzip version was compiled with LARGE_FILE_SUPPORT and ZIP64_SUPPORT. This is standard with UnZip 6.00 and can be checked by typing:

.. parsed-literal::

    unzip -version

This shouldn't be an issue with more recent linux distributions.