package com.DFP.payload.request;

import javax.validation.constraints.NotBlank;

public class PipelineRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String pipeline;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPipeline() {
        return pipeline;
    }

    public void setPipeline(String pipeline) {
        this.pipeline = pipeline;
    }
}
