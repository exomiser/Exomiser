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

import org.junit.After;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public abstract class AbstractAutoConfigurationTest {

    protected static final Path TEST_DATA = Paths.get("src/test/resources/data");
    protected static final String TEST_DATA_ENV = "exomiser.data-directory=" + TEST_DATA;

    protected ConfigurableApplicationContext context;

    @After
    public void closeContext() {
        if (this.context != null) {
            this.context.close();
        }
    }

    protected void load(Class<?> config, String... environment) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(config);
        EnvironmentTestUtils.addEnvironment(ctx, environment);
        ctx.refresh();
        this.context = ctx;
    }
}
