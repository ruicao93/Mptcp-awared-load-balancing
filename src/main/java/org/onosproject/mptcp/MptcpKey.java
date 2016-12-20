package org.onosproject.mptcp;

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
}
