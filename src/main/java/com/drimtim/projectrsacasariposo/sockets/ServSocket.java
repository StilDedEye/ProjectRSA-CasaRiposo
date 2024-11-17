package com.drimtim.projectrsacasariposo.sockets;

import com.drimtim.projectrsacasariposo.sockets.Utilities.CommandsBuilder;

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

    // string --> {username, [map]} --> {"socket" socket, "in" in, "out" out}
    public static Map<String, Map<String, Object>> clients = new HashMap<>();
    public static List<String> connectedClients = new ArrayList<String>(); // it saves only usernames


    public void initializeListening () throws IOException {
        serverSocket = new ServerSocket(port);
        do {
            System.out.println("Listening for new clients...");
            Socket s = serverSocket.accept();
            System.out.println("New client detected {" + s.getRemoteSocketAddress() + "}");

            // Crea gli stream
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);

            // the first print from the client is the username
            String username = in.readLine();

            /*If the client gives an already taken username, the connection is refused*/
            if (clients.containsKey(username)) {
                System.out.println("Username already used, client disconnected.");
                out.println(":CONNECTIONREFUSED:usernameAlreadyTaken");
                s.close();
                continue;
            }
            out.println(":CONNECTIONESTABLISHED:");

            connectedClients.add(username);
            Map<String, Object> streams = new HashMap<>();
            streams.put("socket", s);
            streams.put("in", in);
            streams.put("out", out);

            clients.put(username, streams);


// region THREADS & DAEMONS
            // create and start a thread for each client
            startListeningThread (username, in, out);
            startPingRequestDaemon(5000, username, in, out);
// endregion



            out.println(":giveAckIpPort:" + s.getLocalAddress() +"-"+s.getPort());
            out.println(":updatedClientsList:" + adaptClientListToPrint());
            sendBroadcastMessage (":updatedClientsList:" + adaptClientListToPrint());
            out.println(":pingRequest:");

        } while (true);
    }


    private void startListeningThread (String username, BufferedReader in, PrintWriter out) {
        Thread threadListeningByServer = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println("/t/tRicevuto dal client: | " + line);

                    if (CommandsBuilder.isACommand(line)) { // se è un comando lo gestisce come tale
                        String commandPrefix = CommandsBuilder.getCommandPrefix(line);
                        String commandSuffix = CommandsBuilder.getCommandSuffix(line);
                        System.out.println("command: " + commandPrefix);

                        switch (commandPrefix) {
                            case "pingAnswer":
                                System.err.println("Ping executed successfully with " + commandSuffix);
                                break;
                        }
                    }
                }
            } catch (IOException e) {
                if (e.getMessage().equals("Connection reset")) {
                    System.err.println("Il client ["+username+"] si è disconnesso");
                    clients.remove(username);
                    connectedClients.remove(username);

                    sendBroadcastMessage(":updatedClientsList:" + adaptClientListToPrint());
                }


            }
        });
        threadListeningByServer.setName("threadListeningByServer");
        threadListeningByServer.start();
    }

    private void startPingRequestDaemon (long millis, String username, BufferedReader in, PrintWriter out) {
        Thread threadPingRequestDaemon = new Thread(()->{
            while (true) {
                try {
                    Thread.sleep(millis);
                    out.println(":pingRequest:");
                } catch (InterruptedException e) {throw new RuntimeException(e);}
            }
        });
        threadPingRequestDaemon.setName("threadPingRequestDaemon");
        threadPingRequestDaemon.start();
    }

    private String adaptClientListToPrint () {
        StringBuilder output = new StringBuilder();  // it should be "username1,username2,username3,username4,... etc"
        if (connectedClients.isEmpty())
            return "";
        for (String client: connectedClients) {
            output.append(client).append(",");
        }
        return output.toString();
    }

    private void sendBroadcastMessage (String message) {
        for (String username: connectedClients) {
            PrintWriter out = (PrintWriter) clients.get(username).get("out");
            out.println(message);
        }
    }
}
