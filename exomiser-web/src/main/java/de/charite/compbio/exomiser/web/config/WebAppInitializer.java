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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.util.Set;

/**
 * Replacement for the old web.xml
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class WebAppInitializer implements WebApplicationInitializer {

    private static final Logger logger = LoggerFactory.getLogger(WebAppInitializer.class);

    @Override
    public void onStartup(ServletContext servletContext) {
        WebApplicationContext rootContext = createRootContext(servletContext);

        configureSpringMvc(servletContext, rootContext);
    }

    
    private WebApplicationContext createRootContext(ServletContext servletContext) {
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();

        servletContext.addListener(new ContextLoaderListener(rootContext));
        servletContext.setInitParameter("defaultHtmlEscape", "true");

        return rootContext;
    }

    private void configureSpringMvc(ServletContext servletContext, WebApplicationContext rootContext) {
        AnnotationConfigWebApplicationContext mvcContext = new AnnotationConfigWebApplicationContext();
        mvcContext.register(ControllerConfig.class);
        mvcContext.register(ExomiserConfig.class);
        mvcContext.setParent(rootContext);
        
        ServletRegistration.Dynamic appServlet = servletContext.addServlet("exomiser", new DispatcherServlet(mvcContext));
        appServlet.setLoadOnStartup(1);
        Set<String> mappingConflicts = appServlet.addMapping("/");

        if (!mappingConflicts.isEmpty()) {
            for (String s : mappingConflicts) {
                logger.error("Mapping conflict: " + s);
            }
            throw new IllegalStateException("'exomiser' cannot be mapped to '/'");
        }
    }

}
