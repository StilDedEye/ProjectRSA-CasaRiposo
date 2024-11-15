package com.drimtim.projectrsacasariposo.sockets;

import com.drimtim.projectrsacasariposo.sockets.Utilities.CommandsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSocket {
    public static ClientSocket instance = new ClientSocket();

    private String username;
    private String ip;
    private int port;

    private int serverPort;
    private String serverIp;
    private Socket serverSocket;
    private BufferedReader in;
    private PrintWriter out;

    // chiavi

    public void configureServerSocket () throws IOException {
        serverSocket = new Socket(serverIp, serverPort);
        in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        out = new PrintWriter(serverSocket.getOutputStream(), true);

        out.println(username);

        String inputPortClient = in.readLine();
        ip = CommandsBuilder.getIpFromAck(inputPortClient);
        port = CommandsBuilder.getPortFromAck(inputPortClient);

        // start a continuous listening thread
        initializeListeningThread();
    }

    private void initializeListeningThread () {
        Thread threadListeningByClient = new Thread(()->{
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println("Ricevuto dal server: " + line);

                    if (CommandsBuilder.isACommand(line)) { // se è un comando lo gestisce come tale
                        String command = CommandsBuilder.getCommandPrefix(line);
                        System.out.println("command: " + command);

                        switch (command) {
                            case "pingRequest":
                                out.println(":pingAnswer:"+username +" | "+ ip + ":"+ port);
                                break;
                        }
                    }
                }
            } catch (IOException e) {
                if (e.getMessage().equals("Connection reset"))
                    System.err.println("Il server ["+username+"] ti ha disconnesso");
            }
        });
        threadListeningByClient.setName("threadListeningByClient");
        threadListeningByClient.start();

    }


    public void setUsername (String username) {
        this.username = username;
    }

    public void setServerPort (int port) {
        this.serverPort = port;
    }

    public void setServerIp (String ip) {
        this.serverIp = ip;
    }

}
