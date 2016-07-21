package com.qcloud.cos;

public class ClientConfig {
	// cos server的域名地址
    private static final String COS_ENDPOINT = "http://web.file.myqcloud.com/files/v1";
    // 多次签名的默认过期时间,单位秒
    private static final int DEFAULT_SIGN_EXPIRED = 300;
    // 默认的最大重试次数(发生了socketException时)
    private static final int DEFAULT_MAX_RETRIES = 3;
    // 默认的获取连接的超时时间
    private static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT = -1;
    // 默认连接超时, 单位ms
    private static final int DEFAULT_CONNECTION_TIMEOUT = 30 * 1000;
    // 默认的SOCKET读取超时时间, 默认毫秒
    private static final int DEFAULT_SOCKET_TIMEOUT = 30 * 1000;
    // 默认的维护最大HTTP连接数
    private static final int DEFAULT_MAX_CONNECTIONS_COUNT = 100;
    // 默认的user_agent标识
    private static final String DEFAULT_USER_AGENT = "cos-java-sdk-v3.3";

    
    private String cosEndPoint = COS_ENDPOINT;
    private int signExpired = DEFAULT_SIGN_EXPIRED;
    private int maxFailedRetry = DEFAULT_MAX_RETRIES;
    private int connectionRequestTimeout = DEFAULT_CONNECTION_REQUEST_TIMEOUT;
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;
    private int maxConnectionsCount = DEFAULT_MAX_CONNECTIONS_COUNT;
    private String userAgent = DEFAULT_USER_AGENT;
 

    public int getMaxFailedRetry() {
        return maxFailedRetry;
    }

    public void setMaxFailedRetry(int maxFailedRetry) {
        this.maxFailedRetry = maxFailedRetry;
    }
    

    public int getSignExpired() {
		return signExpired;
	}

	public void setSignExpired(int signExpired) {
		this.signExpired = signExpired;
	}

	public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getMaxConnectionsCount() {
        return maxConnectionsCount;
    }

    public void setMaxConnectionsCount(int maxConnectionsCount) {
        this.maxConnectionsCount = maxConnectionsCount;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getCosEndPoint() {
        return cosEndPoint;
    }

    public void setCosEndPoint(String cosEndpoint) {
        this.cosEndPoint = cosEndpoint;
    }
    
}
