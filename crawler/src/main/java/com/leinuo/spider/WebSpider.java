package com.leinuo.spider;

import cn.edu.hfut.dmic.webcollector.crawldb.DBManager;
import cn.edu.hfut.dmic.webcollector.crawler.Crawler;
import cn.edu.hfut.dmic.webcollector.fetcher.Executor;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.plugin.ram.RamDB;
import cn.edu.hfut.dmic.webcollector.plugin.ram.RamDBManager;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by leinuo on 17-1-4.
 */
public class WebSpider {
    public Map<String, String> hydrologyDataMap = new HashMap<String, String>();

    public Map<String, String> getHydrologyDataMap() {
        return hydrologyDataMap;
    }

    static {
        //禁用Selenium的日志
        Logger logger = Logger.getLogger("com.gargoylesoftware.htmlunit");
        logger.setLevel(Level.OFF);
    }

    public void getData() throws Exception {
        Executor executor = new Executor() {
            public void execute(CrawlDatum datum, CrawlDatums next) throws Exception {
                HtmlUnitDriver driver = new HtmlUnitDriver();
                driver.setJavascriptEnabled(true);
                driver.get(datum.getUrl());
                WebElement elementZdhd = driver.findElementById("zdhd");
                WebElement elementZdsk = driver.findElementById("zdsk");
                //WebElement element=driver.findElementByCssSelector("span#outlink1");
                hydrologyDataMap.put("zdhd", elementZdhd.getText());
                hydrologyDataMap.put("zdsk", elementZdsk.getText());
               /* System.out.println("全国重点河道实时水情:" + elementZdhd.getText());
                System.out.println("全国重点水库实时水情:" + elementZdsk.getText());*/
            }
        };
        // DBManager manager=new BerkeleyDBManager("crawl");
        RamDB ramDB = new RamDB();
        DBManager manager1 = new RamDBManager(ramDB);
        //创建一个Crawler需要有DBManager和Executor
        Crawler crawler = new Crawler(manager1, executor);
        crawler.addSeed("http://xxfb.hydroinfo.gov.cn/");
        crawler.start(1);
    }

    public static void main(String[] args) throws Exception {
       WebSpider webSpider = new WebSpider();
       webSpider.getData();
        System.out.println(webSpider.getHydrologyDataMap().get("zdhd"));
    }
}
