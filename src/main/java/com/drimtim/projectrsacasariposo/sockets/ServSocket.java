package com.drimtim.projectrsacasariposo.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ServSocket {
    private static int port = 1201;
    private static String ip = "localhost";

    public static ServerSocket serverSocket;

    // string --> username, map --> {"socket" socket, "in" in, "out" out}
    public static Map<String, Map<String, Object>> clients = new HashMap<>();



    public static void initializeListening () throws IOException {
        serverSocket = new ServerSocket(port);
        do {
            System.out.println("Listening for new clients...");
            Socket s = serverSocket.accept();
            // Crea gli stream
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);

            String username = in.readLine();

            Map<String, Object> streams = new HashMap<>();
            streams.put("socket", s);
            streams.put("in", in);
            streams.put("out", out);

            clients.put(username, streams);


            startListeningThread (username, streams);

        } while (true);
    }


    private static void startListeningThread (String username, Map<String, Object> infoSocket) {
        // avvio thread lettura
        Thread listeningSocketThread = new Thread(() -> {
            try {
                String line;
                while ((line = ((BufferedReader) infoSocket.get("in")).readLine()) != null) {
                    System.out.println("Ricevuto dal client: " + line);
                }
            } catch (IOException e) {
                if (e.getMessage().equals("Connection reset"))
                    System.err.println("Il client ["+username+"] si è disconnesso");

            }
        });
        listeningSocketThread.start();
    }
}
