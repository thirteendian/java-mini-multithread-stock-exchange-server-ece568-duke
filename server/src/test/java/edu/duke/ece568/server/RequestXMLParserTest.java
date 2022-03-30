package edu.duke.ece568.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.sql.SQLException;
import java.time.Instant;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

public class RequestXMLParserTest {

    private void helper_responseComparator(String request, String expectedResponse) throws ClassNotFoundException, 
        SQLException, ParserConfigurationException, SAXException, IOException, TransformerException{
        PostgreJDBC jdbc = Shared.helper_generateValidJdbc();

        Shared.cleanAllTables(jdbc);

        RequestXMLParser parser = new RequestXMLParser(jdbc, request);
        String response = parser.parseAndProcessRequest();
        System.out.println(response);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void test_parseAndProcessRequest_illegal() throws ClassNotFoundException, SQLException, 
        ParserConfigurationException, SAXException, IOException, TransformerException{

        String request = "asdf";
        String expected = 
            "<error>" + 
                "org.xml.sax.SAXParseException; lineNumber: 1; columnNumber: 1; Content is not allowed in prolog." + 
            "</error>";

        this.helper_responseComparator(request, expected);
    }

    @Test
    public void test_parseAndProcessRequest_malformed() throws ClassNotFoundException, SQLException, 
    ParserConfigurationException, SAXException, IOException, TransformerException{
        String request = 
        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +   
        "<create" + 
            "<account id=\"123456\" balance=\"1000\"/>" +
            "<account id=\"738\" balance=\"2000\"/>" +
        "</create>";

        String expected = 
            "<error>" + 
                "org.xml.sax.SAXParseException; lineNumber: 1; columnNumber: 51; " + 
                "Element type \"create\" must be followed by either attribute specifications, \">\" or \"/>\"." + 
            "</error>";
        this.helper_responseComparator(request, expected);
    }

    @Test
    public void test_parseAndProcessRequest_illegalRoot() throws ClassNotFoundException, SQLException, 
        ParserConfigurationException, SAXException, IOException, TransformerException{

        String request = 
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +   
            "<reate>" + 
                "<account id=\"123456\" balance=\"1000\"/>" +
                "<account id=\"738\" balance=\"2000\"/>" +
            "</reate>";

        String expected = 
            "<results>" + 
                "<error>root node must be create or transactions</error>" + 
            "</results>";

        this.helper_responseComparator(request, expected);
    }

    /*---------------------------------------- testcases for create ----------------------------------------*/
    @Test
    public void test_parseAndProcessRequest_illegalCreateTag() throws ClassNotFoundException, SQLException, 
        ParserConfigurationException, SAXException, IOException, TransformerException{

        String request = 
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +   
            "<create>" + 
                "<accout id=\"123456\" balance=\"1000\"/>" +
            "</create>";

        String expected = 
            "<results>" + 
                "<error>create node must only have children account or symbol</error>" + 
            "</results>";

        this.helper_responseComparator(request, expected);

        request = 
        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +   
        "<create>" + 
            "<symbo sym=\"SPY\">" + 
                "<account id=\"123456\">100000</account>" + 
            "</symbo>" + 
        "</create>";
        
        this.helper_responseComparator(request, expected);
    }

    @Test
    public void test_parseAndProcessRequest_createAccountWithoutAttributes() throws ParserConfigurationException, 
        SAXException, IOException, ClassNotFoundException, SQLException, TransformerException{

        String request = 
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +   
            "<create>" + 
                "<account i=\"123456\" balance=\"1000\"/>" +
            "</create>";

        String expected = 
            "<results>" +
                "<error>acccount must have attribute id</error>" + 
            "</results>";

        this.helper_responseComparator(request, expected);

        request = 
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +   
            "<create>" + 
                "<account id=\"123456\" blance=\"1000\"/>" +
            "</create>";

        expected = 
            "<results>" +
                "<error>acccount must have attribute balance</error>" + 
            "</results>";

        this.helper_responseComparator(request, expected);
    }
            
    @Test
    public void test_parseAndProcessRequest_createAccountWrongNumberFormat() throws ParserConfigurationException, 
        SAXException, IOException, ClassNotFoundException, SQLException, TransformerException{

        String request = 
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +   
            "<create>" + 
                "<account id=\"x23456\" balance=\"1000\"/>" +
            "</create>";

        String expected = 
            "<results>" + 
                "<error>java.lang.NumberFormatException: For input string: \"x23456\"</error>" + 
            "</results>";

        this.helper_responseComparator(request, expected);
    }

    @Test
    public void test_parseAndProcessRequest_createAccountSuccess() throws ParserConfigurationException, 
        SAXException, IOException, ClassNotFoundException, SQLException, TransformerException{

        String request = 
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +   
            "<create>" + 
                "<account id=\"123456\" balance=\"1000\"/>" +
                "<account id=\"738\" balance=\"2000\"/>" +
            "</create>";

        String expected = 
            "<results>" + 
                "<created balance=\"1000.0\" id=\"123456\"/>" + 
                "<created balance=\"2000.0\" id=\"738\"/>" + 
            "</results>";

        this.helper_responseComparator(request, expected);
    }

    @Test
    public void test_parseAndProcessRequest_createDuplicateAccount() throws ParserConfigurationException, 
        SAXException, IOException, ClassNotFoundException, SQLException, TransformerException{

        String request = 
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +   
            "<create>" + 
                "<account id=\"123456\" balance=\"1000\"/>" +
                "<account id=\"123456\" balance=\"2000\"/>" +
            "</create>";

        String expected = 
            "<results>" + 
                "<created balance=\"1000.0\" id=\"123456\"/>" + 
                "<error id=\"123456\">org.postgresql.util.PSQLException: " + 
                    "ERROR: duplicate key value violates unique constraint \"account_pkey\"  " + 
                    "Detail: Key (account_number)=(123456) already exists." + 
                "</error>" +
            "</results>";

        this.helper_responseComparator(request, expected);
    }

    @Test
    public void test_parseAndProcessRequest_createPositionSuccess() throws ParserConfigurationException, 
        SAXException, IOException, ClassNotFoundException, SQLException, TransformerException{

        String request = 
        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +   
        "<create>" + 
            "<account id=\"123456\" balance=\"1000\"/>" +
            "<symbol sym=\"SPY\">" + 
                "<account id=\"123456\">100000</account>" + 
            "</symbol>" + 
        "</create>";

        String expected = 
            "<results>" + 
                "<created balance=\"1000.0\" id=\"123456\"/>" + 
                "<created id=\"123456\" sym=\"SPY\"/>" +
            "</results>";

        this.helper_responseComparator(request, expected);
    }

    @Test
    public void test_parseAndProcessRequest_createPositionNoChildren() throws ParserConfigurationException, 
        SAXException, IOException, ClassNotFoundException, SQLException, TransformerException{

        String request = 
        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +   
        "<create>" + 
            "<account id=\"123456\" balance=\"1000\"/>" +
            "<symbol sym=\"SPY\">" + 
            "</symbol>" + 
        "</create>";

        String expected = 
            "<results>" + 
                "<created balance=\"1000.0\" id=\"123456\"/>" + 
                "<error>symbol must have 1 our more children</error>" + 
            "</results>";

        this.helper_responseComparator(request, expected);
    }

    @Test
    public void test_parseAndProcessRequest_createPositionWrongChildren() throws ParserConfigurationException, 
        SAXException, IOException, ClassNotFoundException, SQLException, TransformerException{

        String request = 
        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +   
        "<create>" + 
            "<account id=\"123456\" balance=\"1000\"/>" +
            "<symbol sym=\"SPY\">" + 
                "<account id=\"123456\">100000</account>" + 
                "<accountt id=\"123456\">100000</accountt>" + 
            "</symbol>" + 
        "</create>";

        String expected = 
            "<results>" + 
                "<created balance=\"1000.0\" id=\"123456\"/>" + 
                "<created id=\"123456\" sym=\"SPY\"/>" +
                "<error>symbol must only have children account</error>" + 
            "</results>";

        this.helper_responseComparator(request, expected);
    }

    @Test
    public void test_parseAndProcessRequest_createPositionWrongAttributes() throws ParserConfigurationException, 
        SAXException, IOException, ClassNotFoundException, SQLException, TransformerException{

        String request = 
        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +   
        "<create>" + 
            "<account id=\"123456\" balance=\"1000\"/>" +
            "<symbol sy=\"SPY\">" + 
                "<account id=\"123456\">100000</account>" + 
            "</symbol>" + 
        "</create>";

        String expected = 
            "<results>" + 
                "<created balance=\"1000.0\" id=\"123456\"/>" + 
                "<error>symbol must have attribute sym</error>" + 
            "</results>";

        this.helper_responseComparator(request, expected);
    }

    @Test
    public void test_parseAndProcessRequest_createPostionAccountNoId() throws ParserConfigurationException, 
        SAXException, IOException, ClassNotFoundException, SQLException, TransformerException{

        String request = 
        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +   
        "<create>" + 
            "<account id=\"123456\" balance=\"1000\"/>" +
            "<symbol sym=\"SPY\">" + 
                "<account iasd=\"123456\">100000</account>" + 
            "</symbol>" + 
        "</create>";

        String expected = 
            "<results>" + 
                "<created balance=\"1000.0\" id=\"123456\"/>" + 
                "<error>account under symbol must have attribute id</error>" + 
            "</results>";

        this.helper_responseComparator(request, expected);
    }

    @Test
    public void test_parseAndProcessRequest_integrated() throws ParserConfigurationException, 
        SAXException, IOException, ClassNotFoundException, SQLException, TransformerException{

        String request = 
        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +   
        "<create>" + 
            "<account id=\"123456\" balance=\"1000\"/>" +
            "<account id=\"123456\" balance=\"1000\"/>" + // error
            "<account id=\"23456\" balance=\"2000\"/>" +
            "<account id=\"23x56\" balance=\"2000\"/>" +
            "<symbol sym=\"SPY\">" + 
                "<account id=\"123456\">100000</account>" + 
                "<account id=\"34556\">100000</account>" + // error
                "<account id=\"34e56\">100000</account>" + // error
            "</symbol>" + 
            "<account id=\"34567\" balance=\"2000\"/>" +
            "<symbol sym=\"AMAZ\">" + 
                "<account i=\"123456\">20000</account>" + // error
                "<account id=\"23456\">10000</account>" +
            "</symbol>" + 
        "</create>";

        String expected = 
            "<results>" + 
            "<created balance=\"1000.0\" id=\"123456\"/>" + 
            "<error id=\"123456\">" + 
                "org.postgresql.util.PSQLException: ERROR: duplicate key value violates unique constraint \"account_pkey\"  "  + 
                "Detail: Key (account_number)=(123456) already exists.</error><created balance=\"2000.0\" id=\"23456\"/>" + 
            "<error>java.lang.NumberFormatException: For input string: \"23x56\"</error>" + 
            "<created id=\"123456\" sym=\"SPY\"/>" + 
            "<error id=\"34556\" sym=\"SPY\">" + 
                "org.postgresql.util.PSQLException: ERROR: insert or update on table \"position\" violates foreign key constraint \"fk_account\"  " + 
                "Detail: Key (account_number)=(34556) is not present in table \"account\"." + 
            "</error>" + 
            "<error id=\"34e56\" sym=\"SPY\">java.lang.NumberFormatException: For input string: \"34e56\"</error>" + 
            "<created balance=\"2000.0\" id=\"34567\"/>" + 
            "<error>account under symbol must have attribute id</error>" + 
            "<created id=\"23456\" sym=\"AMAZ\"/>" + 
        "</results>";

        this.helper_responseComparator(request, expected);
    }

    /*---------------------------------------- testcases for create ----------------------------------------*/
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
