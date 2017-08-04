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

package org.monarchinitiative.exomiser.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Controller
public class StaticPageController {

    @GetMapping(value = "/")
    public String root() {
        return "index";
    }

    @GetMapping(value = "index")
    public String index() {
        return "index";
    }

    @GetMapping(value = "publications")
    public String publications() {
        return "publications";
    }

    @GetMapping(value = "download")
    public String download() {
        return "download";
    }

    @GetMapping(value = "legal")
    public String legal() {
        return "legal";
    }

    @GetMapping(value = "about")
    public String about() {
        return "about";
    }
}
