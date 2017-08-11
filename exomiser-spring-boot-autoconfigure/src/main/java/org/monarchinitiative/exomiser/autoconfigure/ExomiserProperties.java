/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@ConfigurationProperties(prefix = "exomiser")
public class ExomiserProperties {

    private String dataDirectory;

    private String workingDirectory;

    @NestedConfigurationProperty
    private ExomiserProperties.H2 h2 = new H2();

    //genomiser variant data files
    private String caddSnvPath = "";
    private String caddInDelPath = "";
    private String remmPath = "";

    private String localFrequencyPath = "";

    //http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-caching.html
//    private CacheType cache = CacheType.NONE;

    /**
     * none/mem/ehcache
     */
    private String cache = "none";

    /**
     * name of transcript data .ser file created from Jannovar for defining known exon locations
     */
    private String transcriptDataFileName = "hg19_ucsc.ser";

    //Random walk matrix for hiPhive and exomeWalker
    private String randomWalkFileName = "rw_string_9_05.gz";
    private String randomWalkIndexFileName = "rw_string_9_05_id2index.gz";

    //Phenix data
    private String phenixDataDir = "phenix";
    private String hpoFileName = "hp.obo";
    private String hpoAnnotationFile = "ALL_SOURCES_ALL_FREQUENCIES_genes_to_phenotype.txt";

    private String boqaDataDir = "boqa";

    public String getDataDirectory() {
        return dataDirectory;
    }

    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public ExomiserProperties.H2 getH2() {
        return h2;
    }

    public void setH2(ExomiserProperties.H2 h2) {
        this.h2 = h2;
    }

    public String getCaddSnvPath() {
        return caddSnvPath;
    }

    public void setCaddSnvPath(String caddSnvPath) {
        this.caddSnvPath = caddSnvPath;
    }

    public String getCaddInDelPath() {
        return caddInDelPath;
    }

    public void setCaddInDelPath(String caddInDelPath) {
        this.caddInDelPath = caddInDelPath;
    }

    public String getRemmPath() {
        return remmPath;
    }

    public void setRemmPath(String remmPath) {
        this.remmPath = remmPath;
    }

    public String getLocalFrequencyPath() {
        return localFrequencyPath;
    }

    public void setLocalFrequencyPath(String localFrequencyPath) {
        this.localFrequencyPath = localFrequencyPath;
    }

    public String getCache() {
        return cache;
    }

    public void setCache(String cache) {
        this.cache = cache;
    }

    public String getTranscriptDataFileName() {
        return transcriptDataFileName;
    }

    public void setTranscriptDataFileName(String transcriptDataFileName) {
        this.transcriptDataFileName = transcriptDataFileName;
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

    public String getPhenixDataDir() {
        return phenixDataDir;
    }

    public void setPhenixDataDir(String phenixDataDir) {
        this.phenixDataDir = phenixDataDir;
    }

    public String getBoqaDataDir() {
        return boqaDataDir;
    }

    public void setBoqaDataDir(String boqaDataDir) {
        this.boqaDataDir = boqaDataDir;
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

    public static class H2 {

        private String directory = "";

        private String user = "sa";
        private String password = "";
        private String url = "jdbc:h2:file:${h2Path}/exomiser;MODE=PostgreSQL;SCHEMA=EXOMISER;DATABASE_TO_UPPER=FALSE;IFEXISTS=TRUE;AUTO_RECONNECT=TRUE;ACCESS_MODE_DATA=r;";
        private int maxConnections = 3;


        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getMaxConnections() {
            return maxConnections;
        }

        public void setMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
        }
    }
}
