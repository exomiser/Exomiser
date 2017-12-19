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

import org.monarchinitiative.exomiser.autoconfigure.genome.Hg19GenomeProperties;
import org.monarchinitiative.exomiser.autoconfigure.genome.Hg38GenomeProperties;
import org.monarchinitiative.exomiser.autoconfigure.phenotype.PhenotypeProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@ConfigurationProperties(prefix = "exomiser")
public class ExomiserProperties {

    private String dataDirectory;

    private String workingDirectory;

    //exomiser.phenotype...
    @NestedConfigurationProperty
    private PhenotypeProperties phenotype = new PhenotypeProperties();

    //exomiser.hg19...
    @NestedConfigurationProperty
    private Hg19GenomeProperties hg19 = new Hg19GenomeProperties();

    @NestedConfigurationProperty
    private Hg38GenomeProperties hg38 = new Hg38GenomeProperties();

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

    public PhenotypeProperties getPhenotype() {
        return phenotype;
    }

    public void setPhenotype(PhenotypeProperties phenotype) {
        this.phenotype = phenotype;
    }

    public Hg19GenomeProperties getHg19() {
        return hg19;
    }

    public void setHg19(Hg19GenomeProperties hg19) {
        this.hg19 = hg19;
    }

    public Hg38GenomeProperties getHg38() {
        return hg38;
    }

    public void setHg38(Hg38GenomeProperties hg38) {
        this.hg38 = hg38;
    }

}
