package com.drimtim.projectrsacasariposo.sockets;

import com.drimtim.projectrsacasariposo.MAIN_server.ClientSerializedPublicKey;
import com.drimtim.projectrsacasariposo.sockets.Utilities.CommandsBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ServSocket {
    private static int port = 1201;
    private static String ip = "localhost";

    public static ServerSocket serverSocket;

    // string --> {username, [map]} --> {"socket" socket, "in" in, "out" out}
    public static Map<String, Map<String, Object>> clients = new HashMap<>();
    public static List<String> connectedClients = new ArrayList<>(); // it saves only usernames
    public static List<ClientSerializedPublicKey> serializedClientsPublicKeys = new ArrayList<>();

    public void initializeListening () throws IOException {
        serverSocket = new ServerSocket(port);
        do {
            System.out.println("Listening for new clients...");
            Socket s = serverSocket.accept();
            System.out.println("New client detected {" + s.getRemoteSocketAddress() + "}");

            // Crea gli stream
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            DataInputStream inData = new DataInputStream(s.getInputStream());

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

            /* visto che la connessione è stata stabilita, ora riceverà la chiave pubblica dal client
            * e se la salverà, associandola all'username*/
            int keyLength = inData.readInt();
            System.out.println("LUNGHEZZA CHIAVE APPENA RICEVUTA: " + keyLength);
            byte[] serializedKey = new byte[keyLength];
            inData.readFully(serializedKey);
            System.out.println("CHIAVE SERIALIZZATA " +username+ " : " + Arrays.toString(serializedKey));
            serializedClientsPublicKeys.add(new ClientSerializedPublicKey(username, keyLength, serializedKey));

            // aggiunge l'username del client alla propria lista
            connectedClients.add(username);
            Map<String, Object> streams = new HashMap<>();
            streams.put("socket", s);
            streams.put("in", in);
            streams.put("out", out);

            // salva gli stream di quel client
            clients.put(username, streams);

            out.println(":giveAckIpPort:" + s.getLocalAddress() +"-"+s.getPort());
            out.println(":updatedClientsList:" + adaptClientListToPrint());
            sendBroadcastMessage (":updatedClientsList:" + adaptClientListToPrint());


// region THREADS & DAEMONS
            startListeningThread (username, in, out);
            // create and start a thread for each client
            //startPingRequestDaemon(5000, username, in, out);
// endregion

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
                            case "requestPublicKey":  // suffix = username
                                int index = getPublicKeyIndex(commandSuffix);
                                if (index == -1) {
                                    System.out.println("ERRORE INDICE = -1 " + index);
                                } else {
                                    ClientSerializedPublicKey pubk = serializedClientsPublicKeys.get(index);
                                    //DataOutputStream dataOut = new DataOutputStream(((Socket) clients.get(username).get("socket")).getOutputStream());
                                    out.println(":publicKeyIncoming:");
                                    out.flush();
                                    System.out.println("LUNGHEZZA CHIAVE CHE IO INVIO: " + pubk.keyLength());
                                    out.println(pubk.keyLength());
                                    out.flush();
                                    // Poi invia la chiave serializzata (come stringa codificata in Base64)
                                    String serializedKeyBase64 = Base64.getEncoder().encodeToString(pubk.serializedKey());
                                    out.println(serializedKeyBase64); // invia la chiave serializzata come stringa
                                    out.flush();
                                }
                                break;
                        }
                    } else if (CommandsBuilder.isAMessage(line)) {
                        String destinationUsername = CommandsBuilder.getMessageReceiver(line);
                        // trova il client destinatario e gira il messaggio
                        Socket socket = (Socket) clients.get(destinationUsername).get("socket");
                        PrintWriter out2 = new PrintWriter(socket.getOutputStream(), true);  // true per auto-flush
                        out2.println(line);
                        out2.flush();

                    }
                }
            } catch (IOException e) {
                if (e.getMessage().equals("Connection reset")) {
                    System.err.println("Il client ["+username+"] si è disconnesso");
                    // remove the client from any list
                    removeClientFromAnyList(username);

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

    private void removeClientFromAnyList (String username) {
        clients.remove(username);
        connectedClients.remove(username);
        serializedClientsPublicKeys.forEach(object -> {
            if (object.username().equals("aDW"))
                serializedClientsPublicKeys.remove(object);
        });
    }

    private int getPublicKeyIndex (String username) {
        for (ClientSerializedPublicKey serializedClientsPublicKey : serializedClientsPublicKeys) {
            if (serializedClientsPublicKey.username().equals(username)) {
                return serializedClientsPublicKeys.indexOf(serializedClientsPublicKey);
            }
        }
        return -1;
    }
}
