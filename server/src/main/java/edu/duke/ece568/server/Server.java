package edu.duke.ece568.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Server {
    private final int portNum;
    private final ServerSocket serversocket;
    private ExecutorService service;

    private ArrayList<Socket> clientSocketList;

    public Server() throws IOException {
        this.portNum = 12345;
        this.serversocket = new ServerSocket(this.portNum);
        this.service = Executors.newFixedThreadPool(32);
        this.clientSocketList = new ArrayList<>();

    }

    public void acceptClient() throws IOException {
        this.clientSocketList.add(this.serversocket.accept());
    }

    public void sendMsg(int playerID, String msg) throws IOException {
        OutputStream out = clientSocketList.get(playerID - 1).getOutputStream();
        var writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        writer.write(msg + "\n");
        writer.flush();
    }

    public String recvMsg(int playerID) throws IOException {
        InputStream in = clientSocketList.get(playerID - 1).getInputStream();
        var reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        return reader.readLine();
    }


    public boolean init() {
        try {
            this.acceptClient();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void sendthread(){


    }
}
