/*
 * Copyright (C) 2014 Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.charite.compbio.exomiser.web.model;

import de.charite.compbio.exomiser.core.model.ExomiserSettings;
import org.springframework.hateoas.ResourceSupport;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class Job extends ResourceSupport {
    
    private final String jobId;
    private ExomiserSettings settings;
    
    public Job(String id) {
        this.jobId = id;
        this.settings = new ExomiserSettings.SettingsBuilder().build();
    }

    public String getJobId() {
        return jobId;
    }

    public ExomiserSettings getSettings() {
        return settings;
    }

    public void setSettings(ExomiserSettings settings) {
        this.settings = settings;
    }
    

}
