package com.DFP.controller;

import com.DFP.bean.Pipeline;
import com.DFP.payload.request.PiplineID;
import com.DFP.service.ExecutePipelineService;
import com.DFP.service.PipelineService;
import com.DFP.utils.Message;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    public Message addPipeline(@RequestBody JSONObject pipelineJSON){

        Pipeline pipeLine = new Pipeline(pipelineJSON.get("name").toString(),
                pipelineJSON.get("pipeline").toString());

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
    public JSONObject getCourses(){
        List<Pipeline> pipeline_list = pipelineService.getAllPipelines();
        Map<Long, String> map = new HashMap<>();

        for(Pipeline pipeline: pipeline_list) {
            map.put(pipeline.getId(), pipeline.getName());
        }

        JSONObject pipeline_json =  new JSONObject(map);
        System.out.println(pipeline_json);

        return pipeline_json;
    }

    @PostMapping("/executePipeline")
    public Message executeSpecificPipeline(@RequestBody long pipelineID){
            try{
                Pipeline pipeline = pipelineService.getPipeline(pipelineID);
                Message message = executePipelineService.parseXML(pipeline.getPipelinexml());

                return message;

            }catch(Exception e){
                    return new Message("error",e.getMessage());
            }
    }
}
