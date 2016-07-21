package com.qcloud.cos.request;

import com.qcloud.cos.common_utils.CommonParamCheckUtils;
import com.qcloud.cos.exception.ParamException;

/**
 * @author chengwu 文件分片上传请求
 */
public class UploadSliceFileRequest extends UploadFileRequest {
	// 默认分片大小1MB
	private static final int DEFAULT_SLICE_SIZE = 1024 * 1024;

	private int sliceSize = DEFAULT_SLICE_SIZE;

	public UploadSliceFileRequest(UploadFileRequest request) {
		super(request.getBucketName(), request.getCosPath(), request.getLocalPath(), request.getBizAttr());
		this.setInsertOnly(request.getInsertOnly());
	}

	public UploadSliceFileRequest(String bucketName, String cosPath, String localPath, int sliceSize) {
		super(bucketName, cosPath, localPath);
		this.sliceSize = sliceSize;
	}

	public int getSliceSize() {
		return sliceSize;
	}

	public void setSliceSize(int sliceSize) {
		this.sliceSize = sliceSize;
	}
	
	@Override
	public void check_param() throws ParamException {
		super.check_param();
		CommonParamCheckUtils.AssertLegalSliceSize(this.sliceSize);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(", sliceSize:").append(this.sliceSize);
		return sb.toString();
	}
		
}
