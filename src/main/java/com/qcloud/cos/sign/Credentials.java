package com.qcloud.cos.sign;

/**
 * @author chengwu
 * 鉴权信息, 包括appId, 密钥对, 可从控制台获取
 */
public class Credentials {
    private final int appId;
    private final String secretId;
    private final String secretKey;

    public Credentials(int appId, String secretId, String secretKey) {
        super();
        this.appId = appId;
        this.secretId = secretId;
        this.secretKey = secretKey;
    }

    public int getAppId() {
        return appId;
    }

    public String getSecretId() {
        return secretId;
    }

    public String getSecretKey() {
        return secretKey;
    }

}
