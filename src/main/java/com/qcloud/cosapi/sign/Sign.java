package com.qcloud.cosapi.sign;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chengwu
 * 封装签名类，包括单词有效签名函数与段时间有效签名函数
 */
public class Sign {
    
    private static Logger LOG = LoggerFactory.getLogger(Sign.class);

    /**
     * 返回用户访问资源的签名
     * @param appId       appid，可在控制台查看
     * @param secretId    秘钥ID，可在控制台查看
     * @param secretKey   秘钥，可在控制台查看
     * @param expired     超时时间
     * @param fileId      用户的appId，bucetName和资源路径的字符串拼接
     * @param bucketName  bucket名
     * @return            返回签名
     */
    private static String appSignature(int appId, String secretId, String secretKey, long expired,
            String fileId, String bucketName) {
        if (secretId == null || secretKey == null || fileId == null || bucketName == null) {
            LOG.error("appSignature exist null param!");
            // 之前这里为什么是"-1"
            return "-1";
        }

        long now = System.currentTimeMillis() / 1000;
        int rdm = Math.abs(new Random().nextInt());
        String plainText = new StringBuilder()
                .append("a=").append(appId)
                .append("&k=").append(secretId)
                .append("&e=").append(expired)
                .append("&t=").append(now)
                .append("&r=").append(rdm)
                .append("&f=").append(fileId)
                .append("&b=").append(bucketName)
                .toString();

        byte[] hmacDigest = CommonCodecUtils.HmacSha1(plainText, secretKey);

        byte[] signContent = new byte[hmacDigest.length + plainText.getBytes().length];
        System.arraycopy(hmacDigest, 0, signContent, 0, hmacDigest.length);
        System.arraycopy(plainText.getBytes(), 0, signContent, hmacDigest.length, plainText.getBytes().length);

        return CommonCodecUtils.Base64Encode(signContent);
    }

    /**
     * 返回用户访问资源的签名，签名在一段时间有效，时间由用户设定
     * @param appId       appid，可在控制台查看
     * @param secretId    秘钥ID，可在控制台查看
     * @param secretKey   秘钥，可在控制台查看
     * @param expired     超时时间，单位为妙，表示在该时间之前，签名都有效
     * @param bucketName  bucket名
     * @return            返回签名
     */
    public static String appSignature(int appId, String secretId, String secretKey, long expired,
            String bucketName) {
        return appSignature(appId, secretId, secretKey, expired, "", bucketName);
    }

    /**
     * 返回用户访问资源的签名，签名单次有效
     * @param appId       appid，可在控制台查看
     * @param secretId    秘钥ID，可在控制台查看
     * @param secretKey   秘钥，可在控制台查看
     * @param bucketName  bucket名
     * @param remotePath  资源的远程路径
     * @return            返回单次有效的签名
     */
    public static String appSignatureOnce(int appId, String secretId, String secretKey,
            String remotePath, String bucketName) {
        String fileId = new StringBuilder().append("/").append(appId).append("/").append(bucketName).append(remotePath).toString();
        return appSignature(appId, secretId, secretKey, 0, fileId, bucketName);
    }


}
