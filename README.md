# tencentyun-cos-java-sdk
java sdk for [腾讯云对象存储服务](http://wiki.qcloud.com/wiki/COS%E4%BA%A7%E5%93%81%E4%BB%8B%E7%BB%8D)


## maven信息
GroupId:com.qcloud

ArtifactId:cos_api

## 安装（直接下载源码集成）

### 直接下载源码集成
从github下载源码装入到您的程序中
调用请参考示例

## 修改配置
修改Demo.java内的appid等信息为您的配置

## 上传、查询、删除程序示例
```java
package com.qcloud.cosapi;
import com.qcloud.cosapi.api.*;

public class Demo {
	//通过控制台获取AppId,SecretId,SecretKey
	public static final int APP_ID = 1000000;
	public static final String SECRET_ID = "SECRET_ID";
	public static final String SECRET_KEY = "SECRET_KEY";

	public static void main(String[] args) {
		CosCloud cos = new CosCloud(APP_ID, SECRET_ID, SECRET_KEY);
		try{			
			String result = "";
			String bucketName = "r_test";
            long start = System.currentTimeMillis();
            //result = cos.getFolderList(bucketName, "/", 20, "", 0, CosCloud.FolderPattern.Both);
            //result = cos.createFolder(bucketName, "/sdk/");
            //result = cos.uploadFile(bucketName, "/sdk/xx.txt", "D:\\aa.txt");
            //result = cos.updateFile(bucketName, "/sdk/xx.txt", "test file");
            //result = cos.getFileStat(bucketName, "/sdk/xx.txt");
            //result = cos.updateFolder(bucketName, "/sdk/", "test folder");
            //result = cos.getFolderStat(bucketName, "/sdk/");
            //result = cos.deleteFile(bucketName, "/sdk/xx.txt");
            //result = cos.deleteFolder(bucketName, "/sdk/");
            result = cos.sliceUploadFile(bucketName, "/红警II共和国之辉(简体中文版).rar", "F:\\红警II共和国之辉(简体中文版).rar", 512 * 1024);
            long end = System.currentTimeMillis();
            System.out.println(result);
            System.out.println("总用时：" + (end - start) + "毫秒");
			System.out.println("The End!");
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
}

```
