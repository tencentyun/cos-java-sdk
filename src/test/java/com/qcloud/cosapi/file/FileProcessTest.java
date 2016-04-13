package com.qcloud.cosapi.file;

import static org.junit.Assert.*;

import java.io.FileInputStream;

import static org.hamcrest.core.IsEqual.equalTo;
import org.junit.Test;

public class FileProcessTest {

    private static final String EmptyFilePath = "src/test/resources/empty.txt";
    private static final String UnEmptyFilePath = "src/test/resources/len10.txt";
    private static final String NotExistFilePath = "src/test/resources/what.txt";
    private static final String DirPath = "src/test/resources";

    // 测试空文件长度
    @Test
    public void testGetFileLengthEmpty() {
        try {
            long fileLen = FileProcess.getFileLength(EmptyFilePath);
            assertThat(fileLen, equalTo(0L));
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    // 测试长度为10的文件
    @Test
    public void testGetFileLengthLen10() {
        try {
            long fileLen = FileProcess.getFileLength(UnEmptyFilePath);
            assertThat(fileLen, equalTo(10L));
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    // 测试文件不存在
    @Test
    public void testGetFileLengthFileNotExist() {
        try {
            FileProcess.getFileLength(NotExistFilePath);
            fail();
        } catch (Exception e) {
            return;
        }
    }

    // 测试读取目录的长度
    @Test
    public void testGetFileLengthDirectory() {
        try {
            FileProcess.getFileLength(DirPath);
            fail();
        } catch (Exception e) {
            return;
        }
    }


    @Test
    public void testGetFileInputStreamNormal() {
        try {
            FileProcess.getFileInputStream(EmptyFilePath);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetFileInputStreamNotExist() {
        try {
            FileProcess.getFileInputStream(NotExistFilePath);
            fail();
        } catch (Exception e) {
            return;
        }
    }


    @Test
    public void testCloseFileStreamNormal() {
        FileInputStream inputStream = null;
        try {
            inputStream = FileProcess.getFileInputStream(EmptyFilePath);
            FileProcess.closeFileStream(inputStream, EmptyFilePath);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetFileContentStringLongInt() {
        try {
            int len = ((Long) FileProcess.getFileLength(UnEmptyFilePath)).intValue();
            byte[] fileContent = FileProcess.getFileContent(UnEmptyFilePath, 0, len);
            assertThat(fileContent.length, equalTo(len));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetFileContentInputStreamUnEmptyFile() {
        FileInputStream inputStream = null;
        try {
            inputStream = FileProcess.getFileInputStream(UnEmptyFilePath);
            int len = ((Long) FileProcess.getFileLength(UnEmptyFilePath)).intValue();
            byte[] fileContent = FileProcess.getFileContent(inputStream);
            assertThat(fileContent.length, equalTo(len));
        } catch (Exception e) {
            fail();
        } finally {
            try {
                FileProcess.closeFileStream(inputStream, UnEmptyFilePath);
            } catch (Exception e) {
                fail();
            }
        }
    }
    
    @Test
    public void testGetFileContentInputStreamLongIntEmptyFile() {
        FileInputStream inputStream = null;
        try {
            inputStream = FileProcess.getFileInputStream(EmptyFilePath);
            byte[] fileContent = FileProcess.getFileContent(inputStream, 0, 10);
            assertThat(fileContent.length, equalTo(0));
        } catch (Exception e) {
            fail();
        } finally {
            try {
                FileProcess.closeFileStream(inputStream, EmptyFilePath);
            } catch (Exception e) {
                fail();
            }
        }
    }

    @Test
    public void testGetFileContentInputStreamLongInt() {
        FileInputStream inputStream = null;
        try {
            inputStream = FileProcess.getFileInputStream(UnEmptyFilePath);
            int len = ((Long) FileProcess.getFileLength(UnEmptyFilePath)).intValue();
            byte[] fileContent = FileProcess.getFileContent(inputStream, 0, len * 2);
            assertThat(fileContent.length, equalTo(len));
        } catch (Exception e) {
            fail();
        } finally {
            try {
                FileProcess.closeFileStream(inputStream, UnEmptyFilePath);
            } catch (Exception e) {
                fail();
            }
        }
    }

    @Test
    public void testGetFileSha1() {
        try {
        String sha1Digest = FileProcess.getFileSha1(UnEmptyFilePath);
        String expectSha1Digest = "87acec17cd9dcd20a716cc2cf67417b71c8a7016";
        boolean cmpResult = sha1Digest.equals(expectSha1Digest);
        assertTrue(cmpResult);
        } catch (Exception e) {
            fail();
        }
        
    }

}
