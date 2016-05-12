package com.qcloud.cosapi.api;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import com.qcloud.cosapi.file.FileProcess;
import com.qcloud.cosapi.http.HttpSender;
import com.qcloud.cosapi.http.RequestBodyKey;
import com.qcloud.cosapi.http.RequestBodyValue;
import com.qcloud.cosapi.http.RequestHeaderKey;
import com.qcloud.cosapi.http.RequestHeaderValue;
import com.qcloud.cosapi.http.ResponseBodyKey;
import com.qcloud.cosapi.sign.CommonCodecUtils;
import com.qcloud.cosapi.sign.Sign;

/**
 * @author chengwu
 *  封装Cos JAVA SDK暴露给用户的接口函数
 */
public class CosCloud {

    private static final Logger LOG = LoggerFactory.getLogger(CosCloud.class);

    private static final String COSAPI_CGI_URL = "http://web.file.myqcloud.com/files/v1";

    // 默认的发送HTTP请求时，连接与socket的超时时间
    private static final int DEFAULT_TIMEOUT_IN_SECONDS = 60;

    // 文件路径的分隔符
    private static final String PATH_DELIMITER = "/";

    // 签名的超时时间，单位为秒
    private static final int DEFAULT_SIGN_EXPIRED_IN_SECONDS = 1800;

    // 默认的分片大小，512KB
    private static final int DEFAULT_SLICE_LEN = 512 * 1024;

    // 分片重传最多次数
    private static final int SLICE_UPLOAD_RETRY_COUNT = 3;

    // 用来执行并行重传的线程池的大小
    private static final int THREAD_POOL_SIZE = 20;
    // 线程池对象
    private final ExecutorService executorService =
        Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    // 主线程等待子线程执行并发发送的最长时间, 单位为秒
    private static final int MAX_WAIT_TIME_PARALL_SEND_FILE = 1800;
    
    private HttpSender httpSender = null;

    // 文件类型
    public enum FolderPattern {
        File, Folder, Both
    };

    // 开发者访问cos的唯一资源标识符,可在控制台获取
    private int appId;
    // 签名的密钥对 可在控制台获取
    private String secretId;
    private String secretKey;
    // 发送请求时，socket与连接的超时时间,单位为秒
    private int timeOut;

    // 初始化Cos，使用默认超时时间， 30S
    public CosCloud(int appId, String secretId, String secretKey) {
        this(appId, secretId, secretKey, DEFAULT_TIMEOUT_IN_SECONDS);
    }

    public CosCloud(int appId, String secretId, String secretKey, int timeOut) {
        this.appId = appId;
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.timeOut = timeOut;
        this.httpSender = new HttpSender();
    }

    /**
     * 用于设置超时时间
     * @param timeOut 超时时间, 单位为秒
     */
    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    /**
     * 获取超时时间，单位秒
     * @return  返回超时时间
     */
    public int getTimeOut() {
        return this.timeOut;
    }
    /**
     * 判断用户输入的是否是根路径,即路径只包含空格和/
     *
     * @param remotePath
     * @return 如果是根路径，则返回true，否则返回false
     */
    private boolean isRootPath(String remotePath) {
        return remotePath.equals(PATH_DELIMITER);
    }

    /**
     * 获取完整的经过编码的 COS URL路径
     * @param bucketName bucket名称
     * @param remotePath cos上的路径
     * @return  经过编码的完成的COS URL路径
     */
    private String getEncodedCosUrl(String bucketName, String remotePath) {
        String cosUrl =
            new StringBuilder().append(PATH_DELIMITER).append(appId).append(PATH_DELIMITER)
            .append(bucketName).append(PATH_DELIMITER).append(remotePath).toString();
        return COSAPI_CGI_URL + encodeRemotePath(cosUrl);
    }

    /**
     * 使用URL encode编码远程路径，即在bucketName下的路径
     * @param remotePath  远程路径
     * @return 返回URL encode编码的远程路径
     */
    private String encodeRemotePath(String remotePath) {
        String[] pathSegmentsArr = remotePath.split(PATH_DELIMITER);
        StringBuilder pathBuilder = new StringBuilder();
        for (String pathSegment : pathSegmentsArr) {
            if (!pathSegment.trim().isEmpty()) {
                try {
                    pathBuilder.append(PATH_DELIMITER)
                        .append(URLEncoder.encode(pathSegment.trim(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    LOG.error("Unsupported ecnode exception", e);
                }
            }
        }
        if (remotePath.endsWith(PATH_DELIMITER)) {
            pathBuilder.append(PATH_DELIMITER);
        }
        return pathBuilder.toString();
    }

    /**
     * 标准化远程路径，去掉冗余分隔符，在头部加上分隔符，如果是目录，尾部加上分隔符
     * @param remotePath 要标准化的远程路径
     * @return 返回标准化的远程路径
     */
    private String formatPath(String remotePath) {

        String[] pathSegmentsArr = remotePath.split(PATH_DELIMITER);
        StringBuilder pathBuilder = new StringBuilder();

        for (String pathSegment : pathSegmentsArr) {
            String trimedPathSegment = pathSegment.trim();
            if (!trimedPathSegment.isEmpty()) {
                pathBuilder.append(PATH_DELIMITER).append(trimedPathSegment);
            }
        }

        if (remotePath.endsWith(PATH_DELIMITER)) {
            pathBuilder.append(PATH_DELIMITER);
        }

        return pathBuilder.toString();
    }

    /**
     * 更新目录属性信息
     * @param bucketName bucket名称
     * @param remotePath 远程目录路径
     * @param bizAttribute 更新的属性信息
     * @return 服务器端返回的操作结果，成员code为0表示成功，其他表示失败，具体参照文档手册
     * @throws Exception
     */
    public String updateFolder(String bucketName, String remotePath, String bizAttribute)
        throws Exception {
        remotePath = formatPath(remotePath);
        return updateFile(bucketName, remotePath, bizAttribute);
    }

    /**
     * 更新文件属性信息
     * @param bucketName bucket名称
     * @param remotePath 远程文件路径
     * @param bizAttribute 更新的属性信息
     * @return 服务器端返回的操作结果，成员code为0表示成功，具体参照文档手册
     * @throws Exception
     */
    public String updateFile(String bucketName, String remotePath, String bizAttribute)
        throws Exception {
        remotePath = formatPath(remotePath);
        if (isRootPath(remotePath)) {
            String errorMsg = "update bucket only allowed by http://console.qcloud.com/cos!";
            LOG.error(errorMsg);
            JSONObject errorRet = new JSONObject();
            errorRet.put(ResponseBodyKey.CODE, ErrorCode.PARAMS_ERROR);
            errorRet.put(ResponseBodyKey.MESSAGE, errorMsg);
            return errorRet.toString();
        }
        String url = getEncodedCosUrl(bucketName, remotePath);

        HashMap<String, String> updateData = new HashMap<>();
        updateData.put(RequestBodyKey.OP, RequestBodyValue.OP_UPDATE);
        updateData.put(RequestBodyKey.BIZ_ATTR, bizAttribute);

        String sign = Sign.appSignatureOnce(appId, secretId, secretKey, remotePath, bucketName);
        HashMap<String, String> httpHeader = new HashMap<>();
        httpHeader.put(RequestHeaderKey.Authorization, sign);
        httpHeader.put(RequestHeaderKey.Content_TYPE, RequestHeaderValue.ContentType.JSON);

        return httpSender.sendJsonRequest(url, httpHeader, updateData, timeOut);
    }

    /**
     * 删除目录
     * @param bucketName bucket名称
     * @param remotePath 远程目录路径
     * @return 服务器端返回的操作结果，成员code为0表示成功，具体参照文档手册
     * @throws Exception
     */
    public String deleteFolder(String bucketName, String remotePath) throws Exception {
        remotePath = formatPath(remotePath);
        return deleteFile(bucketName, remotePath);
    }

    /**
     * 删除文件
     * @param bucketName bucket名称
     * @param remotePath 远程文件路径
     * @return 服务器端返回的操作结果，成员code为0表示成功，具体参照文档手册
     * @throws Exception
     */
    public String deleteFile(String bucketName, String remotePath) throws Exception {
        remotePath = formatPath(remotePath);
        if (isRootPath(remotePath)) {
            String errorMsg = "delete bucket only allowed by http://console.qcloud.com/cos!";
            LOG.error(errorMsg);
            JSONObject errorRet = new JSONObject();
            errorRet.put(ResponseBodyKey.CODE, ErrorCode.PARAMS_ERROR);
            errorRet.put(ResponseBodyKey.MESSAGE, errorMsg);
            return errorRet.toString();
        }
        String url = getEncodedCosUrl(bucketName, remotePath);

        HashMap<String, String> postBody = new HashMap<>();
        postBody.put(RequestBodyKey.OP, RequestBodyValue.OP_DELETE);

        String sign = Sign.appSignatureOnce(appId, secretId, secretKey, remotePath, bucketName);
        HashMap<String, String> httpHeader = new HashMap<>();
        httpHeader.put(RequestHeaderKey.Authorization, sign);
        httpHeader.put(RequestHeaderKey.Content_TYPE, RequestHeaderValue.ContentType.JSON);

        return httpSender.sendJsonRequest(url, httpHeader, postBody, timeOut);
    }

    /**
     * 获取目录信息
     * @param bucketName bucket名称
     * @param remotePath 远程目录路径
     * @return 服务器端返回的目录属性
     * @throws Exception
     */
    public String getFolderStat(String bucketName, String remotePath) throws Exception {
        remotePath = formatPath(remotePath);
        return getFileStat(bucketName, remotePath);
    }

    /**
     * 获取文件信息
     * @param bucketName bucket名称
     * @param remotePath 远程文件路径
     * @return 服务器端返回的文件属性
     * @throws Exception
     */
    public String getFileStat(String bucketName, String remotePath) throws Exception {
        String url = getEncodedCosUrl(bucketName, remotePath);

        long expired = getExpiredTimeInSec();
        String sign = Sign.appSignature(appId, secretId, secretKey, expired, bucketName);

        HashMap<String, String> getBody = new HashMap<>();
        getBody.put(RequestBodyKey.OP, RequestBodyValue.OP_STAT);

        HashMap<String, String> httpHeader = new HashMap<>();
        httpHeader.put(RequestHeaderKey.Authorization, sign);
        return httpSender.sendGetRequest(url, httpHeader, getBody, timeOut);
    }



    /**
     * 创建目录
     *
     * @param bucketName bucket名称
     * @param remotePath 远程目录路径
     * @return 服务器端返回的操作结果，成员code为0表示成功，具体参照文档手册
     * @throws Exception
     */
    public String createFolder(String bucketName, String remotePath) throws Exception {
        remotePath = formatPath(remotePath);
        if (isRootPath(remotePath)) {
            String errorMsg = "create bucket only allowed by http://console.qcloud.com/cos!";
            LOG.error(errorMsg);
            JSONObject errorRet = new JSONObject();
            errorRet.put(ResponseBodyKey.CODE, ErrorCode.PARAMS_ERROR);
            errorRet.put(ResponseBodyKey.MESSAGE, errorMsg);
            return errorRet.toString();
        }
        String url = getEncodedCosUrl(bucketName, remotePath);

        long expired = getExpiredTimeInSec();
        String sign = Sign.appSignature(appId, secretId, secretKey, expired, bucketName);

        HashMap<String, String> postBody = new HashMap<>();
        postBody.put(RequestBodyKey.OP, RequestBodyValue.OP_CREATE);

        HashMap<String, String> httpHeader = new HashMap<>();
        httpHeader.put(RequestHeaderKey.Authorization, sign);
        httpHeader.put(RequestHeaderKey.Content_TYPE, RequestHeaderValue.ContentType.JSON);
        return httpSender.sendJsonRequest(url, httpHeader, postBody, timeOut);
    }

    /**
     * 获取文件类型对应的字符串
     * @param pattern  文件类型的枚举值
     * @return         对应类型的字符串
     */
    private String getPatternString(FolderPattern pattern) {
        String patternStr = null;
        switch (pattern) {
            case File:
                patternStr = "eListFileOnly";
                break;
            case Folder:
                patternStr = "eListDirOnly";
                break;
            case Both:
            default:
                patternStr = "eListBoth";
                break;
        }
        return patternStr;
    }

    /**
     * 获取目录列表
     * @param bucketName bucket名称
     * @param remotePath 远程目录路径
     * @param num 拉取的列表总数
     * @param context
     *        透传字段，查看第一页，则传空字符串。若需要翻页，需要将前一页返回值中的context透传到参数中。order用于指定翻页顺序。若order填0，则从当前页正序/往下翻页；
     *        若order填1，则从当前页倒序/往上翻页。
     * @param order 默认正序(=0), 填1为反序
     * @param pattern 拉取模式，取值有三种枚举值:只是文件，只是目录，全部
     * @return  服务器端返回的列表详情，列表项最多为num个
     * @throws Exception
     */
    public String getFolderList(String bucketName, String remotePath, int num, String context,
            int order, FolderPattern pattern) throws Exception {
        remotePath = formatPath(remotePath);
        return getFolderList(bucketName, remotePath, "", num, context, order, pattern);
    }

    /**
     * 获取目录列表,可通过前缀搜索
     * @param bucketName bucket名称
     * @param remotePath 远程目录路径
     * @param prefix 读取文件/目录前缀
     * @param num 拉取的总数
     * @param context
     *        透传字段，查看第一页，则传空字符串。若需要翻页，需要将前一页返回值中的context透传到参数中。order用于指定翻页顺序。若order填0，则从当前页正序/往下翻页；
     *        若order填1，则从当前页倒序/往上翻页。
     * @param order 默认正序(=0), 填1为反序
     * @param pattern 拉取模式，取值有三种枚举值:只是文件，只是目录，全部
     * @return 服务器端返回的列表详情，列表项最多为num个
     * @throws Exception
     */
    public String getFolderList(String bucketName, String remotePath, String prefix, int num,
            String context, int order, FolderPattern pattern) throws Exception {
        remotePath = remotePath + PATH_DELIMITER + prefix;
        remotePath = formatPath(remotePath);
        String url = getEncodedCosUrl(bucketName, remotePath);

        long expiredTimeInSec = getExpiredTimeInSec();
        String sign = Sign.appSignature(appId, secretId, secretKey, expiredTimeInSec, bucketName);

        HashMap<String, String> getBody = new HashMap<>();
        getBody.put(RequestBodyKey.OP, RequestBodyValue.OP_LIST);
        getBody.put(RequestBodyKey.NUM, String.valueOf(num));
        getBody.put(RequestBodyKey.CONTEXT, context);
        getBody.put(RequestBodyKey.ORDER, String.valueOf(order));
        getBody.put(RequestBodyKey.PATTERN, getPatternString(pattern));

        HashMap<String, String> httpHeader = new HashMap<>();
        httpHeader.put(RequestHeaderKey.Authorization, sign);
        return httpSender.sendGetRequest(url, httpHeader, getBody, timeOut);
    }

    /**
     * 单个文件上传,适用于小文件
     * @param bucketName bucket名称
     * @param remotePath 远程文件路径
     * @param localPath 本地文件路径
     * @return  服务器端返回的操作结果，成员code为0表示成功，具体参照文档手册
     * @throws Exception
     */
    public String uploadFile(String bucketName, String remotePath, String localPath)
        throws Exception {
        if (!FileProcess.isLegalFile(localPath)) {
            String errorMsg = localPath + " is not file or not exist or can't be read!";
            LOG.error(errorMsg);
            JSONObject errorRet = new JSONObject();
            errorRet.put(ResponseBodyKey.CODE, ErrorCode.PARAMS_ERROR);
            errorRet.put(ResponseBodyKey.MESSAGE, errorMsg);
            return errorRet.toString();
        }
        FileInputStream localFileInputStream = null;
        try {
            localFileInputStream = FileProcess.getFileInputStream(localPath);
            return uploadFile(bucketName, remotePath, localFileInputStream);
        } catch (Exception e) {
            LOG.error("UploadFile {} occur a error {}", localPath, e.toString());
            throw e;
        } finally {
            FileProcess.closeFileStream(localFileInputStream, localPath);
        }
    }

    /**
     * 流文件上传，适用于小文件，如果对应的文件过大，容易造成OOM
     * @param bucketName bucket名称
     * @param remotePath 远程文件路径
     * @param inputStream 文件流
     * @return  服务器端返回的操作结果，成员code为0表示成功，具体参照文档手册
     * @throws Exception
     */
    public String uploadFile(String bucketName, String remotePath, InputStream inputStream)
        throws Exception {
        String url = getEncodedCosUrl(bucketName, remotePath);

        byte[] fileContent = FileProcess.getFileContent(inputStream);
        String shaDigest = CommonCodecUtils.getFileSha1(fileContent);

        HashMap<String, String> postData = new HashMap<>();
        postData.put(RequestBodyKey.OP, RequestBodyValue.OP_UPLOAD);
        postData.put(RequestBodyKey.SHA, shaDigest);

        long expired = getExpiredTimeInSec();
        String sign = Sign.appSignature(appId, secretId, secretKey, expired, bucketName);

        HashMap<String, String> httpHeader = new HashMap<>();
        httpHeader.put(RequestHeaderKey.Authorization, sign);

        return httpSender.sendFileRequest(url, httpHeader, postData, fileContent, timeOut);
    }

    /**
     * 分片上传，不需要指定分片大小，采取默认切片大小512KB
     *
     * @param bucketName bucket名称
     * @param remotePath 远程文件路径
     * @param localPath 本地文件路径
     * @return  服务器端返回的操作结果，成员code为0表示成功，具体参照文档手册
     * @throws Exception
     */
    public String sliceUploadFile(String bucketName, String remotePath, String localPath)
        throws Exception {
        return sliceUploadFile(bucketName, remotePath, localPath, DEFAULT_SLICE_LEN);
    }

    /**
     * 分片上传函数，需要提供分片大小，建议适用512KB~10MB之间
     * @param bucketName bucket名称
     * @param remotePath 远程文件路径
     * @param localPath 本地文件路径
     * @param sliceSize 分片大小，单位为字节
     * @return 服务器端返回的操作结果，成员code为0表示成功，具体参照文档手册
     * @throws Exception
     */
    public String sliceUploadFile(String bucketName, String remotePath, String localPath,
            int sliceSize) throws Exception {
        if (!FileProcess.isLegalFile(localPath)) {
            String errorMsg = localPath + " is not file or not exist or can't be read!";
            LOG.error(errorMsg);
            JSONObject errorRet = new JSONObject();
            errorRet.put(ResponseBodyKey.CODE, ErrorCode.PARAMS_ERROR);
            errorRet.put(ResponseBodyKey.MESSAGE, errorMsg);
            return errorRet.toString();
        }
        String url = getEncodedCosUrl(bucketName, remotePath);
        JSONObject sliceControlResult =
            sliceUploadFileControlCmd(url, bucketName, localPath, sliceSize);
        LOG.info("Slice upload first step:localfile {} remotePath {} result {}", localPath,
                remotePath, sliceControlResult);
        if (sliceControlResult.getInt(ResponseBodyKey.CODE) != 0) {
            return sliceControlResult.toString();
        }

        JSONObject controlDataMember = sliceControlResult.getJSONObject(ResponseBodyKey.DATA);
        if (controlDataMember.has(ResponseBodyKey.Data.ACCESS_URL)) {
            return sliceControlResult.toString();
        } else {
            String sessionId = controlDataMember.getString(ResponseBodyKey.Data.SESSION);
            sliceSize = controlDataMember.getInt(ResponseBodyKey.Data.SLICE_SIZE);
            long offset = controlDataMember.getLong(ResponseBodyKey.Data.OFFSET);
            return sliceUploadFileData(url, bucketName, sessionId, offset, sliceSize, localPath);
        }

    }

    /**
     * 并行上传分片（目前还不稳定)
     * @param bucketName bucket名称
     * @param remotePath 远程文件路径
     * @param localPath 本地文件路径
     * @param sliceSize 切片大小（字节）
     * @return 服务器端返回的操作结果，结果存在一些问题，如果最后一个分片成功，应该返回access_url地址
     * @throws Exception
     */
    public String sliceUploadFileParallel(String bucketName, String remotePath, String localPath,
            int sliceSize) throws Exception {
        if (!FileProcess.isLegalFile(localPath)) {
            String errorMsg = localPath + " is not file or not exist or can't be read!";
            LOG.error(errorMsg);
            JSONObject errorRet = new JSONObject();
            errorRet.put(ResponseBodyKey.CODE, ErrorCode.PARAMS_ERROR);
            errorRet.put(ResponseBodyKey.MESSAGE, errorMsg);
            return errorRet.toString();
        }
        String url = getEncodedCosUrl(bucketName, remotePath);
        JSONObject resultJson = sliceUploadFileControlCmd(url, bucketName, localPath, sliceSize);
        if (resultJson.getInt(ResponseBodyKey.CODE) != 0) {
            return resultJson.toString();
        }

        JSONObject data = resultJson.getJSONObject(ResponseBodyKey.DATA);
        if (data.has(ResponseBodyKey.Data.ACCESS_URL)) {
            return resultJson.toString();
        } else {
            String sessionId = data.getString(ResponseBodyKey.Data.SESSION);
            sliceSize = data.getInt(ResponseBodyKey.Data.SLICE_SIZE);
            long offset = data.getLong(ResponseBodyKey.Data.OFFSET);

            long fileLen = FileProcess.getFileLength(localPath);

            List<FutureTask<JSONObject>> futureTasks = new ArrayList<>();
            List<SliceUploadInner> sliceUploadInners = new ArrayList<>();

            while (offset < fileLen) {
                SliceUploadInner sliceUploadInner = new SliceUploadInner(url, bucketName, localPath,
                        sessionId, offset, sliceSize);
                sliceUploadInners.add(sliceUploadInner);
                FutureTask<JSONObject> task = new FutureTask<>(sliceUploadInner);
                futureTasks.add(task);
                offset = offset + sliceSize;
            }

            CountDownLatch doneSignal = new CountDownLatch(futureTasks.size());
            for (SliceUploadInner sliceUploadInner : sliceUploadInners) {
                sliceUploadInner.setCountDownLatch(doneSignal);
            }

            for (FutureTask<JSONObject> task : futureTasks) {
                executorService.execute(task);
            }

            if (!doneSignal.await(MAX_WAIT_TIME_PARALL_SEND_FILE, TimeUnit.SECONDS)) // 30s
            {
                String errMsg = "sliceUpload File parallel" + localPath + " time out!";
                LOG.error(errMsg);
                throw new Exception("slice uploadFile parall time out");
            }

            Iterator<FutureTask<JSONObject>> it = futureTasks.iterator();
            JSONObject taskResult = null;
            String uploadParaRet = "";
            while (it.hasNext()) {
                FutureTask<JSONObject> task = it.next();
                try {
                    taskResult = task.get();
                    if (taskResult.getInt(ResponseBodyKey.CODE) != 0) {
                        return taskResult.toString();
                    } else {
                        JSONObject dataJson = taskResult.getJSONObject(ResponseBodyKey.DATA);
                        if (dataJson.has(ResponseBodyKey.Data.ACCESS_URL)) {
                            return taskResult.toString();
                        }
                        uploadParaRet = taskResult.toString();
                    }
                } catch (Exception e) {
                    LOG.error("task execute occur a exception {}", e.toString());
                    throw e;
                }
            }
            return uploadParaRet;
        }
    }

    /**
     * 分片上传第一步，发送控制命令
     * @param url  上传到的cos路径
     * @param bucketName bucket名称
     * @param localPath 本地文件路径
     * @param sliceSize 切片大小（字节）
     * @return 服务器端返回的操作结果，包括sessionId、offset等，具体参见文档手册
     * @throws Exception
     */
    private JSONObject sliceUploadFileControlCmd(String url, String bucketName, String localPath,
            int sliceSize) throws Exception {

        long fileSize = FileProcess.getFileLength(localPath);
        String sha1Digest = FileProcess.getFileSha1(localPath);
        HashMap<String, String> postBody = new HashMap<>();
        postBody.put(RequestBodyKey.OP, RequestBodyValue.OP_UPLOAD_SLICE);
        postBody.put(RequestBodyKey.SHA, sha1Digest);
        postBody.put(RequestBodyKey.FILE_SIZE, String.valueOf(fileSize));
        postBody.put(RequestBodyKey.SLICE_SIZE, String.valueOf(sliceSize));

        long expired = getExpiredTimeInSec();
        String sign = Sign.appSignature(appId, secretId, secretKey, expired, bucketName);
        HashMap<String, String> header = new HashMap<>();
        header.put(RequestHeaderKey.Authorization, sign);

        String resultStr = httpSender.sendFileRequest(url, header, postBody, null, timeOut);
        try {
            JSONObject resultJson = new JSONObject(resultStr);
            return resultJson;
        } catch (JSONException e) {
            LOG.error("sendJsonResult return illegal json, result {}", resultStr);
            throw e;
        }
    }

    /**
     * 分片上传第二部，上传数据
     * @param url           上传到的COS的完整路径
     * @param bucketName    bucket名称
     * @param sessionId     分片上传第一步返回的session值
     * @param offset        分片上传第一步返回的offset值
     * @param sliceSize     分片大小,单位为字节
     * @param localPath     本地路径
     * @return              服务器端返回的操作结果，成员code为0表示成功，最后会返回access_url, 具体参照文档手册
     * @throws Exception
     */
    private String sliceUploadFileData(String url, String bucketName, String sessionId, long offset,
            int sliceSize, String localPath) throws Exception {
        InputStream inputStream = null;
        try {
            long fileSize = FileProcess.getFileLength(localPath);
            inputStream = FileProcess.getFileInputStream(localPath);
            inputStream.skip(offset);
            JSONObject resultJson = null;
            while (offset < fileSize) {
                byte[] sliceContent = FileProcess.getFileContent(inputStream, 0, sliceSize);
                if (sliceContent == null) {
                    String errMsg = new StringBuilder()
                        .append("get FileSlicent content return empty, file:").append(localPath)
                        .append(" offset:").append(offset).append(" sliceSize")
                        .append(sliceSize).toString();
                    LOG.error(errMsg);
                    throw new Exception(errMsg);
                }
                resultJson = sliceUploadFileData(url, bucketName, sessionId, offset, sliceContent);
                if (resultJson.getInt(ResponseBodyKey.CODE) == 0) {
                    offset += sliceSize;
                } else {
                    return resultJson.toString();
                }
            }
            return resultJson.toString();
        } finally {
            FileProcess.closeFileStream(inputStream, localPath);
        }
    }

    /**
     * 分片上传第二部，上传单个分片，包含多次重试
     * @param url 上传的到的cos路径
     * @param bucketName bucket名称
     * @param sessionId 分片上传会话ID
     * @param offset 文件分片偏移量
     * @param sliceContent 单个分片的内容
     * @return   服务器端返回的操作结果，成员code为0表示成功，具体参照文档手册
     * @throws Exception
     */
    public JSONObject sliceUploadFileData(String url, String bucketName, String sessionId,
            long offset, byte[] sliceContent) throws Exception {

        HashMap<String, String> postBody = new HashMap<>();
        postBody.put(RequestBodyKey.OP, RequestBodyValue.OP_UPLOAD_SLICE);
        postBody.put(RequestBodyKey.SESSION, sessionId);
        postBody.put(RequestBodyKey.OFFSET, String.valueOf(offset));

        long expired = getExpiredTimeInSec();
        String sign = Sign.appSignature(appId, secretId, secretKey, expired, bucketName);
        HashMap<String, String> header = new HashMap<>();
        header.put(RequestHeaderKey.Authorization, sign);

        int retryCount = 0;
        String resultStr = null;
        JSONObject resultJson = null;
        while (retryCount < SLICE_UPLOAD_RETRY_COUNT) {
            resultStr = httpSender.sendFileRequest(url, header, postBody, sliceContent, timeOut);
            try {
                resultJson = new JSONObject(resultStr);
                if (resultJson.getInt(ResponseBodyKey.CODE) == 0) {
                    return resultJson;
                } else {
                    ++retryCount;
                }
            } catch (JSONException e) {
                LOG.error("sendFileRequest result is not legal json, result {}", resultStr);
                throw e;
            }
        }
        // 如果重传多次仍然失败，则返回最后一次的结果
        return resultJson;
    }
    
    // 关闭后台线程,关闭后不可复用，必须重新生成CosCloud对象
    public void shutdown() {
        executorService.shutdown();
        httpSender.shutdown();
    }

    /**
     * 返回签名失效的时间，默认为半个小时
     * @return  返回签名失效的时间，单位为秒，默认为半个小时后
     */
    private long getExpiredTimeInSec() {
        return System.currentTimeMillis() / 1000 + DEFAULT_SIGN_EXPIRED_IN_SECONDS;
    }



    /**
     * 封装执行并行分片上传的线程的task
     *
     */
    private class SliceUploadInner implements java.util.concurrent.Callable<JSONObject> {

        private String url;
        private String bucketName;
        private String localPath;
        private String sessionId;
        private long offset;
        private int sliceSize;

        // 每一个线程执行结束，则把计数值减1
        CountDownLatch countDownLatch;

        public void setCountDownLatch(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        public SliceUploadInner(String url, String bucketName, String localPath, String sessionId,
                long offset, int sliceSize) {
            this.url = url;
            this.bucketName = bucketName;
            this.localPath = localPath;
            this.sessionId = sessionId;
            this.offset = offset;
            this.sliceSize = sliceSize;
        }

        @Override
        public JSONObject call() throws Exception {
            try {
                byte[] sliceContent = FileProcess.getFileContent(localPath, offset, sliceSize);
                JSONObject resultJson =
                    sliceUploadFileData(url, bucketName, sessionId, offset, sliceContent);
                return resultJson;
            } catch (Exception e) {
                String errMsg = new StringBuilder("SliceUploadInner send file failed!")
                    .append(" url:").append(url)
                    .append(" bucketName:").append(bucketName)
                    .append(" filePath:").append(localPath)
                    .append(" offset:").append(offset)
                    .append(" sliceSize:").append(sliceSize).toString();
                LOG.error(errMsg);
                throw e;
            } finally {
                countDownLatch.countDown();
            }
        }
    }
}


