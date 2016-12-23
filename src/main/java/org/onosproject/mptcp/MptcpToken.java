package org.onosproject.mptcp;

import java.util.Arrays;

/**
 * Created by cr on 16-12-20.
 */
public class MptcpToken {
    private byte[] token;

    public MptcpToken(byte[] token) {
        this.token = Arrays.copyOf(token, token.length);
    }

    public byte[] getToken() {
        return token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MptcpToken that = (MptcpToken) o;

        return Arrays.equals(token, that.token);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(token);
    }

    @Override
    public String toString() {
        return "MptcpToken{" +
                "token=" + Arrays.toString(token) +
                '}';
    }
}
