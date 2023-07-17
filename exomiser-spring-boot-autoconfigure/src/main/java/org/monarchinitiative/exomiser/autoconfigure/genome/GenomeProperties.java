/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.autoconfigure.genome;

import com.zaxxer.hikari.HikariDataSource;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.jannovar.TranscriptSource;

import java.nio.file.Path;

/**
 * Package-private interface. Required for polymorphic dispatch in the {@link GenomeAnalysisServiceConfigurer} constructor.
 * <p>
 * Do not make this public as this will lead to duplicate entries in the Spring application.properties auto-completion.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
interface GenomeProperties {

    GenomeAssembly getAssembly();

    String getDataVersion();

    void setDataVersion(String dataVersion);

    TranscriptSource getTranscriptSource();

    void setTranscriptSource(TranscriptSource transcriptSource);

    void setTranscriptSource(String name);

    Path getDataDirectory();

    void setDataDirectory(String dataDirectory);

    String getClinVarDataVersion();

    void setClinVarDataVersion(String name);

    boolean useClinVarWhiteList();

    void setUseClinVarWhiteList(boolean useClinVarWhiteList);

    HikariDataSource genomeDataSource();

    //Optional tabix variant data

    String getVariantWhiteListPath();

    void setVariantWhiteListPath(String VariantWhiteListPath);

    String getCaddSnvPath();

    void setCaddSnvPath(String caddSnvPath);

    String getCaddInDelPath();

    void setCaddInDelPath(String caddInDelPath);

    String getRemmPath();

    void setRemmPath(String remmPath);

    String getLocalFrequencyPath();

    void setLocalFrequencyPath(String localFrequencyPath);

    String getTestPathogenicityScorePath();

    void setTestPathogenicityScorePath(String testPathogenicityScorePath);
}
