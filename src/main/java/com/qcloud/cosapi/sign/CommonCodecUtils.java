package com.qcloud.cosapi.sign;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author chengwu
 * 封装了常用的MD5、SHA1、HmacSha1函数
 */
public class CommonCodecUtils {

    private static final Logger LOG = LoggerFactory.getLogger(CommonCodecUtils.class);

    private static final String HMAC_SHA1 = "HmacSHA1";

    /**
     * 对二进制数据进行BASE64编码
     * @param binaryData 二进制数据
     * @return 编码后的字符串
     */
    public static String Base64Encode(byte[] binaryData) {
        String encodedstr = Base64.encodeBase64String(binaryData);
        return encodedstr;
    }

    /**
     * 获取二进制数据的MD5值
     * @param binaryData  二进制数据
     * @return  对应的MD5值
     */
    public static String HashMd5(byte[] binaryData) {
        String md5Digest = DigestUtils.md5Hex(binaryData);
        return md5Digest;
    }

    /**
    * 获取文件的SHA1
    * @param fileContent  文件的二进制数据
    * @return  文件对应的SHA1值
    */
    public static String getFileSha1(byte[] fileContent) {
        String sha1Digest = DigestUtils.sha1Hex(fileContent);
        return sha1Digest;
    }

    /**
     * 获取文件的SHA1
     * @param fileInputStream  文件的输入流
     * @return                 文件对应的SHA1值
     * @throws Exception
     */
    public static String getFileSha1(InputStream fileInputStream) throws Exception {
        String sha1Digest = DigestUtils.sha1Hex(fileInputStream);
        return sha1Digest;
    }

    /**
     * 计算数据的Hmac值
     * @param binaryData    二进制数据
     * @param key           秘钥
     * @return              加密后的hmacsha1值
     */
    public static byte[] HmacSha1(byte[] binaryData, String key) {
        Mac mac = null;
        try {
            mac = Mac.getInstance(HMAC_SHA1);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("mac not find algorithm {}", HMAC_SHA1);
            return null;
        }

        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1);
        try {
            mac.init(secretKey);
        } catch (InvalidKeyException e) {
            LOG.error("mac init key {} occur a error {}", key, e.toString());
            return null;
        }

        byte[] HmacSha1Digest = null;
        try {
            HmacSha1Digest = mac.doFinal(binaryData);
        } catch (IllegalStateException e) {
            LOG.error("mac.doFinal occur a error {}", e.toString());
        }
        return HmacSha1Digest;
    }

    /**
     * 计算数据的Hmac值
     * @param plainText     文本数据
     * @param key           秘钥
     * @return              加密后的hmacsha1值
     */
    public static byte[] HmacSha1(String plainText, String key) {
        return HmacSha1(plainText.getBytes(), key);
    }
}
