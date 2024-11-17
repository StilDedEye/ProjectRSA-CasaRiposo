package com.drimtim.projectrsacasariposo.MAIN_server;

public record ClientSerializedPublicKey(String username, int keyLength, byte[] serializedKey) {
}
