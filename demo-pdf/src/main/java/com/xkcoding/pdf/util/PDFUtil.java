package com.xkcoding.pdf.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.codec.Base64;
import com.itextpdf.tool.xml.Pipeline;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.html.CssAppliersImpl;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.AbstractImageProvider;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;
import com.itextpdf.tool.xml.pipeline.html.ImageProvider;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * @author minicoder
 */
public class PDFUtil {
    private static final String FONTS = "/Library/Fonts/";

    public static void generatePdf(String htmlStr, OutputStream out) throws IOException, DocumentException {

        final String CHARSET_NAME = "UTF-8";
        //final ServletContext servletContext = getServletContext();

        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        document.setMargins(30, 30, 30, 30);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("text.pdf"));
        document.open();
        document.add(new Chunk(""));

        // html内容解析
        HtmlPipelineContext htmlContext = new HtmlPipelineContext(
            new CssAppliersImpl(new XMLWorkerFontProvider() {
//                @Override
//                public Font getFont(String fontname, String encoding,
//                                    float size, final int style) {
//                    if (fontname == null) {
//                        // 操作系统需要有该字体, 没有则需要安装; 当然也可以将字体放到项目中， 再从项目中读取
//                        fontname = "Apple Braille";
//                    }
//                    return super.getFont(fontname, encoding, size,
//                        style);
//                }

                @Override
                public Font getFont(String fontname, String encoding, boolean embedded, float size, int style, BaseColor color) {
                    try {
                        //用于中文显示的Provider
                        BaseFont bfChinese = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", BaseFont.EMBEDDED);
                        return new Font(bfChinese, size, style);
                    } catch (Exception e) {
                        return super.getFont(fontname, encoding, size, style);
                    }
                }
            })) {
            @Override
            public HtmlPipelineContext clone()
                throws CloneNotSupportedException {
                HtmlPipelineContext context = super.clone();
                ImageProvider imageProvider = this.getImageProvider();
                context.setImageProvider(imageProvider);
                return context;
            }
        };

        // 图片解析
        htmlContext.setImageProvider(new AbstractImageProvider() {

            // String rootPath = servletContext.getRealPath("/");

            @Override
            public Image retrieve(String src) {
                //支持图片显示
                int pos = src.indexOf("base64,");
                try {
                    if (src.startsWith("data") && pos > 0) {
                        byte[] img = Base64.decode(src.substring(pos + 7));
                        return Image.getInstance(img);
                    } else if (src.startsWith("http")) {
                        return Image.getInstance(src);
                    }
                } catch (BadElementException ex) {
                    return null;
                } catch (IOException ex) {
                    return null;
                }
                return null;
            }

            @Override
            public String getImageRootPath() {
                return null;
            }
        });
        htmlContext.setAcceptUnknown(true).autoBookmark(true).setTagFactory(Tags.getHtmlTagProcessorFactory());

        // css解析
        CSSResolver cssResolver = XMLWorkerHelper.getInstance().getDefaultCssResolver(true);
//        cssResolver.setFileRetrieve(new FileRetrieve() {
//            @Override
//            public void processFromStream(InputStream in, ReadingProcessor processor) throws IOException {
//                try (InputStreamReader reader = new InputStreamReader(in, CHARSET_NAME)) {
//                    int i = -1;
//                    while (-1 != (i = reader.read())) {
//                        processor.process(i);
//                    }
//                } catch (Throwable e) {
//                }
//            }
//
//            // 解析href
//            @Override
//            public void processFromHref(String href, ReadingProcessor processor) throws IOException {
//                // InputStream is = servletContext.getResourceAsStream(href);
//                URL url = new URL(href);
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setRequestMethod("GET");
//                conn.setConnectTimeout(5 * 1000);
//                InputStream is = conn.getInputStream();
//
//                try (InputStreamReader reader = new InputStreamReader(is, CHARSET_NAME)) {
//                    int i = -1;
//                    while (-1 != (i = reader.read())) {
//                        processor.process(i);
//                    }
//                } catch (Throwable e) {
//                    e.printStackTrace();
//                }
//            }
//        });

        HtmlPipeline htmlPipeline = new HtmlPipeline(htmlContext, new PdfWriterPipeline(document, writer));
        Pipeline<?> pipeline = new CssResolverPipeline(cssResolver, htmlPipeline);
        XMLWorker worker = new XMLWorker(pipeline, true);
        XMLParser parser = new XMLParser(true, worker, Charset.forName(CHARSET_NAME));
        parser.parse(new FileInputStream("index.html"), Charset.forName(CHARSET_NAME));
        document.close();
        System.out.println("done");

//        //对html进行格式化为标准xhtml
//        org.jsoup.nodes.Document document1 = Jsoup.connect("https://labuladong.gitee.io/algo/di-er-zhan-a01c6/dong-tai-g-a223e/basecase-h-de925/")
//            .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36")
//            .get();
//        document1.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
//        document1.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
//
//        //html相对路径内容转绝对路径
//        Elements relativePathElements = document1.select("[src],[href]");
//        for (org.jsoup.nodes.Element element : relativePathElements) {
//            if (element.hasAttr("href")) {
//                String href = element.attr("href");
//                if (!href.matches("^.*:[\\d\\D]*") && !href.equals("#")) {
//                    element.attr("href", element.attr("abs:href"));
//                }
//            }
//            if (element.hasAttr("src")) {
//                String src = element.attr("src");
//                if (!src.matches("^.*:[\\d\\D]*")) {
//                    element.attr("src", element.attr("abs:src"));
//                }
//            }
//        }
//        String html = document1.html();
//
//        //写入index.html
//        Writer writer1 = new FileWriter("index.html");
//        writer1.write(html);
//        writer1.close();
//
//
////        try (InputStream inputStream = new ByteArrayInputStream(html.getBytes("utf-8"))) {
////            parser.parse(inputStream, Charset.forName(CHARSET_NAME));
////        }
//
//
//        com.itextpdf.kernel.pdf.PdfWriter pdfWriter = new com.itextpdf.kernel.pdf.PdfWriter(new File("yy.pdf"));
//
//        //url直接转pdf
//        PdfDocument pdf = new PdfDocument(pdfWriter);
//        com.itextpdf.kernel.geom.PageSize pageSize = new com.itextpdf.kernel.geom.PageSize(850, 1700);
//        pdf.setDefaultPageSize(pageSize);
//        ConverterProperties properties = new ConverterProperties();
//        MediaDeviceDescription mediaDeviceDescription = new MediaDeviceDescription(MediaType.SCREEN);
//        mediaDeviceDescription.setWidth(pageSize.getWidth());
//        properties.setMediaDeviceDescription(mediaDeviceDescription);
//        FontProvider fontProvider = new DefaultFontProvider();
//        fontProvider.addDirectory(FONTS);
//        properties.setFontProvider(fontProvider);
//        properties.setCharset("UTF-8");
//        properties.setCssApplierFactory(new DefaultCssApplierFactory());
//        properties.setCreateAcroForm(false);
//        properties.setBaseUri(document1.baseUri());
//
////        HtmlConverter.convertToPdf(html, pdfWriter, properties);
//        HtmlConverter.convertToPdf(new File("index.html"),new File("aa.pdf"),properties);
//        pdfWriter.close();
    }
}
