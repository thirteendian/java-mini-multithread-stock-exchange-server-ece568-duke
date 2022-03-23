package edu.duke.ece568.server;

import java.net.Socket;
import java.util.concurrent.Callable;

public class ServerCallable implements Callable<RequestXMLParser> {
    private Socket socket;

    ServerCallable(Socket socket) {
        this.socket = socket;
    }


    @Override
    public RequestXMLParser call() throws Exception {

        //receive XML string

        //Parser XML String
        RequestXMLParser requestXMLParser = new RequestXMLParser();

        //SQL

        //send ResponseXML

        return null;
    }
}