package edu.duke.ece568.server;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

class RequestXMLParser {
    private String originalRequest;
    private boolean debug;

    public RequestXMLParser(String originalRequest, boolean debug){
        this.originalRequest = originalRequest;
        this.debug = debug;
    }

    public void parseAndProcessRequest() throws IOException{
        try{
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource(new StringReader(this.originalRequest)));

            document.getDocumentElement().normalize();
            Element root = document.getDocumentElement();
            String nodeName = root.getNodeName();
            if(nodeName.equals("create")){
                this.parseCreate(root);
            }
            else if(nodeName.equals("transactions")){
                this.parseTransactions(root);
            }
            else{
                // TODO: generate error message
                System.out.print("root node must be create or transactions\n");
            }
        }
        catch(Exception e){
            System.out.print(e + "\n");
        }
    }

    protected void parseCreate(Element createNode){
        Element element = (Element)createNode.getFirstChild();
        while(element != null && element.getNodeType() == Node.ELEMENT_NODE){
            String nodeName = element.getNodeName();
            
            if(nodeName.equals("account")){
                this.parseAccount(element);
            }
            else if(nodeName.equals("symbol")){
                this.parseSymbol(element);
            }
            else{
                System.out.print("create node must only have children account or symbol\n");
                // TODO: generate error message
            }
            element = (Element)element.getNextSibling();
        }
    }

    protected void parseAccount(Element accountNode){
        if(!accountNode.hasAttribute("id")){
            System.out.print("acccount must have attribute id\n");
            // TODO: genereate error
            return;
        }
        if(!accountNode.hasAttribute("balance")){
            System.out.print("acccount must have attribute balance\n");
            // TODO: genereate error
            return;
        }
        try{
            int accountNumber = Integer.parseInt(accountNode.getAttribute("id"));
            double balance = Double.parseDouble(accountNode.getAttribute("balance"));
            System.out.print(accountNumber  + ", " + balance + "\n");
            // TODO: create account
        }
        catch(Exception e){
            // TODO: generate error message
            System.out.print(e + "\n");
        }
    }

    protected void parseSymbol(Element symbolNode){
        if(!symbolNode.hasAttribute("sym")){
            // TODO: generate error message
            return;
        }

        String symbol = symbolNode.getAttribute("sym");
        Element element = (Element)symbolNode.getFirstChild();
        if(element == null){
            System.out.print("symbol must have 1 our more children\n");
            // TODO: generate error message
            return;
        }
        
        while(element != null && element.getNodeType() == Node.ELEMENT_NODE){
            if(!element.getNodeName().equals("account")){
                System.out.print("symbol must only have children account\n");
                // TODO: generate error message
                return;
            }
            if(!element.hasAttribute("id")){
                System.out.print("account under symbol must have attribute id\n");
                // TODO: generate error message
                return;
            }
            try{
                int accountNumber = Integer.parseInt(element.getAttribute("id"));
                double amount = Double.parseDouble(element.getTextContent());     
                System.out.println(accountNumber + ", " + symbol + ", " + amount);

                // TODO: create symbol
            }
            catch(Exception e){
                // TODO: generate error message
                System.out.print(e +"\n");
            }

            element = (Element)element.getNextSibling();
        }
    }

    protected void parseTransactions(Element transactionsNode){
        if(!transactionsNode.hasAttribute("id")){
            System.out.print("transaction must have attribute id\n");
            // TODO: generate error message
            return;
        }
        int accountNumber = Integer.parseInt(transactionsNode.getAttribute("id"));

        Element element = (Element)transactionsNode.getFirstChild();
        if(element == null){
            System.out.print("transaction must have 1 or more elements\n");
            // TODO: generate error message
            return;
        }

        while(element != null && element.getNodeType() == Node.ELEMENT_NODE){
            switch(element.getNodeName()){
                case "order":
                    this.parseOrder(element, accountNumber);
                    break;
                case "query":
                    this.parseQuery(element, accountNumber);
                    break;
                case "cancel":
                    this.parseCancel(element, accountNumber);
                    break;
                default:
                    // TODO: generate error message
                    System.out.print("transaction must have children order, query or cancel\n");
                    break;
            }
            element = (Element)element.getNextSibling();
        }
    }

    protected void parseOrder(Element orderNode, int accountNumber){
        if(!orderNode.hasAttribute("sym")){
            System.out.print("order must have attribute sys\n");
            // TODO: generate error message
            return;
        }
        if(!orderNode.hasAttribute("amount")){
            System.out.print("order must have attribute amount\n");
            // TODO: generate error message
            return;
        }
        if(!orderNode.hasAttribute("limit")){
            System.out.print("order must have attribute limit\n");
            // TODO: generate error message
            return;
        }
        try{
            String symbol = orderNode.getAttribute("sym");
            double amount = Double.parseDouble(orderNode.getAttribute("amount"));
            double limitPrice = Double.parseDouble(orderNode.getAttribute("limit"));
            System.out.print(accountNumber + ", " + symbol + ", " + amount + ", " + limitPrice + "\n");
            // TODO: create order
        }
        catch(Exception e){
            System.out.print(e + "\n");
            // TODO: generate error message
        }
    }

    protected void parseQuery(Element queryNode, int accountNumber){
        if(!queryNode.hasAttribute("id")){
            System.out.print("order must have attribute sys\n");
            // TODO: generate error message
            return;
        }
        try{
            int orderId = Integer.parseInt(queryNode.getAttribute("id"));
            System.out.print("query: " + accountNumber + ", " + orderId + "\n");
            // TODO: query orders
        }
        catch(Exception e){
            System.out.print(e + "\n");
            // TODO: generate error message
        }
    }

    protected void parseCancel(Element cancelNode, int accountNumber){
        if(!cancelNode.hasAttribute("id")){
            System.out.print("cancel must have attribute id\n");
             // TODO: generate error message
             return;
        }
        try{
            int orderId = Integer.parseInt(cancelNode.getAttribute("id"));
            System.out.println("cancel: " + accountNumber + ", " + orderId);
            // TODO: cancel orders
        }
        catch(Exception e){
            System.out.print(e + "\n");
            // TODO: generate error message
        }
    }
}
