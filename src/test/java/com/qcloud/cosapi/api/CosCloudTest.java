package com.qcloud.cosapi.api;

import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.Test;

import com.qcloud.cosapi.api.CosCloud.FolderPattern;
import com.qcloud.cosapi.http.ResponseBodyKey;

/**
 * @author chengwu
 * CosCloud UT
 */
public class CosCloudTest {
    // 通过控制台获取AppId,SecretId,SecretKey
    private static final int APP_ID = 1000027;
    private static final String SECRET_ID = "AKID3LqsccIPChNU2mQ7pzJv5jJYfvmSbfqf";
    private static final String SECRET_KEY = "eSkMsxzUT1zVIa5zRYv92CyI1huqvtUa";
    // 默认超时时间，单位秒
    private static final int Time_Out = 60;

    // 通过控制台提前建好的bucket
    private static final String bucketName = "javasdkutbucket";

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

    private static final CosCloud cos = new CosCloud(APP_ID, SECRET_ID, SECRET_KEY, 60);

    // 预初始化COS环境,上传一个目录和文件
    @Before
    public void initCosEnv() {
        try {
            JSONObject retJson = new JSONObject(cos.createFolder(bucketName, DIR_REMOTE_PATH));
            assertThat(retJson.getInt(ResponseBodyKey.CODE), equalTo(0));
        } catch (Exception e) {
            e.printStackTrace();
            fail("initCosEnv createFolder fail!");
        }
        try {
            JSONObject retJson = new JSONObject(
                    cos.uploadFile(bucketName, SMALLFile_REMOTE_PATH, SMALLFile_LOCAL_PATH));
            assertThat(retJson.getInt(ResponseBodyKey.CODE), equalTo(0));
            JSONObject dataMember = retJson.getJSONObject(ResponseBodyKey.DATA);
            assertTrue(dataMember.has(ResponseBodyKey.Data.ACCESS_URL));
        } catch (Exception e) {
            e.printStackTrace();
            fail("initCosEnv uploadFile fail!");
        }
    }

    // 清理COS环境,清理目录和环境
    @After
    public void clearCosEnv() {
        try {
            JSONObject retJson = new JSONObject(cos.deleteFolder(bucketName, DIR_REMOTE_PATH));
            assertThat(retJson.getInt(ResponseBodyKey.CODE), equalTo(0));
            retJson = new JSONObject(cos.deleteFile(bucketName, SMALLFile_REMOTE_PATH));
            assertThat(retJson.getInt(ResponseBodyKey.CODE), equalTo(0));
        } catch (Exception e) {
            e.printStackTrace();
            fail("clearCosEnv fail!");
        }
    }

    // 测试设置超时时间
    @Test
    public void testSetTimeOut() {
        assertThat(cos.getTimeOut(), equalTo(Time_Out));
        cos.setTimeOut(300);
        assertThat(cos.getTimeOut(), equalTo(300));
        cos.setTimeOut(Time_Out);
        assertThat(cos.getTimeOut(), equalTo(Time_Out));
    }

    // 测试更新目录属性
    @Test
    public void testUpdateFolder() {
        try {
            JSONObject retJson = new JSONObject(cos.getFolderStat(bucketName, DIR_REMOTE_PATH));
            assertThat(retJson.getInt(ResponseBodyKey.CODE), equalTo(0));
            JSONObject dataMember = retJson.getJSONObject(ResponseBodyKey.DATA);
            String bizAttr = dataMember.getString(ResponseBodyKey.Data.BIZ_ATTR);
            assertTrue(bizAttr.isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
            fail("getFolder origin stat fail!");
        }

        try {
            String newBizAttr = "test folder!";
            JSONObject retJson =
                    new JSONObject(cos.updateFolder(bucketName, DIR_REMOTE_PATH, newBizAttr));
            assertThat(retJson.getInt(ResponseBodyKey.CODE), equalTo(0));
            retJson = new JSONObject(cos.getFolderStat(bucketName, DIR_REMOTE_PATH));
            JSONObject dataMember = retJson.getJSONObject(ResponseBodyKey.DATA);
            String retBizAttr = dataMember.getString(ResponseBodyKey.Data.BIZ_ATTR);
            assertTrue(retBizAttr.equals(newBizAttr));
        } catch (Exception e) {
            e.printStackTrace();
            fail("updateFolderStat fail!");
        }
    }

    // 测试更新文件属性
    @Test
    public void testUpdateFile() {
        try {
            JSONObject retJson = new JSONObject(cos.getFileStat(bucketName, SMALLFile_REMOTE_PATH));
            assertThat(retJson.getInt(ResponseBodyKey.CODE), equalTo(0));
            JSONObject dataMember = retJson.getJSONObject(ResponseBodyKey.DATA);
            String bizAttr = dataMember.getString(ResponseBodyKey.Data.BIZ_ATTR);
            assertTrue(bizAttr.isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
            fail("getFolder origin stat fail!");
        }

        try {
            String newBizAttr = "test file!";
            JSONObject retJson =
                    new JSONObject(cos.updateFolder(bucketName, SMALLFile_REMOTE_PATH, newBizAttr));
            assertThat(retJson.getInt(ResponseBodyKey.CODE), equalTo(0));
            retJson = new JSONObject(cos.getFileStat(bucketName, SMALLFile_REMOTE_PATH));
            JSONObject dataMember = retJson.getJSONObject(ResponseBodyKey.DATA);
            String retBizAttr = dataMember.getString(ResponseBodyKey.Data.BIZ_ATTR);
            assertTrue(retBizAttr.equals(newBizAttr));
        } catch (Exception e) {
            e.printStackTrace();
            fail("updateFolderStat fail!");
        }
    }

    // 测试获取目录成员列表
    @Test
    public void testGetFolderListStringStringIntStringIntFolderPattern() {
        try {
            JSONObject retJson = new JSONObject(
                    cos.getFolderList(bucketName, "/", DIR_NUM, "", 0, FolderPattern.Both));
            assertThat(retJson.getInt(ResponseBodyKey.CODE), equalTo(0));
            JSONObject dataMemeber = retJson.getJSONObject(ResponseBodyKey.DATA);
            assertThat(dataMemeber.getInt("filecount"), equalTo(1));
            assertThat(dataMemeber.getInt("dircount"), equalTo(1));
        } catch (Exception e) {
            e.printStackTrace();
            fail("getFolderList fail!");
        }

    }

    // 测试串行分片上传
    @Test
    public void testSliceUploadFileStringStringString() {
        try {
            JSONObject retJson = new JSONObject(cos.sliceUploadFile(bucketName,
                    SLICEFILE_REMOTE_PATH, SLICEFILE_LOCAL_PATH, 512 * 1024));
            assertThat(retJson.getInt(ResponseBodyKey.CODE), equalTo(0));
            JSONObject dataMember = retJson.getJSONObject(ResponseBodyKey.DATA);
            assertTrue(dataMember.has(ResponseBodyKey.Data.ACCESS_URL));
        } catch (Exception e) {
            e.printStackTrace();
            fail("sliceUpload file fail!");
        }
        try {
            JSONObject retJson = new JSONObject(cos.deleteFile(bucketName, SLICEFILE_REMOTE_PATH));
            assertThat(retJson.getInt(ResponseBodyKey.CODE), equalTo(0));
        } catch (Exception e) {
            e.printStackTrace();
            fail("clear slice file fail!");
        }
    }

    // 测试并行分片上传文件
    @Ignore("server parallel is not ok")
    @Test
    public void testSliceUploadFileParallel() {
        try {
            JSONObject retJson = new JSONObject(cos.sliceUploadFileParallel(bucketName,
                    PARA_SLICEFILE_REMOTE_PATH, PARA_SLICEFILE_LOCAL_PATH, 512 * 1024));
            assertThat(retJson.getInt(ResponseBodyKey.CODE), equalTo(0));
            JSONObject dataMember = retJson.getJSONObject(ResponseBodyKey.DATA);
            assertTrue(dataMember.has(ResponseBodyKey.Data.ACCESS_URL));
        } catch (Exception e) {
            e.printStackTrace();
            fail("sliceUpload file fail!");
        }
        try {
            JSONObject retJson =
                    new JSONObject(cos.deleteFile(bucketName, PARA_SLICEFILE_REMOTE_PATH));
            assertThat(retJson.getInt(ResponseBodyKey.CODE), equalTo(0));
        } catch (Exception e) {
            e.printStackTrace();
            fail("clear slice file fail!");
        }
    }

}
