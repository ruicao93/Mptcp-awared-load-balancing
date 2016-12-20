package org.onosproject.mptcp;

import java.util.Arrays;

/**
 * Created by cr on 16-12-20.
 */
public class MptcpOption {

    public static final byte MPTCP_ENABLE = 0x30;
    public static final byte MPTCP_CAPABLE = 0x00;
    public static final byte MPTCP_JOIN = 0x01;

    private byte[] options;

    private boolean mptcpEnbaled = false;
    private boolean mptcpCapable = false;
    private boolean mptcpJoin = false;

    private byte kind;
    private byte subType;
    private byte[] senderKey;
    private byte[] token;


    private MptcpOption(byte[] options) {
        this.options = Arrays.copyOf(options, options.length);
    }

    public static MptcpOption parse(byte[] options) {
        MptcpOption mptcpOption = new MptcpOption(options);
        mptcpOption.parse();
        return null;
    }

    private void parse() {
        if (options.length > 20) {
            mptcpCapable = true;
        }
        kind = options[20];
        mptcpEnbaled = kind == MPTCP_ENABLE;
        subType = options[22];
        if (subType == MPTCP_CAPABLE) {
            mptcpCapable = true;
        } else if (subType == MPTCP_JOIN) {
            mptcpJoin = true;
        }
        if (!isMptcpEnabled()) return;
        if (hasMptcpCapable()) {
            senderKey = Arrays.copyOfRange(options, 24, 32);
        }
        if (hasMptcpJoin()) {
            token = Arrays.copyOfRange(options, 24, 28);
        }
    }

    public boolean isMptcpEnabled() {
        return mptcpEnbaled;
    }

    public boolean hasMptcpCapable() {
        return mptcpCapable;
    }

    public boolean hasMptcpJoin() {
        return mptcpJoin;
    }

    public byte[] getKey() {
        return new byte[0];
    }

    public byte[] getToken() {
        return new byte[0];
    }

    public void setMptcpEnbaled(boolean mptcpEnbaled) {
        this.mptcpEnbaled = mptcpEnbaled;
    }

    public void setMptcpCapable(boolean mptcpCapable) {
        this.mptcpCapable = mptcpCapable;
    }

    public void setMptcpJoin(boolean mptcpJoin) {
        this.mptcpJoin = mptcpJoin;
    }

    public void setSenderKey(byte[] key) {
        this.senderKey = key;
    }

    public void setToken(byte[] token) {
        this.token = token;
    }
}
