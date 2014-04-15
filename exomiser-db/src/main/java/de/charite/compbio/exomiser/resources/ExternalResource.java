/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.resources;

import java.util.Objects;

/**
 * Bean for storing information about an external resource required for building
 * the Exomiser database.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ExternalResource {

    private String name;
    private String url;
    private String remoteFileName;
    private String version;
    private String extractedFileName;
    private String extractionScheme;
    private String parser;
    private String parserGroup;
    private String parsedFileName;
    
    private ResourceOperationStatus downloadStatus;
    private ResourceOperationStatus extractStatus;
    private ResourceOperationStatus parseStatus;

    public ExternalResource() {
        downloadStatus = ResourceOperationStatus.UNTRIED;
        extractStatus = ResourceOperationStatus.UNTRIED;
        parseStatus = ResourceOperationStatus.UNTRIED;
    }
    
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getParser() {
        return parser;
    }

    public void setParser(String parser) {
        this.parser = parser;
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

    public String getParserGroup() {
        return parserGroup;
    }

    public void setParserGroup(String parserGroup) {
        this.parserGroup = parserGroup;
    }

    public String getParsedFileName() {
        return parsedFileName;
    }

    public void setParsedFileName(String parsedFileName) {
        this.parsedFileName = parsedFileName;
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
        hash = 71 * hash + Objects.hashCode(this.parser);
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
        final ExternalResource other = (ExternalResource) obj;
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
        if (!Objects.equals(this.parser, other.parser)) {
            return false;
        }
        if (!Objects.equals(this.parsedFileName, other.parsedFileName)) {
            return false;
        }
        return true;
    }
   
    public String getStatus() {
        return String.format("Status for: %-8s Download: %s, Extract: %s, Parse: %s", name, downloadStatus, extractStatus, parseStatus);
    }
    
    @Override
    public String toString() {
        return "ExternalResource{" + "name=" + name + ", url=" + url + ", fileName=" + remoteFileName + ", version=" + version + ", parser=" + parser + ", parsedFileName=" + parsedFileName +'}';
    }
}
