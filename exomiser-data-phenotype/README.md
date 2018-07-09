The Exomiser - Phenotype DB build
===============================================================

This maven project is used to build the Exomiser phenotype database used by the Exomiser. 

Currently the build relies on a preliminary OWLSim2 file build which in future versions will
be incorporated into this codebase:

1. OWLTools
```
    git clone https://github.com/owlcollab/owltools.git or git pull
    cd owltools/OWLTools-Parent
    mvn clean install
    cd ../../
    chmod +x owltools/OWLTools-Runner/bin/owltools
    ```
    Add ```owltools/OWLTools-Oort/bin/ontology-release-runner``` and ```owltools/OWLTools-Runner/bin/owltools``` to path
2. Clone Monarch OwlSim:
```git clone https://github.com/monarch-initiative/monarch-owlsim-data or git pull``` 
3. Clone the HPO:
```git clone https://github.com/obophenotype/human-phenotype-ontology/ or git pull```
4. Clone the MPO: 
```
    git clone https://github.com/obophenotype/mammalian-phenotype-ontology/ or git pull
    cd mammalian-phenotype-ontology/src/ontology
    make mp.owl
    cd ../../../
   ```
5. Clone uPheno:
```git clone https://github.com/obophenotype/upheno```
6. Get ZPO:
```wget http://compbio.charite.de/jenkins/job/zp-owl/lastSuccessfulBuild/artifact/zp.owl```
7. Replace human phenotype annotation files in Monarch git repo as these include common disease and merge together some 
OMIM and Orphanet entries in a way that does not represent the data in our db. Requires logic like:

system("wget http://compbio.charite.de/jenkins/job/hpo.annotations/lastStableBuild/artifact/misc/phenotype_annotation.tab");
```
open(IN,"phenotype_annotation.tab");
open(OUT1,">monarch-owlsim-data/data/Homo_sapiens/Hs_disease_phenotype.txt");
open(OUT2,">monarch-owlsim-data/data/Homo_sapiens/Hs_disease_labels.txt");
my %data;
while (my $line = <IN>){
    my @line = split(/\t/,$line);
    my $id = $line[0].":".$line[1];
    $id =~ s/ //g;
    my $label = $line[2];
    my $hp =  $line[4];
    $hp =~ s/ //g;
    $data{$id}{$label}{$hp} = 1;
}
close IN;
foreach my $id(sort keys %data){
    foreach my $label(sort keys %{$data{$id}}){
	print OUT2 "$id\t$label\n";
	foreach my $hp (sort keys %{$data{$id}{$label}}){
	    print OUT1 "$id\t$hp\n";
	}
    }
}
close OUT1;
close OUT2;
```

8. Run owltools commands:
```
owltools --catalog-xml upheno/catalog-v001.xml mammalian-phenotype-ontology/scratch/mp-importer.owl mammalian-phenotype-ontology/src/ontology/mp.owl human-phenotype-ontology/hp.owl zp.owl monarch-owlsim-data/data/Mus_musculus/Mm_gene_phenotype.txt monarch-owlsim-data/data/Homo_sapiens/Hs_disease_phenotype.txt monarch-owlsim-data/data/Danio_rerio/Dr_gene_phenotype.txt --merge-imports-closure --load-instances monarch-owlsim-data/data/Mus_musculus/Mm_gene_phenotype.txt --load-labels monarch-owlsim-data/data/Mus_musculus/Mm_gene_labels.txt --merge-support-ontologies -o Mus_musculus-all.owl

owltools --catalog-xml upheno/catalog-v001.xml human-phenotype-ontology/scratch/hp-importer.owl mammalian-phenotype-ontology/src/ontology/mp.owl human-phenotype-ontology/hp.owl zp.owl monarch-owlsim-data/data/Mus_musculus/Mm_gene_phenotype.txt monarch-owlsim-data/data/Homo_sapiens/Hs_disease_phenotype.txt monarch-owlsim-data/data/Danio_rerio/Dr_gene_phenotype.txt --merge-imports-closure --load-instances monarch-owlsim-data/data/Homo_sapiens/Hs_disease_phenotype.txt --load-labels monarch-owlsim-data/data/Homo_sapiens/Hs_disease_labels.txt --merge-support-ontologies -o Homo_sapiens-all.owl

owltools --catalog-xml upheno/catalog-v001.xml upheno/vertebrate.owl mammalian-phenotype-ontology/src/ontology/mp.owl human-phenotype-ontology/hp.owl zp.owl monarch-owlsim-data/data/Mus_musculus/Mm_gene_phenotype.txt monarch-owlsim-data/data/Homo_sapiens/Hs_disease_phenotype.txt monarch-owlsim-data/data/Danio_rerio/Dr_gene_phenotype.txt --load-instances monarch-owlsim-data/data/Danio_rerio/Dr_gene_phenotype.txt --load-labels monarch-owlsim-data/data/Danio_rerio/Dr_gene_labels.txt --load-instances monarch-owlsim-data/data/Homo_sapiens/Hs_disease_phenotype.txt --load-labels monarch-owlsim-data/data/Homo_sapiens/Hs_disease_labels.txt --merge-support-ontologies --merge-imports-closure --remove-disjoints --remove-equivalent-to-nothing-axioms --run-reasoner -r elk --assert-implied --make-super-slim HP,ZP -o hp-zp-all.owl

owltools Homo_sapiens-all.owl --merge-import-closure --remove-disjoints --remove-equivalent-to-nothing-axioms -o Homo_sapiens-all-merged.owl
owltools Mus_musculus-all.owl --merge-import-closure --remove-disjoints --remove-equivalent-to-nothing-axioms -o Mus_musculus-all-merged.owl
owltools hp-zp-all.owl --merge-import-closure --remove-disjoints --remove-equivalent-to-nothing-axioms -o hp-zp-all-merged.owl

OWLTOOLS_MEMORY=14G owltools Homo_sapiens-all-merged.owl --sim-save-phenodigm-class-scores -m 2.5 -x HP,HP -a hp-hp-phenodigm-cache.txt
OWLTOOLS_MEMORY=14G owltools Mus_musculus-all-merged.owl Homo_sapiens-all-merged.owl upheno/hp-mp/mp_hp-align-equiv.owl --merge-support-ontologies --sim-save-phenodigm-class-scores -m 2.5 -x HP,MP -a hp-mp-phenodigm-cache.txt
OWLTOOLS_MEMORY=14G owltools hp-zp-all-merged.owl --sim-save-phenodigm-class-scores -m 2.5 -x HP,ZP -a hp-zp-phenodigm-cache.txt
```
8. mv *-cache.txt to downloads directory of database build

The application requires configuration in a couple of places depending on how it
is to be used.

It functions by loading in the Resources from ResourceConfig and then will attempt to
download, extract, parse and then import the parsed resources into a freshly created
database schema called EXOMISER. 

Flyway (http://flywaydb.org/) maven migrations for importing data into the database
can be performed from an IDE or the command-line using the appropriate maven
profile, for example:

    mvn -P migrate-H2 flyway:info
    
or specified from the command-line:

    java -jar exomiser-data-phenotype-${project.version}.jar --exomiser.h2.url="jdbc:h2:file:/data/exomiser-build/exomiser-data-phenotype;MODE=Postg
    reSQL;LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0;MV_STORE=FALSE" --data.path=/data/exomiser-build/

In order for this to work the settings in the POM file may need to be changed. These
won't work without any data to import which is what App.main is all about.   

Running the application from App.main requires that the files in src/main/resources 
are correctly configured. These files and other configurations are injected into 
the application by the classes in the exomiser.config package:

* org.monarchinitiative.exomiser.config.AppConfig
    
Injects the data from the app.properties 

* org.monarchinitiative.exomiser.config.DataSourceConfig
    
Injects the DataSources for the database connections

* org.monarchinitiative.exomiser.config.ResourceConfig
    
Injects the Resources - these specify where the data should be downloaded 
from, how it should be handled in order that the parser can parse it, what parser 
is needed to parse it, which other files should be parsed with it and what the 
output file should be called.

Commenting out a resource means it won't be processed. If the resource was part 
of a parserGroup then that entire group will not be handled and ultimately the 
database build will halt at the migration of the resource or group's parsedFileName.
    
The application can also be run from the command line. The full download, unpack, parse and load database looks like so:

```bash
java -jar exomiser-data-phenotype-${project.version}-SNAPSHOT.jar 
        --data.path=/data/exomiser-build/data 
        --downloadResources=true 
        --extractResources=true
        --parseResources=true 
        --dumpPhenoDigmData=true
        --migrateH2=true
        --exomiser.h2.url="jdbc:h2:file:/data/exomiser-data-phenotype/exomiser;MODE=PostgreSQL;LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0;MV_STORE=FALSE"
```
These are overriding the properties in ```app.properties``` which default to false. The ```exomiser.h2.url``` overrides the database properties in ```jdbc.properties```.

    
In general you shouldn't need to touch anything but, in case you do the resources
are detailed below and check the log output:

## src/main/resources

### app.properties

This will provide the application with the location of where you want it to
run, download, unpack and process the resources. The resources are described in
the exomiser.config.ResourceConfig class.  
    
### jdbc.properties
    
Contains the database connection settings for jdbc used by Flyway when called
from App.main.

## src/main/resources/data

This is where some static data required by some parsers but which requires 
manual processing to produce is stored. The resources are referred to in the 
exomiser.config.ResourceConfig class but are moved into the process directory by 
spring.

### pheno2gene.txt

This requires extensive messing about with a one-off dump-file from OMIM, a 
perl parser, data from Entrez Gene and some java parsing. Given this was a one-off
it seemed best to just include the final processed file which can be updated 
whenever appropriate. 

### ucsc_hg19.ser

Requires the following UCSC known genes files - these are defined and downloaded as resources
so they will be found, ready to use in data/extracted:

        http://hgdownload.soe.ucsc.edu/goldenPath/hg19/database/knownGene.txt.gz
        http://hgdownload.soe.ucsc.edu/goldenPath/hg19/database/kgXref.txt.gz
        http://hgdownload.soe.ucsc.edu/goldenPath/hg19/database/knownGeneMrna.txt.gz
        http://hgdownload.soe.ucsc.edu/goldenPath/hg19/database/knownToLocusLink.txt.gz

then convert to ucsc.ser with jannovar:

    ant annotator

    java -Xmx2G -jar Annotator.jar -U knownGene.txt -M knownGeneMrna.txt -X kgXref.txt -L knownToLocusLink.txt -S ucsc.ser
or...
    java -Xms1G -Xmx2G -jar Jannovar.jar --create-ucsc


## src/main/resources/db/migration

Contains the database schema and the import sql files for the H2 database. The
PostgreSQL migrations are run by Flyway as java migrations. Consequently they are
found in src/main/java/db/migration/postgres.


## Adding a Resource

* Add a new Bean to org.monarchinitiative.exomiser.config.ResourceConfig and ensure 
this is loaded in the ResourceConfig.resources() method.

* Add a new Parser and or ParserGroup if there are several parsers which need to 
exchange data.

* Add a new migration for the data which needs loading. These need to be 
duplicated - one for the H2 database, the other for PostgreSQL.
