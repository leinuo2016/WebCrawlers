package hysw.leinuo.com.service;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.util.concurrent.TimeUnit;

/**
 * Created by leinuo on 16-12-20.
 */
public class HtmlUnitCrawler {
    private static HtmlPage getHtmlPage(String url) throws Exception{
        final WebClient webClient=new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setMaxInMemory(500);
        final HtmlPage page = webClient.getPage(url);
        System.err.println("查询中，请稍候");
        TimeUnit.SECONDS.sleep(5);  //web请求数据需要时间，必须让主线程休眠片刻
        webClient.close();
        return page;
    }

    private static void printTable(HtmlTableBody tbody) throws Exception{
        DomNodeList<HtmlElement> trs = tbody.getElementsByTagName("tr");
        for(int i=0;i<trs.size();i++){
            HtmlElement node = trs.get(i);
            DomNodeList<HtmlElement> tds = node.getElementsByTagName("td");
            for(int j=0;j<tds.size();j++){
                HtmlElement td = tds.get(j);
                System.err.print(td.asText()+"\t");
            }
            System.err.println();
        }
    }
    public static void main (String[] args ) throws Exception
    {
       String blog = "http://www.cnblogs.com/";
       HtmlPage page = getHtmlPage(blog);
        final HtmlDivision div = (HtmlDivision) page.getElementById("post_list");
   //     final HtmlDivision div2 = (HtmlDivision) page.getElementById("hdtable");
      //  final HtmlDivision div2 = (HtmlDivision) page1.getElementById("hdtable");
        System.out.println(div.asText().toString());
        System.out.println("-------------");
      //  System.out.println(div2.asText().toString());
       /* HtmlTableBody tbody = (HtmlTableBody) div.getChildNodes().get(0);
        printTable(tbody);*/
        System.err.println("查询数据成功");
    }


}
