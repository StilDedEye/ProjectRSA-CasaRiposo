package com.drimtim.projectrsacasariposo.sockets;

import com.drimtim.projectrsacasariposo.MAIN_client.ControllerChatSelection;
import com.drimtim.projectrsacasariposo.sockets.Utilities.CommandsBuilder;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.management.PlatformManagedObject;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClientSocket {
    public static ClientSocket instance = new ClientSocket();

    private String username;
    private String ip;
    private int port;

    private String destinationUsername;

    public List<String> connectedClients = new ArrayList<String>(); // it saves only usernames

    private int serverPort;
    private String serverIp;
    private Socket serverSocket;
    private BufferedReader in;
    private PrintWriter out;

    // TODO mettere chiavi

    public boolean configureServerSocket () throws IOException {
        serverSocket = new Socket(serverIp, serverPort);
        in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        out = new PrintWriter(serverSocket.getOutputStream(), true);

        out.println(username);
        if(in.readLine().equals(":CONNECTIONREFUSED:usernameAlreadyTaken")) {
            in = null;
            out = null;
            serverSocket = null;
            return false;
        } else System.out.println("CONNECTION ESTABLISHED with server " +serverIp + ":" +port);

        String inputIpPortClient = in.readLine(); // the client gets its own ip + port
        ip = CommandsBuilder.getIpFromAck(inputIpPortClient);
        port = CommandsBuilder.getPortFromAck(inputIpPortClient);

        String clientsAlreadyConnected = in.readLine();

        System.err.println(clientsAlreadyConnected);
        parseConnectedClients (clientsAlreadyConnected);

        System.err.println(Arrays.toString(connectedClients.toArray()));

        // start a continuous listening thread
        initializeListeningThread();

        return true;
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

                            case "updatedClientsList":
                                parseConnectedClients(CommandsBuilder.getCommandSuffix(line));
                                if (ControllerChatSelection.instance!=null)
                                    Platform.runLater(()->ControllerChatSelection.instance.updateVboxChats());

                        }
                    }
                }
            } catch (IOException e) {
                if (e.getMessage().equals("Connection reset"))
                    System.err.println("Sei stato disconnesso dal server.");
            }
        });
        threadListeningByClient.setName("threadListeningByClient");
        threadListeningByClient.start();

    }

    public void sendMessage (String message) {
        out.println("MESSAGGIO: " + message);  // TODO  PASSARE USERNAME DESTINATARIO
    }

    private void parseConnectedClients (String clientsListAsString) {
        connectedClients = new ArrayList<>(Arrays.asList(clientsListAsString.split(",")));
        connectedClients.remove(username);  // the client removes itself by the list
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
