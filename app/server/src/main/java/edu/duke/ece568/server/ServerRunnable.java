package edu.duke.ece568.server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ServerRunnable implements Runnable {
    Socket clientSocket;

    public ServerRunnable(Socket clientSocket) {
        this.clientSocket = clientSocket;
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
        reader.readLine();//length number
        StringBuilder stringBuilder = new StringBuilder(new String());
        int c;
        while((c = reader.read())!=-1){
            stringBuilder.append((char) c);
            if(String.valueOf(stringBuilder).contains("</create>")|| String.valueOf(stringBuilder).contains("</transactions>")) break;
        }
        return String.valueOf(stringBuilder);
    }

    @Override
    public void run() {
        while (true) {
            try {
                // sendMsg("type your input:");
                // PostgreJDBC jdbc = new PostgreJDBC("localhost", "5432", "ece568_hw4", "postgres", "passw0rd");
                PostgreJDBC jdbc = new PostgreJDBC("db", "5432", "postgres", "postgres", "postgres");

                String request = recvMsg();
                RequestXMLParser parser = new RequestXMLParser(jdbc, request);
                String response = parser.parseAndProcessRequest();

                System.out.println(response);
                sendMsg(response);

                jdbc.getConnection().close();

            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

        }
    }

}
