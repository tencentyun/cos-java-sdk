package com.qcloud.cos.request;

import com.qcloud.cos.common_utils.CommonParamCheckUtils;
import com.qcloud.cos.exception.ParamException;
import com.qcloud.cos.meta.ListOrder;
import com.qcloud.cos.meta.ListPattern;


/**
 * @author chengwu
 * 获取目录成员请求
 */
public class ListFolderRequest extends AbstractBaseRequest {
	
    // 默认获取的最大目录成员数量
    private final int DEFAULT_LIST_NUM = 199;
   
    private int num = DEFAULT_LIST_NUM;
    private String prefix = "";
    private String context = "";
    private ListPattern pattern = ListPattern.BOTH;
    private ListOrder order = ListOrder.POSITIVE;

    public ListFolderRequest(String bucketName, String cosPath) {
        super(bucketName, cosPath);
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
    

    public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

	public ListPattern getPattern() {
		return pattern;
	}

	public void setPattern(ListPattern pattern) {
		this.pattern = pattern;
	}

	public ListOrder getOrder() {
		return order;
	}

	public void setOrder(ListOrder order) {
		this.order = order;
	}

	@Override
	public void check_param() throws ParamException {
		super.check_param();
		CommonParamCheckUtils.AssertLegalCosFolderPath(getCosPath());
		CommonParamCheckUtils.AssertNotNull("order", this.order);
		CommonParamCheckUtils.AssertNotNull("context", this.context);
		CommonParamCheckUtils.AssertNotNull("prefix", this.prefix);
		CommonParamCheckUtils.AssertNotNull("pattern", this.pattern);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(", num:").append(this.num);
		sb.append(", order:");
		if (this.order == null) {
			sb.append("null");
		} else {
			sb.append(this.order.ordinal());
		}
		sb.append(", prefix:").append(getMemberStringValue(this.prefix));
		sb.append(", context:").append(getMemberStringValue(this.context));
		sb.append(", pattern:");
		if (this.pattern == null) {
			sb.append("null");
		} else {
			sb.append(this.pattern.toString());
		}
		return sb.toString();
	}

}
