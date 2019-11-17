package fr.pcscol.printer.service;

import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import fr.opensagres.xdocreport.template.formatter.NullImageBehaviour;
import fr.pcscol.printer.service.exception.DocumentGenerationException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class PrinterContextTest {

    @Test
    public void putAndGetTest(){
        PrinterContext context = new PrinterContext();
        context.put("a.b.c.d.e", true);
        Assert.assertTrue(context.get("a") instanceof Map);
        Assert.assertTrue(context.get("a.b") instanceof Map);
        Assert.assertTrue(context.get("a.b.c") instanceof Map);
        Assert.assertTrue(context.get("a.b.c.d.e") instanceof Boolean);
        Assert.assertNull(context.get("a.b.c.d.e.x"));
        Assert.assertNull(context.get("a.b.c.d.x"));
        Assert.assertNull(context.get("x"));

        context.put("a.b.c.d.f", true);
        Assert.assertTrue(context.get("a.b.c.d.f") instanceof Boolean);

        try {
            context.put("a.b.c.d.e.f", true);
            Assert.fail();
        }catch (IllegalArgumentException e){
            //fail because we add an entry to a non map value.
        }
    }

    @Test
    public void setupImagesTestNoFail(){
        FieldsMetadata metadata = new FieldsMetadata();
        metadata.addFieldAsImage("image", NullImageBehaviour.RemoveImageTemplate);

        PrinterContext context = new PrinterContext();
        context.put("image", new Object());

        try {
            context.setupImages(metadata);
        } catch (DocumentGenerationException e) {
            Assert.fail();
        }
    }

    @Test
    public void setupImagesTestWithFailure(){
        FieldsMetadata metadata = new FieldsMetadata();
        metadata.addFieldAsImage("image", NullImageBehaviour.ThrowsError);

        PrinterContext context = new PrinterContext();
        context.put("image", new Object());

        try {
            context.setupImages(metadata);
            Assert.fail();
        } catch (DocumentGenerationException e) {
            //ok
        }
    }

    @Test
    public void setupImagesTestGoodFormat(){
        FieldsMetadata metadata = new FieldsMetadata();
        metadata.addFieldAsImage("image", NullImageBehaviour.ThrowsError);

        PrinterContext context = new PrinterContext();
        context.put("image", new String());

        try {
            context.setupImages(metadata);
        } catch (DocumentGenerationException e) {
            Assert.fail();
        }
    }
}
