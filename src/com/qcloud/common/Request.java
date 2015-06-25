package com.qcloud.common;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * @brief 请求调用类
 * @author robinslsun
 */
public class Request 
{	
	public static String sendRequest(String url, Map<String, Object> data, String requestMethod, Map<String, String> header){
		return sendRequest(url, data, requestMethod, header, null);
	}
	
	public static String sendRequest(String url, Map<String, Object> data, String requestMethod, Map<String, String> header, String fileName){
		return sendRequest(url, data, requestMethod, header, fileName, -1, 0);
	}
	
	@SuppressWarnings({ "deprecation", "resource" })
	public static String sendRequest(String url, Map<String, Object> data, String requestMethod, Map<String, String> header, String fileName, int offset, int sliceSize){
		HttpClient httpClient = new DefaultHttpClient();
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
				return EntityUtils.toString(httpResponse.getEntity());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
				MultipartEntity multipartEntity = new MultipartEntity();
				if(data != null){
					for(String key : data.keySet()){
						multipartEntity.addPart(key, new StringBody(data.get(key).toString()));
					}
				}
				//文件上传
				if(fileName != null){
					File file = new File(fileName);
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
                    	byte[] bufferOut = new byte[sliceSize];
                    	ins.read(bufferOut);
                    	ContentBody contentBody =  new ByteArrayBody(bufferOut, file.getName());//new ByteArrayBody(bytes, fileName);
                    	multipartEntity.addPart("fileContent", contentBody);
					}
				}
				httpPost.setEntity(multipartEntity);
				HttpResponse httpResponse = httpClient.execute(httpPost);
				return EntityUtils.toString(httpResponse.getEntity());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "";
	}
}
