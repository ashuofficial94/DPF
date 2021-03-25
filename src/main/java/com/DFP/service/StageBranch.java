package com.DFP.service;

import com.DFP.bean.Feed;
import org.w3c.dom.Element;

public class StageBranch extends Thread{
    private Element branch;
    private Feed feed;
    public StageBranch(Feed feed, Element element){
        this.branch = element;
        this.feed = feed;
    }
    public void run() {

        try {
            sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Run: "+ getName() + " "+ branch.getTagName());
    }
}
