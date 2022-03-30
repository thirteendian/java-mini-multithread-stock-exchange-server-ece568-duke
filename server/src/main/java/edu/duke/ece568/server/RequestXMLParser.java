package edu.duke.ece568.server;

import edu.duke.ece568.server.Request.XMLClientCreateRequest;
import edu.duke.ece568.server.Request.XMLClientRequest;
import edu.duke.ece568.server.Request.XMLClientTransactionRequest;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

class RequestXMLParser {
    private XMLClientRequest xmlClientRequest;
    private ArrayList<String> xmlClientRequestOrder;
    //XML
    private String recvMsg;
    private Integer byteLength;
    private String XMLMsg;
    //DOM
    private DocumentBuilderFactory dbf;
    private DocumentBuilder db;
    private Document doc;
    private Node root;
    //JAXBParser
    private JAXBContext jc;
    private Unmarshaller jcUnmarshaller;
    private Marshaller jcMarshaller;

    public RequestXMLParser(String msg) throws ParserConfigurationException, JAXBException, IOException, SAXException {
        this.recvMsg = msg;
        this.parseRecvString();
        this.xmlClientRequestOrder = new ArrayList<>();
        this.constructDOM();
        this.constructJAXB();
    }

    /**
     * parse recvMsg to be Integer + XML
     */
    private void parseRecvString() {
        String[] temp = this.recvMsg.split("\n", 2);
        this.byteLength = Integer.parseInt(temp[0]);
        this.XMLMsg = temp[1];
    }

    /**
     * Get first Element that are element type
     * Rather than Text type
     *
     * @param root root Node
     * @return First Child Element
     */
    private Node getFirstElementChild(Node root) {
        NodeList childs = root.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node child = childs.item(i);
            if (child instanceof Element) {
                return child;
            }
        }
        return null;
    }

    /**
     * Construct DOM and doc
     */
    private void constructDOM() throws ParserConfigurationException, IOException, SAXException {
        this.dbf = DocumentBuilderFactory.newInstance();
        this.db = this.dbf.newDocumentBuilder();
        StringReader createStringReader = new StringReader(this.XMLMsg);
        this.doc = this.db.parse(new InputSource(createStringReader));
        this.doc.getDocumentElement().normalize();
        this.root = (Node) this.doc.getDocumentElement();
        Node requestElement = this.getFirstElementChild(this.root);

        //Create Order
        while (requestElement != null) {
            if (requestElement.getNodeType() == Node.ELEMENT_NODE) {
                String requestName = requestElement.getNodeName();
                if (requestName != null) {
                    this.xmlClientRequestOrder.add(requestName);
                }
            }
            requestElement = requestElement.getNextSibling();
        }
    }

    private void constructJAXB() throws JAXBException {
        if (this.root.getNodeName().equals("create")) {
            this.jc = JAXBContext.newInstance(XMLClientCreateRequest.class);
        } else if (this.root.getNodeName().equals("transactions")) {
            this.jc = JAXBContext.newInstance(XMLClientTransactionRequest.class);
        }
        this.jcUnmarshaller = this.jc.createUnmarshaller();
        this.jcMarshaller = this.jc.createMarshaller();
        StringReader createStringReader = new StringReader(this.XMLMsg);
        this.xmlClientRequest = (XMLClientRequest) this.jcUnmarshaller.unmarshal(createStringReader);
    }

    public XMLClientRequest getXmlClientRequest() {
        return xmlClientRequest;
    }

    public ArrayList<String> getXmlClientRequestOrder() {
        return xmlClientRequestOrder;
    }
}
