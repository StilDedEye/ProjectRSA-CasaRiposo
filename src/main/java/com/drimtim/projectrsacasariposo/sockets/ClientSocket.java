package com.drimtim.projectrsacasariposo.sockets;

import com.drimtim.projectrsacasariposo.MAIN_client.*;
import com.drimtim.projectrsacasariposo.PRIMENUMBERS.PrimeFetcher;
import com.drimtim.projectrsacasariposo.sockets.Utilities.CommandsBuilder;
import com.drimtim.projectrsacasariposo.sockets.Utilities.Utilities;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.controlsfx.control.Notifications;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

/**
 * Classe che gestisce la comunicazione lato client.
 */
public class ClientSocket {
    /**
     * Istanza statica della classe ClientSocket
     */
    public static ClientSocket instance = new ClientSocket();

    /**
     * Username dell'utente
     */
    private String username;

    /**
     * Indirizzo IP dell'utente
     */
    private String ip;

    /**
     * Porta utilizzata dall'utente per la comunicazione col server
     */
    private int port;

    /**
     * Lista degli username di tutti gli utenti connessi al server
     */
    public List<String> connectedClients = new ArrayList<String>(); // it saves only usernames

    /**
     * Porta utilizzata dal server
     */
    private int serverPort;

    /**
     * Indirizzo IP del server
     */
    private String serverIp;

    /**
     * Socket composto da indirizzo IP e porta del server
     */
    private Socket serverSocket;

    private BufferedReader in;
    private PrintWriter out;

    /**
     * Stream utilizzato per la trasmissione dei dati relativi alle chiavi
     */
    private DataOutputStream dataOut;

    /**
     * Chiave pubblica dell'utente
     */
    private ClientKey publicKey;

    /**
     * Chiave pubblica dell'utente sotto forma di Array di Byte.
     * La chiave viene trasformata in Array di Byte per poterla inviare
     */
    private byte[] serializedPublicKey;

    /**
     * Chiave privata dell'utente
     */
    private ClientKey privateKey;

    /**
     * Chiave pubblica dell'utente con cui si sta comunicando
     */
    private ClientKey receiverPublicKey;

    /**
     * Username dell'utente con cui si sta comunicando
     */
    public String receiverUsername;

    /**
     * Mappa contenente:<br>
     * - Chiave: Username dell'utente<br>
     * - Campo: Messaggi inviati e ricevuti da quell'utente<br>
     */
    public Map<String, List<String>> allMessages = new HashMap<>();

    /**
     * Istanzia il socket per la comunicazione col server:<br>
     *  - Invia lo username al server<br>
     *  - Genera la chiave pubblica e privata<br>
     *  - Invia la chiave pubblica al server<br>
     *  - Riceve dal server la lista di tutti i client connessi
     * @return True se l'username scelto non è già stato utilizzato<br>
     * False se l'username scelto è già stato utilizzato
     * @throws IOException
     */
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

    /**
     * Inizializza il Thread per ascoltare messaggi in arrivo dal server o da altri client<br>
     * Classifica il messaggio in base al suo prefisso:<br>
     *  - Comandi (':'):
     *      - pingRequest: Richiesta e risposta di ping<br>
     *      - updatedClientList: Lista degli username aggiornata<br>
     *      - publicKeyIncoming: Ricezione della chiave da parte di un client<br>
     *  - Messaggi ('!')
     */
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
                            List<String> l = allMessages.get(senderUsername);
                            l.add("sentByOther:"+plainText);
                            allMessages.put(senderUsername, l);


                            // se si trova già nella chat, printa il messaggio
                            if (receiverUsername!=null && receiverUsername.equals(senderUsername)) {
                                // se è il client con il quale sta chattando, lo printa a schermo
                                Platform.runLater(()->{
                                    System.out.println("STO PRINTANDO");
                                    ControllerChatClient.instance.addMessage(plainText, false);
                                });
                            } else {
                                sendNotification(senderUsername, plainText);
                            }
                        // se non esiste nella mappa il client, allora lo crea e aggiunge il messaggio
                        } else {
                            List <String> l = new ArrayList<>();
                            l.add("sentByOther:"+plainText);
                            allMessages.put(senderUsername, l);
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
                if (e.getMessage().equals("Connection reset")) {
                    Platform.runLater(() -> ControllerChatSelection.instance.updateVboxChats());
                    parseConnectedClients(username);

                    Platform.runLater(()->{
                        FXMLLoader fxmlLoader = new FXMLLoader(ControllerClientSplash.class.getResource("/com/drimtim/projectrsacasariposo/client/clientSplash.fxml"));
                        VBox vBox = null;
                        try {
                            vBox = fxmlLoader.load();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        Scene scene = new Scene(vBox);

                        MainClient.primaryStage.setTitle("Client");
                        MainClient.primaryStage.setScene(scene);
                        MainClient.currentScene=scene;
                        MainClient.primaryStage.setResizable(false);
                    });
                }
            }
        });
        threadListeningByClient.setName("threadListeningByClient");
        threadListeningByClient.start();

    }

    /**
     * Cripta il messaggio<br>
     * Salva il messaggio nella lista allMessages <br>
     * Invia il messaggio con un prefisso:
     * <blockquote>
     * <pre>
     * out.println("!"+usernameDestinatario +"!?"+usernameMittente+"?:" + messaggioCifrato);
     * </pre>
     * </blockquote>
     * @param plainText Testo in chiaro del messaggio
     */
    public void sendMessageToClient(String plainText) {
        if (checkIfUsernameExistsInsideMessages (receiverUsername)) {
            // lo tiene, aggiorna la mappa
            List<String> l = allMessages.get(receiverUsername);
            l.add("sentByMe:"+plainText);
            allMessages.put(receiverUsername, l);
        //se non esiste nella mappa il client, allora lo crea e aggiunge il messaggio
        } else {
            List <String> l = new ArrayList<>();
            l.add("sentByMe:"+plainText);
            allMessages.put(receiverUsername, l);
        }


        String finalMessage = receiverPublicKey.encryptMessage(plainText, receiverPublicKey.coprime(), receiverPublicKey.n());
        System.out.println("MESSAGGIO DA INVIARE: " + finalMessage);
        out.println("!"+receiverUsername +"!?"+username+"?:" + finalMessage);
        out.flush();
    }

    /**
     * Invia comunicazioni al server
     * @param message Messaggio da inviare
     */
    public void sendMessageToServer (String message) {
        out.println(message);
    }

    /**
     * Riceve la lista degli utenti connessi dal server ed aggiorna quella attuale
     * @param clientsListAsString Stringa contente tutti gli username degli utenti connessi separati da: ','
     */
    private void parseConnectedClients (String clientsListAsString) {
        connectedClients = new ArrayList<>(Arrays.asList(clientsListAsString.split(",")));
        connectedClients.remove(username);  // the client removes itself by the list
    }

    /**
     * Genera la chiave pubblica e privata
     */
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

    /**
     * Controlla se lo username è già presente nella lista dei messaggi
     * @param username Username da controllare
     * @return <blockquote><pre>allMessages.get(username) != null</pre></blockquote>
     */
    public boolean checkIfUsernameExistsInsideMessages (String username) {
        return allMessages.get(username) != null;
    }

    /**
     * Mostra la notifica alla ricezione di un messaggio
     * @param senderUsername Username del mittente
     * @param plainText Messaggio in chiaro
     */
    private void sendNotification (String senderUsername, String plainText) {
        Platform.runLater(() -> {
            Notifications newMessageNotification = Notifications.create();
            newMessageNotification.position(Pos.BASELINE_RIGHT);
            newMessageNotification.title("Messaggio da " + senderUsername);
            String message = plainText;
            // inserisce testo nella notifica ed eventualmente lo taglia se troppo lungo
            if (plainText.length()>25)
                message = plainText.substring(25) + "...";

            newMessageNotification.text(message);
            Image profilePic = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/drimtim/projectrsacasariposo/other/interface/defaultProfilePic/pic" + Utilities.getNumberFromUsername(senderUsername) + ".png")));
            ImageView imgViewProfilePic = new ImageView(profilePic);
            imgViewProfilePic.setFitWidth(50);
            imgViewProfilePic.setFitHeight(50);
            newMessageNotification.graphic(imgViewProfilePic);
            newMessageNotification.show();

            Utilities.playSound(plainText);
        });
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
