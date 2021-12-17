===============
Troubleshooting
===============

java.lang.UnsupportedClassVersionError:
=======================================
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
