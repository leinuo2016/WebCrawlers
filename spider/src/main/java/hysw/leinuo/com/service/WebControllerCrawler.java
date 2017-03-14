package hysw.leinuo.com.service;

import cn.edu.hfut.dmic.webcollector.crawldb.DBManager;
import cn.edu.hfut.dmic.webcollector.crawler.Crawler;
import cn.edu.hfut.dmic.webcollector.fetcher.Executor;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.plugin.ram.RamDB;
import cn.edu.hfut.dmic.webcollector.plugin.ram.RamDBManager;
import com.sun.jna.platform.win32.WinDef;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.*;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.*;
import java.util.*;

/**
 * Created by leinuo on 16-12-21.
 */
public class WebControllerCrawler {

    public Map<String, String> hydrologyDataMap = new HashMap<String, String>();

    public Map<String, String> getHydrologyDataMap() {
        return hydrologyDataMap;
    }

    static {
        //禁用Selenium的日志
        Logger logger = Logger.getLogger("com.gargoylesoftware.htmlunit");
        logger.setLevel(Level.OFF);
    }

    private HSSFWorkbook writeToExcelXLS(List<String> lists, String type) {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet;
        if (type.equals("hd")) {
            sheet = wb.createSheet("全国重点河道实时水情");
        } else {
            sheet = wb.createSheet("全国重点水库实时水情");
        }
        // 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet
        HSSFRow row = sheet.createRow((int) 0);
        // 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
        // 第四步，创建单元格，并设置值表头 设置表头居中
        HSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式
        HSSFCell cell = row.createCell(0);
        cell.setCellValue("站名");
        cell.setCellStyle(style);
        cell = row.createCell(1);
        cell.setCellValue("站址");
        cell.setCellStyle(style);
        cell = row.createCell(2);
        cell.setCellValue("河名");
        cell.setCellStyle(style);
        if (type.equals("hd")) {
            cell = row.createCell(3);
            cell.setCellValue("水位(米)");
            cell.setCellStyle(style);
            cell = row.createCell(4);
            cell.setCellValue("流量(米/秒)");
            cell.setCellStyle(style);
            cell = row.createCell(5);
            cell.setCellValue("警戒水位");
            cell.setCellStyle(style);
        } else {
            cell = row.createCell(3);
            cell.setCellValue("库水位(米)");
            cell.setCellStyle(style);
            cell = row.createCell(4);
            cell.setCellValue("蓄水量(亿米)");
            cell.setCellStyle(style);
            cell = row.createCell(5);
            cell.setCellValue("汛限水位");
            cell.setCellStyle(style);
        }
        cell = row.createCell(6);
        cell.setCellValue("时间");
        cell.setCellStyle(style);
        List<String> lists1;
        for (int j = 0; j < lists.size(); j++) {
            lists1 = Arrays.asList(lists.get(j).split(" "));
            row = sheet.createRow((int) j + 1);
            row.createCell(0).setCellValue(lists1.get(0));
            row.createCell(1).setCellValue(lists1.get(1));
            row.createCell(2).setCellValue(lists1.get(2));
            row.createCell(3).setCellValue(lists1.get(3));
            row.createCell(4).setCellValue(lists1.get(4));
            row.createCell(5).setCellValue(lists1.get(7));
            row.createCell(6).setCellValue(lists1.get(5) + " " + lists1.get(6));
        }
        return wb;
    }

    public void writeToFile(List<String> lists, String type) throws IOException {
        HSSFWorkbook wb = writeToExcelXLS(lists, type);
        FileOutputStream fileOutputStream = null;
        String fileName;
        if (type.equals("hd")) {
            fileName = "全国重点河道实时水情.xls";
        } else {
            fileName = "全国重点水库实时水情.xls";
        }
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

    public void getData1() throws Exception {
        Executor executor = new Executor() {
            public void execute(CrawlDatum datum, CrawlDatums next) throws Exception {
                HtmlUnitDriver driver = new HtmlUnitDriver();
                driver.setJavascriptEnabled(true);
                driver.get(datum.getUrl());
                WebElement elementJhhd = driver.findElementById("jhimg");
                elementJhhd.click();
                WebElement elementHdtable = driver.findElementById("hdtable");
                hydrologyDataMap.put("jh", elementHdtable.getText());
            }
        };
        //创建一个基于伯克利DB的DBManager
        RamDB ramDB = new RamDB();
        DBManager manager1 = new RamDBManager(ramDB);
        //创建一个Crawler需要有DBManager和Executor
        Crawler crawler = new Crawler(manager1, executor);
        crawler.addSeed("http://xxfb.hydroinfo.gov.cn/ssIndex.html");
        crawler.start(1);
    }

    public static void main(String[] args) throws Exception {
        WebControllerCrawler webControllerCrawler = new WebControllerCrawler();
        webControllerCrawler.getData();
        webControllerCrawler.getData1();
        System.out.println("全国重点河道实时水情:" + webControllerCrawler.getHydrologyDataMap().get("zdhd"));
        System.out.println("全国重点水库实时水情:" + webControllerCrawler.getHydrologyDataMap().get("zdsk"));
        List<String> list1 = Arrays.asList(webControllerCrawler.getHydrologyDataMap().get("zdhd").split("    "));
        List<String> list2 = Arrays.asList(webControllerCrawler.getHydrologyDataMap().get("zdsk").split("    "));
        webControllerCrawler.writeToFile(list1,"hd");
        webControllerCrawler.writeToFile(list2,"sk");
    }

}
