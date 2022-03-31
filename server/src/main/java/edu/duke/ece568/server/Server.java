package edu.duke.ece568.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.postgresql.ds.PGConnectionPoolDataSource;

class Server {
    private final ServerSocket serversocket;
    private ExecutorService service;
    private PGConnectionPoolDataSource connectionPool;
    public final static int THREAD_COUNT = 32;
    public final static int PORT_NUM = 12345;


    /**
     * Server Constructor
     * @throws IOException
     */
    public Server() throws IOException {
        this.serversocket = new ServerSocket(PORT_NUM);
        this.service = Executors.newFixedThreadPool(THREAD_COUNT);

        this.connectionPool = new PGConnectionPoolDataSource();
        this.connectionPool.setUrl("jdbc:postgresql://localhost:5432/ece568_hw4");
        this.connectionPool.setUser("postgres");
        this.connectionPool.setPassword("passw0rd");
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

        Server myServer = new Server();

        // PostgreJDBC jdbc = new PostgreJDBC("localhost", "5432", "ece568_hw4", "postgres", "passw0rd");
        Connection conn =  myServer.connectionPool.getPooledConnection().getConnection();
        PostgreJDBC jdbc = new PostgreJDBC(conn);
        jdbc.createTablesIfNotExist();
        jdbc.getConnection().close();
            
        while (true) {
            // Accept Connections
            Socket clientSocket = myServer.acceptConnection();
            ServerRunnable serverRunnable = new ServerRunnable(clientSocket, myServer.connectionPool);
            //Once connected, send thread
            Thread t = new Thread(serverRunnable);
            t.start();
        }


    }
}
