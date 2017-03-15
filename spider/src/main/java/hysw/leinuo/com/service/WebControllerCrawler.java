package hysw.leinuo.com.service;

import cn.edu.hfut.dmic.webcollector.crawldb.DBManager;
import cn.edu.hfut.dmic.webcollector.crawler.Crawler;
import cn.edu.hfut.dmic.webcollector.fetcher.Executor;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.plugin.ram.RamDB;
import cn.edu.hfut.dmic.webcollector.plugin.ram.RamDBManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.*;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.*;
import java.util.*;

/**
 * Created by leinuo on 16-12-21.
 */
public class WebControllerCrawler {

    public Map<String, Object> DataMap = new HashMap<String, Object>();

    public Map<String, Object> getDataMap() {
        return DataMap;
    }

    static {
        //禁用Selenium的日志
        Logger logger = Logger.getLogger("com.gargoylesoftware.htmlunit");
        logger.setLevel(Level.OFF);
    }

    private HSSFWorkbook writeToExcelXLS( List<JsonObject> jsonObjects) {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("blog");
        // 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet
        HSSFRow row = sheet.createRow((int) 0);
        // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
        // 第四步，创建单元格，并设置值表头 设置表头居中
        HSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式
        HSSFCell cell = row.createCell(0);
        cell.setCellValue("标题");
        cell.setCellStyle(style);
        cell = row.createCell(1);
        cell.setCellValue("链接");
        cell.setCellStyle(style);
        cell = row.createCell(2);
        cell.setCellValue("内容");
        cell.setCellStyle(style);
        JsonObject jsonObject;
        for (int j = 0; j < jsonObjects.size(); j++) {
           jsonObject = jsonObjects.get(j);
            row = sheet.createRow(j + 1);
            row.createCell(0).setCellValue(jsonObject.get("title").toString());
            row.createCell(1).setCellValue(jsonObject.get("href").toString());
            row.createCell(2).setCellValue(jsonObject.get("content").toString());
        }
        return wb;
    }

    public void writeToFile( List<JsonObject> jsonObjects) throws IOException {
        HSSFWorkbook wb = writeToExcelXLS(jsonObjects);
        FileOutputStream fileOutputStream = null;
        String fileName = "blog.xls";
        try {
            fileOutputStream = new FileOutputStream(fileName);
            wb.write(fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fileOutputStream.close();
        }

    }

    public void getData() throws Exception {
        Executor executor = new Executor() {
            public void execute(CrawlDatum datum, CrawlDatums next) throws Exception {
                HtmlUnitDriver driver = new HtmlUnitDriver();
                driver.setJavascriptEnabled(true);
                driver.get(datum.getUrl());
                List<WebElement> titleElements = driver.findElementsByCssSelector("a.titlelnk");
                List<WebElement> contentElements = driver.findElementsByCssSelector("p.post_item_summary");
                System.out.print("文章数为："+titleElements.size());
                JsonObject jsonObject;
                List<JsonObject> jsonObjects = new ArrayList<JsonObject>();
                for(int i=0;i<titleElements.size();i++){
                    jsonObject = new JsonObject();
                    jsonObject.addProperty("title",titleElements.get(i).getText());
                    jsonObject.addProperty("href",titleElements.get(i).getAttribute("href"));
                    jsonObject.addProperty("content",contentElements.get(i).getText());
                    jsonObjects.add(jsonObject);
                }
                DataMap.put("data", jsonObjects);
            }
        };
        // DBManager manager=new BerkeleyDBManager("crawl");
        RamDB ramDB = new RamDB();
        DBManager manager1 = new RamDBManager(ramDB);
        //创建一个Crawler需要有DBManager和Executor
        Crawler crawler = new Crawler(manager1, executor);
        crawler.addSeed("http://www.cnblogs.com/");
        crawler.start(1);
    }

    public static void main(String[] args) throws Exception {
        WebControllerCrawler webControllerCrawler = new WebControllerCrawler();
        webControllerCrawler.getData();
        System.out.print(webControllerCrawler.getDataMap().get("data"));
        List<JsonObject> jsonObjects = (List<JsonObject>)(webControllerCrawler.getDataMap().get("data"));
        webControllerCrawler.writeToFile(jsonObjects);
    }

}
