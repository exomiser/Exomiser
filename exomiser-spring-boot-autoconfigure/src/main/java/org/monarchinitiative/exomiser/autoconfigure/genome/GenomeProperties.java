/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

import org.monarchinitiative.exomiser.autoconfigure.DataSourceProperties;
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

    public GenomeAssembly getAssembly();

    public String getDataVersion();

    public void setDataVersion(String dataVersion);

    public TranscriptSource getTranscriptSource();

    public void setTranscriptSource(TranscriptSource transcriptSource);

    public void setTranscriptSource(String name);

    public Path getDataDirectory();

    public void setDataDirectory(String dataDirectory);

    //IMPORTANT! Do not change this name to match the usual java conventions as this will break the application.properties parsing
    //where the property path is exomiser.hg19.datasource....
    public DataSourceProperties getDatasource();

    public void setDatasource(DataSourceProperties dataSourceProperties);

    //Optional tabix variant data

    public String getVariantWhiteListPath();

    public void setVariantWhiteListPath(String VariantWhiteListPath);

    public String getCaddSnvPath();

    public void setCaddSnvPath(String caddSnvPath);

    public String getCaddInDelPath();

    public void setCaddInDelPath(String caddInDelPath);

    public String getRemmPath();

    public void setRemmPath(String remmPath);

    public String getLocalFrequencyPath();

    public void setLocalFrequencyPath(String localFrequencyPath);

    public String getTestPathogenicityScorePath();

    public void setTestPathogenicityScorePath(String testPathogenicityScorePath);
}
