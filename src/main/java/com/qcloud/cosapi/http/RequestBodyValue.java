package com.qcloud.cosapi.http;
/**
 * @author chengwu
 * 封装HTTP请求包体的k-v对中的value枚举值类
 */
public class RequestBodyValue {
    public static final String OP_CREATE = "create";
    public static final String OP_LIST = "list";
    public static final String OP_UPDATE = "update";
    public static final String OP_STAT = "stat";
    public static final String OP_DELETE = "delete";
    public static final String OP_UPLOAD = "upload";
    public static final String OP_UPLOAD_SLICE = "upload_slice";
}
