package com.DFP.controller;

import com.DFP.bean.Pipeline;
import com.DFP.payload.request.PipelineRequest;
import com.DFP.payload.request.PiplineID;
import com.DFP.service.ExecutePipelineService;
import com.DFP.service.PipelineService;
import com.DFP.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.nio.channels.Pipe;
import java.util.List;


@RestController
@CrossOrigin
public class PipelineController {

    @Autowired
    private PipelineService pipelineService;

    @Autowired
    private ExecutePipelineService executePipelineService;

//    @GetMapping("/")
//    @RequestMapping("/")
//    public String home(){
//        return "index.html";
//    }

    @PostMapping("/getXML")
    public Message getXML(@RequestBody String xml){
        Message message = executePipelineService.parseXML(xml);
//        System.out.println(xml);
        return message;

    }
    @PostMapping("/savePipeline")
    public Message addPipeline(@RequestBody PipelineRequest pipelineRequest){
                Pipeline pipeLine = new Pipeline(pipelineRequest.getName(),pipelineRequest.getPipeline());
                try{
                    Pipeline result = pipelineService.addCourse(pipeLine);
                    if(result == null){
                        return new Message("error","Unable to add pipeline");
                    } else {
                        return new Message("success","Pipeline created successfully");
                    }
                } catch (Exception e){
                    return new Message("error",e.getMessage());
                }


    }
    @GetMapping("/getpipelines")
    public List<Pipeline> getCourses(){
        return pipelineService.getAllPipelines();
    }
    @PostMapping("/executePipeline")
    public Message executeSpecificPipeline(@RequestBody PiplineID piplineID){
            try{
                Pipeline pipeline = pipelineService.getPipeline(piplineID.getId());
                Message message = executePipelineService.parseXML(pipeline.getPipelinexml());

                return message;
            }catch(Exception e){
                    return new Message("error",e.getMessage());
            }
    }
}
