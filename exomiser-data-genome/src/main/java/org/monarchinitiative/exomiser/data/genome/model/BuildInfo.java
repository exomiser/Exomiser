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

package org.monarchinitiative.exomiser.data.genome.model;

import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class BuildInfo {

    private final GenomeAssembly assembly;
    private final String version;

    private BuildInfo(GenomeAssembly assembly, String version) {
        this.assembly = assembly;
        this.version = version;
    }

    public static BuildInfo of(GenomeAssembly assembly, String version) {
        Objects.requireNonNull(assembly);
        Objects.requireNonNull(version);

        return new BuildInfo(assembly, version);
    }

    public GenomeAssembly getAssembly() {
        return assembly;
    }

    public String getVersion() {
        return version;
    }

    public String getBuildString() {
        return version + "_" + assembly;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuildInfo buildInfo = (BuildInfo) o;
        return assembly == buildInfo.assembly &&
                Objects.equals(version, buildInfo.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assembly, version);
    }

    @Override
    public String toString() {
        return "Build{" +
                "assembly=" + assembly +
                ", version='" + version + '\'' +
                '}';
    }
}
