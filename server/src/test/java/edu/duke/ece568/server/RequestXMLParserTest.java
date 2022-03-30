package edu.duke.ece568.server;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.sql.SQLException;
import java.time.Instant;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

public class RequestXMLParserTest {
    @Disabled
    @Test
    public void test_parseAndProcessRequest_create() throws ParserConfigurationException, SAXException, 
        IOException, ClassNotFoundException, SQLException, TransformerException{

        PostgreJDBC jdbc = Shared.helper_generateValidJdbc();
        Shared.cleanAllTables(jdbc);

        String xml = 
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +   
            "<create>" + 
                "<account id=\"123456\" balance=\"1000\"/>" +
                "<symbol sym=\"SPY\">" + 
                    "<account id=\"123456\">100000</account>" + 
                    "<account id=\"9876\">123</account>" + 
                "</symbol>" + 
                "<account id=\"738\" balance=\"2000\"/>" +
            "</create>";
        RequestXMLParser parser = new RequestXMLParser(jdbc, xml);
        parser.parseAndProcessRequest();
    }

    @Disabled
    @Test
    public void test_parseAndProcessRequest_transactions() throws ParserConfigurationException, SAXException, 
        IOException, ClassNotFoundException, SQLException, InvalidAlgorithmParameterException, TransformerException{

        PostgreJDBC jdbc = Shared.helper_generateValidJdbc();
        Shared.cleanAllTables(jdbc);

        Account account = new Account(jdbc, 0, 100);
        account.commitToDb();
        StockOrder order = new StockOrder(jdbc, 1, 0, "AAZ", 10, 20, java.sql.Timestamp.from(Instant.now()), "OPEN");
        order.commitToDb();

        String xml = 
        "<transactions id=\"0\">" + 
            "<order sym=\"SYM\" amount=\"10\" limit=\"5.2\"/>" + 
            "<query id=\"" +order.getOrderId() + "\"/>" + 
            "<cancel id=\"2\"/>" + 
        "</transactions>";
        RequestXMLParser parser = new RequestXMLParser(jdbc, xml);
        parser.parseAndProcessRequest();
    }
}
