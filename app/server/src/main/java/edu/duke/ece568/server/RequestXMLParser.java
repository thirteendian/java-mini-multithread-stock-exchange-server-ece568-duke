package edu.duke.ece568.server;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

class RequestXMLParser {
    private String originalRequest;
    private PostgreJDBC jdbc;
    private Document responseXml;

    public RequestXMLParser(PostgreJDBC jdbc, String originalRequest){
        this.originalRequest = originalRequest;
        this.jdbc = jdbc;
        this.responseXml = null;
    }

    protected void senitizeInput() throws Exception{
        // remove all new lines
        this.originalRequest = this.originalRequest.replaceAll("\r\n", "");
        this.originalRequest = this.originalRequest.replaceAll("\n", "");

        // remove length indicator before xml
        int locationFirstTag = this.originalRequest.indexOf("<");
        if(locationFirstTag == -1){
            throw new Exception("malformed XML");
        }
        this.originalRequest = this.originalRequest.substring(locationFirstTag);
    }

    protected void printXml(Document doc) throws TransformerException{
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", 2);

        Transformer transformer = transformerFactory.newTransformer();

        DOMSource source = new DOMSource(doc);
        StreamResult consoleResult = new StreamResult(System.out);
        transformer.transform(new DOMSource(), consoleResult);
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        transformer.transform(source, consoleResult);
    }

    protected void createErrorElementWithoutAttributes(Element responseParentNode, String errorMessage){        
        Element element = responseXml.createElement("error");
        element.appendChild(responseXml.createTextNode(errorMessage));
        responseParentNode.appendChild(element);
    }

    protected String getResponse() throws TransformerException{
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(this.responseXml), new StreamResult(writer));
        String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
        return output;
    }

    public String parseAndProcessRequest() throws ParserConfigurationException, SAXException, IOException, 
        SQLException, TransformerException{

        try{
            this.senitizeInput();

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document requestXml = documentBuilder.parse(new InputSource(new StringReader(this.originalRequest)));
            this.responseXml = documentBuilder.newDocument();

            requestXml.getDocumentElement().normalize();
            Element root = requestXml.getDocumentElement();
            String nodeName = root.getNodeName();

            Element responseRoot = responseXml.createElement("results");
            responseXml.appendChild(responseRoot);

            if(nodeName.equals("create")){
                this.parseCreate(root, responseRoot);
            }
            else if(nodeName.equals("transactions")){
                this.parseTransactions(root, responseRoot);
            }
            else{
                // generate error message
                String errorMessage = "root node must be create or transactions";
                this.createErrorElementWithoutAttributes(responseRoot, errorMessage);
            }
            return this.getResponse();
        }
        catch(Exception e){
            return "<error>" + e.toString() + "</error>";
        }
    }

    protected void parseCreate(Element createNode, Element responseParentNode) throws SQLException{
        Element element = (Element)createNode.getFirstChild();
        while(element != null && element.getNodeType() == Node.ELEMENT_NODE){
            String nodeName = element.getNodeName();
            
            if(nodeName.equals("account")){
                this.parseAccount(element, responseParentNode);
            }
            else if(nodeName.equals("symbol")){
                this.parseSymbol(element, responseParentNode);
            }
            else{
                String errorMessage = "create node must only have children account or symbol";
                this.createErrorElementWithoutAttributes(responseParentNode, errorMessage);
            }
            element = (Element)element.getNextSibling();
        }
    }

    protected void parseAccount(Element accountNode, Element responseParentNode) throws SQLException{
        if(!accountNode.hasAttribute("id")){
            String errorMessage = "acccount must have attribute id";
            this.createErrorElementWithoutAttributes(responseParentNode, errorMessage);
            return;
        }
        if(!accountNode.hasAttribute("balance")){
            String errorMessage = "acccount must have attribute balance";
            this.createErrorElementWithoutAttributes(responseParentNode, errorMessage);
            return;
        }
        try{
            int accountNumber = Integer.parseInt(accountNode.getAttribute("id"));
            double balance = Double.parseDouble(accountNode.getAttribute("balance"));

            this.jdbc.getConnection().setAutoCommit(false);
            Account account = new Account(jdbc, accountNumber, balance);
            account.commitToDb();
            this.jdbc.getConnection().commit();
            
            // generate message
            Element element = this.responseXml.createElement("created");
            element.setAttribute("id", Integer.toString(accountNumber));
            element.setAttribute("balance", Double.toString(balance));
            responseParentNode.appendChild(element);
        }
        catch(NumberFormatException e){
            this.createErrorElementWithoutAttributes(responseParentNode, e.toString());
        }
        catch(Exception e){
            this.jdbc.getConnection().rollback();
            
            // generate error message
            Element element = responseXml.createElement("error");
            element.setAttribute("id", accountNode.getAttribute("id"));
            element.appendChild(responseXml.createTextNode(e.toString()));
            responseParentNode.appendChild(element);
        }
    }

    protected void parseSymbol(Element symbolNode, Element responseParentNode) throws SQLException{
        if(!symbolNode.hasAttribute("sym")){
            String errorMessage = "symbol must have attribute sym";
            this.createErrorElementWithoutAttributes(responseParentNode, errorMessage);
            return;
        }

        String symbol = symbolNode.getAttribute("sym");
        Element element = (Element)symbolNode.getFirstChild();
        if(element == null){
            String errorMessage = "symbol must have 1 our more children";            
            this.createErrorElementWithoutAttributes(responseParentNode, errorMessage);
            return;
        }
        
        while(element != null && element.getNodeType() == Node.ELEMENT_NODE){
            if(!element.getNodeName().equals("account")){
                String errorMessage = "symbol must only have children account";
                this.createErrorElementWithoutAttributes(responseParentNode, errorMessage);
                element = (Element)element.getNextSibling();
                continue;
            }
            if(!element.hasAttribute("id")){
                String errorMessage = "account under symbol must have attribute id";
                this.createErrorElementWithoutAttributes(responseParentNode, errorMessage);
                element = (Element)element.getNextSibling();
                continue;
            }
            try{
                int accountNumber = Integer.parseInt(element.getAttribute("id"));
                double amount = Double.parseDouble(element.getTextContent());     

                this.jdbc.getConnection().setAutoCommit(false);
                Position position = new Position(jdbc, accountNumber, symbol, amount);
                position.commitToDb();
                this.jdbc.getConnection().commit();

                // generate message
                Element responseElement = this.responseXml.createElement("created");
                responseElement.setAttribute("sym", symbol);
                responseElement.setAttribute("id", Integer.toString(accountNumber));
                responseParentNode.appendChild(responseElement);
            }
            catch(NumberFormatException e){
                // generate message
                Element responseElement = this.responseXml.createElement("error");
                responseElement.setAttribute("sym", symbol);
                responseElement.setAttribute("id", element.getAttribute("id"));
                responseElement.appendChild(this.responseXml.createTextNode(e.toString()));
                responseParentNode.appendChild(responseElement);
            }
            catch(Exception e){
                this.jdbc.getConnection().rollback();

                // generate message
                Element responseElement = this.responseXml.createElement("error");
                responseElement.setAttribute("sym", symbol);
                responseElement.setAttribute("id", element.getAttribute("id"));
                responseElement.appendChild(this.responseXml.createTextNode(e.toString()));
                responseParentNode.appendChild(responseElement);
            }

            element = (Element)element.getNextSibling();
        }
    }

    protected void parseTransactions(Element transactionsNode, Element responseParentNode) throws SQLException{
        if(!transactionsNode.hasAttribute("id")){
            String errorMessage = "transaction must have attribute id";
            this.createErrorElementWithoutAttributes(responseParentNode, errorMessage);
            return;
        }
        int accountNumber = Integer.parseInt(transactionsNode.getAttribute("id"));

        Element element = (Element)transactionsNode.getFirstChild();
        if(element == null){
            String errorMessage = "transaction must have 1 or more elements";
            this.createErrorElementWithoutAttributes(responseParentNode, errorMessage);
            return;
        }

        while(element != null && element.getNodeType() == Node.ELEMENT_NODE){
            switch(element.getNodeName()){
                case "order":
                    this.parseOrder(element, responseParentNode, accountNumber);
                    break;
                case "query":
                    this.parseQuery(element, responseParentNode, accountNumber);
                    break;
                case "cancel":
                    this.parseCancel(element, responseParentNode, accountNumber);
                    break;
                default:
                    String errorMessage = "transaction must have children order, query or cancel";
                    this.createErrorElementWithoutAttributes(responseParentNode, errorMessage);
                    break;
            }
            element = (Element)element.getNextSibling();
        }
    }

    protected void parseOrder(Element orderNode, Element responseParentNode, int accountNumber) throws SQLException{
        if(!orderNode.hasAttribute("sym")){
            String errorMessage = "order must have attribute sym";
            this.createErrorElementWithoutAttributes(responseParentNode, errorMessage);
            return;
        }
        if(!orderNode.hasAttribute("amount")){
            String errorMessage = "order must have attribute amount";
            this.createErrorElementWithoutAttributes(responseParentNode, errorMessage);
            return;
        }
        if(!orderNode.hasAttribute("limit")){
            String errorMessage = "order must have attribute limit";
            this.createErrorElementWithoutAttributes(responseParentNode, errorMessage);
            return;
        }
        try{
            String symbol = orderNode.getAttribute("sym");
            double amount = Double.parseDouble(orderNode.getAttribute("amount"));
            double limitPrice = Double.parseDouble(orderNode.getAttribute("limit"));

            this.jdbc.getConnection().setAutoCommit(false);
            Account account = new Account(jdbc, accountNumber);
            int newOrderId = account.placeOrder(symbol, amount, limitPrice);
            this.jdbc.getConnection().commit();

            // create message
            Element responseElement = this.responseXml.createElement("opened");
            responseElement.setAttribute("sym", symbol);
            responseElement.setAttribute("amount", Double.toString(amount));
            responseElement.setAttribute("limit", Double.toString(limitPrice));
            responseElement.setAttribute("id", Integer.toString(newOrderId));
            responseParentNode.appendChild(responseElement);

        }
        catch(NumberFormatException e){
            this.createErrorElementWithoutAttributes(responseParentNode, e.toString());
        }
        catch(Exception e){
            this.jdbc.getConnection().rollback();
            
            Element responseElement = this.responseXml.createElement("error");
            responseElement.setAttribute("sym", orderNode.getAttribute("sym"));
            responseElement.setAttribute("id", Integer.toString(accountNumber));
            responseElement.appendChild(this.responseXml.createTextNode(e.toString()));
            responseParentNode.appendChild(responseElement);
        }
    }

    protected void parseOpenOrderStatus(Element responseParentNode, int orderId) throws SQLException{
        ArrayList<StockOrder> stockOrders = StockOrder.getAllStockOrdersByCriteria(this.jdbc, orderId, "OPEN");
        if(!stockOrders.isEmpty()){
            for(StockOrder stockOrder: stockOrders){
                Element responseElement = this.responseXml.createElement("open");
                responseElement.setAttribute("shares", Double.toString(stockOrder.getAmount()));
                responseParentNode.appendChild(responseElement);
            }
        }
    }

    protected void parseCancelledOrderStatus(Element responseParentNode, int orderId) throws SQLException{
        ArrayList<StockOrder> stockOrders = StockOrder.getAllStockOrdersByCriteria(this.jdbc, orderId, "CANCELLED");
        if(!stockOrders.isEmpty()){
            for(StockOrder stockOrder: stockOrders){
                Element responseElement = this.responseXml.createElement("canceled");
                responseElement.setAttribute("shares", Double.toString(stockOrder.getAmount()));
                responseElement.setAttribute("time", stockOrder.getIssueTime().toString());
                responseParentNode.appendChild(responseElement);
            }
        }
    }

    protected void parseExecuteddOrderStatus(Element responseParentNode, int orderId) throws SQLException, IllegalAccessException{
        ArrayList<ExecutedOrder> executedOrders = ExecutedOrder.getAllExecutedOrdersByOrderId(jdbc, orderId);
        for(ExecutedOrder executOrder: executedOrders){
            Element responseElement = this.responseXml.createElement("executed");
            responseElement.setAttribute("shares", Double.toString(executOrder.getAmount()));
            responseElement.setAttribute("price", Double.toString(executOrder.getLimitPrice()));
            responseElement.setAttribute("time", executOrder.getIssueTime().toString());
            responseParentNode.appendChild(responseElement);
        }
    }


    protected void parseQuery(Element queryNode, Element responseParentNode, int accountNumber) throws SQLException{
        if(!queryNode.hasAttribute("id")){
            String errorMessage = "order must have attribute id";
            this.createErrorElementWithoutAttributes(responseParentNode, errorMessage);
            return;
        }
        try{
            int orderId = Integer.parseInt(queryNode.getAttribute("id"));

            Element statusElement = this.responseXml.createElement("status");
            statusElement.setAttribute("id", Integer.toString(orderId));
            responseParentNode.appendChild(statusElement);
            
            // opened orders
            this.parseOpenOrderStatus(statusElement, orderId);

            // cancelled orders
            this.parseCancelledOrderStatus(statusElement, orderId);

            // execited orders
            this.parseExecuteddOrderStatus(statusElement, orderId);

        }
        catch(Exception e){
            this.createErrorElementWithoutAttributes(responseParentNode, e.toString());
        }
    }

    protected void parseCancel(Element cancelNode, Element responseParentNode,int accountNumber) throws SQLException{
        if(!cancelNode.hasAttribute("id")){
            String errorMessage = "order must have attribute id";
            this.createErrorElementWithoutAttributes(responseParentNode, errorMessage);
            return;
        }

        try{
            Integer.parseInt(cancelNode.getAttribute("id"));
        }
        catch(Exception e){
            this.createErrorElementWithoutAttributes(responseParentNode, e.toString());
            return;
        }

        int orderId = Integer.parseInt(cancelNode.getAttribute("id"));
        Element statusElement = this.responseXml.createElement("canceled");
        statusElement.setAttribute("id", Integer.toString(orderId));
        responseParentNode.appendChild(statusElement);
        responseParentNode = statusElement;

        // perform cancellation
        this.performCancelOrder(cancelNode, responseParentNode, accountNumber);

        // query cancelled but executed
        this.parseCancelExecuted(cancelNode, responseParentNode);
    }

    protected void performCancelOrder(Element cancelNode, Element responseParentNode, int accountNumber) throws SQLException{
        int orderId = Integer.parseInt(cancelNode.getAttribute("id"));

        try{
            this.jdbc.getConnection().setAutoCommit(false);
            StockOrder stockOrder = new StockOrder(jdbc, orderId);
            if(stockOrder.getAccountNumber() != accountNumber){
                throw new Exception("cannot cancel order if the account number is not the order owner");
            }
            stockOrder.cancelOrder();
            this.jdbc.getConnection().commit();

            // generate message
            Element element = this.responseXml.createElement("canceled");
            element.setAttribute("shares", Double.toString(stockOrder.getAmount()));
            element.setAttribute("time", stockOrder.getIssueTime().toString());
            responseParentNode.appendChild(element);

        }
        catch(Exception e){
            this.jdbc.getConnection().rollback();

            Element responseElement = this.responseXml.createElement("error");
            responseElement.setAttribute("id", Integer.toString(orderId));
            responseElement.appendChild(this.responseXml.createTextNode(e.toString()));
            responseParentNode.appendChild(responseElement);
        }
    }

    protected void parseCancelExecuted(Element cancelNode, Element responseParentNode){
        int orderId = Integer.parseInt(cancelNode.getAttribute("id"));
        
        try{
            ArrayList<ExecutedOrder> executedOrders = ExecutedOrder.getAllExecutedOrdersByOrderId(this.jdbc, orderId);
            for(ExecutedOrder executOrder: executedOrders){
                Element responseElement = this.responseXml.createElement("executed");
                responseElement.setAttribute("shares", Double.toString(executOrder.getAmount()));
                responseElement.setAttribute("price", Double.toString(executOrder.getLimitPrice()));
                responseElement.setAttribute("time", executOrder.getIssueTime().toString());
                responseParentNode.appendChild(responseElement);
            }
        }
        catch(Exception e){
            Element responseElement = this.responseXml.createElement("error");
            responseElement.setAttribute("id", Integer.toString(orderId));
            responseElement.appendChild(this.responseXml.createTextNode(e.toString()));
            responseParentNode.appendChild(responseElement);
        }
    }
}
