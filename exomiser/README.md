The Exomiser - Main Exomiser Executable 
===============================================================

This maven project is used to build the main exomiser jar for distribution.

For distribution after doing a build the following steps also need to happen:

cd target
mkdir data
# copy in the data made from the db build or run this now if you haven't already
cp ../../exomiser-db/data/exomiser.h2.db data/.
cp ../../exomiser-db/data/extracted/ucsc_hg19.ser data/.
mv Pfeiffer.vcf data/.
# copy in the extra phenix data to data/
... from somewhere
# delete existing tgz if there is one as the next step will add files
rm exomiser.tgz
# make the archive.
tar -cvzf exomiser.tgz exomiser-2.0.0.jar jdbc.properties log4j2.xml lib data 

