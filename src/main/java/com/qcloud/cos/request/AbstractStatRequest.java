package com.qcloud.cos.request;

import com.qcloud.cos.exception.ParamException;

/**
 * @author chengwu
 * 获取属性信息
 */
public class AbstractStatRequest extends AbstractBaseRequest {

    public AbstractStatRequest(String bucketName, String cosPath) {
        super(bucketName, cosPath);
    }
    
    @Override
    public void check_param() throws ParamException {
    	super.check_param();
    }
    
    @Override
    public String toString() {
    	return super.toString();
    }

}
