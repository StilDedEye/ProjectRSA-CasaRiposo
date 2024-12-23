package com.drimtim.projectrsacasariposo.MAIN_client;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
* Record che memorizza una generica chiave del client
* @param coprime rappresenta "e" nella chiave pubblica o "d" nella chiave privata
* @param n il termine comune tra la chiave pubblica e privata
* */
public record ClientKey(BigInteger coprime, BigInteger n) {
    /**
    * Serializza la chiave comprendente i due numeri in un array di byte.
    * L'array è composto da 4 byte che indicano la lunghezza in byte di n, seguiti dai byte di n.
    * In seguito altri 4 byte indicano la grandezza dell'altro numero, mentre i restanti byte rappresentano il numero.
    * @throws IOException
    * @return l'array di byte contenente i due numeri che compongono la chiave
    * */
    // Serializzazione semplificata con DataOutputStream
    public byte[] serializeKey() throws IOException {
        /* conterrà l'array finale di byte  (esempio = [10,50,200,240]. Ogni numero è rappresentato da
        1 byte, 8 bit, 2^8=256 (da -127 a 128)*/
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // usata per costruire l'array di byte in modo semplice
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);


        byte[] nBytes = n.toByteArray(); // trasforma n in byte array
        dataOutputStream.writeInt(nBytes.length);  // scrive la lunghezza come int (4 byte occupati)

        /* la chiave si genera da numeri a 2048 bit = 256 byte minimo. Come numero, 256 più, si rappresenta benissimo
        con lunghezza di 4 bytes*/
        dataOutputStream.write(nBytes);  // scrive i dati di n


        byte[] coprimeBytes = coprime.toByteArray();
        dataOutputStream.writeInt(coprimeBytes.length);  // scrive la lunghezza come int (4 byte)
        dataOutputStream.write(coprimeBytes);  // scrive i dati del coprimo

        // Ritorna l'array di byte
        return byteArrayOutputStream.toByteArray();
    }

    /**
    * Traduce l'array di byte nei due numeri che compongono la chiave
    * @param key l'array di byte da deserializzare
    * @throws IOException
    * @return l'oggetto ClientKey contenente la chiave
    * */
    public static ClientKey deserializePublicKey(byte[] key) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(key);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

        int nLength = dataInputStream.readInt();  // legge la lunghezza di n
        byte[] nBytes = new byte[nLength];
        dataInputStream.readFully(nBytes);  // legge i dati di n
        BigInteger n = new BigInteger(nBytes);

        int coprimeLength = dataInputStream.readInt();  // legge la lunghezza del coprimo
        byte[] coprimeBytes = new byte[coprimeLength];
        dataInputStream.readFully(coprimeBytes);  // legge i dati del coprimo
        BigInteger coprime = new BigInteger(coprimeBytes);

        // restituisce l'oggetto ClientKey
        return new ClientKey(coprime, n);
    }

    /**
    * Cifra il messaggio usando la chiave pubblica
    * @param message il messaggio da cifrare
    * @param e il coprimo
    * @param n il numero con cui va calcolato il modulo
    * @return l'array di byte contenente il messaggio cifrato codificato sottoforma di stringa
    *
    * */
    public String encryptMessage(String message, BigInteger e, BigInteger n) {
        // Traduce il messaggio prima in byte, poi converte i byte in un BigInteger
        BigInteger plainText = new BigInteger(1, message.getBytes(StandardCharsets.UTF_8));

        BigInteger cypherText = plainText.modPow(e, n);
        byte[] cypherBytes = cypherText.toByteArray();

        // Usa Base64 per codificare l'array di byte in una stringa sicura per la trasmissione
        return Base64.getEncoder().encodeToString(cypherBytes);
    }

    /**
    * Decifra il messaggio usando la chiave privata
    * @param cypherMessage il messaggio cifrato
    * @param d il numero a cui va elevato il messaggio
    * @param n il numero con cui va calcolato il modulo
    * @return il messaggio in chiaro
    * */
    public String decryptMessage(String cypherMessage, BigInteger d, BigInteger n) {
        // Decodifica il messaggio da Base64
        byte[] cypherBytes = Base64.getDecoder().decode(cypherMessage);

        // Converte i byte in un BigInteger
        BigInteger cypherText = new BigInteger(1, cypherBytes);

        // Decifra il messaggio con modPow
        BigInteger decrypted = cypherText.modPow(d, n);

        // Ritorna il messaggio in chiaro come stringa
        return new String(decrypted.toByteArray(), StandardCharsets.UTF_8);
    }

    /*
     * Utilizziamo il costruttore BigInteger(int signum, byte[] magnitude)
     * per garantire che i byte vengano trattati come un numero positivo,
     * indipendentemente dai valori di byte che potrebbero rappresentare
     * caratteri accentati in UTF-8 (che possono avere byte negativi).
     *
     * Il costruttore standard BigInteger(byte[] val) interpreta i byte come
     * un valore con segno, cioè usa il complemento a due per determinare
     * se il numero è positivo o negativo. Ad esempio, un array di byte
     * che rappresenta il carattere 'à' (in UTF-8) potrebbe contenere valori
     * come -61 e -95, che potrebbero essere interpretati come un numero negativo.
     *
     * Passando 1 come parametro "signum", forziamo BigInteger a trattare
     * il valore come positivo, evitando problemi di interpretazione errata
     * e garantendo una corretta gestione dei dati durante la crittografia e
     * la decodifica.
     */
}




