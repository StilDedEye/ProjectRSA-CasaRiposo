package com.drimtim.projectrsacasariposo.MAIN_client;

import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;

public record ClientKey(BigInteger coprime, BigInteger n) {

    /* Serializza la chiave. Questo la rende adatta ad essere trasmessa lungo lo stream di output e
     * facilita i vari processi di lettura. Basta un solo invio per inviare sia il coprimo sia n ed
     * il formato di invio è universale (array di byte) */
    // Serializzazione semplificata con DataOutputStream
    public byte[] serializeKey() throws IOException {
        /* conterrà l'array finale di byte  (esempio = [10,50,200,240]. Ogni numero è rappresentato da
        1 byte, 8 bit, 2^8=256 (da 0 a 255)*/
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
}




