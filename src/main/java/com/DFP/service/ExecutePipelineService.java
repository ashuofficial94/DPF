package com.DFP.service;

import com.DFP.bean.Feed;
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
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import java.sql.Timestamp;
import java.util.ArrayList;

@Service
public class ExecutePipelineService {

    @Autowired
    private DataBase db;
    @Autowired
    private ConditionalProcessing conditionalProcessing;
    private String pipelineName;

    private ArrayList<Thread> branchList = new ArrayList<Thread>();
    private ArrayList<Thread> parallelStagesList = new ArrayList<Thread>();

    public Message parseXML(String xml, Integer validate) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = null;
        try {
            //Create DocumentBuilder with default configuration
            builder = factory.newDocumentBuilder();

            //Parse the content to Document object
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            doc.getDocumentElement().normalize();

            Element pipelines = doc.getDocumentElement();


            Message message = null;
            Node pipeline = pipelines.getFirstChild();
            while (pipeline.getNextSibling() != null) {
                pipeline = pipeline.getNextSibling();
                if (pipeline.getNodeType() == Node.ELEMENT_NODE) {
                    Element pipelineElement = (Element) pipeline;
                    message = parsePipeline(pipelineElement, validate);

                }
            }
            if (message != null) {
                return message;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new Message("error", e.getMessage());
        }
        return new Message("success", "Pipeline Executed Successfully");
    }

    public Message parsePipeline(Element pipelineElement, Integer validate) {
        Node childNode = pipelineElement.getFirstChild();
        this.pipelineName = pipelineElement.getAttribute("pipelineName");
        Feed feed = null;
        while (childNode.getNextSibling() != null) {
            childNode = childNode.getNextSibling();
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) childNode;
                System.out.println("feed " + childElement.getTagName());
                if (childElement.getTagName().equals("Feed")) {
                    feed = parseFeedElement(childElement);
                } else {
                    return executePipeline(feed, childElement, validate);
                }
            }
        }

        return null;
    }

    public Message executePipeline(Feed feed, Element stages, Integer validate) {
        Node stageNode = stages.getFirstChild();
        while (stageNode.getNextSibling() != null) {
            stageNode = stageNode.getNextSibling();
            if (stageNode.getNodeType() == Node.ELEMENT_NODE) {
                Element stageElement = (Element) stageNode;
                System.out.println("Stage Name " + stageElement.getTagName() + " " + stageElement.getAttribute("number"));

                String stageNumber = stageElement.getAttribute("number");
                String stageName = stageElement.getElementsByTagName("stageName").item(0).getTextContent();
                String stageDesc = stageElement.getElementsByTagName("stageDesciption").item(0).getTextContent();

                if (stageElement.getElementsByTagName("parallelStages").item(0) != null) {
                    Message msg = parseParrallelStages(feed, stageElement);
                    if (msg != null) return msg;
                    Thread t = Thread.currentThread();

                } else {


                    String output = stageElement.getElementsByTagName("output").item(0).getTextContent();
                    boolean conditionStatus = false;
                    boolean stageSkip = false;
                    if (stageElement.getElementsByTagName("conditionProcessing").item(0) != null) {
                        while (true) {

                            String conditionalQuery = stageElement.getElementsByTagName("conditionProcessing").item(0).getTextContent();
                            ArrayList<ArrayList<String>> conditionQueryResult = db.executeSelectQuery(feed.getDburl(), feed.getDbName(), feed.getDbUserName(), feed.getDbPassword(), conditionalQuery);
                            String conditionalAction = stageElement.getElementsByTagName("value").item(0).getTextContent();
                            if (conditionQueryResult.size() > 0) {
                                conditionStatus = true;
                                break;
                            } else {
                                if (conditionalAction.equals("wait")) {
                                    String time = stageElement.getElementsByTagName("time").item(0).getTextContent();
                                    Thread t = Thread.currentThread();
                                    try {
                                        System.out.println("going to sleep");
                                        t.sleep(Long.parseLong(time));
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                } else if (conditionalAction.equals("halt")) {
                                    return new Message("error", "Stage Number " + stageNumber + " Stage Name:" + stageName + " Conditional Query Failed");
                                } else if (conditionalAction.equals("skip")) {
                                    stageSkip = true;
                                    break;
                                }

                            }

                        }
                    }
                    if (stageSkip == true) continue;

                    if (conditionStatus == true || stageElement.getElementsByTagName("conditionProcessing").item(0) == null) {
                        if (stageElement.getElementsByTagName("sqlProcessing").item(0) != null) {
                            NodeList nList = stageElement.getElementsByTagName("sqlProcessing");
                            int stageCount = nList.getLength();
                            for (int temp = 0; temp < nList.getLength(); temp++) {
                                Node nNode = nList.item(temp);

                                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                                    Element eElement = (Element) nNode;

                                    String query = eElement.getTextContent();
                                    if (query.contains("INSERT") || query.contains("insert") || query.contains("UPDATE") || query.contains("update")) {

                                        int rowsEffected = db.executeDMLQuery(feed.getDburl(), feed.getDbName(), feed.getDbUserName(), feed.getDbPassword(), query);
                                        System.out.println("Number of rows effected " + rowsEffected);
                                    } else {
                                        ArrayList<ArrayList<String>> result = db.executeSelectQuery(feed.getDburl(), feed.getDbName(), feed.getDbUserName(), feed.getDbPassword(), query);
                                        System.out.println("STAGE OUTPUT");
                                        if (result == null) {
                                            return new Message("error", "Stage Number " + stageNumber + " Stage Name:" + stageElement.getElementsByTagName("stageName"));
                                        } else if (result.size() == 0) {
                                            return new Message("error", "Stage Number " + stageNumber + " Stage Name: " + stageName + " ->Query did not return any result");
                                        }

                                        if (output.equals("Display")) {
                                            displayResult(result);
                                        } else if (output.equals("File") && validate != 1) {
                                            System.out.println("Printing to File");
                                            writetoFile(result, stageNumber, stageName);
                                        }
                                        displayResult(result);
                                    }
                                }
                            }
                        }

                        if (stageElement.getElementsByTagName("javaProcessing").item(0) != null) {
                            String javaFileName = stageElement.getElementsByTagName("javaProcessing").item(0).getTextContent();

                            String path = "src/main/java/com/DFP/utils/" + javaFileName;

                            String javaFileSepeartor = ".";
                            String className = javaFileName.substring(0, javaFileName.lastIndexOf(javaFileSepeartor));
                            String classPath = "com/DFP/utils/" + className;
                            System.out.println(path);
                            System.out.println(classPath);

                            runProcess("javac -cp src " + path);
                            runProcess("java -cp src/main/java " + classPath);
                        }

                    }
                }

            }

        }
//        for(int i =0 ; i<branchList.size();i++){
//            Thread t = branchList.get(i);
//            while(t.isAlive()) {
//                    System.out.println("Branch running");
//            }
//        }
        return null;
    }

    public void runProcess(String command) {
        Process pro = null;
        try {
            pro = Runtime.getRuntime().exec(command);
            printLines(command + " stdout:", pro.getInputStream());
            printLines(command + " stderr:", pro.getErrorStream());
            pro.waitFor();
            System.out.println(command + " exitValue() " + pro.exitValue());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void printLines(String cmd, InputStream ins) throws Exception {
        String line = null;
        BufferedReader in = new BufferedReader(
                new InputStreamReader(ins));
        while ((line = in.readLine()) != null) {
            System.out.println(cmd + " " + line);
        }
    }

    public Message parseParrallelStages(Feed feed, Element stage) {
        Node childNode = stage.getFirstChild();
        Element parallelStage = null;
        while (childNode.getNextSibling() != null) {
            childNode = childNode.getNextSibling();
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) childNode;
                if (childElement.getTagName().equals("parallelStages")) {
                    parallelStage = childElement;
                    break;
                }
            }
        }
        Node stages = parallelStage.getFirstChild();
        while (stages.getNextSibling() != null) {
            stages = stages.getNextSibling();
            if (stages.getNodeType() == Node.ELEMENT_NODE) {
                Element stageElement = (Element) stages;
                ParallelStages stageThread = new ParallelStages(feed, stageElement, this.pipelineName);
                parallelStagesList.add(stageThread);
                stageThread.start();

//                try{
//                    stageThread.sleep(2000);
//                }
//                catch(Exception e){
//                    System.out.println("in here");
//                    System.out.println(e.getMessage());
//                }
            }
        }
        for (int i = 0; i < parallelStagesList.size(); i++) {
            Thread t = parallelStagesList.get(i);
            while (t.isAlive()) {
//                    System.out.println("Branch running");
            }
        }
        return null;

    }

    //    public Message parseBranch(Feed feed,Element stage){
//            Node childNode = stage.getFirstChild();
//            Element branch = null;
//            while(childNode.getNextSibling()!= null){
//                childNode = childNode.getNextSibling();
//                if(childNode.getNodeType() == Node.ELEMENT_NODE){
//                    Element childElement = (Element) childNode;
//                    if(childElement.getTagName().equals("branch")){
//                        branch = childElement;
//                        break;
//                    }
//                }
//            }
//            StageBranch branchThread = new StageBranch(feed,branch,this.pipelineName);
//            branchList.add(branchThread);
//            branchThread.start();
//
//        try {
//            branchThread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            return new Message("error",e.getMessage());
//        }
//
//        return null;
//    }
    public Feed parseFeedElement(Element feedElement) {
        return new Feed(feedElement.getElementsByTagName("DBURL").item(0).getTextContent(),
                feedElement.getElementsByTagName("DBName").item(0).getTextContent(),
                feedElement.getElementsByTagName("DBUserName").item(0).getTextContent(),
                feedElement.getElementsByTagName("DBPassword").item(0).getTextContent());
    }

    public void getConditionalResult(String value, String conditionType) {
        conditionalProcessing.handleConditionalRequest(value, conditionType);
    }

    public void displayResult(ArrayList<ArrayList<String>> rs) {
        System.out.println(rs);
    }

    public void writetoFile(ArrayList<ArrayList<String>> rs, String stageNumber, String stageName) {
        Path path = Paths.get("", "output.txt");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String text = "\nPipeline Name " + this.pipelineName + " \nStage Number " + stageNumber + "\nStageName :" + stageName + "\nOutput :" + rs + "\nTimestamp :" + timestamp;
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
            writer.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


//            //Find the pipeline Name
//            NodeList pipelineList = doc.getElementsByTagName("Pipeline");
//            Element pipelineElement = (Element) pipelineList.item(0);
//            String pipelineName = pipelineElement.getAttribute("pipelineName");
//            System.out.println("Pipeline Name : "+pipelineName);
//
//
//            NodeList feedList = doc.getElementsByTagName("Feed");
//            Element feedData = (Element) feedList.item(0);
//
//            // Reading database information
//            String dburl = feedData.getElementsByTagName("DBURL").item(0).getTextContent();
//            String dbName = feedData.getElementsByTagName("DBName").item(0).getTextContent();
//            String dbUserName = feedData.getElementsByTagName("DBUserName").item(0).getTextContent();
//            String dbPassword = feedData.getElementsByTagName("DBPassword").item(0).getTextContent();
//
//
//            NodeList stageList = doc.getElementsByTagName("stage");
//            Element stageData = (Element) stageList.item(0);
//
//            NodeList nList = doc.getElementsByTagName("stage");
//            int stageCount = nList.getLength();
//            System.out.println("Number of stages :"+stageCount);
//            for (int temp = 0; temp < nList.getLength(); temp++) {
//                Node nNode = nList.item(temp);
//                System.out.println("\nCurrent Element :" + nNode.getNodeName());
//                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
//                    Element eElement = (Element) nNode;
//                    String stageName = eElement.getElementsByTagName("stageName").item(0).getTextContent();
//                    System.out.println(stageName);
////                    String stageNumber = eElement.getAttribute("number");
////                    String stageName = eElement.getElementsByTagName("stageName").item(0).getTextContent();
////                    String stageDesc = eElement.getElementsByTagName("stageDesciption").item(0).getTextContent();
////                    String query = eElement.getElementsByTagName("sqlProcessing").item(0).getTextContent();
////                    String output = eElement.getElementsByTagName("output").item(0).getTextContent();
////
////                    System.out.println("Stage number : " + stageNumber);
////                    System.out.println("Stage Name  : "+ stageName);
////                    System.out.println("Stage desc  : "+stageDesc);
////                    System.out.println("Stage Query  : "+ query);
////
////
////                    ArrayList <ArrayList<String>> result = db.executeQuery(dburl, dbName,dbUserName,dbPassword,query);
////                    System.out.println("STAGE OUTPUT");
////                    if(result == null){
////                        return new Message("error", "Stage Number "+stageNumber+" Stage Name:"+eElement.getElementsByTagName("stageName"));
////                    }else if (result.size() ==0 ){
////                        return new Message("error", "Stage Number "+stageNumber+" Stage Name: "+stageName+" ->Query did not return any result");
////                    }
////                    if(eElement.getElementsByTagName("conditionProcessing").item(0) != null){
////                        String conditionType = eElement.getElementsByTagName("conditionProcessing").item(0).getTextContent();
////                        getConditionalResult(result.get(0).get(0),conditionType);
////                    }
////
////                    if(output.equals("Display")){
////                        displayResult(result);
////                    } else if (output.equals("File")){
////                        System.out.println("Printing to File");
//////                        writetoFile(result,pipelineName,stageNumber,stageName);
////                    }
////                    displayResult(result);
//                }
//            }