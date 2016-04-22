/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package com.qcloud.cosapi.demo;

import com.qcloud.cosapi.api.*;

/**
 * @author chengwu 
 * cos Demo代码
 */
public class Demo {

    // 通过控制台获取AppId,SecretId,SecretKey
    private static final int APP_ID = 1000027;
    private static final String SECRET_ID = "AKID3LqsccIPChNU2mQ7pzJv5jJYfvmSbfqf";
    private static final String SECRET_KEY = "eSkMsxzUT1zVIa5zRYv92CyI1huqvtUa";

    // 通过控制台提前建好的bucket
    private static final String bucketName = "javasdkdemobucket";

    // 默认获取的目录列表的数量
    private static final int DIR_NUM = 20;
    // 用来测试上传文件夹的目录
    private static final String DIR_REMOTE_PATH = "/folder1/";

    // 用来测试单文件上传的小文件的远程路径与本地路径
    private static final String SMALLFile_REMOTE_PATH = "/len10.txt";
    private static final String SMALLFile_LOCAL_PATH = "src/test/resources/len10.txt";

    // 用来测试分片串行上传的远程路径与本地路径
    private static final String SLICEFILE_REMOTE_PATH = "/slicefile.txt";
    private static final String SLICEFILE_LOCAL_PATH = "src/test/resources/slicefile.txt";

    // 用来测试分片并行上传的远程路径与本地路径
    private static final String PARA_SLICEFILE_REMOTE_PATH = "/para_slicefile.txt";
    private static final String PARA_SLICEFILE_LOCAL_PATH = "src/test/resources/para_slicefile.txt";

    public static void main(String[] args) {
        try {
            // 初始化cos
            CosCloud cos = new CosCloud(APP_ID, SECRET_ID, SECRET_KEY, 60);
            String result = null;
                        
            // 先获取bucketName下的成员，应该为空
            // 注意getFolderList获得的是目录的子成员信息
            result = cos.getFolderList(bucketName, "/", DIR_NUM, "", 0,
                    CosCloud.FolderPattern.Both);
            System.out.println("getFolderList result: " + result);

            // 创造一个文件夹folder1
            result = cos.createFolder(bucketName, "/folder1/");
            System.out.println("createFolder result: " + result);
            // 上传一个长度为10个字节的文件
            result = cos.uploadFile(bucketName, SMALLFile_REMOTE_PATH, SMALLFile_LOCAL_PATH);
            System.out.println("uploadFile result:" + result);
            // 再次获取bucket列表，则应该包含folder1和len10.txt两个成员
            result = cos.getFolderList(bucketName, "/", DIR_NUM, "", 0,
                    CosCloud.FolderPattern.Both);
            System.out.println("getFolderList result: " + result);

            // 更新文件夹属性
            result = cos.updateFolder(bucketName, DIR_REMOTE_PATH, "demo test folder1");
            System.out.println("updateFolder result: " + result);
            // 更新文件属性
            result = cos.updateFile(bucketName, SMALLFile_REMOTE_PATH, "demo test smallfile");
            System.out.println("updateFile result: " + result);
            // 获取目录列表，这时可以发现文件夹和文件属性发生了改变
            result = cos.getFolderList(bucketName, "/", DIR_NUM, "", 0,
                    CosCloud.FolderPattern.Both);
            System.out.println("getFolderList result: " + result);

            // 获取文件属性
            result = cos.getFileStat(bucketName, SMALLFile_REMOTE_PATH);
            System.out.println("getFileStat result: " + result);
            // 获取文件夹属性
            result = cos.getFolderStat(bucketName, DIR_REMOTE_PATH);
            System.out.println("getFolderStat result: " + result);

            // 串行分片上传文件, 使用默认分片大小
            result = cos.sliceUploadFile(bucketName, SLICEFILE_REMOTE_PATH, SLICEFILE_LOCAL_PATH);
            System.out.println("sliceUploadFile result: " + result);
            // 串行分片上传文件, 使用分片大小512KB
            result = cos.sliceUploadFileParallel(bucketName, PARA_SLICEFILE_REMOTE_PATH,
                    PARA_SLICEFILE_LOCAL_PATH, 512 * 1024);
            System.out.println("para sliceUploadFile result: " + result);
            // 再次获取bucket列表，则应该包含folder1和len10.txt, slicefile.txt, para_slicefile.txt四个成员
            result = cos.getFolderList(bucketName, "/", DIR_NUM, "", 0,
                    CosCloud.FolderPattern.Both);
            System.out.println("getFolderList result: " + result);

            result = cos.deleteFile(bucketName, SMALLFile_REMOTE_PATH);
            System.out.println("deleteFile len10.txt result: " + result);
            result = cos.deleteFile(bucketName, SLICEFILE_REMOTE_PATH);
            System.out.println("deleteFile slicefile.txt result: " + result);
            result = cos.deleteFile(bucketName, PARA_SLICEFILE_REMOTE_PATH);
            System.out.println("deleteFile para_slicefile.txt result: " + result);
            result = cos.deleteFolder(bucketName, DIR_REMOTE_PATH);
            System.out.println("deleteFolder /folder1 result: " + result);
            // 再次获取bucket列表，此时应该为空
            result = cos.getFolderList(bucketName, "/", DIR_NUM, "", 0,
                    CosCloud.FolderPattern.Both);
            System.out.println("getFolderList result: " + result);
            
            // 关闭释放资源
            cos.shutdown();
            System.out.println("shutdown!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
