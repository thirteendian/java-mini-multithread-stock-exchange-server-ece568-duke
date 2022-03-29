package edu.duke.ece568.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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


    public static void main(String[] args) throws IOException {

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
