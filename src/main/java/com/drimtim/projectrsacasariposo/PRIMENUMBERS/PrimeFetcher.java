package com.drimtim.projectrsacasariposo.PRIMENUMBERS;

import com.drimtim.projectrsacasariposo.MAIN_client.ClientKey;

import java.math.BigInteger;
import java.security.SecureRandom;

public class PrimeFetcher {


    public ClientKey[] generateKeys () {
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

        return new ClientKey[]{new ClientKey (e,n), new ClientKey (d,n)};
    }

    public static void main(String[] args) {
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


        String message = "Alice che invia sex a Bob";
        System.out.println("\n\n\n\nMessaggio in chiaro: " + message);
        BigInteger mBytes = new BigInteger(message.getBytes());
        System.out.println("Messaggio in bytes: " + mBytes);

        // Cifrato = Messaggio^e mod n
        BigInteger cifrato = mBytes.modPow(e,n);
        System.out.println("Messaggio cifrato: " + cifrato);

        // Decifrato = Messaggio^d mod n
        BigInteger decifrato = cifrato.modPow(d,n);
        String decMessage = new String(decifrato.toByteArray());
        System.err.println("Messaggio decifrato: " + decMessage);
    }

    // Metodo per trovare un e coprimo con m
    public static BigInteger findCoprime(BigInteger m) {
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