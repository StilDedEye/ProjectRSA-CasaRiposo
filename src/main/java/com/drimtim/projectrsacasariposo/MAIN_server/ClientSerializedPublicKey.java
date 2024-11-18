package com.drimtim.projectrsacasariposo.MAIN_server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public record ClientSerializedPublicKey(String username, int keyLength, byte[] serializedKey) {
}
