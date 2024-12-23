package com.drimtim.projectrsacasariposo.sockets;

import com.drimtim.projectrsacasariposo.MAIN_server.ClientSerializedPublicKey;
import com.drimtim.projectrsacasariposo.sockets.Utilities.CommandsBuilder;
import com.drimtim.projectrsacasariposo.sockets.Utilities.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Classe che gestisce la comunicazione lato server.
 */
public class ServSocket {
    /**
     * Porta utilizzata dal server per la comunicazione coi client
     */
    public static int port = 12122;
  
    /**
     * Istanza statica della classe ServSocket
     */
    public static ServSocket instance;

    /**
     * Socket del server per accettare richieste da parte degli utenti
     */
    public static ServerSocket serverSocket;

    // string --> {username, [map]} --> {"socket" socket, "in" in, "out" out}
    /**
     * Mappa contenente:<br>
     *  -Chiave: Username dell'utente<br>
     *  -Campo: Mappa contenente il socket e gli stream di input e output dell'utente
     */
    public static Map<String, Map<String, Object>> clients = new HashMap<>();

    /**
     * Lista contenente tutti gli username degli utenti attualmente connessi
     */
    public static List<String> connectedClients = new ArrayList<>(); // it saves only usernames

    /**
     * Lista delle chiavi sotto forma di array di byte degli utenti
     */
    public static List<ClientSerializedPublicKey> serializedClientsPublicKeys = new ArrayList<>();

    public ServSocket () throws IOException {
        instance = this;
        initializeListening();
    }

    /**
     * Riceve richieste di connessioni da parte degli utenti, rifiutandole se l'username inserito è già stato utilizzato<br>
     * Una volta stabilita la connessione con l'utente riceve la sua chiave pubblica
     * @throws IOException
     */
    public void initializeListening () throws IOException {
        serverSocket = new ServerSocket(port);
        do {
            Logger.log("listening for new clients...", Logger.SERVER);
            Socket s = serverSocket.accept();
            Logger.log("new client detected {" + s.getRemoteSocketAddress().toString().substring(1) + "}", Logger.SERVER);

            // Crea gli stream
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            DataInputStream inData = new DataInputStream(s.getInputStream());

            PrintWriter out = new PrintWriter(s.getOutputStream(), true, StandardCharsets.UTF_8);

            // the first print from the client is the username
            String username = in.readLine();

            /*If the client gives an already taken username, the connection is refused*/
            if (clients.containsKey(username)) {
                Logger.log("username{"+username+"} already used, client disconnected", Logger.SERVER);
                out.println(":CONNECTIONREFUSED:usernameAlreadyTaken");
                s.close();
                continue;
            }
            out.println(":CONNECTIONESTABLISHED:");

            /* visto che la connessione è stata stabilita, ora riceverà la chiave pubblica dal client
            * e se la salverà, associandola all'username*/
            int keyLength = inData.readInt();
            Logger.log("client{"+username+"} key length -> " + keyLength, Logger.CLIENT);
            byte[] serializedKey = new byte[keyLength];
            inData.readFully(serializedKey);
            Logger.log("serialized public key client{" +username+ "} -> " + Arrays.toString(serializedKey), Logger.CLIENT);
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


    /**
     * Inizializza il Thread per ascoltare messaggi in arrivo dai client<br>
     * Classifica il messaggio in base al suo prefisso:<br>
     *  - Comandi (':'):
     *      - pingRequest: Richiesta e risposta di ping<br>
     *      - publicKeyIncoming: Ricezione della chiave da parte di un client<br>
     *  - Messaggi ('!')
     * @throws IOException
     */
    private void startListeningThread (String username, BufferedReader in, PrintWriter out) {
        Thread threadListeningByServer = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    Logger.log("message by client -> " + line, Logger.CLIENT);

                    if (CommandsBuilder.isACommand(line)) { // se è un comando lo gestisce come tale
                        String commandPrefix = CommandsBuilder.getCommandPrefix(line);
                        String commandSuffix = CommandsBuilder.getCommandSuffix(line);
                        Logger.log("extracting command... ", Logger.SERVER);
                        Logger.log("extracted command : " + commandPrefix, Logger.SERVER);

                        switch (commandPrefix) {
                            case "pingAnswer":
                                Logger.log("ping answer by -> " + commandSuffix, Logger.SERVER);
                                break;
                            case "requestPublicKey":  // suffix = username
                                int index = getPublicKeyIndex(commandSuffix);
                                if (index == -1) {
                                    Logger.log("ERROR -> couldn't send public key because username{" + commandSuffix +"} doesn't exist", Logger.SERVER);
                                } else {
                                    ClientSerializedPublicKey pubk = serializedClientsPublicKeys.get(index);
                                    //DataOutputStream dataOut = new DataOutputStream(((Socket) clients.get(username).get("socket")).getOutputStream());
                                    out.println(":publicKeyIncoming:");
                                    out.flush();
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
                    Logger.log("client{"+username+"} has disconnected from the server", Logger.SERVER);
                    Logger.log("removing client{"+username+"} from lists...", Logger.SERVER);

                    // remove the client from any list
                    removeClientFromAnyList(username);

                    sendBroadcastMessage(":updatedClientsList:" + adaptClientListToPrint());

                    Logger.log("client{"+username+"} successfully deleted", Logger.SERVER);
                } else {
                    Logger.log(getStackTraceAsString(e), Logger.EXCEPTION);
                }
            }
        });
        threadListeningByServer.setName("threadListeningByServer");
        threadListeningByServer.start();
    }

    /**
     * Invia una richiesta di ping a un utente
     * @deprecated Utilizzato per debugging
     * @param millis intervallo tra le richieste in millisecondi
     * @param username username dell'utente
     * @param in stream di input dell'utente
     * @param out stream di output dell'utente
     */
    private void startPingRequestDaemon (long millis, String username, BufferedReader in, PrintWriter out) {
        Thread threadPingRequestDaemon = new Thread(()->{
            while (true) {
                try {
                    Thread.sleep(millis);
                    out.println(":pingRequest:");
                } catch (InterruptedException e) {
                    Logger.log(getStackTraceAsString(e), Logger.EXCEPTION);
                }
            }
        });
        threadPingRequestDaemon.setName("threadPingRequestDaemon");
        threadPingRequestDaemon.start();
    }

    /**
     * Formatta la lista degli username di client in una stringa in formato csv
     * @return Stringa csv degli username dei client
     */
    private String adaptClientListToPrint () {
        StringBuilder output = new StringBuilder();  // it should be "username1,username2,username3,username4,... etc"
        if (connectedClients.isEmpty())
            return "";
        for (String client: connectedClients) {
            output.append(client).append(",");
        }
        return output.toString();
    }

    /**
     * Invia un messaggio a tutti gli utenti connessi
     * @param message Messaggio da inviare
     */
    private void sendBroadcastMessage (String message) {
        for (String username: connectedClients) {
            PrintWriter out = (PrintWriter) clients.get(username).get("out");
            out.println(message);
        }
    }

    /**
     * Rimuove l'utente dalla lista degli username degli utenti
     * @param username Username da rimuovere
     */
    private void removeClientFromAnyList (String username) {
        clients.remove(username);
        connectedClients.remove(username);
        serializedClientsPublicKeys.removeIf(object -> object.username().equals(username));
    }

    /* se ritorna -1, vuol dire che l'username del client del quale si vuole sapere la chiave
    non è registrato nel server*/
    /**
     * Ritorna l'indice della chiave di un utente nella di chiavi in base al suo username
     * @param username Username dell'utente
     * @return Chiave dell'utente se l'utente è presente nella lista<br>-1 se l'utente non è presente nella lista
     */
    private int getPublicKeyIndex (String username) {
        for (ClientSerializedPublicKey serializedClientsPublicKey : serializedClientsPublicKeys) {
            if (serializedClientsPublicKey.username().equals(username)) {
                return serializedClientsPublicKeys.indexOf(serializedClientsPublicKey);
            }
        }
        return -1;
    }

    /**
     * Ritorna il messaggio di un'eccezione come stringa
     * @param e Eccezione
     * @return Messaggio dell'eccezione come stringa
     */
    private static String getStackTraceAsString(Exception e) {
        // Usa StringWriter e PrintWriter per catturare lo stack trace
        /* lo stringwriter si basa su un buffer, nel quale viene inserito lo stacktrace
         * e dal contenuto del buffer si può passare poi ad una stringa effettiva*/
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw); // Scrive lo stack trace nel PrintWriter
        return sw.toString();  // Converte il contenuto dello StringWriter in una stringa
    }

    /**
     * Interrompe il server
     * @throws IOException
     */
    public void closeServer () throws IOException {
        clients.clear();
        connectedClients.clear();
        serializedClientsPublicKeys.clear();
        sendBroadcastMessage (":updatedClientsList:" + adaptClientListToPrint());
        serverSocket.close();
    }
}
