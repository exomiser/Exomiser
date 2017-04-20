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
