package com.drimtim.projectrsacasariposo.PRIMENUMBERS;

import com.drimtim.projectrsacasariposo.MAIN_client.ClientKey;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;

/**
* Genera i numeri primi p e q e da essi ricava le chiavi pubblica e privata
* @author Drim Studios
* */
public class PrimeFetcher {

    /**
     * Genera la chiave pubblica (e,n) e la chiave privata (d,n)
     *
     * @return il vettore di record di tipo ClientKey contenente le due chiavi
     */
    public ClientKey[] generateKeys() {
        SecureRandom random = new SecureRandom();


        // Generazione numeri primi p e q
        BigInteger p = BigInteger.probablePrime(2048, random);
        BigInteger q = BigInteger.probablePrime(2048, random);

        // Calcolo n=p*q ed m=(p-1)*(q-1)
        BigInteger n = p.multiply(q);
        BigInteger m = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        // Calcolo e, coprimo con m (1<e<m)
        BigInteger e = findCoprime(m);

        // Calcolo d. La classe biginter già prevede il suo calcolo
        BigInteger d = e.modInverse(m);

        // [E,N] chiave pubblica | [D,N] chiave privata
        System.out.println("N: " + n);
        System.out.println("E: " + e);
        System.out.println("D: " + d);

        return new ClientKey[]{new ClientKey(e, n), new ClientKey(d, n)};
    }

    public static void main(String[] args) {

    }

    /**
     * Calcola il coprimo e. In genere controlla se 65537 è coprimo di m, in caso contrario aggiunge 2 a 65537 finchè non diventa coprimo.
     * @param m Il numero su cui va calcolato il coprimo
     * @return il coprimo, di tipo BigInteger
     * */
    public static BigInteger findCoprime (BigInteger m){
        BigInteger e = BigInteger.valueOf(65537); // Valore standard usato nella maggior parte dei casi
        if (!m.gcd(e).equals(BigInteger.ONE)) { // calcolo MCD tra e ed m, se esce 1 va bene come coprimo
            while (!m.gcd(e).equals(BigInteger.ONE)) {
                e = e.add(BigInteger.TWO); /*si incrementa 65537 di 2, facendolo rimanere dispari,
                                            così sicuramente non sarà divisibile per 2*/
            }
        }
        return e; // e che viene ritornato sarà sicuramente coprimo con m
    }
}