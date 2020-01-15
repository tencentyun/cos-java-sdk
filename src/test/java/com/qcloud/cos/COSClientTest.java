package com.qcloud.cos;

import static org.junit.Assert.*;

import org.json.JSONObject;

import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.Before;
import org.junit.Test;

import com.qcloud.cos.http.ResponseBodyKey;
import com.qcloud.cos.meta.FileAuthority;
import com.qcloud.cos.meta.InsertOnly;
import com.qcloud.cos.request.CreateFolderRequest;
import com.qcloud.cos.request.DelFileRequest;
import com.qcloud.cos.request.DelFolderRequest;
import com.qcloud.cos.request.ListFolderRequest;
import com.qcloud.cos.request.MoveFileRequest;
import com.qcloud.cos.request.StatFileRequest;
import com.qcloud.cos.request.StatFolderRequest;
import com.qcloud.cos.request.UpdateFileRequest;
import com.qcloud.cos.request.UpdateFolderRequest;
import com.qcloud.cos.request.UploadFileRequest;

/**
 * @author chengwu CosCloud UT
 */
public class COSClientTest {
	// 设置用户属性, 包括appid, secretId和SecretKey
	// 这些属性可以通过cos控制台获取(https://console.qcloud.com/cos)
	private int appId = 10022105;
	private String secretId = "xxx";
	private String secretKey = "xxx";
	// 初始化cosClient
	private COSClient cosClient = new COSClient(appId, secretId, secretKey);
	// 设置要操作的bucket
	private String bucketName = "chengwu";
	
	@Before
	public void clean() {
		// 删除测试文件
		DelFileRequest delFileRequest = new DelFileRequest(bucketName, "/bigfile.txt");
		cosClient.delFile(delFileRequest);
		delFileRequest.setCosPath("/sample_file.txt");
		cosClient.delFile(delFileRequest);
		delFileRequest.setCosPath("/sample_file_move.txt");
		cosClient.delFile(delFileRequest);
		
		// 删除目录
		DelFolderRequest delFolderRequest = new DelFolderRequest(bucketName, "/xxsample_folder/");
		cosClient.delFolder(delFolderRequest);
	}

	@Test
	public void testCosClient() {
		try {


			///////////////////////////////////////////////////////////////
			// 文件操作 //
			///////////////////////////////////////////////////////////////
			// 1. 上传文件(默认不覆盖)
			// 将本地的empty.txt上传到bucket下的根分区下,并命名为sample_file.txt
			// 默认不覆盖, 如果cos上已有文件, 则返回错误
			String cosFilePath = "/sample_file.txt";
			String localFilePath1 = "src/test/resources/empty.txt";
			UploadFileRequest uploadFileRequest = new UploadFileRequest(bucketName, cosFilePath, localFilePath1);
			String uploadFileRet = cosClient.uploadFile(uploadFileRequest);
			JSONObject uploadFileJson = new JSONObject(uploadFileRet);
			assertThat(uploadFileJson.getInt(ResponseBodyKey.CODE), equalTo(0));

			// 2. 上传文件(覆盖)
			// 将本地的local_file_1.txt上传到bucket下的根分区下,并命名为sample_file.txt
			String localFilePath2 = "src/test/resources/local_file_1.txt";
			UploadFileRequest overWriteFileRequest = new UploadFileRequest(bucketName, cosFilePath, localFilePath2);
			overWriteFileRequest.setInsertOnly(InsertOnly.OVER_WRITE);
			String overWriteFileRet = cosClient.uploadFile(overWriteFileRequest);
			JSONObject overWriteFileJson = new JSONObject(overWriteFileRet);
			assertThat(overWriteFileJson.getInt(ResponseBodyKey.CODE), equalTo(0));

			// 3. 获取文件属性
			StatFileRequest statFileRequest = new StatFileRequest(bucketName, cosFilePath);
			String statFileRet = cosClient.statFile(statFileRequest);
			JSONObject statFileJson = new JSONObject(statFileRet);
			assertThat(statFileJson.getInt(ResponseBodyKey.CODE), equalTo(0));

			// 4. 更新文件属性
			UpdateFileRequest updateFileRequest = new UpdateFileRequest(bucketName, cosFilePath);
			updateFileRequest.setBizAttr("测试目录");
			updateFileRequest.setAuthority(FileAuthority.WPRIVATE);
			updateFileRequest.setCacheControl("no cache");
			updateFileRequest.setContentDisposition("cos_sample.txt");
			updateFileRequest.setContentLanguage("english");
			updateFileRequest.setContentType("application/json");
			updateFileRequest.setContentEncoding("gzip");
			updateFileRequest.setXCosMeta("x-cos-meta-xxx", "xxx");
			updateFileRequest.setXCosMeta("x-cos-meta-yyy", "yyy");
			String updateFileRet = cosClient.updateFile(updateFileRequest);
			JSONObject updateFileJson = new JSONObject(updateFileRet);
			assertThat(updateFileJson.getInt(ResponseBodyKey.CODE), equalTo(0));

			// 5. 更新目录后再次获取属性
			statFileRet = cosClient.statFile(statFileRequest);
			statFileJson = new JSONObject(statFileRet);
			assertThat(statFileJson.getInt(ResponseBodyKey.CODE), equalTo(0));
			assertThat(statFileJson.getJSONObject("data").getString("biz_attr"), equalTo("测试目录"));
			assertTrue(statFileJson.getJSONObject("data").has("custom_headers"));
			JSONObject customHeadersJson = statFileJson.getJSONObject("data").getJSONObject("custom_headers");
			assertTrue(customHeadersJson.has("Cache-Control"));
			assertThat(customHeadersJson.getString("Cache-Control"), equalTo("no cache"));
			assertTrue(customHeadersJson.has("Content-Language"));
			assertThat(customHeadersJson.getString("Content-Language"), equalTo("english"));
			assertTrue(customHeadersJson.has("Content-Disposition"));
			assertThat(customHeadersJson.getString("Content-Disposition"), equalTo("cos_sample.txt"));
			assertTrue(customHeadersJson.has("Content-Type"));
			assertThat(customHeadersJson.getString("Content-Type"), equalTo("application/json"));
			assertTrue(customHeadersJson.has("Content-Encoding"));
			assertThat(customHeadersJson.getString("Content-Encoding"), equalTo("gzip"));
			assertTrue(customHeadersJson.has("x-cos-meta-xxx"));
			assertThat(customHeadersJson.getString("x-cos-meta-xxx"), equalTo("xxx"));
			assertTrue(customHeadersJson.has("x-cos-meta-yyy"));
			assertThat(customHeadersJson.getString("x-cos-meta-yyy"), equalTo("yyy"));

			// 6. 移动文件
			String moveFilePath = "/sample_file_move.txt";
			MoveFileRequest moveFileRequest = new MoveFileRequest(bucketName, cosFilePath, moveFilePath);
			String moveFileRet = cosClient.moveFile(moveFileRequest);
			JSONObject moveFileJson = new JSONObject(moveFileRet);
			assertThat(moveFileJson.getInt(ResponseBodyKey.CODE), equalTo(0));

			// 7. 删除文件
			DelFileRequest delFileRequest = new DelFileRequest(bucketName, moveFilePath);
			String delFileRet = cosClient.delFile(delFileRequest);
			JSONObject delFileJson = new JSONObject(delFileRet);
			assertThat(delFileJson.getInt(ResponseBodyKey.CODE), equalTo(0));

			// 8. 上传大文件, 将本地文件bigfile.txt上传到cos上, 并命名big_file.txt
			String largeCosFilePath = "/bigfile.txt";
			String largeLocalFilePath = "src/test/resources/bigfile.txt";
			uploadFileRequest = new UploadFileRequest(bucketName, largeCosFilePath, largeLocalFilePath);
			String uploadBigFileRet = cosClient.uploadFile(uploadFileRequest);
			JSONObject uploadBigFileJson = new JSONObject(uploadBigFileRet);
			assertThat(uploadBigFileJson.getInt(ResponseBodyKey.CODE), equalTo(0));
			
			// 9. 删除大文件
			delFileRequest = new DelFileRequest(bucketName, largeCosFilePath);
			delFileRet = cosClient.delFile(delFileRequest);
			delFileJson = new JSONObject(delFileRet);
			assertThat(delFileJson.getInt(ResponseBodyKey.CODE), equalTo(0));

			///////////////////////////////////////////////////////////////
			// 目录操作 //
			///////////////////////////////////////////////////////////////
			// 1. 生成目录, 目录名为sample_folder
			String cosFolderPath = "/xxsample_folder/";
			CreateFolderRequest createFolderRequest = new CreateFolderRequest(bucketName, cosFolderPath);
			String createFolderRet = cosClient.createFolder(createFolderRequest);
			JSONObject createFolderJson = new JSONObject(createFolderRet);
			assertThat(createFolderJson.getInt(ResponseBodyKey.CODE), equalTo(0));

			// 2. 更新目录的biz_attr属性
			UpdateFolderRequest updateFolderRequest = new UpdateFolderRequest(bucketName, cosFolderPath);
			updateFolderRequest.setBizAttr("这是一个测试目录");
			String updateFolderRet = cosClient.updateFolder(updateFolderRequest);
			JSONObject updateFolderJson = new JSONObject(updateFolderRet);
			assertThat(updateFolderJson.getInt(ResponseBodyKey.CODE), equalTo(0));

			// 3. 获取目录属性
			StatFolderRequest statFolderRequest = new StatFolderRequest(bucketName, cosFolderPath);
			String statFolderRet = cosClient.statFolder(statFolderRequest);
			JSONObject statFolderJson = new JSONObject(statFolderRet);
			assertThat(statFolderJson.getInt(ResponseBodyKey.CODE), equalTo(0));

			// 4. list目录, 获取目录下的成员
			ListFolderRequest listFolderRequest = new ListFolderRequest(bucketName, cosFolderPath);
			String listFolderRet = cosClient.listFolder(listFolderRequest);
			JSONObject listFolderJson = new JSONObject(listFolderRet);
			assertThat(listFolderJson.getInt(ResponseBodyKey.CODE), equalTo(0));

			// 5. 删除目录
			DelFolderRequest delFolderRequest = new DelFolderRequest(bucketName, cosFolderPath);
			String delFolderRet = cosClient.delFolder(delFolderRequest);
			JSONObject delFolderJson = new JSONObject(delFolderRet);
			assertThat(delFolderJson.getInt(ResponseBodyKey.CODE), equalTo(0));

			// 关闭释放资源
			cosClient.shutdown();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
