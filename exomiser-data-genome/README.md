Exomiser - Genome DB Build
=

This is a Spring Boot CLI application and as such has one idiosyncrasy which will prevent a build from launching, if not
set in the `application.properties`.

The absolute requirement for anything, even `--help` to work is for the `--build-dir` variable to be set and this _must_
be set using an equals sign i.e. 

```shell
$ java -jar exomiser-data-genome-${project.version}.jar --build-dir=. --help
```

By default, this is set in the `application.properties` to `.` i.e. the current working directory but can be overriden.

Also, note that the `--assembly` and `--version` **must come before any other arguments** in order that they are correctly
set for use with other optional arguments. 

Build transcript databases and build/annotate ClinVar data. The ClinVar data build now requires a transcript database so
that the variants can be annotated for gene symbol and variant effect to be in line with the output from Exomiser. Whilst
these are available in the `MC` field of the ClinVar VCF file, the effects are not sorted according to reference
transcript order, so in cases where more than one transcript overlaps a variant, the most damaging effect is reported
first, even if the MANE/MANE_Clinical or canonical transcript has a less damaging effect.

```shell
$ java -jar exomiser-data-genome-${project.version}.jar --assembly hg38 --version 2311 --transcripts ensembl --clinvar
```
Will create an output directory `2311_hg38` containing the files:

```shell
2311_hg38/
├── 2311_hg38_clinvar.mv.db
└── 2311_hg38_transcripts_ensembl.ser
```

Create a new ClinVar database from the latest ClinVar release using an existing Exomiser release, in this case 2309_hg38:

```shell
$ java -jar exomiser-data-genome-${project.version}.jar --assembly hg38 --version 231112 --clinvar /data/exomiser/2309_hg38/2309_hg38_transcripts_ensembl.ser
```

Will just create the ClinVar database, annotated using the specified transcript data:

```shell
231112_hg38/
└── 231112_hg38_clinvar.mv.db
```
_n.b_ here the ClinVar data has been created for the 20231112 release, so it is possible to update the clinvar data for
Exomiser on a weekly basis to keep up with ClinVar