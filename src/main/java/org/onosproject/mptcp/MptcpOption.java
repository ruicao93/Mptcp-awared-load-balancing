package org.onosproject.mptcp;

import java.util.Arrays;

/**
 * Created by cr on 16-12-20.
 */
public class MptcpOption {

    public static final byte MPTCP_ENABLE = 0x1e;
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
        if (null != options && options.length > 0) {
            this.options = Arrays.copyOf(options, options.length);
        } else {
            this.options = new byte[0];
        }

    }

    public static MptcpOption parse(byte[] options) {
        MptcpOption mptcpOption = new MptcpOption(options);
        mptcpOption.parse();
        return mptcpOption;
    }

    private void parse() {
        parseOptions(0);
    }

    public void parseOptions(int index) {
        if (index >= options.length) return;
        int kind = options[0 + index];
        switch (kind) {
            case MPTCP_ENABLE:
                parseMptcpOption(index);
                break;
            case 0:
                parseNopOption(index);
                break;
            case 1:
                parseEndOption(index);
                break;
            default:
                parseOtherOption(index);

        }
    }

    public void parseOtherOption(int index) {
        int kind = options[0 + index];
        int length = options[1 + index];
        parseOptions(index + length);
    }

    public void parseNopOption(int index) {
        parseOptions(index + 1);
    }

    public void parseEndOption(int index) {
        parseOptions(index + 1);
    }

    public void parseMptcpOption(int index) {
        int kind = options[0 + index];
        int length = options[1 + index];
        if (kind == MPTCP_ENABLE) {
            this.mptcpEnbaled = true;
        }
        byte subType = (byte) (options[index + 2] >> 4);
        if (subType == MPTCP_CAPABLE) {
            this.mptcpCapable = true;
        } else if (subType == MPTCP_JOIN) {
            mptcpJoin = true;
        }
        if (hasMptcpCapable()) {
            senderKey = Arrays.copyOfRange(options, index + 4, index + 12);
        }
        if (hasMptcpJoin()) {
            token = Arrays.copyOfRange(options, index + 4, index + 8);
        }
        parseOptions(index + length);
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
        return senderKey;
    }

    public byte[] getToken() {
        return token;
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
