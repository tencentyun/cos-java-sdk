package com.qcloud.cos.request;

import com.qcloud.cos.common_utils.CommonParamCheckUtils;
import com.qcloud.cos.exception.ParamException;
import com.qcloud.cos.meta.OverWrite;

/**
 * @author chengwu 移动文件请求(重命名文件)
 */
public class MoveFileRequest extends AbstractBaseRequest {

	private String dstCosPath = "";
	// 移动文件, 如果目的路径已有文件存在, 默认不覆盖
	private OverWrite overWrite = OverWrite.NO_OVER_WRITE;

	public MoveFileRequest(String bucketName, String srcCosPath, String dstCosPath) {
		super(bucketName, srcCosPath);
		this.dstCosPath = dstCosPath;
	}

	public String getDstCosPath() {
		return dstCosPath;
	}

	public void setDstCosPath(String dstCosPath) {
		this.dstCosPath = dstCosPath;
	}

	public OverWrite getOverWrite() {
		return overWrite;
	}

	public void setOverWrite(OverWrite overWrite) {
		this.overWrite = overWrite;
	}

	@Override
	public void check_param() throws ParamException {
		super.check_param();
		CommonParamCheckUtils.AssertLegalCosFilePath(this.getCosPath());
		CommonParamCheckUtils.AssertLegalCosFilePath(this.dstCosPath);
		CommonParamCheckUtils.AssertNotNull("overWrite", this.overWrite);
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
