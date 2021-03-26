package com.DFP.service;

import com.DFP.bean.Feed;
import com.DFP.dao.DataBase;
import com.DFP.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
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


public class StageBranch extends Thread{


    private DataBase db = new DataBase();
    @Autowired
    private ConditionalProcessing conditionalProcessing;

    private ArrayList<Thread> branchList = new ArrayList<Thread>();

    private Element branch;
    private Feed feed;
    private String pipelineName;


    public StageBranch(Feed feed, Element element,String pipelineName){
        this.branch = element;
        this.feed = feed;
        this.pipelineName = pipelineName;
    }
    public void run() {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Run: "+ getName() + " "+ branch.getTagName());
        executePipeline();
    }
    public Message executePipeline(){
        Node stages = branch.getFirstChild();
        stages.getNextSibling();
        stages = stages.getNextSibling();
        if(stages.getNodeType() == Node.ELEMENT_NODE){
            Node stage = stages.getFirstChild();
            while(stage.getNextSibling() != null){
                stage = stage.getNextSibling();
                if(stage.getNodeType() == Node.ELEMENT_NODE){
                    Element stageElement = (Element) stage;

                    String stageNumber = stageElement.getAttribute("number");
                    String stageName = stageElement.getElementsByTagName("stageName").item(0).getTextContent();
                    String stageDesc = stageElement.getElementsByTagName("stageDesciption").item(0).getTextContent();
                    System.out.println(stageName);
                    if(stageElement.getElementsByTagName("branch").item(0) != null){
//                        Message msg =  parseBranch(this.feed,stageElement);
//                        if(msg != null) return msg;
//                        Thread t = Thread.currentThread();

                    } else {

                        String query = stageElement.getElementsByTagName("sqlProcessing").item(0).getTextContent();
                        String output = stageElement.getElementsByTagName("output").item(0).getTextContent();
                        try{
                            ArrayList <ArrayList<String>> result = db.executeQuery(feed.getDburl(), feed.getDbName(),feed.getDbUserName(),feed.getDbPassword(),query);
                            System.out.println("STAGE OUTPUT");
                            if(result == null){
                                return new Message("error", "Stage Number "+stageNumber+" Stage Name:"+stageElement.getElementsByTagName("stageName"));
                            }else if (result.size() ==0 ){
                                return new Message("error", "Stage Number "+stageNumber+" Stage Name: "+stageName+" ->Query did not return any result");
                            }
                            if(stageElement.getElementsByTagName("conditionProcessing").item(0) != null){
                                String conditionType = stageElement.getElementsByTagName("conditionProcessing").item(0).getTextContent();
                                getConditionalResult(result.get(0).get(0),conditionType);
                            }

                            if(output.equals("Display")){
                                displayResult(result);
                            } else if (output.equals("File")){
                                System.out.println("Printing to File");
                                writetoFile(result,stageNumber,stageName);
                            }
                            displayResult(result);
                        }catch (NullPointerException e){
                            e.printStackTrace();
                        }

                    }

                }
            }

        }
        for(int i =0 ; i<branchList.size();i++){
            Thread t = branchList.get(i);
            while(t.isAlive()) {
//                    System.out.println("Branch running");
            }
        }
        return null;
    }
    public Message parseBranch(Feed feed,Element stage){
        Node childNode = stage.getFirstChild();
        Element branch = null;
        while(childNode.getNextSibling()!= null){
            childNode = childNode.getNextSibling();
            if(childNode.getNodeType() == Node.ELEMENT_NODE){
                Element childElement = (Element) childNode;
                if(childElement.getTagName().equals("branch")){
                    branch = childElement;
                    break;
                }
            }
        }
        StageBranch branchThread = new StageBranch(feed,branch,this.pipelineName);
        branchList.add(branchThread);
        branchThread.start();

//        try {
//            branchThread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            return new Message("error",e.getMessage());
//        }

        return null;
    }
    public void getConditionalResult(String value,String conditionType){
        conditionalProcessing.handleConditionalRequest(value,conditionType);
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
