package com.xkcoding.pdf;

import com.itextpdf.text.DocumentException;
import com.xkcoding.pdf.util.PDFUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootDemoPdfApplicationTests {


    /**
     * 测试生成pdf
     */
    @Test
    public void testGenerate() {
        try {
            OutputStream outputStream = new FileOutputStream("out.pdf");
            PDFUtil.generatePdf("", outputStream);
        } catch (IOException | DocumentException e) {
            throw new RuntimeException(e);
        }
    }

}

