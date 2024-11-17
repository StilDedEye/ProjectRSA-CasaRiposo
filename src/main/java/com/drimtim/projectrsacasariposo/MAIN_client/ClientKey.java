package com.drimtim.projectrsacasariposo.MAIN_client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public record ClientKey(BigInteger coprime, BigInteger n) {

    /*Serializza la chiave. Questo la rende adatta ad essere trasmessa lungo lo stream di output e
    * facilita i vari processi di lettura. Basta un solo invio per inviare sia il coprimo sia n ed
    * il formato di invio è universale (array di byte)*/
    public byte[] serializeKey() throws IOException {
        // Converte n ed il coprimo in array di byte
        byte[] nBytes = n.toByteArray();
        byte[] coprimeBytes = coprime.toByteArray();

        // Utilizzo di ByteArrayOutputStream per concatenare i byte
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(nBytes.length);  // scrive la lunghezza di n
        byteArrayOutputStream.write(nBytes); // scrive n
        byteArrayOutputStream.write(coprimeBytes.length);  // scrive la lunghezza del coprimo
        byteArrayOutputStream.write(coprimeBytes); // scrive il coprimo

        return byteArrayOutputStream.toByteArray();
    }

    public ClientKey deserializePublicKey(int keyLength, byte[] key) {
        // Deserializza la chiave (n, coprime)
        int nLength = key[0];  // Lunghezza di n
        byte[] nBytes = new byte[nLength];

        /*Copia un determinato numero di byte da un array all'altro.
        * Iniziamo da key[1] perchè key[0] contiene la lunghezza di n.
        * Li copiamo in nBytes a partire dalla posizione 0*/
        System.arraycopy(key, 1, nBytes, 0, nLength);
        BigInteger n = new BigInteger(nBytes);

        int eLength = key[nLength + 1];  // Lunghezza del coprime
        byte[] eBytes = new byte[eLength];
        System.arraycopy(key, nLength + 2, eBytes, 0, eLength);
        BigInteger e = new BigInteger(eBytes);

        return new ClientKey(e,n);
    }
}
