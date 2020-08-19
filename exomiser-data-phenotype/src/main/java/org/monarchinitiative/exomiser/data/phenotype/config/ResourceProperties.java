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

import java.util.Objects;

/**
 * Java bean for holding configuration properties for a resource.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ResourceProperties {

    /**
     * The parent url for the resource. For example, if a file is available from http://foo.bar.org/downloads/baz.txt,
     * this field should be 'http://foo.bar.org/downloads/'.
     *
     * If the resource is local only this field should be left blank.
     */
    private  String url = "";
    /**
     * The remote file name/identifier for the resource. For example, if a file is available from http://foo.bar.org/downloads/baz.txt,
     * this field should be 'baz.txt'.
     *
     * If the resource is local only this field should be left blank.
     */
    private  String remoteFile = "";
    /**
     * The local filename of the resource, if different from the remote file name.
     */
    private  String localFile = "";


    // required for Spring @ConfigurationProperties to work
    public ResourceProperties() {
    }

    public ResourceProperties(String url, String remoteFile, String localFile) {
        this.url = Objects.requireNonNull(url);
        this.remoteFile = Objects.requireNonNull(remoteFile);
        this.localFile = Objects.requireNonNull(localFile);
    }

    public static ResourceProperties ofRemote(String url, String remoteFile) {
        return new ResourceProperties(url, remoteFile, "");
    }

    public static ResourceProperties ofLocal(String localFile) {
        return new ResourceProperties("", "", localFile);
    }

    public static ResourceProperties empty() {
        return new ResourceProperties();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRemoteFile() {
        return remoteFile;
    }

    public void setRemoteFile(String remoteFile) {
        this.remoteFile = remoteFile;
    }

    public String getLocalFile() {
        return localFile;
    }

    public void setLocalFile(String localFile) {
        this.localFile = localFile;
    }

    @Override
    public String toString() {
        return "ResourceProperties{" +
                "url='" + url + '\'' +
                ", remoteFile='" + remoteFile + '\'' +
                ", localFile='" + localFile + '\'' +
                '}';
    }
}
