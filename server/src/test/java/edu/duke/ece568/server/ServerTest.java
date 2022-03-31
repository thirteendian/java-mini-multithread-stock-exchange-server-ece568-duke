package edu.duke.ece568.server;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class ServerTest {
    @Test
    public void test_ServerTest() throws IOException {
        Server myserver = new Server();
        MockClient mockClient = new MockClient(12345, "127.0.0.1");
        Socket clientSocket = myserver.acceptConnection();
        ServerRunnable serverRunnable = new ServerRunnable(clientSocket);
        Thread t = new Thread(serverRunnable);
        t.start();


        String XML_str = Files.readString(Path.of("/Users/yuxuan/Project/erss-hwk4-ac692-yy340/SampleXML/ARequest.xml"));
        XML_str = "172\n"+XML_str;
        mockClient.sendMsg(XML_str);
        String recvMsg;
        recvMsg = mockClient.recvMsg();
        System.out.println(recvMsg);
    }

}
