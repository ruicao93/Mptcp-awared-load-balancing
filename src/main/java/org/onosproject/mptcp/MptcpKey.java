package org.onosproject.mptcp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by cr on 16-12-20.
 */
public class MptcpKey {

    private byte[] key;

    public MptcpKey(byte[] key) {
        this.key = Arrays.copyOf(key, key.length);
    }

    public byte[] getKey() {
        return key;
    }

    public MptcpToken toToken() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] sha1 = md.digest(key);
            byte[] token = Arrays.copyOfRange(sha1, 0, 4);
            return new MptcpToken(token);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MptcpKey mptcpKey = (MptcpKey) o;

        return Arrays.equals(key, mptcpKey.key);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(key);
    }

    @Override
    public String toString() {
        return "MptcpKey{" +
                "key=" + Arrays.toString(key) +
                '}';
    }
}
