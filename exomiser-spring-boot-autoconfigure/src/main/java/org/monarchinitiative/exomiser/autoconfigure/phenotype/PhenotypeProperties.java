/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.autoconfigure.phenotype;

import org.monarchinitiative.exomiser.autoconfigure.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@ConfigurationProperties("exomiser.phenotype")
public class PhenotypeProperties {

    private Path dataDirectory;

    private String dataVersion = "";

    @NestedConfigurationProperty
    private DataSourceProperties datasource = new DataSourceProperties();

    //Random walk matrix for hiPhive and exomeWalker
    // pre 10.0.0. the randomWalkFileName ended with a .gz extension
    // this was changed to use an MVStore with a .mv extension in version 10.0.0
    private String randomWalkFileName = "rw_string_10.mv";
    private String randomWalkIndexFileName = "rw_string_9_05_id2index.gz";
    private boolean randomWalkPreload = false;

    //Phenix data
    private String phenixDataDir = "phenix";
    private String hpoFileName = "hp.obo";
    private String hpoAnnotationFile = "ALL_SOURCES_ALL_FREQUENCIES_genes_to_phenotype.txt";

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = Paths.get(dataDirectory);
    }

    public String getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(String dataVersion) {
        this.dataVersion = dataVersion;
    }

    public DataSourceProperties getDatasource() {
        return this.datasource;
    }

    public void setDatasource(DataSourceProperties dataSourceProperties) {
        this.datasource = dataSourceProperties;
    }

    public String getRandomWalkFileName() {
        return randomWalkFileName;
    }

    public void setRandomWalkFileName(String randomWalkFileName) {
        this.randomWalkFileName = randomWalkFileName;
    }

    public String getRandomWalkIndexFileName() {
        return randomWalkIndexFileName;
    }

    public void setRandomWalkIndexFileName(String randomWalkIndexFileName) {
        this.randomWalkIndexFileName = randomWalkIndexFileName;
    }

    public boolean isRandomWalkPreload() {
        return randomWalkPreload;
    }

    public void setRandomWalkPreload(boolean randomWalkPreload) {
        this.randomWalkPreload = randomWalkPreload;
    }

    public String getPhenixDataDir() {
        return phenixDataDir;
    }

    public void setPhenixDataDir(String phenixDataDir) {
        this.phenixDataDir = phenixDataDir;
    }

    public String getHpoFileName() {
        return hpoFileName;
    }

    public void setHpoFileName(String hpoFileName) {
        this.hpoFileName = hpoFileName;
    }

    public String getHpoAnnotationFile() {
        return hpoAnnotationFile;
    }

    public void setHpoAnnotationFile(String hpoAnnotationFile) {
        this.hpoAnnotationFile = hpoAnnotationFile;
    }

    @Override
    public String toString() {
        return "PhenotypeProperties{" +
                "dataVersion='" + dataVersion + '\'' +
                ", datasource=" + datasource +
                ", randomWalkFileName='" + randomWalkFileName + '\'' +
                ", randomWalkIndexFileName='" + randomWalkIndexFileName + '\'' +
                ", phenixDataDir='" + phenixDataDir + '\'' +
                ", hpoFileName='" + hpoFileName + '\'' +
                ", hpoAnnotationFile='" + hpoAnnotationFile + '\'' +
                '}';
    }
}
