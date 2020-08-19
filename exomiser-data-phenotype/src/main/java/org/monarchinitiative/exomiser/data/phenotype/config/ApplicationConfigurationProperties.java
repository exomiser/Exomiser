/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.phenotype.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@ConfigurationProperties(prefix = "phenotype")
public class ApplicationConfigurationProperties {

    /**
     * Build version string for this release. Default follows the pattern yyMM e.g. 2008 for August 2020
     */
    private String buildVersion = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMM"));

    /**
     * Root build directory for the data to be downloaded, processed and migrated. This must be an absolute path.
     * Recommended pattern is yyMM-phenotype-build.
     */
    private String buildDir = buildVersion + "-phenotype-build";

    /**
     * Toggle to enable/disable downloading of resources. Default is 'true'.
     */
    private boolean downloadResources = true;

    /**
     * Toggle to enable/disable processing of resources. Default is 'true'.
     */
    private boolean processResources = true;

    /**
     * Toggle to enable/disable migrating the database. Default is 'true'.
     */
    private boolean migrateDatabase = true;

    public String getBuildDir() {
        return buildDir;
    }

    public void setBuildDir(String buildDir) {
        this.buildDir = buildDir;
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public void setBuildVersion(String buildVersion) {
        this.buildVersion = buildVersion;
    }

    public boolean isDownloadResources() {
        return downloadResources;
    }

    public void setDownloadResources(boolean downloadResources) {
        this.downloadResources = downloadResources;
    }

    public boolean isProcessResources() {
        return processResources;
    }

    public void setProcessResources(boolean processResources) {
        this.processResources = processResources;
    }

    public boolean isMigrateDatabase() {
        return migrateDatabase;
    }

    public void setMigrateDatabase(boolean migrateDatabase) {
        this.migrateDatabase = migrateDatabase;
    }

}
