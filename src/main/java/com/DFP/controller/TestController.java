package com.DFP.controller;

import com.DFP.dao.DataBase;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

@RestController
@CrossOrigin
public class TestController {
    DataBase db = new DataBase();
    @GetMapping("/")
    public String home(){
        return "This is home page";
    }

    @PostMapping("/getXML")
    public String getXML(@RequestBody String xml){


        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = null;
        try
        {
            //Create DocumentBuilder with default configuration
            builder = factory.newDocumentBuilder();

            //Parse the content to Document object

            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            doc.getDocumentElement().normalize();
            NodeList feedList = doc.getElementsByTagName("Feed");
            Element feedData = (Element) feedList.item(0);
            System.out.println(feedData.getElementsByTagName("DBURL").item(0).getTextContent());

            NodeList stageList = doc.getElementsByTagName("stage");
            Element stageData = (Element) stageList.item(0);

            db.getUser(feedData.getElementsByTagName("DBURL").item(0).getTextContent(),
                    feedData.getElementsByTagName("DBName").item(0).getTextContent(),
                    feedData.getElementsByTagName("DBUserName").item(0).getTextContent(),
                    feedData.getElementsByTagName("DBPassword").item(0).getTextContent(),
                    stageData.getElementsByTagName("input").item(0).getTextContent());


            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
            NodeList nList = doc.getElementsByTagName("stage");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                System.out.println("\nCurrent Element :" + nNode.getNodeName());
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    System.out.println("Stage number : " + eElement.getAttribute("number"));

                    System.out.println("Stage Name  : "+ eElement.getElementsByTagName("stageName")
                            .item(0)
                            .getTextContent());
                    System.out.println("Stage desc  : "+ eElement.getElementsByTagName("stageDesciption")
                            .item(0)
                            .getTextContent());
                }


            }


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return xml;
    }
}
