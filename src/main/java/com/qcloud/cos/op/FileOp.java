package com.qcloud.cos.op;

import java.io.InputStream;

import org.json.JSONObject;

import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.common_utils.CommonCodecUtils;
import com.qcloud.cos.common_utils.CommonFileUtils;
import com.qcloud.cos.exception.AbstractCosException;
import com.qcloud.cos.exception.ParamException;
import com.qcloud.cos.exception.UnknownException;
import com.qcloud.cos.http.AbstractCosHttpClient;
import com.qcloud.cos.http.HttpContentType;
import com.qcloud.cos.http.HttpMethod;
import com.qcloud.cos.http.HttpRequest;
import com.qcloud.cos.http.RequestBodyKey;
import com.qcloud.cos.http.RequestBodyValue;
import com.qcloud.cos.http.RequestHeaderKey;
import com.qcloud.cos.http.RequestHeaderValue;
import com.qcloud.cos.http.ResponseBodyKey;
import com.qcloud.cos.request.DelFileRequest;
import com.qcloud.cos.request.MoveFileRequest;
import com.qcloud.cos.request.StatFileRequest;
import com.qcloud.cos.request.UpdateFileRequest;
import com.qcloud.cos.request.UploadFileRequest;
import com.qcloud.cos.request.UploadSliceFileRequest;
import com.qcloud.cos.sign.Credentials;
import com.qcloud.cos.sign.Sign;

/**
 * @author chengwu 
 * 此类封装了文件操作
 */
public class FileOp extends BaseOp {

	public FileOp(ClientConfig config, Credentials cred, AbstractCosHttpClient client) {
		super(config, cred, client);
	}

	/**
	 * 更新文件属性请求
	 * 
	 * @param request
	 *            更新文件属性请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
	 * @throws AbstractCosException
	 *             SDK定义的COS异常, 通常是输入参数有误或者环境问题(如网络不通)
	 */
	public String updateFile(final UpdateFileRequest request) throws AbstractCosException {
		request.check_param();

		String url = buildUrl(request);
		String sign = Sign.getOneEffectiveSign(request.getBucketName(), request.getCosPath(), this.cred);

		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setUrl(url);
		httpRequest.addHeader(RequestHeaderKey.Authorization, sign);
		httpRequest.addHeader(RequestHeaderKey.Content_TYPE, RequestHeaderValue.ContentType.JSON);
		httpRequest.addHeader(RequestHeaderKey.USER_AGENT, this.config.getUserAgent());
		httpRequest.addParam(RequestBodyKey.OP, RequestBodyValue.OP.UPDATE);
		int updateFlag = request.getUpdateFlag();
		httpRequest.addParam(RequestBodyKey.UPDATE_FLAG, String.valueOf(updateFlag));
		if ((updateFlag & 0x01) != 0) {
			httpRequest.addParam(RequestBodyKey.BIZ_ATTR, request.getBizAttr());
		}
		if ((updateFlag & 0x40) != 0) {
			String customHeaderStr = new JSONObject(request.getCustomHeaders()).toString();
			httpRequest.addParam(RequestBodyKey.CUSTOM_HEADERS, customHeaderStr);
		}
		if ((updateFlag & 0x80) != 0) {
			httpRequest.addParam(RequestBodyKey.AUTHORITY, request.getAuthority().toString());
		}
		httpRequest.setMethod(HttpMethod.POST);
		httpRequest.setContentType(HttpContentType.APPLICATION_JSON);
		return httpClient.sendHttpRequest(httpRequest);
	}

	/**
	 * 删除文件请求
	 * 
	 * @param request
	 *            删除文件请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
	 * @throws AbstractCosException
	 *             SDK定义的COS异常, 通常是输入参数有误或者环境问题(如网络不通)
	 */
	public String delFile(DelFileRequest request) throws AbstractCosException {
		return super.delBase(request);
	}
	
	/**
	 * 移动文件请求(重命名)
	 * 
	 * @param request
	 *            移动文件请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
	 * @throws AbstractCosException
	 *             SDK定义的COS异常, 通常是输入参数有误或者环境问题(如网络不通)
	 */
	public String moveFile(MoveFileRequest request) throws AbstractCosException {
		request.check_param();

		String url = buildUrl(request);
		long signExpired = System.currentTimeMillis() / 1000 + this.config.getSignExpired();
		String sign = Sign.getPeriodEffectiveSign(request.getBucketName(), request.getCosPath(), this.cred, signExpired);

		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setUrl(url);
		httpRequest.addHeader(RequestHeaderKey.Authorization, sign);
		httpRequest.addHeader(RequestHeaderKey.Content_TYPE, RequestHeaderValue.ContentType.JSON);
		httpRequest.addHeader(RequestHeaderKey.USER_AGENT, this.config.getUserAgent());
		httpRequest.addParam(RequestBodyKey.OP, RequestBodyValue.OP.MOVE);
		httpRequest.addParam(RequestBodyKey.DEST_FIELD, request.getDstCosPath());
		httpRequest.addParam(RequestBodyKey.TO_OVER_WRITE, String.valueOf(request.getOverWrite().ordinal()));
		httpRequest.setMethod(HttpMethod.POST);
		httpRequest.setContentType(HttpContentType.APPLICATION_JSON);
		return httpClient.sendHttpRequest(httpRequest);
	}
	
	/**
	 * 获取文件属性请求
	 * 
	 * @param request
	 *            获取文件属性请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
	 * @throws AbstractCosException
	 *             SDK定义的COS异常, 通常是输入参数有误或者环境问题(如网络不通)
	 */
	public String statFile(StatFileRequest request) throws AbstractCosException {
		return super.statBase(request);
	}

	/**
	 * 上传文件请求, 对小文件(8MB以下使用单文件上传接口）, 大文件使用分片上传接口
	 * 
	 * @param request
	 *            上传文件请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
	 * @throws AbstractCosException
	 *             SDK定义的COS异常, 通常是输入参数有误或者环境问题(如网络不通)
	 */
	public String uploadFile(UploadFileRequest request) throws AbstractCosException {
		request.check_param();

		String localPath = request.getLocalPath();
		long fileSize = 0;
		try {
			fileSize = CommonFileUtils.getFileLength(localPath);
		} catch (Exception e) {
			throw new UnknownException(e.toString());
		}

		long suitSingleFileSize = 8 * 1024 * 1024;
		if (fileSize < suitSingleFileSize) {
			return uploadSingleFile(request);
		} else {
			UploadSliceFileRequest sliceRequest = new UploadSliceFileRequest(request);
			sliceRequest.setInsertOnly(request.getInsertOnly());
			return uploadSliceFile(sliceRequest);
		}
	}

	/**
	 * 上传单文件请求, 不分片
	 * 
	 * @param request
	 *            上传文件请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
	 * @throws AbstractCosException
	 *             SDK定义的COS异常, 通常是输入参数有误或者环境问题(如网络不通)
	 */
	public String uploadSingleFile(UploadFileRequest request) throws AbstractCosException {
		request.check_param();

		String localPath = request.getLocalPath();
		long fileSize = 0;
		try {
			fileSize = CommonFileUtils.getFileLength(localPath);
		} catch (Exception e) {
			throw new UnknownException(e.toString());
		}
		// 单文件上传上限不超过20MB
		if (fileSize > 20 * 1024 * 1024) {
			throw new ParamException("file is to big, please use uploadFile interface!");
		}

		String fileContent = "";
		String shaDigest = "";
		try {

			fileContent = CommonFileUtils.getFileContent(localPath);
			shaDigest = CommonCodecUtils.getEntireFileSha1(localPath);
		} catch (Exception e) {
			throw new UnknownException(e.toString());
		}

		String url = buildUrl(request);
		long signExpired = System.currentTimeMillis() / 1000 + this.config.getSignExpired();
		String sign = Sign.getPeriodEffectiveSign(request.getBucketName(), request.getCosPath(), this.cred, signExpired);

		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setUrl(url);
		httpRequest.addHeader(RequestHeaderKey.Authorization, sign);
		httpRequest.addHeader(RequestHeaderKey.USER_AGENT, this.config.getUserAgent());

		httpRequest.addParam(RequestBodyKey.OP, RequestBodyValue.OP.UPLOAD);
		httpRequest.addParam(RequestBodyKey.SHA, shaDigest);
		httpRequest.addParam(RequestBodyKey.BIZ_ATTR, request.getBizAttr());
		httpRequest.addParam(RequestBodyKey.FILE_CONTENT, fileContent);
		httpRequest.addParam(RequestBodyKey.INSERT_ONLY, String.valueOf(request.getInsertOnly().ordinal()));

		httpRequest.setMethod(HttpMethod.POST);
		httpRequest.setContentType(HttpContentType.MULTIPART_FORM_DATA);

		return httpClient.sendHttpRequest(httpRequest);
	}

	/**
	 * 分片上传文件
	 * 
	 * @param request
	 *            分片上传请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":$mess}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
	 * @throws AbstractCosException
	 *             SDK定义的COS异常, 通常是输入参数有误或者环境问题(如网络不通)
	 */
	public String uploadSliceFile(UploadSliceFileRequest request) throws AbstractCosException {
		request.check_param();
		String controlRet = uploadSliceControl(request);
		JSONObject controlRetJson = new JSONObject(controlRet);
		// 如果控制分片已经出错, 则返回
		if (controlRetJson.getInt(ResponseBodyKey.CODE) != 0) {
			return controlRet;
		}
		// 命中秒传
		if (controlRetJson.getJSONObject(ResponseBodyKey.DATA).has(ResponseBodyKey.Data.ACCESS_URL)) {
			return controlRet;
		}
		int sliceSize = controlRetJson.getJSONObject(ResponseBodyKey.DATA).getInt(ResponseBodyKey.Data.SLICE_SIZE);
		long offset = controlRetJson.getJSONObject(ResponseBodyKey.DATA).getLong(ResponseBodyKey.Data.OFFSET);
		String session = controlRetJson.getJSONObject(ResponseBodyKey.DATA).getString(ResponseBodyKey.Data.SESSION);
		String localPath = request.getLocalPath();
		InputStream inputStream = null;
		try {
			long fileSize = CommonFileUtils.getFileLength(localPath);
			inputStream = CommonFileUtils.getFileInputStream(localPath);
			inputStream.skip(offset);
			String uploadDataRet = "";
			while (offset < fileSize) {
				String sliceContent = CommonFileUtils.getFileContent(inputStream, 0, sliceSize);
				uploadDataRet = uploadSliceData(request, sliceContent, session, offset);
				JSONObject dataRetJson = new JSONObject(uploadDataRet);
				if (dataRetJson.getInt(ResponseBodyKey.CODE) != 0) {
					return uploadDataRet;
				} else {
					if (dataRetJson.getJSONObject(ResponseBodyKey.DATA).has(ResponseBodyKey.Data.ACCESS_URL)) {
						return uploadDataRet;
					}
				}
				offset += sliceSize;
			}
			return uploadDataRet;
		} catch (Exception e) {
			throw new UnknownException(e.getMessage());
		} finally {
			CommonFileUtils.closeFileStream(inputStream, localPath);
		}
	}

	/**
	 * 上传控制分片
	 * 
	 * @param request
	 *            分片上传请求
	 * @return JSON格式的字符串, 格式为{"code":$code, "data":{}}, code为0表示成功,
	 *         data为一个JSON结构体,详情请参见WIKI
	 * @throws AbstractCosException
	 *             SDK定义的COS异常, 通常是输入参数有误或者环境问题(如网络不通)
	 */
	private String uploadSliceControl(UploadSliceFileRequest request) throws AbstractCosException {
		String url = buildUrl(request);
		long signExpired = System.currentTimeMillis() / 1000 + this.config.getSignExpired();
		String sign = Sign.getPeriodEffectiveSign(request.getBucketName(), request.getCosPath(), this.cred, signExpired);

		long fileSize = 0;
		String shaDigest = "";
		try {
			String localPath = request.getLocalPath();
			fileSize = CommonFileUtils.getFileLength(localPath);
			shaDigest = CommonCodecUtils.getEntireFileSha1(localPath);
		} catch (Exception e) {
			throw new UnknownException(e.toString());
		}

		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setUrl(url);
		httpRequest.addHeader(RequestHeaderKey.Authorization, sign);
		httpRequest.addHeader(RequestHeaderKey.USER_AGENT, this.config.getUserAgent());

		httpRequest.addParam(RequestBodyKey.OP, RequestBodyValue.OP.UPLOAD_SLICE);
		httpRequest.addParam(RequestBodyKey.SHA, shaDigest);
		httpRequest.addParam(RequestBodyKey.FILE_SIZE, String.valueOf(fileSize));
		httpRequest.addParam(RequestBodyKey.SLICE_SIZE, String.valueOf(request.getSliceSize()));
		httpRequest.addParam(RequestBodyKey.BIZ_ATTR, request.getBizAttr());
		httpRequest.addParam(RequestBodyKey.INSERT_ONLY, String.valueOf(request.getInsertOnly().ordinal()));

		return httpClient.sendHttpRequest(httpRequest);
	}

	/**
	 * 上传分片数据
	 * 
	 * @param request
	 *            分片上传请求
	 * @param sliceContent
	 *            分片内容
	 * @param session
	 *            session会话值
	 * @param offset
	 *            分片偏移量
	 * @return JSON格式的字符串, 格式为{"code":$code, "message":"$mess"}, code为0表示成功,
	 *         其他为失败, message为success或者失败原因
	 * @throws AbstractCosException
	 *             SDK定义的COS异常, 通常是输入参数有误或者环境问题(如网络不通)
	 */
	private String uploadSliceData(UploadSliceFileRequest request, String sliceContent, String session, long offset)
			throws AbstractCosException {
		String url = buildUrl(request);
		long signExpired = System.currentTimeMillis() / 1000 + this.config.getSignExpired();
		String sign = Sign.getPeriodEffectiveSign(request.getBucketName(), request.getCosPath(), this.cred, signExpired);

		HttpRequest httpRequest = new HttpRequest();
		httpRequest.setUrl(url);
		httpRequest.addHeader(RequestHeaderKey.Authorization, sign);
		httpRequest.addHeader(RequestHeaderKey.USER_AGENT, this.config.getUserAgent());

		httpRequest.addParam(RequestBodyKey.OP, RequestBodyValue.OP.UPLOAD_SLICE);
		httpRequest.addParam(RequestBodyKey.FILE_CONTENT, sliceContent);
		httpRequest.addParam(RequestBodyKey.SESSION, session);
		httpRequest.addParam(RequestBodyKey.OFFSET, String.valueOf(offset));

		return httpClient.sendHttpRequest(httpRequest);
	}
}
