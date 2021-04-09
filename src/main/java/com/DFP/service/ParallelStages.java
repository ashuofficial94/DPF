package com.DFP.service;

import com.DFP.bean.Feed;
import com.DFP.dao.DataBase;
import com.DFP.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.ArrayList;

public class ParallelStages extends Thread{

    private DataBase db = new DataBase();
    @Autowired
    private ConditionalProcessing conditionalProcessing;

    private Element stage;
    private Feed feed;
    private String pipelineName;

    public ParallelStages(Feed feed, Element element,String pipelineName){
        this.stage = element;
        this.feed = feed;
        this.pipelineName = pipelineName;
    }
    public void run() {
//        throw new RuntimeException();
        executeStage();
    }
    public Message executeStage(){

                System.out.println("Stage Name " + this.stage.getTagName() + " " + this.stage.getAttribute("number"));
                String stageNumber = this.stage.getAttribute("number");
                String stageName = this.stage.getElementsByTagName("stageName").item(0).getTextContent();
                String stageDesc = this.stage.getElementsByTagName("stageDesciption").item(0).getTextContent();
                String query = this.stage.getElementsByTagName("sqlProcessing").item(0).getTextContent();
                String output = this.stage.getElementsByTagName("output").item(0).getTextContent();

                ArrayList <ArrayList<String>> result = db.executeSelectQuery(feed.getDburl(), feed.getDbName(),feed.getDbUserName(),feed.getDbPassword(),query);
                System.out.println("STAGE OUTPUT");
                if(result == null){
                    return new Message("error", "Stage Number "+stageNumber+" Stage Name:"+this.stage.getElementsByTagName("stageName"));
                }else if (result.size() ==0 ){
                    return new Message("error", "Stage Number "+stageNumber+" Stage Name: "+stageName+" ->Query did not return any result");
                }
                if(output.equals("Display")){
                    displayResult(result);
                } else if (output.equals("File")){
                    System.out.println("Printing to File");
        //                          writetoFile(result,stageNumber,stageName);
                }
                displayResult(result);
                return null;
    }
    public void displayResult(ArrayList <ArrayList<String>> rs)  {
        System.out.println(rs);
    }
    public void writetoFile(ArrayList <ArrayList<String>> rs,String stageNumber,String stageName){
        Path path = Paths.get("", "output.txt");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String text = "\nPipeline Name "+this.pipelineName +" \nStage Number "+stageNumber +"\nStageName :"+stageName+ "\nOutput :"+rs+"\nTimestamp :"+timestamp;
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND,StandardOpenOption.CREATE)) {
            writer.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
