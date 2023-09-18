The Exomiser - Phenotype DB build
===============================================================

This maven project is used to build the Exomiser phenotype database used by the Exomiser. 

N.B. Now all run from apocrita (home folder on login.hpc.qmul.ac.uk) and from owltools installation at /data/WHRI-Phenogenomics/software/opt/owltools/owltools. To install from scratch use 

```git clone https://github.com/owlcollab/owltools.git
cd owltools/OWLTools-Parent
mvn clean install
cd ../../
chmod +x owltools/OWLTools-Runner/bin/owltools
```

1. ```git clone https://github.com/obophenotype/upheno``` or ```cd upheno and git pull```
2. ```qsub owltools_preprocess1.sh```
3. Run final commands on high mem machines on apocrita (home folder on login.hpc.qmul.ac.uk)

```
qsub owltools_hp_hp.sh
qsub owltools_hp_mp.sh
qsub owltools_hp_zp.sh

```

12. Running the build. More detail below but essentially

```
gzip hp-*-mapping-cache.txt
cd /data/WHRI-Phenogenomics/projects/Damian/
mkdir -p 2109-phenotype-build/resources
cp ~/hp-*cache.txt.gz 2109-phenotype-build/resources
java -Djava.io.tmpdir=/data/WHRI-Phenogenomics/projects/Damian  -jar exomiser-data-phenotype-13.0.0-SNAPSHOT.jar --phenotype.build-version=2109 --phenotype.build-dir=/data/WHRI-Phenogenomics/projects/Damian/2109-phenotype-build
```


This application will handle the downloading and processing resources and building the H2 database.

The application requires two commands to run `--phenotype.build-version` and `--phenotype.build-dir`
typically the build version follows the pattern yyMM (e.g. 2108 for August 2021), the build directory *must* be a full 
system path as otherwise the H2 database migration will fail.

Assuming the build-dir is named `2008-phenotype-build`, the directory layout under the build-dir will be like this:

```bash
2008-phenotype-build
|__ 2008_phenotype
|    |__ 2008_phenotype.h2.db
|__ processed
|__ resources
```

**IMPORTANT** before the ontology group can run the phenodigm cache files e.g. `hp-hp-mapping-cache.txt.gz` produced in 
the previous steps need to be moved into the `resources` subfolder, so that needs creating first.

Example command:
```shell script
mkdir -p /data/2008-phenotype-build/resources
cp hp-*-mapping-cache.txt.gz /data/2008-phenotype-build/resources/.
java -jar exomiser-data-phenotype-{$project.version}.jar --phenotype.build-version=2008 --phenotype.build-dir=/data/2008-phenotype-build
``` 

The application will then collect the resources defined in the `application.properties` and inject them into the classes 
required to orchestrate reading and processing of them into `.pg` files, which are then read into the database.
 
### Toggling steps
The application has three main steps, these are `download-resources`, `process-resources` and `migrate-database`. By 
default, they will all run, in that order. They can be individually toggled by changing the fields in the `application.properties` 
file:
 
 ```properties
phenotype.download-resources=true
phenotype.process-resources=true
phenotype.migrate-database=true
```
 
or overridden on the command line like so:
 
 ```shell script
java -jar exomiser-data-phenotype-{$project.version}.jar --phenotype.build-version=2008 --phenotype.build-dir=/data/2008-phenotype-build --phenotype.download-resources=false
```
 
### Download resources
The `download-resources` step will, as the name suggests, download the resources listed in the `application.properties` 
and places the files in the `resources` directory. 

The `phenotype.resource` is the namespace for all resources and may either have a remote, remote and local or only
 local form as shown here:

```properties
# HGNC - Remote file only example
phenotype.resource.hgnc-complete-set.url=ftp://ftp.ebi.ac.uk/pub/databases/genenames/new/tsv/
phenotype.resource.hgnc-complete-set.remote-file=hgnc_complete_set.txt

# Ensembl gene orthologs - Remote and local form. Here tha data is copied from the remote file to a local file named 'human_mouse_ensembl_orthologs.txt' 
phenotype.resource.ensembl-mouse-human-orthologs.url=http://www.ensembl.org/biomart/martservice?query=%3C?xml%20version=%221.0%22%20encoding=%22UTF-8%22?%3E%20%3C!DOCTYPE%20Query%3E%20%3CQuery%20%20virtualSchemaName%20=%20%22default%22%20formatter%20=%20%22TSV%22%20header%20=%20%220%22%20uniqueRows%20=%20%220%22%20count%20=%20%22%22%20datasetConfigVersion%20=%20%220.6%22%20%3E%20%20%3CDataset%20name%20=%20%22hsapiens_gene_ensembl%22%20interface%20=%20%22default%22%20%3E%20%3CAttribute%20name%20=%20%22entrezgene_id%22%20/%3E%20%3CAttribute%20name%20=%20%22hgnc_symbol%22%20/%3E%20%3C/Dataset%3E%20%3CDataset%20name%20=%20%22mmusculus_gene_ensembl%22%20interface%20=%20%22default%22%20%3E%20%3CAttribute%20name%20=%20%22mgi_id%22%20/%3E%20%3CAttribute%20name%20=%20%22mgi_symbol%22%20/%3E%20%3C/Dataset%3E%20%3C/
phenotype.resource.ensembl-mouse-human-orthologs.remote-file=Query%3E
phenotype.resource.ensembl-mouse-human-orthologs.local-file=human_mouse_ensembl_orthologs.txt

# HP-HP mappings - Local file only example.
phenotype.resource.hp-hp-mappings.local-file=hp-hp-phenodigm-cache.txt.gz
```  

Resources will check the correct properties have been supplied or throw an error. The application will also know how to 
handle reading from `.gz` or `.zip` files at runtime, so if the value `hp-hp-phenodigm-cache.txt.gz` in the above example
is not a gzip file changing it to `hp-hp-phenodigm-cache.txt` in the properties will change the reader used to process 
the file.

### Process resources
The `process-resources` step will take a logical group of resources, for example all files required to produce a table 
or group of related tables in the database. It will read them from the `resources` directory, process them and output 
zero to many `.pg` files which are pipe-delimited files for bulk import into the database. The `process-resources` stage 
may also perform other steps within a group in order to produce the final release, such as copying the `hpo.obo` file 
into the final `{$build-version}_phenotype` directory.
 
### Migrate database
The `migrate-database` stage reads the `.pg` files from `processed` into the release directory. This is orchestrated 
using Flyway migration scripts in `classpath:db/migration` and the `spring.flyway` namespace keys in the `application.properties` 
file. The datasource for the database is configured using the `spring.datasource.hikari` properties. In general, these 
should not need to be changed.

## Adding a Resource

* Add a new `org.monarchinitiative.exomiser.data.phenotype.config.ResourceProperties` to `org.monarchinitiative.exomiser.data.phenotype.config.ResourceConfigurationProperties` as a `@NestedConfigurationProperty` 
with the default values.

* Add a new `ResourceReader` and add this to the relevant `ProcessingStep`/`ProcessingGroup`. Implement the `OutputLine` interface 
to collect the data in to be written out to a `.pg` file with the `OutFileWriter`.

* Wire the resource, reader, writer etc. from the previous step together in a `@Configuration` file. New `ProcessingGroup`
 classes will need to be added to the constructor in `org.monarchinitiative.exomiser.data.phenotype.Main`.

* Add a new migration for the `.pg` file which needs loading.
