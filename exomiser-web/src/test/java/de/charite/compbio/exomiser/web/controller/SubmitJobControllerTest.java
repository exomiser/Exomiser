/*
 * Copyright (C) 2014 jj8
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
package de.charite.compbio.exomiser.web.controller;

import config.TestDaoConfig;
import config.TestExomiserConfig;
import de.charite.compbio.exomiser.web.config.WebAppConfig;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author jj8
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {WebAppConfig.class, TestExomiserConfig.class, TestDaoConfig.class})
public class SubmitJobControllerTest {
    
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
            mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    
    @Test
    public void submitJobControllerGetReturnsOkStatusAndSubmitPage() throws Exception {      
        mockMvc.perform(MockMvcRequestBuilders.get("/submit"))
                .andExpect(status().isOk())
                .andExpect(view().name("submit"));
    }
    
    @Ignore
    @Test
    public void submitJobControllerPostRequestBuildsSettings() throws Exception {
        MultipartFile vcfFile = new MockMultipartFile("testVcf", Files.newInputStream(Paths.get("src/test/resources/Pfeiffer.vcf")));
        
        mockMvc.perform(MockMvcRequestBuilders.post("/submit")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("vcf", vcfFile.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("submit"));
    }
}
