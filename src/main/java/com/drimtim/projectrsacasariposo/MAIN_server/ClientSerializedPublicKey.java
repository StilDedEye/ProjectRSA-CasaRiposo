package com.drimtim.projectrsacasariposo.MAIN_server;

/**
 * Record che memorizza la chiave pubblica del client sotto forma di vettore di byte
 * @param username username del client
 * @param keyLength lunghezza della chiave
 * @param serializedKey chiave sotto forma di array di byte
 */
public record ClientSerializedPublicKey(String username, int keyLength, byte[] serializedKey) {
}
