package com.qcloud.cosapi.api;

import java.io.InputStream;

/**
 * Created by Sean Lei on 2/26/16.
 */
public interface CosCloudApi {
    /**
     * 更新文件夹信息
     *
     * @param bucketName   bucket名称
     * @param remotePath   远程文件夹路径
     * @param bizAttribute 更新信息
     * @return
     * @throws Exception
     */
    String updateFolder(String bucketName, String remotePath, String bizAttribute) throws Exception;

    /**
     * 更新文件信息
     *
     * @param bucketName   bucket名称
     * @param remotePath   远程文件路径
     * @param bizAttribute 更新信息
     * @return
     * @throws Exception
     */
    String updateFile(String bucketName, String remotePath, String bizAttribute) throws Exception;

    /**
     * 删除文件夹
     *
     * @param bucketName bucket名称
     * @param remotePath 远程文件夹路径
     * @return
     * @throws Exception
     */
    String deleteFolder(String bucketName, String remotePath) throws Exception;

    /**
     * 删除文件
     *
     * @param bucketName bucket名称
     * @param remotePath 远程文件路径
     * @return
     * @throws Exception
     */
    String deleteFile(String bucketName, String remotePath) throws Exception;

    /**
     * 获取文件夹信息
     *
     * @param bucketName bucket名称
     * @param remotePath 远程文件夹路径
     * @return
     * @throws Exception
     */
    String getFolderStat(String bucketName, String remotePath) throws Exception;

    /**
     * 获取文件信息
     *
     * @param bucketName bucket名称
     * @param remotePath 远程文件路径
     * @return
     * @throws Exception
     */
    String getFileStat(String bucketName, String remotePath) throws Exception;

    /**
     * 创建文件夹
     *
     * @param bucketName bucket名称
     * @param remotePath 远程文件夹路径
     * @return
     * @throws Exception
     */
    String createFolder(String bucketName, String remotePath) throws Exception;

    /**
     * 目录列表
     *
     * @param bucketName bucket名称
     * @param remotePath 远程文件夹路径
     * @param num        拉取的总数
     * @param context    透传字段，查看第一页，则传空字符串。若需要翻页，需要将前一页返回值中的context透传到参数中。
     *                   order用于指定翻页顺序。若order填0，则从当前页正序/往下翻页；若order填1，则从当前页倒序/往上翻页。
     * @param order      默认正序(=0), 填1为反序
     * @param pattern    拉取模式:只是文件，只是文件夹，全部
     * @return
     * @throws Exception
     */
    String getFolderList(String bucketName, String remotePath, int num, String context, int order,
                         CosCloud.FolderPattern pattern) throws Exception;

    /**
     * 目录列表,前缀搜索
     *
     * @param bucketName bucket名称
     * @param remotePath 远程文件夹路径
     * @param prefix     读取文件/文件夹前缀
     * @param num        拉取的总数
     * @param context    透传字段，查看第一页，则传空字符串。若需要翻页，需要将前一页返回值中的context透传到参数中。
     *                   order用于指定翻页顺序。若order填0，则从当前页正序/往下翻页；若order填1，则从当前页倒序/往上翻页。
     * @param order      默认正序(=0), 填1为反序
     * @param pattern    拉取模式:只是文件，只是文件夹，全部
     * @return
     * @throws Exception
     */
    String getFolderList(String bucketName, String remotePath, String prefix, int num, String context, int order,
                         CosCloud.FolderPattern pattern) throws Exception;

    /**
     * 单个文件上传
     *
     * @param bucketName bucket名称
     * @param remotePath 远程文件路径
     * @param localPath  本地文件路径
     * @return
     * @throws Exception
     */
    String uploadFile(String bucketName, String remotePath, String localPath) throws Exception;

    /**
     * 流单个文件上传
     *
     * @param bucketName  bucket名称
     * @param remotePath  远程文件路径
     * @param inputStream 文件流
     * @return
     * @throws Exception
     */
    String uploadFile(String bucketName, String remotePath, InputStream inputStream) throws Exception;

    /**
     * 分片上传第一步
     *
     * @param bucketName bucket名称
     * @param remotePath 远程文件路径
     * @param localPath  本地文件路径
     * @param sliceSize  切片大小（字节）
     * @return
     * @throws Exception
     */
    String sliceUploadFileFirstStep(String bucketName, String remotePath, String localPath, int sliceSize) throws Exception;

    /**
     * 分片上传后续步骤
     *
     * @param bucketName bucket名称
     * @param remotePath 远程文件路径
     * @param localPath  本地文件路径
     * @param sessionId  分片上传会话ID
     * @param offset     文件分片偏移量
     * @param sliceSize  切片大小（字节）
     * @return
     * @throws Exception
     */
    String sliceUploadFileFollowStep(String bucketName, String remotePath, String localPath, String sessionId,
                                     long offset, int sliceSize) throws Exception;

    /**
     * 分片上传，默认切片大小为512K
     *
     * @param bucketName bucket名称
     * @param remotePath 远程文件路径
     * @param localPath  本地文件路径
     * @return
     * @throws Exception
     */
    String sliceUploadFile(String bucketName, String remotePath, String localPath) throws Exception;

    /**
     * 分片上传
     *
     * @param bucketName bucket名称
     * @param remotePath 远程文件路径
     * @param localPath  本地文件路径
     * @param sliceSize  切片大小（字节）
     * @return
     * @throws Exception
     */
    String sliceUploadFile(String bucketName, String remotePath, String localPath, int sliceSize) throws Exception;
}
