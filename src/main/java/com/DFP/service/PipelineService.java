package com.DFP.service;

import com.DFP.Repository.PipelineRepository;
import com.DFP.bean.Pipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PipelineService {

    @Autowired
    private PipelineRepository pipelineRepository;



    public Pipeline addCourse(Pipeline pipeline) {
//        list.add(course);
        pipelineRepository.save(pipeline);
        return pipeline;
    }

    public List<Pipeline> getAllPipelines(){
        return pipelineRepository.findAll();
    }

    public Pipeline getPipeline(long pipelineId) {
        return pipelineRepository.getOne(pipelineId);
    }
}
