/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.web.controller;

import de.charite.compbio.exomiser.web.model.Job;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import de.charite.compbio.exomiser.core.model.ExomiserSettings;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author jj8
 */
@RestController
@RequestMapping("/service")
public class RestServiceController {

    private final Logger logger = LoggerFactory.getLogger(RestServiceController.class);
        
    private static final Map<String, Job> jobMap = new HashMap<>();
    
    @RequestMapping(value="/job", method=RequestMethod.GET)
    public HttpEntity<Job> jobId(HttpSession session) {
        Job job = new Job(session.getId());
        job.add(linkTo(methodOn(RestServiceController.class).getJob(job.getJobId())).withSelfRel());
        jobMap.put(job.getJobId(), job);
        logger.info("Returning new job: {}", job);
        return new ResponseEntity<>(job, HttpStatus.OK);
    }
    
    @RequestMapping(value="/job/{id}", method=RequestMethod.GET)
    public Job getJob(@PathVariable String id) {
        return jobMap.get(id);
    }
    
    @RequestMapping(value="/job/{id}/settings", method=RequestMethod.GET)
    public ExomiserSettings getJobSettings(@PathVariable String id) {
        return jobMap.get(id).getSettings();
    }
    
    @RequestMapping(value="/job/{id}/addSettings", method=RequestMethod.POST, headers = {"Content-type= application/json"})
    public HttpEntity postJobSettings(@PathVariable String id, @RequestBody ExomiserSettings settings) {
        logger.info("Adding settings: {}", settings);
        Job job = jobMap.get(id);
        job.setSettings(settings);
        logger.info("{}", job.getSettings());
       
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @RequestMapping(value="/job/post", method=RequestMethod.POST, headers = {"Content-type= application/text"})
    public void postTest(@RequestBody String settings) {
        logger.info("Adding job: {}", settings);
        
//        logger.info("{}", job.getSettings());
       
        
    }
}
