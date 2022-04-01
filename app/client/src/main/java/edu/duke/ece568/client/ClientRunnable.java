package edu.duke.ece568.client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientRunnable implements Runnable {
    private Socket clientSocket;

    public ClientRunnable(Socket clientSocket){
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
        return reader.readLine();
    }


    @Override
    public void run() {
        for(int i = 0; i < 100; i++){
            try{
                String request = 
                    "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +   
                    "<create>" + 
                        "<account id=\"123456\" balance=\"1000\"/>" +
                        "<account id=\"123456\" balance=\"1000\"/>" + // error
                        "<account id=\"23456\" balance=\"2000\"/>" +
                        "<account id=\"23x56\" balance=\"2000\"/>" +
                        "<symbol sym=\"SPY\">" + 
                            "<account id=\"123456\">100000</account>" + 
                            "<account id=\"34556\">100000</account>" + // error
                            "<account id=\"34e56\">100000</account>" + // error
                        "</symbol>" + 
                        "<account id=\"34567\" balance=\"2000\"/>" +
                        "<symbol sym=\"AMAZ\">" + 
                            "<account i=\"123456\">20000</account>" + // error
                            "<account id=\"23456\">10000</account>" +
                        "</symbol>" + 
                    "</create>";

                this.sendMsg(request);
                String response = this.recvMsg();

                System.out.println(response);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }   
}
