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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.data.phenotype.resources;

import org.monarchinitiative.exomiser.data.phenotype.parsers.ResourceGroupParser;
import org.monarchinitiative.exomiser.data.phenotype.parsers.ResourceParser;

import java.util.Objects;

/**
 * This is the fundamental atomic unit of work for storing information about a 
 * resource required for building the Exomiser database.
 *
 * It specifies where a required resource (a file) is found, both on the web and 
 * locally. How it should be downloaded and extracted, which class is required to
 * parse the resource and whether this is also required by other resources in 
 * order to parse them as part of a 
 * {@code org.monarchinitiative.exomiser.resources.ResourceGroup}.
 *
 * It also tracks the download, extract and parse steps as 
 * {@code org.monarchinitiative.exomiser.resources.ResourceOperationStatus}s
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class Resource {

    private final String name;
    private String url;
    private String remoteFileName;
    private String version;
    private String extractedFileName;
    private String extractionScheme;
    private Class<? extends ResourceParser> parserClass;
    private String parsedFileName;
    private String resourceGroupName;
    private Class<? extends ResourceGroupParser> resourceGroupParserClass;

    private ResourceOperationStatus downloadStatus;
    private ResourceOperationStatus extractStatus;
    private ResourceOperationStatus parseStatus;


    public Resource(String name) {
        this.name = name;
        downloadStatus = ResourceOperationStatus.UNTRIED;
        extractStatus = ResourceOperationStatus.UNTRIED;
        parseStatus = ResourceOperationStatus.UNTRIED;
    }


    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRemoteFileName() {
        return remoteFileName;
    }

    public void setRemoteFileName(String fileName) {
        this.remoteFileName = fileName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Class<? extends ResourceParser> getParserClass() {
        return parserClass;
    }

    public void setParserClass(Class<? extends ResourceParser> parserClass) {
        this.parserClass = parserClass;
    }

    public String getExtractedFileName() {
        return extractedFileName;
    }

    public void setExtractedFileName(String extractedFileName) {
        this.extractedFileName = extractedFileName;
    }

    public String getExtractionScheme() {
        return extractionScheme;
    }

    public void setExtractionScheme(String extractionScheme) {
        this.extractionScheme = extractionScheme;
    }

    public void setDownloadStatus(ResourceOperationStatus downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public void setExtractStatus(ResourceOperationStatus extractStatus) {
        this.extractStatus = extractStatus;
    }

    public void setParseStatus(ResourceOperationStatus parseStatus) {
        this.parseStatus = parseStatus;
    }

    public String getParsedFileName() {
        return parsedFileName;
    }

    public void setParsedFileName(String parsedFileName) {
        this.parsedFileName = parsedFileName;
    }

    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

    public Class<? extends ResourceGroupParser> getResourceGroupParserClass() {
        return resourceGroupParserClass;
    }

    public void setResourceGroupParserClass(Class<? extends ResourceGroupParser> resourceGroupParserClass) {
        this.resourceGroupParserClass = resourceGroupParserClass;
    }

    public ResourceOperationStatus getDownloadStatus() {
        return downloadStatus;
    }

    public ResourceOperationStatus getExtractStatus() {
        return extractStatus;
    }

    public ResourceOperationStatus getParseStatus() {
        return parseStatus;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.name);
        hash = 71 * hash + Objects.hashCode(this.url);
        hash = 71 * hash + Objects.hashCode(this.remoteFileName);
        hash = 71 * hash + Objects.hashCode(this.version);
        hash = 71 * hash + Objects.hashCode(this.extractedFileName);
        hash = 71 * hash + Objects.hashCode(this.extractionScheme);
        hash = 71 * hash + Objects.hashCode(this.parserClass);
        hash = 71 * hash + Objects.hashCode(this.parsedFileName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Resource other = (Resource) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.url, other.url)) {
            return false;
        }
        if (!Objects.equals(this.remoteFileName, other.remoteFileName)) {
            return false;
        }
        if (!Objects.equals(this.version, other.version)) {
            return false;
        }
        if (!Objects.equals(this.extractedFileName, other.extractedFileName)) {
            return false;
        }
        if (!Objects.equals(this.extractionScheme, other.extractionScheme)) {
            return false;
        }
        if (!Objects.equals(this.parserClass, other.parserClass)) {
            return false;
        }
        return Objects.equals(this.parsedFileName, other.parsedFileName);
    }

    public String getStatus() {
        return String.format("Status for: %-23s Download: %s, Extract: %s, Parse: %s", name, downloadStatus, extractStatus, parseStatus);
    }

    @Override
    public String toString() {
        return "Resource{" + "name=" + name + ", url=" + url + ", fileName=" + remoteFileName + ", version=" + version + ", parser=" + parserClass + ", parsedFileName=" + parsedFileName + ", resourceGroupName=" + resourceGroupName +'}';
    }
}
