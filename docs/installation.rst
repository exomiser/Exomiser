============
Installation
============

Quick Start Overview
~~~~~~~~~~~~~~~~~~~~

Installing Exomiser involves four steps:

1. **Check your system** meets the requirements below
2. **Download** the Exomiser program and data files
3. **Configure** Exomiser to find your data files
4. **Run** a test analysis to confirm everything works


Software and Hardware Requirements
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Exomiser is designed to run on a standard laptop or desktop computer. However, before installing
make sure your computer meets these requirements:

- **Operating system:** Any 64-bit operating system (Windows, Linux, or macOS)
- **Java:** Version 21 or above. Download from https://adoptium.net if needed.
  To check your current version, run: ``java -version``
- **RAM (memory):**

  - Exome analysis (typically ~30,000 variants): 4 GB RAM minimum
  - Genome analysis (typically ~4,400,000 variants): 8 GB RAM minimum
  - We recommend 16 GB total system RAM for comfortable general use

- **Disk space:** At least 100 GB free. An SSD (solid-state drive) is
  strongly preferred for performance.
- **Internet connection:** Only needed to download files. Exomiser itself
  runs entirely offline once installed.


Key Concepts
~~~~~~~~~~~~

Before you begin, here are a few terms used throughout this guide:

- **Genome assembly (hg19 / hg38):** Human genome data exists in two common
  reference versions — hg19 (also called GRCh37) and hg38 (GRCh38). You
  will need to download data for whichever version your VCF file uses.
  If you are unsure, ask whoever generated your VCF file.

- **VCF file:** A standard file format containing genetic variant data.
  This is the input file you will analyse with Exomiser.

- **application.properties:** A plain text configuration file that tells
  Exomiser where your data files are stored and which versions to use.
  You will edit this file as part of setup.

- **Data version (e.g. 2402):** Exomiser data files are released
  periodically. The number refers to the year and month of release
  (e.g. 2402 = February 2024). Your ``application.properties`` must
  reference the version you downloaded.


Installation
~~~~~~~~~~~~

Exomiser consists of two parts you need to download:

1. **The Exomiser application** (the distribution ``.zip`` file)
2. **The data files** (Additional``.zip`` files containing genomic and phenotype databases)

Both are available from:

- https://github.com/exomiser/Exomiser/releases/
- https://data.monarchinitiative.org/exomiser/latest


Windows Installation
--------------------

**Step 1: Install 7-Zip**

Windows' built-in zip tool cannot handle Exomiser's large files. Download
and install 7-Zip from http://www.7-zip.org before proceeding.

**Step 2: Download the files**
Go to https://data.monarchinitiative.org/exomiser/latest and download:

- ``exomiser-cli-|version|-distribution.zip`` (the program)
- ``2402_phenotype.zip`` (required for all analyses)
- ``2402_hg19.zip`` and/or ``2402_hg38.zip`` (download whichever matches
  your VCF file; download both if unsure)

**Step 3: Extract the program**

Right-click ``exomiser-cli-|version|-distribution.zip`` and choose
**7-Zip > Extract Here**. This will create a folder called
``exomiser-cli-|version|`` in the same location.

**Step 4: Extract the data files**

Right-click each data ``.zip`` file (e.g. ``2402_phenotype.zip``,
``2402_hg19.zip``) and choose **7-Zip > Extract files...**. Extract them
into the ``data`` subfolder inside your Exomiser folder, e.g.:

.. parsed-literal::

    exomiser-cli-\ |version|\/data

**Step 5: Configure application.properties**

Open the file ``exomiser-cli-|version|\application.properties`` in a text
editor (e.g. Notepad). Find the data version lines and make sure they match
the version you downloaded (e.g. ``2402``):

.. code-block:: properties

    exomiser.hg19.data-version=2402
    exomiser.phenotype.data-version=2402

If you downloaded hg38 instead of (or as well as) hg19, also add or update:

.. code-block:: properties

    exomiser.hg38.data-version=2402

Save the file.

**Step 6: Open a Command Prompt and run a test analysis**

Open Command Prompt, navigate into the Exomiser folder, and run the
test analysis:

.. parsed-literal::

    cd exomiser-cli-\ |version|
    java -jar exomiser-cli-\ |version|\.jar analyse --analysis examples/test-analysis-exome.yml

If Exomiser runs and produces output, your installation is working correctly.

.. _linux-install:

Linux/macOS Installation
------------------------

Copy and run the following commands in your terminal. The data download
is approximately 80 GB and may take several hours depending on your
internet speed.

.. parsed-literal::

    # Download the Exomiser program (small, fast)
    wget https://data.monarchinitiative.org/exomiser/latest/exomiser-cli-\ |version|\-distribution.zip

    # Download the data files (large — allow several hours)
    # If you only need one genome assembly, download only the relevant file.
    wget https://data.monarchinitiative.org/exomiser/latest/2402_phenotype.zip
    wget https://data.monarchinitiative.org/exomiser/latest/2402_hg38.zip
    wget https://data.monarchinitiative.org/exomiser/latest/2402_hg19.zip

    # Extract the program
    unzip exomiser-cli-\ |version|\-distribution.zip

    # Extract the data files into the data subdirectory
    unzip 2402_*.zip -d exomiser-cli-\ |version|\/data

    # Navigate into the Exomiser folder
    cd exomiser-cli-\ |version|

**Configure application.properties**

Open ``application.properties`` in a text editor (e.g. ``nano application.properties``)
and confirm the data version lines match what you downloaded:

.. code-block:: properties

    exomiser.hg19.data-version=2402
    exomiser.phenotype.data-version=2402

Add or update the hg38 line if you downloaded that assembly:

.. code-block:: properties

    exomiser.hg38.data-version=2402

Save and close the file.

**Run a test analysis**

.. parsed-literal::

    java -jar exomiser-cli-\ |version|\.jar analyse --analysis examples/test-analysis-exome.yml

If Exomiser produces output, your installation is working. The test analysis
uses a sample containing a known disease-causing variant in the *FGFR2* gene.


.. _homebrew-install:

Linux/macOS Installation via Homebrew
-------------------------------------

If you have Homebrew installed (https://brew.sh), you can install Exomiser
with a single command:

.. code-block:: bash

    brew install exomiser/tap/exomiser

This will install the Exomiser program. You will still need to download the
data files separately (see the :ref:`linux-install` instructions above for the
list of files to download from https://data.monarchinitiative.org/exomiser/latest).

After downloading and extracting the data files, configure
``application.properties`` as described in the Linux section above.

Given the nature of how Homebrew manages the application, you might want to consider
installing the data in a separate data directory (see :ref:`data-directory`).

.. _data-directory:

Configuring the Data Directory
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

By default, Exomiser expects its data files to be in a ``data`` subfolder
inside the Exomiser program folder. If you store your data elsewhere, you
must tell Exomiser where to find it.

Open ``application.properties`` and set the ``exomiser.data-directory`` to
the full path of your data folder. For example:

.. code-block:: properties

    exomiser.data-directory=/data/exomiser-data

Your data directory should look something like this (using ``tree -L 1 /data/exomiser-data/``):

.. code-block:: bash

    /data/exomiser-data/
    ├── 2402_hg19
    ├── 2402_hg38
    └── 2402_phenotype

A minimal ``application.properties`` for hg19 exome analysis:

.. code-block:: properties

    exomiser.data-directory=/data/exomiser-data
    exomiser.hg19.data-version=2402
    exomiser.phenotype.data-version=2402

For hg38 only:

.. code-block:: properties

    exomiser.data-directory=/data/exomiser-data
    exomiser.hg38.data-version=2402
    exomiser.phenotype.data-version=2402

For both assemblies:

.. code-block:: properties

    exomiser.data-directory=/data/exomiser-data
    exomiser.hg19.data-version=2402
    exomiser.hg38.data-version=2402
    exomiser.phenotype.data-version=2402

.. note::

    Each genome assembly loaded requires approximately 1.5 GB of RAM.
    Loading an assembly not present in your data directory will cause
    Exomiser to fail.


Optional: Overriding Settings from the Command Line
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

You can override any ``application.properties`` setting directly on the
command line using ``-D`` flags. This is useful if you want to test
different configurations without editing the file. For example:

.. parsed-literal::

    java -Dexomiser.data-directory=/full/path/to/exomiser-data \
      -Dexomiser.hg19.data-version=\ |genome_data_version| \
      -Dexomiser.phenotype.data-version=\ |phenotype_data_version| \
      -jar exomiser-cli-\ |version|\.jar analyse --sample examples/pfeiffer-phenopacket.yml

Command-line settings take priority over ``application.properties``. If you installed Exomiser
via Homebrew, you will need to use either the JAVA_TOOL_OPTIONS environment variable e.g.

.. code-block:: bash

    export JAVA_TOOL_OPTIONS="-Dexomiser.data-directory=/full/path/to/exomiser-data -Dexomiser.hg38.data-version=2402"

Alternatively, Exomiser will recognise exomiser-specific environment variables, as named in the
``application.properties`` file e.g.

.. code-block:: bash

    export EXOMISER_DATA_DIRECTORY=/full/path/to/exomiser-data
    export EXOMISER_HG38_DATA_VERSION=2602

These can be reverted using the ``unset`` command e.g. ``unset EXOMISER_HG38_DATA_VERSION`` will remove that variable
from the environment and exomiser will not try to load that version. Note that it will still use the version from the
``application.properties`` file if it is set there.

.. _remm:

Optional: Genomiser / REMM Data
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. note::

    This section is only needed if you intend to use the **Genomiser** for
    non-coding variant analysis. Skip this section for standard exome analysis.

Download the REMM data file from https://remm.bihealth.org/download.

Once downloaded, add the path to the file in ``application.properties``:

.. parsed-literal::

    exomiser.hg19.remm-path=${exomiser.hg19.data-directory}/ReMM.v0.4.tsv.gz

If you include ``REMM`` as a ``pathogenicitySources`` value in your analysis
YAML file without completing this step, Exomiser will stop with an error.


.. _cadd-install:

Optional: CADD Data
~~~~~~~~~~~~~~~~~~~~

.. note::

    This section is only needed if you want to use CADD pathogenicity scores.
    Skip this section for standard exome analysis.

Download CADD files from https://cadd.gs.washington.edu/download. Exomiser
only requires the score files (not the full annotation files). Download the
files for each genome assembly you use:

.. parsed-literal::

    # hg38
    wget https://krishna.gs.washington.edu/download/CADD/v1.4/GRCh38/whole_genome_SNVs.tsv.gz
    wget https://krishna.gs.washington.edu/download/CADD/v1.4/GRCh38/whole_genome_SNVs.tsv.gz.tbi
    wget https://krishna.gs.washington.edu/download/CADD/v1.4/GRCh38/InDels.tsv.gz
    wget https://krishna.gs.washington.edu/download/CADD/v1.4/GRCh38/InDels.tsv.gz.tbi

    # hg19
    wget https://krishna.gs.washington.edu/download/CADD/v1.4/GRCh37/whole_genome_SNVs.tsv.gz
    wget https://krishna.gs.washington.edu/download/CADD/v1.4/GRCh37/whole_genome_SNVs.tsv.gz.tbi
    wget https://krishna.gs.washington.edu/download/CADD/v1.4/GRCh37/InDels.tsv.gz
    wget https://krishna.gs.washington.edu/download/CADD/v1.4/GRCh37/InDels.tsv.gz.tbi

Then update ``application.properties`` to enable CADD:

.. parsed-literal::

    cadd.version=1.4

    exomiser.hg19.cadd-snv-path=${exomiser.data-directory}/cadd/${cadd.version}/hg19/whole_genome_SNVs.tsv.gz
    exomiser.hg19.cadd-in-del-path=${exomiser.data-directory}/cadd/${cadd.version}/hg19/InDels.tsv.gz

    # and/or for hg38:
    exomiser.hg38.cadd-snv-path=${exomiser.data-directory}/cadd/${cadd.version}/whole_genome_SNVs.tsv.gz
    exomiser.hg38.cadd-in-del-path=${exomiser.data-directory}/cadd/${cadd.version}/InDels.tsv.gz

The ``.tbi`` index files must be in the same directory as the ``.tsv.gz``
files. To activate CADD in an analysis, include ``CADD`` in the
``pathogenicitySources`` section of your analysis YAML file:

.. code-block:: yaml

    pathogenicitySources: [POLYPHEN, MUTATION_TASTER, SIFT, CADD]

.. warning::

    If you include ``CADD`` or ``REMM`` in ``pathogenicitySources`` without
    having downloaded and configured the corresponding data files, Exomiser
    will not run.


Troubleshooting
~~~~~~~~~~~~~~~

**Java not found**

If you see ``java: command not found``, Java is either not installed or not
on your system PATH. Download Java 21+ from https://adoptium.net and follow
the installer instructions. After installation, open a new terminal window
and try again.

**Wrong Java version**

Run ``java -version``. If the version shown is below 21, install a newer
version from https://adoptium.net.

**"Permission denied" errors (Linux/macOS)**

If you see a permission error when running Exomiser, make sure the ``.jar``
file is readable: ``chmod +r exomiser-cli-|version|.jar``

**Zip file reported as too big or corrupted (Linux)**

If you see an error like::

    error: Zip file too big (greater than 4294959102 bytes)

your version of ``unzip`` does not support large files. Check with:

.. parsed-literal::

    unzip -version

Look for ``ZIP64_SUPPORT`` in the output. If it is absent, upgrade ``unzip``
via your package manager (e.g. ``sudo apt install unzip``). This is not
usually an issue on modern Linux distributions.

**Out of memory errors**

If Exomiser crashes with an out-of-memory error, increase the memory
allocation by raising the ``-Xmx`` value. For genome analysis, try
``-Xmx12G`` or ``-Xmx16G``:

.. parsed-literal::

    java -Xmx12G -jar exomiser-cli-\ |version|\.jar analyse --analysis your-analysis.yml

**Exomiser cannot find data files**

Double-check that the version numbers in ``application.properties``
(e.g. ``exomiser.hg19.data-version=2402``) exactly match the folder names
in your data directory (e.g. ``2402_hg19``). Even a small mismatch will
cause Exomiser to fail.

**Analysis produces no results**

Confirm that the genome assembly specified in your analysis YAML file
(hg19 or hg38) matches the data you downloaded and configured. Analysing
a VCF called against an assembly you have not loaded will produce an error.