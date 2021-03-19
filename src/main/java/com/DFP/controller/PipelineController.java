package com.DFP.controller;

import com.DFP.service.PipelineService;
import com.DFP.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin
public class PipelineController {

    @Autowired
    private PipelineService pipelineService;

//    @GetMapping("/")
//    @RequestMapping("/")
//    public String home(){
//        return "index.html";
//    }

    @PostMapping("/getXML")
    public Message getXML(@RequestBody String xml){
        Message message = pipelineService.parseXML(xml);
//        System.out.println(xml);
        return message;

    }
}
