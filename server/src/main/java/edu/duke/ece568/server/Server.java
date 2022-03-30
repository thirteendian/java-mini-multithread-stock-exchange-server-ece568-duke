package edu.duke.ece568.server;

import edu.duke.ece568.server.Request.XMLClientCreateRequest;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


class Server {
    private final ServerSocket serversocket;
    private ExecutorService service;
    //    private ArrayList<Socket> clientSocketList;
    public final static int THREAD_COUNT = 32;
    public final static int PORT_NUM = 12345;


    /**
     * Server Constructor
     * @throws IOException
     */
    public Server() throws IOException {
        this.serversocket = new ServerSocket(PORT_NUM);
        this.service = Executors.newFixedThreadPool(THREAD_COUNT);
    }

    /**
     * accept incomming connections
     * @return  clientSocket connected with client
     * @throws IOException
     */
    public Socket acceptConnection() throws IOException {
        Socket clientSocket = this.serversocket.accept();
        return clientSocket;
    }


    public static void main(String[] args) {

        //Server myServer = new Server();
        //String XML_str = Files.readString(Path.of("../SampleXML/ARequest.xml"),StandardCharsets.UTF_8);

        
        try {
            //XML String String Reader
            String XML_str = Files.readString(Path.of("/Users/yuxuan/Project/erss-hwk4-ac692-yy340/SampleXML/ARequest.xml"));
            XML_str = "172\n"+XML_str;
            RequestXMLParser requestXMLParser = new RequestXMLParser(XML_str);
            int a = 0;
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        int a = 0;
       /*
        while (true) {
            // Accept Connections
            Socket clientSocket = myServer.acceptConnection();
            ServerRunnable serverRunnable = new ServerRunnable(clientSocket);
            //Once connected, send thread
            Thread t = new Thread(serverRunnable);
            t.start();
        }*/


    }
}
