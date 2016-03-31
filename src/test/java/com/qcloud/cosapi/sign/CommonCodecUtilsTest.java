package com.qcloud.cosapi.sign;

import static org.junit.Assert.*;

import org.junit.Test;

public class CommonCodecUtilsTest {

    private static final String plainText = "681805d9f7c6ab988a00c02f1096b1b68a77aaed";
    private static final String hmacKey = "lw7231!2@7g";

    @Test
    public void testBase64Encode() {
        try {
            String encodeStr = CommonCodecUtils.Base64Encode(plainText.getBytes("UTF-8"));
            String expectEncodeStr = "NjgxODA1ZDlmN2M2YWI5ODhhMDBjMDJmMTA5NmIxYjY4YTc3YWFlZA==";
            boolean cmpResult = encodeStr.equals(expectEncodeStr);
            assertTrue(cmpResult);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testHashMd5() {
        try {
            String encodeStr = CommonCodecUtils.HashMd5(plainText.getBytes("UTF-8"));
            String expectEncodeStr = "ea5075fe1634947b507810a1ffc1f44d";
            boolean cmpResult = encodeStr.equals(expectEncodeStr);
            assertTrue(cmpResult);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testHashSha1() {
        try {
            String encodeStr = CommonCodecUtils.getFileSha1(plainText.getBytes("UTF-8"));
            String expectEncodeStr = "e96203049de6670ddc995e68ba3e9c746e4aff95";
            boolean cmpResult = encodeStr.equals(expectEncodeStr);
            assertTrue(cmpResult);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testHmacSha1StringString() {
        try {
            byte[] hmacDigestByte = CommonCodecUtils.HmacSha1(plainText, hmacKey);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < hmacDigestByte.length; ++i) {
                String hex = Integer.toHexString(hmacDigestByte[i] & 0xff);
                if (hex.length() == 1) {
                    stringBuilder.append("0");
                }
                stringBuilder.append(hex);
            }
            String expectHmacDigest = "e8d7985289f8586a7bd2374590db848c48046874";
            assertTrue(expectHmacDigest.equals(stringBuilder.toString()));
        } catch (Exception e) {
            fail();
        }
    }

}
