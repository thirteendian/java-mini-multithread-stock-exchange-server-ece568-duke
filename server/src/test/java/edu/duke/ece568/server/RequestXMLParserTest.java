package edu.duke.ece568.server;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

public class RequestXMLParserTest {
    @Test
    public void test_parseAndProcessRequest_create() throws ParserConfigurationException, SAXException, IOException{
        String xml = 
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +   
            "<create>" + 
                "<account id=\"123456\" balance=\"1000\"/>" +
                "<symbol sym=\"SPY\">" + 
                    "<account id=\"123456\">100000</account>" + 
                    "<account id=\"9123\">123</account>" + 
                "</symbol>" + 
                "<account id=\"738\" balance=\"2000\"/>" +
            "</create>";
        RequestXMLParser parser = new RequestXMLParser(xml, true);
        parser.parseAndProcessRequest();
    }

    @Test
    public void test_parseAndProcessRequest_transactions() throws ParserConfigurationException, SAXException, IOException{
        String xml = 
        "<transactions id=\"0\">" + 
            "<order sym=\"SYM\" amount=\"10.02\" limit=\"5.2\"/>" + 
            "<query id=\"1\"/>" + 
            "<cancel id=\"2\"/>" + 
        "</transactions>";
        RequestXMLParser parser = new RequestXMLParser(xml, true);
        parser.parseAndProcessRequest();
    }
}
