package com.qcloud.cosapi.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;

import org.json.JSONObject;

import com.qcloud.cosapi.common.HMACSHA1;
import com.qcloud.cosapi.common.Request;
import com.qcloud.cosapi.common.Sign;

public class CosCloud {
	/**
	 * @brief Cos类
	 * @author robinslsun
	 */
	final String COSAPI_CGI_URL = "http://web.file.myqcloud.com/files/v1/";
	public enum FolderPattern {File, Folder, Both};
	private int appId;
	private String secretId;
	private String secretKey;
	private int timeOut;
	
	/**
	 * CosCloud 构造方法
	 * @param appId			授权appid
	 * @param secretId		授权secret id
	 * @param secretKey	 授权secret key
	 */
	public CosCloud(int appId, String secretId, String secretKey){
		this(appId, secretId, secretKey, 60);
	}
	
	/**
	 * CosCloud 构造方法
	 * @param appId			授权appid
	 * @param secretId		授权secret id
	 * @param secretKey	 授权secret key
	 * @param timeOut	网络超时
	 */
	public CosCloud(int appId, String secretId, String secretKey, int timeOut){
		this.appId = appId;
		this.secretId = secretId;
		this.secretKey = secretKey;
		this.timeOut = timeOut * 1000;
	}
	
	/**
	 * 远程路径Encode处理
	 * @param remotePath
	 * @return
	 */
	private String encodeRemotePath(String remotePath)
    {
		if(remotePath.equals("/")){
			return remotePath;
		}
		boolean endWith = remotePath.endsWith("/");
        String[] part = remotePath.split("/");
        remotePath = "";
        for(String s : part){
        	if (!s.equals(""))
            {
        		if(remotePath != ""){
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
	 * @param remotePath 要标准化的远程路径
	 * @return
	 */
    private String standardizationRemotePath(String remotePath)
    {
        if (!remotePath.startsWith("/"))
        {
            remotePath = "/" + remotePath;
        }
        if (!remotePath.endsWith("/"))
        {
            remotePath += "/";
        }
        return remotePath;
    }
	
	/**
	 * 更新文件夹信息
	 * @param bucketName bucket名称
	 * @param remotePath 远程文件夹路径
	 * @param bizAttribute 更新信息
	 * @return
	 * @throws Exception 
	 */
	public String updateFolder(String bucketName, String remotePath, String bizAttribute) throws Exception{
		remotePath = standardizationRemotePath(remotePath);
		return updateFile(bucketName, remotePath, bizAttribute);
	}
	
	/**
	 * 更新文件信息
	 * @param bucketName bucket名称
	 * @param remotePath 远程文件路径
	 * @param bizAttribute 更新信息
	 * @return
	 * @throws Exception 
	 */
	public String updateFile(String bucketName, String remotePath, String bizAttribute) throws Exception{
		String url = COSAPI_CGI_URL + appId + "/" + bucketName + encodeRemotePath(remotePath);
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("op", "update");
		data.put("biz_attr", bizAttribute);
		String sign = Sign.appSignatureOnce(appId, secretId, secretKey, (remotePath.startsWith("/") ? "" : "/") + remotePath, bucketName);
		String qcloud_sign = sign.toString();
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("Authorization", qcloud_sign);
		header.put("Content-Type","application/json");
		return Request.sendRequest(url, data, "POST", header, timeOut);
	}
	
	/**
	 * 删除文件夹
	 * @param bucketName bucket名称
	 * @param remotePath 远程文件夹路径
	 * @return
	 * @throws Exception 
	 */
	public String deleteFolder(String bucketName, String remotePath) throws Exception{
		remotePath = standardizationRemotePath(remotePath);
		return deleteFile(bucketName, remotePath);
	}
	
	/**
	 * 删除文件
	 * @param bucketName bucket名称
	 * @param remotePath 远程文件路径
	 * @return
	 * @throws Exception 
	 */
	public String deleteFile(String bucketName, String remotePath) throws Exception{
		if(remotePath.equals("/")){
			throw new Exception("can not delete bucket using aip! go to http://console.qcloud.com/cos to operate bucket");
		}
		String url = COSAPI_CGI_URL + appId + "/" + bucketName + encodeRemotePath(remotePath);
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("op", "delete");
		String sign = Sign.appSignatureOnce(appId, secretId, secretKey, (remotePath.startsWith("/") ? "" : "/") + remotePath, bucketName);
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("Authorization", sign);
		header.put("Content-Type","application/json");
		return Request.sendRequest(url, data, "POST", header, timeOut);
	}
	
	/**
	 * 获取文件夹信息
	 * @param bucketName bucket名称
	 * @param remotePath 远程文件夹路径
	 * @return
	 * @throws Exception 
	 */
	public String getFolderStat(String bucketName, String remotePath) throws Exception{
		remotePath = standardizationRemotePath(remotePath);
		return getFileStat(bucketName, remotePath);
	}
	
	/**
	 * 获取文件信息
	 * @param bucketName bucket名称
	 * @param remotePath 远程文件路径
	 * @return
	 * @throws Exception 
	 */
	public String getFileStat(String bucketName, String remotePath) throws Exception{
		String url = COSAPI_CGI_URL + appId + "/" + bucketName + encodeRemotePath(remotePath);
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("op", "stat");
		long expired = System.currentTimeMillis() / 1000 + 60;
		String sign = Sign.appSignature(appId, secretId, secretKey, expired, bucketName);
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("Authorization", sign);
		return Request.sendRequest(url, data, "GET", header, timeOut);
	}
	
	/**
	 * 创建文件夹
	 * @param bucketName bucket名称
	 * @param remotePath 远程文件夹路径
	 * @return
	 * @throws Exception 
	 */
	public String createFolder(String bucketName, String remotePath) throws Exception{
		remotePath = standardizationRemotePath(remotePath);
		String url = COSAPI_CGI_URL + appId + "/" + bucketName + encodeRemotePath(remotePath);
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("op", "create");
		long expired = System.currentTimeMillis() / 1000 + 60;
		String sign = Sign.appSignature(appId, secretId, secretKey, expired, bucketName);
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("Authorization", sign);
		header.put("Content-Type","application/json");
		return Request.sendRequest(url, data, "POST", header, timeOut);
	}
	
	/**
	 * 目录列表
	 * @param bucketName bucket名称
	 * @param remotePath 远程文件夹路径
	 * @param num 拉取的总数
	 * @param context 透传字段，查看第一页，则传空字符串。若需要翻页，需要将前一页返回值中的context透传到参数中。order用于指定翻页顺序。若order填0，则从当前页正序/往下翻页；若order填1，则从当前页倒序/往上翻页。
	 * @param order 默认正序(=0), 填1为反序
	 * @param pattern 拉取模式:只是文件，只是文件夹，全部
	 * @return
	 * @throws Exception 
	 */
	public String getFolderList(String bucketName, String remotePath, int num, String context, int order, FolderPattern pattern) throws Exception{
		remotePath = standardizationRemotePath(remotePath);
		return getFolderList(bucketName, remotePath, "", num, context, order, pattern);
	}
	
	/**
	 * 目录列表,前缀搜索
	 * @param bucketName bucket名称
	 * @param remotePath 远程文件夹路径
	 * @param prefix 读取文件/文件夹前缀
	 * @param num 拉取的总数
	 * @param context 透传字段，查看第一页，则传空字符串。若需要翻页，需要将前一页返回值中的context透传到参数中。order用于指定翻页顺序。若order填0，则从当前页正序/往下翻页；若order填1，则从当前页倒序/往上翻页。
	 * @param order 默认正序(=0), 填1为反序
	 * @param pattern 拉取模式:只是文件，只是文件夹，全部
	 * @return
	 * @throws Exception 
	 */
	public String getFolderList(String bucketName, String remotePath, String prefix, int num, String context, int order, FolderPattern pattern) throws Exception{
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
	
	/**
	 * 单个文件上传
	 * @param bucketName bucket名称
	 * @param remotePath 远程文件路径
	 * @param localPath 本地文件路径
	 * @return
	 * @throws Exception 
	 */
	public String uploadFile(String bucketName, String remotePath, String localPath) throws Exception{
		
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
	
	/**
	 * 流单个文件上传
	 * @param bucketName bucket名称
	 * @param remotePath 远程文件路径
	 * @param inputStream 文件流
	 * @return
	 * @throws Exception
	 */
	public String uploadFile(String bucketName, String remotePath, InputStream inputStream) throws Exception{		
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
	
	/**
	 * 分片上传第一步
	 * @param bucketName bucket名称
	 * @param remotePath 远程文件路径
	 * @param localPath 本地文件路径
	 * @param sliceSize 切片大小（字节）
	 * @return
	 * @throws Exception 
	 */
	public String sliceUploadFileFirstStep(String bucketName, String remotePath, String localPath, int sliceSize) throws Exception{
		try{
			String url = COSAPI_CGI_URL + appId + "/" + bucketName + encodeRemotePath(remotePath);
			String sha1 = HMACSHA1.getFileSha1(localPath);
			System.out.println(sha1);
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
	
	/**
	 * 分片上传后续步骤
	 * @param bucketName bucket名称
	 * @param remotePath 远程文件路径
	 * @param localPath 本地文件路径
	 * @param sessionId 分片上传会话ID
	 * @param offset 文件分片偏移量
	 * @param sliceSize  切片大小（字节）
	 * @return
	 * @throws Exception 
	 */
	public String sliceUploadFileFollowStep(String bucketName, String remotePath, String localPath,
			String sessionId, long offset, int sliceSize) throws Exception{
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
	
	/**
	 * 分片上传，默认切片大小为512K
	 * @param bucketName bucket名称
	 * @param remotePath 远程文件路径
	 * @param localPath 本地文件路径
	 * @return
	 * @throws Exception 
	 */
	public String sliceUploadFile(String bucketName, String remotePath, String localPath) throws Exception{
		return sliceUploadFile(bucketName, remotePath, localPath, 512 * 1024);
	}
	
	/**
	 * 分片上传
	 * @param bucketName bucket名称
	 * @param remotePath 远程文件路径
	 * @param localPath 本地文件路径
	 * @param sliceSize 切片大小（字节）
	 * @return
	 * @throws Exception 
	 */
	public String sliceUploadFile(String bucketName, String remotePath, String localPath, int sliceSize) throws Exception{
		String result = sliceUploadFileFirstStep(bucketName, remotePath, localPath, sliceSize);
		try{
			JSONObject jsonObject = new JSONObject(result);
			int code = jsonObject.getInt("code");
			if(code != 0){
				return result;
			}
			JSONObject data = jsonObject.getJSONObject("data");
			if(data.has("access_url")){
				String accessUrl = data.getString("access_url");
				System.out.println("命中秒传：" + accessUrl);
				return result;
			}
			else{
				String sessionId = data.getString("session");
				sliceSize = data.getInt("slice_size"); 
				long offset = data.getLong("offset");
				int retryCount = 0;
				while(true){
					System.out.println("offset : " + offset);
					result = sliceUploadFileFollowStep(bucketName, remotePath, localPath, sessionId, offset, sliceSize);
					System.out.println(result);
					jsonObject = new JSONObject(result);
					code = jsonObject.getInt("code");
					if(code != 0){
						//当上传失败后会重试3次
						if(retryCount < 3){
							retryCount++;
							System.out.println("重试....");
						}
						else{
							return result;
						}
					}
					else{
						data = jsonObject.getJSONObject("data");
						if(data.has("offset")){
							offset = data.getLong("offset") + sliceSize;
						}
						else{
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
