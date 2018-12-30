package ru.javaops.masterjava.xml.util;

import com.google.common.io.Resources;
import org.junit.Test;

import java.io.InputStream;

public class XsltProcessorTest {
    @Test
    public void transform() throws Exception {
        try (InputStream xslInputStream = Resources.getResource("src/main/resources/cities.xsl").openStream();
             InputStream xmlInputStream = Resources.getResource("src/test/resources/payload.xml").openStream()) {

            XsltProcessor processor = new XsltProcessor(xslInputStream);
            System.out.println(processor.transform(xmlInputStream));
        }
    }
}
