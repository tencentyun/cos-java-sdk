package com.qcloud.cosapi.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qcloud.cosapi.sign.CommonCodecUtils;

/**
 * @author chengwu 封装了一些常用的文件操作函数
 */
public class FileProcess {
    private static Logger LOG = LoggerFactory.getLogger(FileProcess.class);

    public static boolean isLegalFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory() || !file.canRead()) {
            return false;
        }
        return true;
    }

    /**
     * 获取文件长度，单位为字节
     * 
     * @param filePath 文件的本地路径
     * @return 文件长度,单位为字节
     * @throws Exception 文件不存在或者是一个目录，则抛出异常
     */
    public static long getFileLength(String filePath) throws Exception {
        if (!isLegalFile(filePath)) {
            String errorMsg = filePath + " is not file or not exist or can't be read!";
            LOG.error(errorMsg);
            throw new Exception(errorMsg);
        }
        File file = new File(filePath);
        return file.length();
    }

    /**
     * 打开对应的文件，并返回文件输入流
     * 
     * @param filePath 文件路径
     * @return 文件输入流
     * @throws FileNotFoundException 如果文件不存在，则抛出异常
     */
    public static FileInputStream getFileInputStream(String filePath) throws Exception {
        if (!isLegalFile(filePath)) {
            String errorMsg = filePath + " is not file or not exist or can't be read!";
            LOG.error(errorMsg);
            throw new Exception(errorMsg);
        }
        FileInputStream localFileInputStream = new FileInputStream(filePath);
        return localFileInputStream;
    }

    /**
     * 关闭对应的文件流
     * 
     * @param inputStream 待关闭的文件流
     * @param filePath 对应的文件名
     * @throws IOException 关闭时发生IO异常，则抛出
     */
    public static void closeFileStream(InputStream inputStream, String filePath)
            throws IOException {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            LOG.error("close file {} occur a IOExcpetion {}", filePath, e);
            throw e;
        }
    }

    /**
     * 获取文件的内容
     * 
     * @param filePath 文件路径
     * @param offset 偏移量，即从哪里开始读取，单位为字节
     * @param length 读取的长度,单位为字节
     * @return 返回读取的内容，实际读取的长度小于等于length
     * @throws Exception
     */
    public static byte[] getFileContent(String filePath, long offset, int length) throws Exception {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = getFileInputStream(filePath);
            return getFileContent(fileInputStream, offset, length);
        } finally {
            closeFileStream(fileInputStream, filePath);
        }
    }

    /**
     * 读取指定流的内容，此函数有一定的风险，如果流对应的内容过大，则会造成OOM
     * 
     * @param inputStream
     * @return 读取的内容
     * @throws Exception
     */
    public static byte[] getFileContent(InputStream inputStream) throws Exception {
        return getFileContent(inputStream, 0, inputStream.available());
    }

    /**
     * 读取指定流从某处开始的内容，此函数有一定的风险，如果流对应的内容过大，则会造成OOM
     * 
     * @param inputStream
     * @param offset 读取的开始偏移
     * @param length 读取的长度
     * @return 读取的内容
     * @throws Exception
     */
    public static byte[] getFileContent(InputStream inputStream, long offset, int length)
            throws Exception {
        if (offset < 0 || length < 0) {
            throw new Exception("get file content param error");
        }

        byte[] fileContent = null;
        byte[] tempBuf = new byte[length];

        inputStream.skip(offset);
        int readLen = inputStream.read(tempBuf);
        if (readLen < 0) {
            fileContent = new byte[0];
            return fileContent;
        }
        if (readLen < length) {
            fileContent = new byte[readLen];
            System.arraycopy(tempBuf, 0, fileContent, 0, readLen);
        } else {
            fileContent = tempBuf;
        }
        return fileContent;
    }

    /**
     * 获取文件的sha1
     * 
     * @param filePath 文件路径
     * @return 返回sha1值
     * @throws Exception
     */
    public static String getFileSha1(String filePath) throws Exception {
        InputStream fileInputStream = null;
        try {
            fileInputStream = getFileInputStream(filePath);
            return CommonCodecUtils.getFileSha1(fileInputStream);
        } catch (Exception e) {
            LOG.error("getFileSha1 occur a exception, file {}, exception {}", filePath,
                    e.toString());
            throw e;
        } finally {
            closeFileStream(fileInputStream, filePath);
        }
    }

}
