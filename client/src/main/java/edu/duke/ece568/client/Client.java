/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package edu.duke.ece568.client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client{
    private final String host;
    private final InputStream in;
    private final OutputStream out;
    private final OutputStreamWriter writer;
    private final BufferedReader reader;
    private final Socket socket;
    private final int PORT_NUM = 12345;
    public Client(String host) throws IOException {
        this.host = host;
        this.socket = new Socket(this.host,this.PORT_NUM);
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
        this.writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    public void sendMsg(String msg) throws IOException {
        this.writer.write(msg + "\n");
        this.writer.flush();
    }

    /**
     * Receive messgae from the server
     *
     * @return String
     * @throws IOException
     */
    public String recvMsg() throws IOException {
        return this.reader.readLine();
    }

    public static void main(String[] args) {

        //Connect
        try{
            Client myClient = new Client("127.0.0.1");

        } catch (Exception e) {
            e.printStackTrace();
        }
        //Send Message



    }
}