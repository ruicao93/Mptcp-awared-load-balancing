package org.onosproject.mptcp;

import org.junit.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
/**
 * Created by cr on 16-12-21.
 */
public class SHA1Test {

    @Test
    public void sha1Test() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] key = {0x4a, (byte)0xab, (byte)0x49, (byte)0x8a, (byte)0x99, 0x44, 0x30, 0x78};
        byte[] expectedToken = {(byte)0x9a, 0x6e, 0x0d, (byte)0xfb};
        byte[] sha1 = md.digest(key);
        byte[] token = Arrays.copyOfRange(sha1, 0, 4);
        System.out.println(token);
        assertThat(token, is(expectedToken));
    }

    @Test
    public void sortTest() {
        List<Integer> numList = new ArrayList<>();
        numList.add(0);
        numList.add(1);
        numList.add(2);
        numList.sort((n1, n2) -> n1 > n2 ? -1 : (n1 < n2 ? 1 : 0));
        System.out.println(numList);
    }
}
