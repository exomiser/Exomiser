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

package org.monarchinitiative.exomiser.autoconfigure.genome;

import org.monarchinitiative.exomiser.autoconfigure.DataSourceProperties;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;

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

    //IMPORTANT! Do not change this name to match the usual java conventions as this will break the application.properties parsing
    //where the property path is exomiser.hg19.datasource....
    public DataSourceProperties getDatasource();

    public void setDatasource(DataSourceProperties dataSourceProperties);

    //Tabix variant data files
    public String getFrequencyPath();

    public void setFrequencyPath(String frequencyPath);

    public String getPathogenicityPath();

    public void setPathogenicityPath(String pathogenicityPath);

    //Optional tabix variant data
    public String getCaddSnvPath();

    public void setCaddSnvPath(String caddSnvPath);

    public String getCaddInDelPath();

    public void setCaddInDelPath(String caddInDelPath);

    public String getRemmPath();

    public void setRemmPath(String remmPath);

    public String getLocalFrequencyPath();

    public void setLocalFrequencyPath(String localFrequencyPath);
}
