package com.xkcoding.pdf.util;

import com.itextpdf.text.DocumentException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.nio.file.FileSystems;

public class ITextPDF {
    public static void main(String[] args) throws IOException {
//        File file = new File("test.html");
//        Document document = Jsoup.parse(file, "UTF-8");
        Document document = Jsoup.connect("https://www.cnblogs.com/-zhuang/articles/10037451.html").get();
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        document.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
        Elements scriptElements = document.select("script");
        for (Element scriptElement : scriptElements) {
            String scriptContent = scriptElement.html();
            String escapedScriptContent = Jsoup.clean(scriptContent, Whitelist.none());
            scriptElement.html(escapedScriptContent);;
        }

        //html相对路径内容转绝对路径
        Elements relativePathElements = document.select("[src],[href]");
        for (org.jsoup.nodes.Element element : relativePathElements) {
            if (element.hasAttr("href")) {
                String href = element.attr("href");
                if (href != "" && !href.matches("^.*:[\\d\\D]*") && !href.equals("#")) {
                    element.attr("href", element.attr("abs:href"));
                }
            }
            if (element.hasAttr("src")) {
                String src = element.attr("src");
                if (src!= "" && !src.matches("^.*:[\\d\\D]*")) {
                    element.attr("src", element.attr("abs:src"));
                }
            }
        }

//        写入index.html
        Writer writer1 = new FileWriter("test.html");
        writer1.write(document.html());
        writer1.close();
        try (OutputStream outputStream = new FileOutputStream("demo-pdf/out.pdf")){
            ITextRenderer iTextRenderer = new ITextRenderer();
            ITextFontResolver fontResolver = iTextRenderer.getFontResolver();
            fontResolver.addFontDirectory("/Library/Fonts", false);
            SharedContext sharedContext = iTextRenderer.getSharedContext();
            sharedContext.setPrint(true);
            sharedContext.setInteractive(false);
            String baseUrl = FileSystems.getDefault().getPath("com.xkcoding.pdf")
                .toUri().toURL().toString();
            Document document1 = Jsoup.parse(new File("demo-pdf/index.html"), "UTF-8");
            document1.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
            document1.outputSettings().escapeMode(Entities.EscapeMode.xhtml);

            Elements scriptElements1 = document1.select("script");
            for (Element scriptElement : scriptElements1) {
                String scriptContent = scriptElement.html();
                String escapedScriptContent = Jsoup.clean(scriptContent, Whitelist.none());
                scriptElement.html(escapedScriptContent);;
            }
            iTextRenderer.setDocumentFromString(document1.html());
            iTextRenderer.layout();
            iTextRenderer.createPDF(outputStream);
            System.out.println("done");
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
}
