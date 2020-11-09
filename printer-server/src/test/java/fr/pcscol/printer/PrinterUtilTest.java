package fr.pcscol.printer;

import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class PrinterUtilTest {

    static {
        TomcatURLStreamHandlerFactory.getInstance();
    }

    @Test
    public void completeUrlTest() {

        URL result;

        //Test with invalid URI
        try {
            PrinterUtil.completeUrl("^InvalidURI!", "classpath:/");
            Assert.fail();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        //Test with classpath schema
        try {
            result = PrinterUtil.completeUrl("test.txt", "classpath:/");
            Assert.assertEquals(new URL("classpath:/test.txt"), result);
        } catch (MalformedURLException e) {
            Assert.fail();
        }

        try {
            result = PrinterUtil.completeUrl("/test.txt", "classpath:/");
            Assert.assertEquals(new URL("classpath:/test.txt"), result);
        } catch (MalformedURLException e) {
            Assert.fail();
        }

        //Test with file schema
        try {
            result = PrinterUtil.completeUrl("test.txt", "file://path/to/baseDir");
            Assert.assertEquals(new URL("file://path/to/baseDir/test.txt"), result);
        } catch (MalformedURLException e) {
            Assert.fail();
        }

        //Test with http schema
        try {
            result = PrinterUtil.completeUrl("test.txt", "http://server.com/base-Dir");
            Assert.assertEquals(new URL("http://server.com/base-Dir/test.txt"), result);
        } catch (MalformedURLException e) {
            Assert.fail();
        }

        //Test with http schema and absolute uri
        try {
            result = PrinterUtil.completeUrl("http://server.com/absolute/path/test.txt", "http://server.com/base-Dir");
            Assert.assertEquals(new URL("http://server.com/absolute/path/test.txt"), result);
        } catch (MalformedURLException e) {
            Assert.fail();
        }

    }

    @Test
    public void getMimeTypeTest() {

        Assert.assertEquals("application/vnd.oasis.opendocument.text", PrinterUtil.getMimeType("test.odt"));
        Assert.assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", PrinterUtil.getMimeType("test.docx"));
        Assert.assertEquals("application/msword", PrinterUtil.getMimeType("test.doc"));
        Assert.assertEquals("application/pdf", PrinterUtil.getMimeType("test.pdf"));
        Assert.assertNull(PrinterUtil.getMimeType("test.xxx"));
    }

}
