package edu.duke.ece568.server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.postgresql.PGConnection;
import org.postgresql.ds.PGConnectionPoolDataSource;

public class ServerRunnable implements Runnable {
    private Socket clientSocket;
    private PGConnectionPoolDataSource connectionPool;

    public ServerRunnable(Socket clientSocket, PGConnectionPoolDataSource connectionPool) {
        this.clientSocket = clientSocket;
        this.connectionPool = connectionPool;
    }

    public void sendMsg(String msg) throws IOException {
        OutputStream out = clientSocket.getOutputStream();
        var writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        writer.write(msg + "\n");
        writer.flush();
    }

    public String recvMsg() throws IOException {
        InputStream in = clientSocket.getInputStream();
        var reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        // StringBuilder stringBuilder = new StringBuilder();
        // String line = "";
        // while((line = reader.readLine())!=null){
        //     stringBuilder.append(line);
        //     stringBuilder.append("\n");
        // }
        return reader.readLine();
    }

    @Override
    public void run() {
        while (true) {
            try {
                // sendMsg("type your input:");
                // PostgreJDBC jdbc = new PostgreJDBC("localhost", "5432", "ece568_hw4", "postgres", "passw0rd");

                String request = recvMsg();
                // RequestXMLParser parser = new RequestXMLParser(jdbc, request);

                RequestXMLParser parser = new RequestXMLParser(this.connectionPool, request);
                String response = parser.parseAndProcessRequest();
                sendMsg(response);

                // jdbc.getConnection().close();

            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

        }
    }

}
