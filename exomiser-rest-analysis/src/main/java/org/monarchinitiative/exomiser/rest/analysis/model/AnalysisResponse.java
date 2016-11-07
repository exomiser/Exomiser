/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.rest.analysis.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class AnalysisResponse {

    private final long id;
    private final AnalysisStatus analysisStatus;
    private final String message;

    @JsonCreator
    public AnalysisResponse(@JsonProperty("id") long id, @JsonProperty("status") AnalysisStatus analysisStatus, @JsonProperty("message") String message) {
        this.id = id;
        this.analysisStatus = analysisStatus;
        this.message = message;
    }

    public long getId() {
        return id;
    }

//    @JsonGetter(value = "id_string")
    public String getIdString() {
        return Long.toString(id);
    }

    @JsonGetter(value = "status")
    public AnalysisStatus getAnalysisStatus() {
        return analysisStatus;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalysisResponse that = (AnalysisResponse) o;
        return id == that.id &&
                analysisStatus == that.analysisStatus &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, analysisStatus, message);
    }

    @Override
    public String toString() {
        return "AnalysisResponse{" +
                "id=" + id +
                ", analysisStatus=" + analysisStatus +
                ", message='" + message + '\'' +
                '}';
    }
}
