package com.qcloud;

import java.io.File;
import java.net.URLEncoder;
import java.util.HashMap;

import org.json.JSONObject;

import com.qcloud.common.Request;
import com.qcloud.sign.FileCloudSign;
import com.qcloud.sign.HMACSHA1;

public class CosCloud {
	/**
	 * @brief Cos类
	 * @author robinslsun
	 */
	final String COSAPI_CGI_URL = "http://web.file.myqcloud.com/files/v1/";
	public enum FolderPattern {File, Folder, Both};
	protected int m_appid;
	protected String m_secret_id;
	protected String m_secret_key;
	
	/**
	 * CosCloud 构造方法
	 * @param appid			授权appid
	 * @param secret_id		授权secret_id
	 * @param secret_key	 授权secret_key
	 */
	public CosCloud(int appId, String secretId, String secretKey){
		m_appid = appId;
		m_secret_id = secretId;
		m_secret_key = secretKey;
	}
	
	/**
	 * 更新文件夹信息
	 * @param bucketName bucket名称
	 * @param folderPath 文件夹路径
	 * @param bizAttribute 更新信息
	 * @return
	 */
	public String updateFolder(String bucketName, String folderPath, String bizAttribute){
		return updateFile(bucketName, folderPath, null, bizAttribute);
	}
	
	/**
	 * 更新文件信息
	 * @param bucketName bucket名称
	 * @param folderPath 文件夹路径
	 * @param fileName 文件名
	 * @param bizAttribute 更新信息
	 * @return
	 */
	public String updateFile(String bucketName, String folderPath, String fileName, String bizAttribute){
		String url = COSAPI_CGI_URL + m_appid + "/" + bucketName + folderPath + (fileName != null ? fileName : "");
		String fileId = "/"  + m_appid + "/" + bucketName + folderPath + (fileName != null ? fileName : "");
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("op", "update");
		data.put("biz_attr", bizAttribute);
		String sign = FileCloudSign.appSignatureOnce(m_appid, m_secret_id, m_secret_key, fileId, bucketName);
		String qcloud_sign = sign.toString();
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("Authorization", qcloud_sign);
		return Request.sendRequest(url, data, "POST", header);
	}
	
	/**
	 * 删除文件夹
	 * @param bucketName bucket名称
	 * @param folderPath 文件夹路径
	 * @return
	 */
	public String deleteFolder(String bucketName, String folderPath){
		return deleteFile(bucketName, folderPath, null);
	}
	
	/**
	 * 删除文件
	 * @param bucketName bucket名称
	 * @param folderPath 文件夹路径
	 * @param fileName 文件名
	 * @return
	 */
	public String deleteFile(String bucketName, String folderPath, String fileName){
		String url = COSAPI_CGI_URL + m_appid + "/" + bucketName + folderPath + (fileName != null ? fileName : "");
		String fileId = "/"  + m_appid + "/" + bucketName + folderPath + (fileName != null ? fileName : "");
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("op", "delete");
		String sign = FileCloudSign.appSignatureOnce(m_appid, m_secret_id, m_secret_key, fileId, bucketName);
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("Authorization", sign);
		return Request.sendRequest(url, data, "POST", header);
	}
	
	/**
	 * 获取文件夹信息
	 * @param bucketName bucket名称
	 * @param folderPath 文件夹路径
	 * @return
	 */
	public String getFolderStat(String bucketName, String folderPath){
		return getFileStat(bucketName, folderPath, null);
	}
	
	/**
	 * 获取文件信息
	 * @param bucketName bucket名称
	 * @param folderPath 文件夹路径
	 * @param fileName 文件名
	 * @return
	 */
	public String getFileStat(String bucketName, String folderPath, String fileName){
		String url = COSAPI_CGI_URL + m_appid + "/" + bucketName + folderPath + (fileName != null ? fileName : "");
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("op", "stat");
		long expired = System.currentTimeMillis() / 1000 + 2592000;
		String sign = FileCloudSign.appSignature(m_appid, m_secret_id, m_secret_key, expired, bucketName);
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("Authorization", sign);
		return Request.sendRequest(url, data, "GET", header);
	}
	
	/**
	 * 创建文件夹
	 * @param bucketName bucket名称
	 * @param folderPath 文件夹路径
	 * @return
	 */
	public String createFolder(String bucketName, String folderPath){
		String url = COSAPI_CGI_URL + m_appid + "/" + bucketName + folderPath;
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("op", "create");
		long expired = System.currentTimeMillis() / 1000 + 2592000;
		String sign = FileCloudSign.appSignature(m_appid, m_secret_id, m_secret_key, expired, bucketName);
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("Authorization", sign);
		return Request.sendRequest(url, data, "POST", header);
	}
	
	/**
	 * 目录列表
	 * @param bucketName bucket名称
	 * @param folderPath 文件夹路径
	 * @param num 拉取的总数
	 * @param offset 透传字段,用于翻页,前端不需理解,需要往前/往后翻页则透传回来 
	 * @param order 默认正序(=0), 填1为反序
	 * @param pattern 拉取模式:只是文件，只是文件夹，全部
	 * @return
	 */
	public String getFolderList(String bucketName, String folderPath, int num, String offset, int order, FolderPattern pattern){
		return getFolderList(bucketName, folderPath, null, num, offset, order, pattern);
	}
	
	/**
	 * 目录列表,前缀搜索
	 * @param bucketName bucket名称
	 * @param folderPath 文件夹路径
	 * @param prefix 读取文件/文件夹前缀
	 * @param num 拉取的总数
	 * @param offset 透传字段,用于翻页,前端不需理解,需要往前/往后翻页则透传回来 
	 * @param order 默认正序(=0), 填1为反序
	 * @param pattern 拉取模式:只是文件，只是文件夹，全部
	 * @return
	 */
	public String getFolderList(String bucketName, String folderPath, String prefix, int num, String offset, int order, FolderPattern pattern){
		String url = COSAPI_CGI_URL + m_appid + "/" + bucketName + folderPath + (prefix != null ? prefix : "");
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("op", "list");
		data.put("num", num);
		data.put("offset", offset);
		data.put("order", order);
		String[] patternArray = {"eListFileOnly", "eListDirOnly", "eListBoth"};
		data.put("pattern", patternArray[pattern.ordinal()]);
		long expired = System.currentTimeMillis() / 1000 + 2592000;
		String sign = FileCloudSign.appSignature(m_appid, m_secret_id, m_secret_key, expired, bucketName);
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("Authorization", sign);
		return Request.sendRequest(url, data, "GET", header);
	}
	
	/**
	 * 单个文件上传
	 * @param bucketName bucket名称
	 * @param folderPath 文件夹路径
	 * @param fileName 上传后的文件名
	 * @param uploadFilePath 本地文件路径
	 * @return
	 */
	public String uploadFile(String bucketName, String folderPath, String fileName, String uploadFilePath){
		
		try {			
			String url = COSAPI_CGI_URL + m_appid + "/" + bucketName + folderPath + URLEncoder.encode(fileName);
			String sha1 = HMACSHA1.getFileSha1(uploadFilePath);
			HashMap<String, Object> data = new HashMap<String, Object>();
			data.put("op", "upload");
			data.put("sha", sha1);
			long expired = System.currentTimeMillis() / 1000 + 2592000;
			String sign = FileCloudSign.appSignature(m_appid, m_secret_id, m_secret_key, expired, bucketName);
			HashMap<String, String> header = new HashMap<String, String>();
			header.put("Authorization", sign);
			return Request.sendRequest(url, data, "POST", header, uploadFilePath);			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 分片上传第一步
	 * @param bucketName bucket名称
	 * @param folderPath 文件夹路径
	 * @param fileName 上传后的文件名
	 * @param uploadFilePath 本地文件路径
	 * @param sliceSize 切片大小（字节）
	 * @return
	 */
	public String sliceUploadFileFirstStep(String bucketName, String folderPath, String fileName, String uploadFilePath, int sliceSize){
		try{
			String url = COSAPI_CGI_URL + m_appid + "/" + bucketName + folderPath + URLEncoder.encode(fileName);
			String sha1 = HMACSHA1.getFileSha1(uploadFilePath);
			long fileSize = new File(uploadFilePath).length();
			HashMap<String, Object> data = new HashMap<String, Object>();
			data.put("op", "upload_slice");
			data.put("sha", sha1);
			data.put("filesize", fileSize);
			data.put("slice_size", sliceSize);
			long expired = System.currentTimeMillis() / 1000 + 2592000;
			String sign = FileCloudSign.appSignature(m_appid, m_secret_id, m_secret_key, expired, bucketName);
			HashMap<String, String> header = new HashMap<String, String>();
			header.put("Authorization", sign);
			return Request.sendRequest(url, data, "POST", header);				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 分片上传后续步骤
	 * @param bucketName bucket名称
	 * @param folderPath 文件夹路径
	 * @param fileName 上传后的文件名
	 * @param uploadFilePath 本地文件路径
	 * @param sessionId 分片上传会话ID
	 * @param offset 文件分片偏移量
	 * @param sliceSize  切片大小（字节）
	 * @return
	 */
	public String sliceUploadFileFollowStep(String bucketName, String folderPath, String fileName, String uploadFilePath,
			String sessionId, int offset, int sliceSize){
		String url = COSAPI_CGI_URL + m_appid + "/" + bucketName + folderPath + URLEncoder.encode(fileName);
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("op", "upload_slice");
		data.put("session", sessionId);
		data.put("offset", offset);
		long expired = System.currentTimeMillis() / 1000 + 2592000;
		String sign = FileCloudSign.appSignature(m_appid, m_secret_id, m_secret_key, expired, bucketName);
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("Authorization", sign);
		return Request.sendRequest(url, data, "POST", header, uploadFilePath, offset, sliceSize);
	}
	
	/**
	 * 分片上传
	 * @param bucketName bucket名称
	 * @param folderPath 文件夹路径
	 * @param fileName 上传后的文件名
	 * @param uploadFilePath 本地文件路径
	 * @return
	 */
	public String sliceUploadFile(String bucketName, String folderPath, String fileName, String uploadFilePath){
		return sliceUploadFile(bucketName, folderPath, fileName, uploadFilePath, 512 * 1024);
	}
	
	public String sliceUploadFile(String bucketName, String folderPath, String fileName, String uploadFilePath, int sliceSize){
		String result = sliceUploadFileFirstStep(bucketName, folderPath, fileName, uploadFilePath, sliceSize);
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
				int offset = data.getInt("offset");
				int retryCount = 0;
				while(true){
					result = sliceUploadFileFollowStep(bucketName, folderPath, fileName, uploadFilePath, sessionId, offset, sliceSize);
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
							offset = data.getInt("offset") + sliceSize;
						}
						else{
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
}
