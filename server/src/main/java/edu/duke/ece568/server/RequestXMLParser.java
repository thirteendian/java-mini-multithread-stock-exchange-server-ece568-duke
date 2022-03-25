package edu.duke.ece568.server;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

class RequestXMLParser {
    private DocumentBuilderFactory dbf;
    private DocumentBuilder db;
    private Document doc;
    private String recvMsg;
    private Integer byteLength;
    private String XMLMsg;
    public RequestXMLParser(String msg) throws ParserConfigurationException {
        this.recvMsg = msg;
        this.dbf = DocumentBuilderFactory.newInstance();
        this.db = this.dbf.newDocumentBuilder();
        this.parseLength();
    }

    /**
     * parse recvMsg to be Integer + XML
     */
    private void parseLength() {
       String[] temp = this.recvMsg.split("\n",2);
        this.byteLength = Integer.parseInt(temp[0]);
        this.XMLMsg = temp[1];
        try {
            this.doc = db.parse(this.XMLMsg);
            //Normalize the node elements to combined adjacent word
            this.doc.getDocumentElement().normalize();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void parseCreate(){
    if(this.doc.getDocumentElement().getNodeName() == "create"){


    }

    }

}