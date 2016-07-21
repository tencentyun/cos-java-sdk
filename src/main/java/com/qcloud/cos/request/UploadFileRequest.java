package com.qcloud.cos.request;

import com.qcloud.cos.common_utils.CommonParamCheckUtils;
import com.qcloud.cos.exception.ParamException;
import com.qcloud.cos.meta.InsertOnly;

/**
 * @author chengwu 上传文件请求,针对文件整体上传，不分片的操作
 */
public class UploadFileRequest extends AbstractBaseRequest {
	// 需要上传的路径
	private String localPath;
	// 上传文件的属性信息
	private String bizAttr;

	private InsertOnly insertOnly = InsertOnly.NO_OVER_WRITE;

	public UploadFileRequest(String bucketName, String cosPath, String localPath, String bizAttr) {
		super(bucketName, cosPath);
		this.localPath = localPath;
		this.bizAttr = bizAttr;
	}

	public UploadFileRequest(String bucketName, String cosPath, String localPath) {
		this(bucketName, cosPath, localPath, "");
	}

	public String getBizAttr() {
		return bizAttr;
	}

	public void setBizAttr(String bizAttr) {
		this.bizAttr = bizAttr;
	}

	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}


	public InsertOnly getInsertOnly() {
		return insertOnly;
	}

	public void setInsertOnly(InsertOnly insertOnly) {
		this.insertOnly = insertOnly;
	}

	@Override
	public void check_param() throws ParamException {
		super.check_param();
		CommonParamCheckUtils.AssertLegalCosFilePath(this.getCosPath());
		CommonParamCheckUtils.AssertLegalLocalFilePath(this.localPath);
		CommonParamCheckUtils.AssertNotNull("biz_attr", this.bizAttr);
		CommonParamCheckUtils.AssertNotNull("insertOnly", this.insertOnly);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(", local_path:").append(getMemberStringValue(this.localPath));
		sb.append(", bizAttr:").append(getMemberStringValue(this.bizAttr));
		sb.append(", insertOnly:");
		if (this.insertOnly == null) {
			sb.append("null");
		} else {
			sb.append(this.insertOnly.ordinal());
		}
		return sb.toString();
	}
}
