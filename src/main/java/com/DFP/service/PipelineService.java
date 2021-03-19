package com.DFP.service;

import com.DFP.dao.DataBase;
import com.DFP.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.ArrayList;

@Service
public class PipelineService {

    @Autowired
    private DataBase db;

    public Message parseXML(String xml){
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = null;
        try
        {
            //Create DocumentBuilder with default configuration
            builder = factory.newDocumentBuilder();

            //Parse the content to Document object
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            doc.getDocumentElement().normalize();
            //Find the pipeline Name
            NodeList pipelineList = doc.getElementsByTagName("Pipeline");
            Element pipelineElement = (Element) pipelineList.item(0);
            String pipelineName = pipelineElement.getAttribute("pipelineName");
            System.out.println("Pipeline Name : "+pipelineName);


            NodeList feedList = doc.getElementsByTagName("Feed");
            Element feedData = (Element) feedList.item(0);

            // Reading database information
            String dburl = feedData.getElementsByTagName("DBURL").item(0).getTextContent();
            String dbName = feedData.getElementsByTagName("DBName").item(0).getTextContent();
            String dbUserName = feedData.getElementsByTagName("DBUserName").item(0).getTextContent();
            String dbPassword = feedData.getElementsByTagName("DBPassword").item(0).getTextContent();


            NodeList stageList = doc.getElementsByTagName("stage");
            Element stageData = (Element) stageList.item(0);

            NodeList nList = doc.getElementsByTagName("stage");
            int stageCount = nList.getLength();
            System.out.println("Number of stages :"+stageCount);
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                System.out.println("\nCurrent Element :" + nNode.getNodeName());
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String stageNumber = eElement.getAttribute("number");
                    String stageName = eElement.getElementsByTagName("stageName").item(0).getTextContent();
                    String stageDesc = eElement.getElementsByTagName("stageDesciption").item(0).getTextContent();
                    String query = eElement.getElementsByTagName("processing").item(0).getTextContent();
                    String output = eElement.getElementsByTagName("output").item(0).getTextContent();

                    System.out.println("Stage number : " + stageNumber);
                    System.out.println("Stage Name  : "+ stageName);
                    System.out.println("Stage desc  : "+stageDesc);
                    System.out.println("Stage Query  : "+ query);

                    ArrayList <ArrayList<String>> result = db.executeQuery(dburl, dbName,dbUserName,dbPassword,query);
                    System.out.println("STAGE OUTPUT");
                    if(result == null){
                        return new Message("error", "Stage Number "+stageNumber+" Stage Name:"+eElement.getElementsByTagName("stageName"));
                    }else if (result.size() ==0 ){
                        return new Message("error", "Stage Number "+stageNumber+" Stage Name: "+stageName+" ->Query did not return any result");
                    }
                    if(output.equals("Display")){
                        displayResult(result);
                    } else if (output.equals("File")){
                        System.out.println("Printing to File");
                        writetoFile(result,pipelineName,stageNumber,stageName);
                    }
                    displayResult(result);
                }
            }


        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new Message("error",e.getMessage());
        }
        return new Message("success","Pipeline Executed Successfully");
    }
    public void displayResult(ArrayList <ArrayList<String>> rs)  {
        System.out.println(rs);
    }
    public void writetoFile(ArrayList <ArrayList<String>> rs,String pipeline,String stageNumber,String stageName){
        Path path = Paths.get("", "output.txt");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String text = "\nPipeline Name "+pipeline +" \nStage Number "+stageNumber +"\nStageName :"+stageName+ "\nOutput :"+rs+"\nTimestamp :"+timestamp;
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND,StandardOpenOption.CREATE)) {
            writer.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
