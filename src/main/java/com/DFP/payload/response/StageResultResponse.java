package com.DFP.payload.response;



public class StageResultResponse {

    private String stageNumber;
    private String stageName;
    private String status;

    public StageResultResponse(String stageNumber, String stageName, String status) {
        this.stageNumber = stageNumber;
        this.stageName = stageName;
        this.status = status;
    }

    public String getStageNumber() {
        return stageNumber;
    }

    public void setStageNumber(String stageNumber) {
        this.stageNumber = stageNumber;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
