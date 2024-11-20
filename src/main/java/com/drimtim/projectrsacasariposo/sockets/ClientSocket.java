package com.drimtim.projectrsacasariposo.sockets;

import com.drimtim.projectrsacasariposo.MAIN_client.ClientKey;
import com.drimtim.projectrsacasariposo.MAIN_client.ControllerChatClient;
import com.drimtim.projectrsacasariposo.MAIN_client.ControllerChatSelection;
import com.drimtim.projectrsacasariposo.PRIMENUMBERS.PrimeFetcher;
import com.drimtim.projectrsacasariposo.sockets.Utilities.CommandsBuilder;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ClientSocket {
    public static ClientSocket instance = new ClientSocket();

    private String username;
    private String ip;
    private int port;

    public List<String> connectedClients = new ArrayList<String>(); // it saves only usernames

    private int serverPort;
    private String serverIp;
    private Socket serverSocket;
    private BufferedReader in;
    private PrintWriter out;
    private DataOutputStream dataOut;

    private ClientKey publicKey;
        private byte[] serializedPublicKey;
    private ClientKey privateKey;

    private ClientKey receiverPublicKey;
    public String receiverUsername;
    public Map<String, List<String>> allReceivedMessages = new HashMap<>();
    public Map<String, List<String>> allSentMessages = new HashMap<>();

    public boolean configureServerSocket () throws IOException {
        serverSocket = new Socket(serverIp, serverPort);
        in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream(), StandardCharsets.UTF_8));
        out = new PrintWriter(serverSocket.getOutputStream(), true, StandardCharsets.UTF_8);
        dataOut = new DataOutputStream(serverSocket.getOutputStream());

        out.println(username);
        if(in.readLine().equals(":CONNECTIONREFUSED:usernameAlreadyTaken")) {
            in = null;
            out = null;
            serverSocket = null;
            return false;
        } else System.out.println("CONNECTION ESTABLISHED with server " +serverIp + ":" +port);

        // GENERATE A PUBLIC AND PRIVATE KEY
        generatePrivatePublicKeys();
        // manda al server la propria chiave pubblica. Prima invia la lunghezza dell'array, poi l'array stesso
        dataOut.writeInt(serializedPublicKey.length);
        dataOut.write(serializedPublicKey);
        System.out.println("CHIAVE INVIATA AL SERVER");

        String inputIpPortClient = in.readLine(); // the client gets its own ip + port
        ip = CommandsBuilder.getIpFromAck(inputIpPortClient);
        port = CommandsBuilder.getPortFromAck(inputIpPortClient);
        // receive from the server the connected clients (usernames)
        String clientsAlreadyConnected = in.readLine();

        // according to clientsAlreadyConnected, parse with its own list
        parseConnectedClients (clientsAlreadyConnected);



        // start a listening thread with the server
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
                                out.println(":pingAnswer:" + username + " | " + ip + ":" + port);
                                break;

                            case "updatedClientsList":
                                parseConnectedClients(CommandsBuilder.getCommandSuffix(line));
                                if (ControllerChatSelection.instance != null)
                                    Platform.runLater(() -> ControllerChatSelection.instance.updateVboxChats());
                                break;
                            case "publicKeyIncoming":
                                //DataInputStream inData = new DataInputStream(serverSocket.getInputStream());
                                System.out.println("1");
                                int keyLength = Integer.parseInt(in.readLine());
                                System.out.println("1");
                                String serializedKeyBase64 = in.readLine();
                                byte[] serializedKey = Base64.getDecoder().decode(serializedKeyBase64); // decodifica Base64
                                System.out.println("Chiave serializzata: " + Arrays.toString(serializedKey));

                                // Deserializza la chiave
                                receiverPublicKey = ClientKey.deserializePublicKey(serializedKey);
                                System.err.println("Chiave ricevuta: " + receiverPublicKey.n());
                                break;
                        }
                    } else {
                        // prende username mittente
                        String senderUsername = CommandsBuilder.getMessageSenderUsername(line);
                        String restOfMessage  =  CommandsBuilder.getRestOfMessage(line);
                        String cypherText = restOfMessage.substring(restOfMessage.indexOf(":")+1);
                        String plainText = privateKey.decryptMessage(cypherText, privateKey.coprime(), privateKey.n());

                        // controlla se nella mappa dei messaggi ha già questo username
                        if (checkIfUsernameExistsInsideMessages (senderUsername)) {
                            // lo tiene, aggiorna la mappa
                            List<String> l = allReceivedMessages.get(senderUsername);
                            l.add("sentByOther:"+plainText);
                            allReceivedMessages.put(senderUsername, l);

                            // se si trova già nella chat, printa il messaggio
                            if (receiverUsername!=null && receiverUsername.equals(senderUsername)) {
                                // se è il client con il quale sta chattando, lo printa a schermo
                                Platform.runLater(()->{
                                    System.out.println("STO PRINTANDO");
                                    ControllerChatClient.instance.addMessage(plainText, false);
                                });
                            }
                        // se non esiste nella mappa il client, allora lo crea e aggiunge il messaggio
                        } else {
                            List <String> l = new ArrayList<>();
                            l.add("sentByOther:"+plainText);
                            allReceivedMessages.put(senderUsername, l);
                            if (receiverUsername!=null && receiverUsername.equals(senderUsername)) {
                                // se è il client con il quale sta chattando, lo printa a schermo
                                Platform.runLater(()->{
                                    ControllerChatClient.instance.addMessage(plainText, false);
                                });
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        threadListeningByClient.setName("threadListeningByClient");
        threadListeningByClient.start();

    }

    public void sendMessageToClient(String plainText) {
        if (checkIfUsernameExistsInsideMessages (receiverUsername)) {
            // lo tiene, aggiorna la mappa
            List<String> l = allReceivedMessages.get(receiverUsername);
            l.add("sentByMe:"+plainText);
            allReceivedMessages.put(receiverUsername, l);
        //se non esiste nella mappa il client, allora lo crea e aggiunge il messaggio
        } else {
            List <String> l = new ArrayList<>();
            l.add("sentByMe:"+plainText);
            allReceivedMessages.put(receiverUsername, l);
        }


        String finalMessage = receiverPublicKey.encryptMessage(plainText, receiverPublicKey.coprime(), receiverPublicKey.n());
        System.out.println("MESSAGGIO DA INVIARE: " + finalMessage);
        out.println("!"+receiverUsername +"!?"+username+"?:" + finalMessage);
        out.flush();
    }

    public void sendMessageToServer (String command) {
        out.println(command);
    }


    private void parseConnectedClients (String clientsListAsString) {
        connectedClients = new ArrayList<>(Arrays.asList(clientsListAsString.split(",")));
        connectedClients.remove(username);  // the client removes itself by the list
    }


    private void generatePrivatePublicKeys () {
        PrimeFetcher keyGenerator = new PrimeFetcher();
        ClientKey[] keys = keyGenerator.generateKeys();  // [public,private]
        publicKey = keys[0];
        privateKey = keys[1];
        // serializza la chiave pubblica e la salva nell'apposito attributo
        try {
            serializedPublicKey = publicKey.serializeKey();
        } catch (Exception e) {e.printStackTrace();}
    }

    public boolean checkIfUsernameExistsInsideMessages (String username) {
        return allReceivedMessages.get(username) != null;
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
