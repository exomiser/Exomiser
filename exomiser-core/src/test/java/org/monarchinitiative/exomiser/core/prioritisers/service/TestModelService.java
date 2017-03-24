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

package org.monarchinitiative.exomiser.core.prioritisers.service;

import org.monarchinitiative.exomiser.core.prioritisers.model.GeneModel;

import java.util.List;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TestModelService implements ModelService {

    private final List<GeneModel> diseaseModels;
    private final List<GeneModel> mouseModels;
    private final List<GeneModel> fishModels;

    public TestModelService(List<GeneModel> diseaseModels, List<GeneModel> mouseModels, List<GeneModel> fishModels) {
        this.diseaseModels = diseaseModels;
        this.mouseModels = mouseModels;
        this.fishModels = fishModels;
    }

    @Override
    public List<GeneModel> getHumanGeneDiseaseModels() {
        return diseaseModels;
    }

    @Override
    public List<GeneModel> getMouseGeneOrthologModels() {
        return mouseModels;
    }

    @Override
    public List<GeneModel> getFishGeneOrthologModels() {
        return fishModels;
    }
}
