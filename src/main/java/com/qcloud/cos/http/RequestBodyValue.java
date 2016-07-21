package com.qcloud.cos.http;
/**
 * @author chengwu
 * 封装HTTP请求包体的k-v对中的value枚举值类
 */
public class RequestBodyValue {
    public class OP {
        public static final String CREATE = "create";
        public static final String LIST = "list";
        public static final String UPDATE = "update";
        public static final String STAT = "stat";
        public static final String MOVE = "move";
        public static final String DELETE = "delete";
        public static final String UPLOAD = "upload";
        public static final String UPLOAD_SLICE = "upload_slice";
    }

}
