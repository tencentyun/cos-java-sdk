package com.qcloud.cosapi.api;

import com.qcloud.cosapi.common.HMACSHA1;
import com.qcloud.cosapi.common.Request;
import com.qcloud.cosapi.common.Sign;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;

public class CosCloud implements CosCloudApi {
    /**
     * @brief Cos类
     * @author robinslsun
     */
    private static final String COSAPI_CGI_URL = "http://web.file.myqcloud.com/files/v1/";

    private static final Logger LG = LoggerFactory.getLogger(CosCloud.class);

    public enum FolderPattern {File, Folder, Both}

    private int appId;
    private String secretId;
    private String secretKey;
    private int timeOut;

    /**
     * CosCloud 构造方法
     *
     * @param appId     授权appid
     * @param secretId  授权secret id
     * @param secretKey 授权secret key
     */
    public CosCloud(int appId, String secretId, String secretKey) {
        this(appId, secretId, secretKey, 60);
    }

    /**
     * CosCloud 构造方法
     *
     * @param appId     授权appid
     * @param secretId  授权secret id
     * @param secretKey 授权secret key
     * @param timeOut   网络超时
     */
    public CosCloud(int appId, String secretId, String secretKey, int timeOut) {
        this.appId = appId;
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.timeOut = timeOut * 1000;
    }

    /**
     * 远程路径Encode处理
     *
     * @param remotePath
     * @return
     */
    private String encodeRemotePath(String remotePath) {
        if (remotePath.equals("/")) {
            return remotePath;
        }
        boolean endWith = remotePath.endsWith("/");
        String[] part = remotePath.split("/");
        remotePath = "";
        for (String s : part) {
            if (!s.equals("")) {
                if (remotePath != "") {
                    remotePath += "/";
                }
                remotePath += URLEncoder.encode(s);
            }
        }
        remotePath = (remotePath.startsWith("/") ? "" : "/") + remotePath + (endWith ? "/" : "");
        return remotePath;
    }

    /**
     * 标准化远程路径
     *
     * @param remotePath 要标准化的远程路径
     * @return
     */
    private String standardizationRemotePath(String remotePath) {
        if (!remotePath.startsWith("/")) {
            remotePath = "/" + remotePath;
        }
        if (!remotePath.endsWith("/")) {
            remotePath += "/";
        }
        return remotePath;
    }

    @Override
    public String updateFolder(String bucketName, String remotePath, String bizAttribute) throws Exception {
        remotePath = standardizationRemotePath(remotePath);
        return updateFile(bucketName, remotePath, bizAttribute);
    }

    @Override
    public String updateFile(String bucketName, String remotePath, String bizAttribute) throws Exception {
        String url = COSAPI_CGI_URL + appId + "/" + bucketName + encodeRemotePath(remotePath);
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("op", "update");
        data.put("biz_attr", bizAttribute);
        String sign = Sign.appSignatureOnce(appId, secretId, secretKey, (remotePath.startsWith("/") ? "" : "/") + remotePath, bucketName);
        String qcloud_sign = sign.toString();
        HashMap<String, String> header = new HashMap<String, String>();
        header.put("Authorization", qcloud_sign);
        header.put("Content-Type", "application/json");
        return Request.sendRequest(url, data, "POST", header, timeOut);
    }

    @Override
    public String deleteFolder(String bucketName, String remotePath) throws Exception {
        remotePath = standardizationRemotePath(remotePath);
        return deleteFile(bucketName, remotePath);
    }

    @Override
    public String deleteFile(String bucketName, String remotePath) throws Exception {
        if (remotePath.equals("/")) {
            throw new Exception("can not delete bucket using aip! go to http://console.qcloud.com/cos to operate bucket");
        }
        String url = COSAPI_CGI_URL + appId + "/" + bucketName + encodeRemotePath(remotePath);
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("op", "delete");
        String sign = Sign.appSignatureOnce(appId, secretId, secretKey, (remotePath.startsWith("/") ? "" : "/") + remotePath, bucketName);
        HashMap<String, String> header = new HashMap<String, String>();
        header.put("Authorization", sign);
        header.put("Content-Type", "application/json");
        return Request.sendRequest(url, data, "POST", header, timeOut);
    }

    @Override
    public String getFolderStat(String bucketName, String remotePath) throws Exception {
        remotePath = standardizationRemotePath(remotePath);
        return getFileStat(bucketName, remotePath);
    }

    @Override
    public String getFileStat(String bucketName, String remotePath) throws Exception {
        String url = COSAPI_CGI_URL + appId + "/" + bucketName + encodeRemotePath(remotePath);
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("op", "stat");
        long expired = System.currentTimeMillis() / 1000 + 60;
        String sign = Sign.appSignature(appId, secretId, secretKey, expired, bucketName);
        HashMap<String, String> header = new HashMap<String, String>();
        header.put("Authorization", sign);
        return Request.sendRequest(url, data, "GET", header, timeOut);
    }

    @Override
    public String createFolder(String bucketName, String remotePath) throws Exception {
        remotePath = standardizationRemotePath(remotePath);
        String url = COSAPI_CGI_URL + appId + "/" + bucketName + encodeRemotePath(remotePath);
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("op", "create");
        long expired = System.currentTimeMillis() / 1000 + 60;
        String sign = Sign.appSignature(appId, secretId, secretKey, expired, bucketName);
        HashMap<String, String> header = new HashMap<String, String>();
        header.put("Authorization", sign);
        header.put("Content-Type", "application/json");
        return Request.sendRequest(url, data, "POST", header, timeOut);
    }

    @Override
    public String getFolderList(String bucketName, String remotePath, int num, String context, int order, FolderPattern pattern) throws Exception {
        remotePath = standardizationRemotePath(remotePath);
        return getFolderList(bucketName, remotePath, "", num, context, order, pattern);
    }

    @Override
    public String getFolderList(String bucketName, String remotePath, String prefix, int num, String context, int order, FolderPattern pattern) throws Exception {
        remotePath = standardizationRemotePath(remotePath);
        String url = COSAPI_CGI_URL + appId + "/" + bucketName + encodeRemotePath(remotePath) + URLEncoder.encode(prefix);
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("op", "list");
        data.put("num", num);
        data.put("context", context);
        data.put("order", order);
        String[] patternArray = {"eListFileOnly", "eListDirOnly", "eListBoth"};
        data.put("pattern", patternArray[pattern.ordinal()]);
        long expired = System.currentTimeMillis() / 1000 + 60;
        String sign = Sign.appSignature(appId, secretId, secretKey, expired, bucketName);
        HashMap<String, String> header = new HashMap<String, String>();
        header.put("Authorization", sign);
        return Request.sendRequest(url, data, "GET", header, timeOut);
    }

    @Override
    public String uploadFile(String bucketName, String remotePath, String localPath) throws Exception {

        try {
            String url = COSAPI_CGI_URL + appId + "/" + bucketName + encodeRemotePath(remotePath);
            String sha1 = HMACSHA1.getFileSha1(localPath);
            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("op", "upload");
            data.put("sha", sha1);
            long expired = System.currentTimeMillis() / 1000 + 60;
            String sign = Sign.appSignature(appId, secretId, secretKey, expired, bucketName);
            HashMap<String, String> header = new HashMap<String, String>();
            header.put("Authorization", sign);
            return Request.sendRequest(url, data, "POST", header, timeOut, localPath);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public String uploadFile(String bucketName, String remotePath, InputStream inputStream) throws Exception {
        try {
            String url = COSAPI_CGI_URL + appId + "/" + bucketName + encodeRemotePath(remotePath);
            int length = inputStream.available();
            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("op", "upload");
            long expired = System.currentTimeMillis() / 1000 + 60;
            String sign = Sign.appSignature(appId, secretId, secretKey, expired, bucketName);
            HashMap<String, String> header = new HashMap<String, String>();
            header.put("Authorization", sign);
            return Request.uploadFileByStream(url, data, header, timeOut, inputStream, length);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public String sliceUploadFileFirstStep(String bucketName, String remotePath, String localPath, int sliceSize) throws Exception {
        try {
            String url = COSAPI_CGI_URL + appId + "/" + bucketName + encodeRemotePath(remotePath);
            String sha1 = HMACSHA1.getFileSha1(localPath);
            LG.info(sha1);
            long fileSize = new File(localPath).length();
            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("op", "upload_slice");
            data.put("sha", sha1);
            data.put("filesize", fileSize);
            data.put("slice_size", sliceSize);
            long expired = System.currentTimeMillis() / 1000 + 60;
            String sign = Sign.appSignature(appId, secretId, secretKey, expired, bucketName);
            HashMap<String, String> header = new HashMap<String, String>();
            header.put("Authorization", sign);
            return Request.sendRequest(url, data, "POST", header, timeOut);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public String sliceUploadFileFollowStep(String bucketName, String remotePath, String localPath,
                                            String sessionId, long offset, int sliceSize) throws Exception {
        String url = COSAPI_CGI_URL + appId + "/" + bucketName + encodeRemotePath(remotePath);
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("op", "upload_slice");
        data.put("session", sessionId);
        data.put("offset", offset);
        long expired = System.currentTimeMillis() / 1000 + (60 * 60 * 24);
        String sign = Sign.appSignature(appId, secretId, secretKey, expired, bucketName);
        HashMap<String, String> header = new HashMap<String, String>();
        header.put("Authorization", sign);
        return Request.sendRequest(url, data, "POST", header, timeOut, localPath, offset, sliceSize);
    }

    @Override
    public String sliceUploadFile(String bucketName, String remotePath, String localPath) throws Exception {
        return sliceUploadFile(bucketName, remotePath, localPath, 512 * 1024);
    }

    @Override
    public String sliceUploadFile(String bucketName, String remotePath, String localPath, int sliceSize) throws Exception {
        String result = sliceUploadFileFirstStep(bucketName, remotePath, localPath, sliceSize);
        try {
            JSONObject jsonObject = new JSONObject(result);
            int code = jsonObject.getInt("code");
            if (code != 0) {
                return result;
            }
            JSONObject data = jsonObject.getJSONObject("data");
            if (data.has("access_url")) {
                String accessUrl = data.getString("access_url");
                LG.info("命中秒传：" + accessUrl);
                return result;
            } else {
                String sessionId = data.getString("session");
                sliceSize = data.getInt("slice_size");
                long offset = data.getLong("offset");
                int retryCount = 0;
                while (true) {
                    LG.info("offset : " + offset);
                    result = sliceUploadFileFollowStep(bucketName, remotePath, localPath, sessionId, offset, sliceSize);
                    LG.info(result);
                    jsonObject = new JSONObject(result);
                    code = jsonObject.getInt("code");
                    if (code != 0) {
                        //当上传失败后会重试3次
                        if (retryCount < 3) {
                            retryCount++;
                            LG.info("重试....");
                        } else {
                            return result;
                        }
                    } else {
                        data = jsonObject.getJSONObject("data");
                        if (data.has("offset")) {
                            offset = data.getLong("offset") + sliceSize;
                        } else {
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return "";
    }
}
