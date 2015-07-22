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
            result = cos.sliceUploadFileFirstStep(bucketName, "/红警II共和国之辉(简体中文版).rar", "F:\\红警II共和国之辉(简体中文版).rar", 512 * 1024);
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
