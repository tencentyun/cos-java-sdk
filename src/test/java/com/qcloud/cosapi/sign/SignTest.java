package com.qcloud.cosapi.sign;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * 
 * @author chengwu UT For Sign
 */

public class SignTest {

    public static final int APP_ID = 1000027;
    public static final String SECRET_ID = "AKID3LqsccIPChNU2mQ7pzJv5jJYfvmSbfqf";
    public static final String SECRET_KEY = "eSkMsxzUT1zVIa5zRYv92CyI1huqvtUa";
    public static final String remotePath = "/mytest/eclipse.conf";
    public static final long expired = System.currentTimeMillis() / 1000 + 60;
    public static final String bucketName = "chengwu";

    @Test
    public void testAppSignature() {
        try {
            Sign.appSignature(APP_ID, SECRET_ID, SECRET_KEY, expired, bucketName);
        } catch (Exception e) {
            fail();
        }

    }

    @Test
    public void testAppSignatureOnce() {
        try {
            Sign.appSignatureOnce(APP_ID, SECRET_ID, SECRET_KEY, remotePath, bucketName);
        } catch (Exception e) {
            fail();
        }
    }


    @Test
    public void testAppSignatureNullParam() {
        try {
            String sign = Sign.appSignatureOnce(APP_ID, null, null, remotePath, null);
            assertTrue(sign.equals("-1"));
        } catch (Exception e) {
            fail();
        }
    }
}
