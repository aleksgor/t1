package com.nomad.server.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;


public class CommandClient {
    @SuppressWarnings("unused")
    public static void main(String[] args) throws IOException {

        String serverHostName = new String("localhost");

        Socket echoSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {

            echoSocket = new Socket(serverHostName, 2221);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + serverHostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for " + "the connection to: " + serverHostName);
            System.exit(1);
        }

        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
        String userInput;

        while ((userInput = buffer.readLine()) != null) {
            out.println(userInput);
            String line = in.readLine();
            if("bye".equals(userInput)){
                break;
            }

        }

        out.close();
        in.close();
        buffer.close();
        echoSocket.close();
    }


}
