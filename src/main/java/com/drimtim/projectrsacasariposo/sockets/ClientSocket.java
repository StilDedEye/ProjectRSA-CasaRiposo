package com.drimtim.projectrsacasariposo.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSocket {
    public static ClientSocket instance = new ClientSocket();
    private String username;
    // chiavi



    public static void main(String[] args) throws IOException {
        Socket s = new Socket("localhost", 1201);
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        PrintWriter out = new PrintWriter(s.getOutputStream(), true);

        out.println("andrea");
        out.println("Sex");
    }

    public void setUsername (String username) {
        this.username = username;
    }

}
