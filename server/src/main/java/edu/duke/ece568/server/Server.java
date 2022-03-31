package edu.duke.ece568.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.bind.JAXBElement;


class Server {
    private final ServerSocket serversocket;
    private ExecutorService service;
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


    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {

        PostgreJDBC jdbc = new PostgreJDBC("localhost", "5432", "ece568_hw4", "postgres", "passw0rd");
        jdbc.createTablesIfNotExist();
        jdbc.getConnection().close();
    
        Server myServer = new Server();

        while (true) {
            // Accept Connections
            Socket clientSocket = myServer.acceptConnection();
            ServerRunnable serverRunnable = new ServerRunnable(clientSocket);
            //Once connected, send thread
            Thread t = new Thread(serverRunnable);
            t.start();
        }


    }
}
