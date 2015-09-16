package com.qcloud.cosapi.common;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 * @brief 请求调用类
 * @author robinslsun
 */
public class Request 
{	
	public static String sendRequest(String url, Map<String, Object> data, String requestMethod, Map<String, String> header, int timeOut) throws Exception{
		return sendRequest(url, data, requestMethod, header, timeOut, null);
	}
	
	public static String sendRequest(String url, Map<String, Object> data, String requestMethod, Map<String, String> header, int timeOut, String localPath) throws Exception{
		return sendRequest(url, data, requestMethod, header, timeOut, localPath, -1, 0);
	}
	
	public static String uploadFileByStream(String url, Map<String, Object> data, Map<String, String> header, int timeOut, InputStream inputStream, int length) throws Exception{
		HttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, timeOut); 
		try {
			HttpPost httpPost = new  HttpPost(url);
			httpPost.setHeader("accept", "*/*");
			httpPost.setHeader("connection", "Keep-Alive");
			httpPost.setHeader("user-agent", "qcloud-java-sdk");
			if(header != null){
            	for(String key : header.keySet()){
            		httpPost.setHeader(key, header.get(key));
            	}
            }
			MultipartEntity multipartEntity = new MultipartEntity();
			if(data != null){
				for(String key : data.keySet()){
					multipartEntity.addPart(key, new StringBody(data.get(key).toString()));
				}
			}
			byte[] bufferOut = new byte[length];
//			fileStream.skip(-length);
			inputStream.read(bufferOut, 0, length);
			ContentBody contentBody =  new ByteArrayBody(bufferOut, url.substring(url.lastIndexOf('/') + 1));
			multipartEntity.addPart("fileContent", contentBody);
			httpPost.setEntity(multipartEntity);
			HttpResponse httpResponse = httpClient.execute(httpPost);
			inputStream.close();
			return EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
		} catch (Exception e) {
			throw e;
		}
	}
	
	@SuppressWarnings({ "deprecation", "resource" })
	public static String sendRequest(String url, Map<String, Object> data, String requestMethod, Map<String, String> header, int timeOut, String localPath, long offset, int sliceSize) throws Exception{
		HttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, timeOut); 
		if(requestMethod.equals("GET")){
			try{
				String paramStr = "";
				for(String key: data.keySet()) {
		            if (!paramStr.isEmpty()) {
		                paramStr += '&';
		            }
		            paramStr += key + '=' + URLEncoder.encode(data.get(key).toString());
		        }
				if (url.indexOf('?') > 0){
					url += '&' + paramStr;
				} else {
	              url += '?' + paramStr;
				}
				HttpGet httpGet = new HttpGet(url);
				httpGet.setHeader("accept", "*/*");
				httpGet.setHeader("connection", "Keep-Alive");
				httpGet.setHeader("user-agent", "qcloud-java-sdk");
				if(header != null){
	            	for(String key : header.keySet()){
	            		httpGet.setHeader(key, header.get(key));
	            	}
	            }
				HttpResponse httpResponse = httpClient.execute(httpGet);
				return EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
			} catch (Exception e) {
				throw e;
			}
		}
		else{
			try {
				HttpPost httpPost = new  HttpPost(url);
				httpPost.setHeader("accept", "*/*");
				httpPost.setHeader("connection", "Keep-Alive");
				httpPost.setHeader("user-agent", "qcloud-java-sdk");
				if(header != null){
	            	for(String key : header.keySet()){
	            		httpPost.setHeader(key, header.get(key));
	            	}
	            }
			    
				if(header.containsKey("Content-Type") && header.get("Content-Type").equals("application/json")){
					JSONObject jsonMap= new JSONObject(data);
					String json = jsonMap.toString();				
			        StringEntity stringEntity = new StringEntity(json);
			        httpPost.setEntity(stringEntity);
				}
				else{				
					MultipartEntity multipartEntity = new MultipartEntity();
					if(data != null){
						for(String key : data.keySet()){
							multipartEntity.addPart(key, new StringBody(data.get(key).toString()));
						}
					}
					//文件上传
					if(localPath != null){
						File file = new File(localPath);
						if(offset == -1){
							//单文件上传
							FileBody fileBody = new FileBody(file);
							multipartEntity.addPart("fileContent", fileBody);
							httpPost.setEntity(multipartEntity);
						}
						else{
							//分片上传
							DataInputStream ins = new DataInputStream(new FileInputStream(file));
							ins.skip(offset);
							int len = (int)(offset + sliceSize > file.length() ? file.length() - offset : sliceSize);
	                    				byte[] bufferOut = new byte[len];
	                    				ins.read(bufferOut);
	                    				ContentBody contentBody =  new ByteArrayBody(bufferOut, file.getName());//new ByteArrayBody(bytes, fileName);
	                    				multipartEntity.addPart("fileContent", contentBody);
						}
					}
					httpPost.setEntity(multipartEntity);
				}
				HttpResponse httpResponse = httpClient.execute(httpPost);
				return EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
			} catch (Exception e) {
				throw e;
			}
		}
	}
}
