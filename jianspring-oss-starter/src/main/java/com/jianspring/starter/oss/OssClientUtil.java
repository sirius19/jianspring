package com.jianspring.starter.oss;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.AbortMultipartUploadRequest;
import com.aliyun.oss.model.CompleteMultipartUploadRequest;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.InitiateMultipartUploadRequest;
import com.aliyun.oss.model.InitiateMultipartUploadResult;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PartETag;
import com.aliyun.oss.model.PutObjectResult;
import com.aliyun.oss.model.UploadPartRequest;
import com.aliyun.oss.model.UploadPartResult;
import com.jianspring.starter.commons.error.CommonErrorCode;
import com.jianspring.starter.commons.exception.BizException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


public class OssClientUtil {
    private static final Logger logger = LoggerFactory.getLogger(OssClientUtil.class);

    private final AliOssProperties ossProperties;
    private final ApplicationContext applicationContext;

    // 修改常量定义
    private static final Long MAX_EXPIRE_TIME = 32400L * 1000L; // OSS最大过期时间9小时

    private OSS ossClient;

    public OssClientUtil(AliOssProperties ossProperties, ApplicationContext applicationContext) {
        this.ossProperties = ossProperties;
        this.applicationContext = applicationContext;
    }

    // Method to reinitialize anything that needs to be reconfigured on refresh
    @EventListener(ContextRefreshedEvent.class)
    public void onRefresh() {
        // Reinitialize the ossClient with new properties
        initializeOSSClient();
    }

    @PostConstruct
    public void init() {
        initializeOSSClient();
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            try {
                ossClient.shutdown();
                logger.info("OSS client has been shutdown");
            } catch (Exception e) {
                logger.error("Error shutting down OSS client", e);
            }
        }
    }

    private void initializeOSSClient() {
        try {
            ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
            conf.setMaxConnections(ossProperties.getMaxConnections());
            conf.setSocketTimeout(ossProperties.getSocketTimeout());
            conf.setConnectionTimeout(ossProperties.getConnectionTimeout());
            conf.setConnectionRequestTimeout(ossProperties.getConnectionRequestTimeout());
            conf.setIdleConnectionTime(ossProperties.getIdleConnectionTime());
            conf.setMaxErrorRetry(ossProperties.getMaxErrorRetry());
            conf.setUserAgent("aliyun-sdk-java");

            ossClient = new OSSClientBuilder().build(
                    ossProperties.getEndpoint(),
                    ossProperties.getAccessKeyId(),
                    ossProperties.getAccessKeySecret(),
                    conf
            );
            logger.info("OSS client initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize OSS client", e);
            throw new RuntimeException("Failed to initialize OSS client", e);
        }
    }

    /**
     * 生成查看链接
     *
     * @param bucketName     存储桶名称
     * @param fileName       文件名
     * @param expirationTime 过期时间戳
     * @return 生成的URL字符串，失败时返回null
     */
    private String createViewUrl(String bucketName, String fileName, Long expirationTime) {
        // 检查OSS客户端是否初始化
        if (ossClient == null) {
            logger.error("OSS客户端未初始化");
            return null;
        }

        try {
            GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucketName, fileName, HttpMethod.GET);
            // 处理过期时间
            long expireTime = Objects.nonNull(expirationTime) ? expirationTime : ossProperties.getDefaultExpireTime();
            // 确保不超过最大限制
            if (expireTime > MAX_EXPIRE_TIME) {
                logger.warn("请求的过期时间{}超过阿里云OSS最大限制（9小时），将使用最大值", expireTime);
                expireTime = MAX_EXPIRE_TIME;
            }
            Date expiration = new Date(System.currentTimeMillis() + expireTime);

            req.setExpiration(expiration);
            URL url = ossClient.generatePresignedUrl(req);
            return url.toString();
        } catch (Exception ex) {
            logger.error("生成OSS查看链接失败: {}", ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * 上传输入流到默认存储桶
     *
     * @param inputStream 输入流
     * @param fileName    文件名
     * @return 对象键
     */
    public String uploadStream(InputStream inputStream, String fileName) {
        return uploadStream(inputStream, fileName, ossProperties.getBucketName());
    }

    /**
     * 上传输入流到指定存储桶
     *
     * @param inputStream 输入流
     * @param fileName    文件名
     * @param bucketName  存储桶名称
     * @return 对象键
     */
    public String uploadStream(InputStream inputStream, String fileName, String bucketName) {
        Assert.notBlank(bucketName, "bucketName不能为空");
        Assert.notBlank(fileName, "文件名不能为空");
        Assert.notNull(inputStream, "输入流不能为空");
        // 检查OSS客户端是否初始化
        if (ossClient == null) {
            logger.error("OSS客户端未初始化");
            throw BizException.of(CommonErrorCode.ERROR.getCode(), "OSS客户端未初始化");
        }
        try {
            // 处理文件名
            String processedFileName = processFileName(fileName);
            String objectKey = getObjectKey(processedFileName);
            PutObjectResult putObjectResult = ossClient.putObject(bucketName, objectKey, inputStream);
            logger.info("文件上传成功: {}", objectKey);
            return objectKey;
        } catch (OSSException e) {
            logger.error("OSS上传文件失败: {}", e.getMessage(), e);
            throw BizException.of(CommonErrorCode.ERROR.getCode(), "文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("上传文件异常: {}", e.getMessage(), e);
            throw BizException.of(CommonErrorCode.ERROR.getCode(), "文件上传异常");
        } finally {
            // 关闭输入流，避免资源泄漏
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.warn("关闭输入流失败", e);
                }
            }
        }
    }

    /**
     * 上传MultipartFile到默认存储桶
     *
     * @param file     MultipartFile文件
     * @param fileName 文件名
     * @return 对象键
     */
    public String uploadFile(MultipartFile file, String fileName) {
        return uploadFile(file, fileName, ossProperties.getBucketName());
    }

    /**
     * 上传MultipartFile到指定存储桶
     *
     * @param file       MultipartFile文件
     * @param fileName   文件名
     * @param bucketName 存储桶名称
     * @return 对象键
     */
    public String uploadFile(MultipartFile file, String fileName, String bucketName) {
        Assert.notNull(file, "文件不能为空");
        Assert.notBlank(fileName, "文件名不能为空");
        Assert.notBlank(bucketName, "bucketName不能为空");

        // 检查文件大小
        if (ossProperties.getMaxFileSize() > 0 && file.getSize() > ossProperties.getMaxFileSize()) {
            logger.error("文件大小超过限制: {} > {}", file.getSize(), ossProperties.getMaxFileSize());
            throw BizException.of(CommonErrorCode.ERROR.getCode(), "文件大小超过限制");
        }

        // 检查文件类型
        if (ossProperties.getAllowedContentTypes() != null && ossProperties.getAllowedContentTypes().length > 0) {
            String contentType = file.getContentType();
            boolean allowed = false;
            for (String allowedType : ossProperties.getAllowedContentTypes()) {
                if (allowedType.equals(contentType) ||
                        (allowedType.endsWith("/*") && contentType != null &&
                                contentType.startsWith(allowedType.substring(0, allowedType.length() - 2)))) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                logger.error("不支持的文件类型: {}", contentType);
                throw BizException.of(CommonErrorCode.ERROR.getCode(), "不支持的文件类型");
            }
        }

        // 处理文件名
        String processedFileName = processFileName(fileName);

        // 检查OSS客户端是否初始化
        if (ossClient == null) {
            logger.error("OSS客户端未初始化");
            throw BizException.of(CommonErrorCode.ERROR.getCode(), "OSS客户端未初始化");
        }

        // 使用try-with-resources自动关闭资源
        try (InputStream inputStream = file.getInputStream()) {
            String objectKey = getObjectKey(processedFileName);
            PutObjectResult putObjectResult = ossClient.putObject(bucketName, objectKey, inputStream);
            logger.info("文件上传成功: {}", objectKey);
            return objectKey;
        } catch (IOException e) {
            logger.error("读取上传文件失败: {}", e.getMessage(), e);
            throw BizException.of(CommonErrorCode.ERROR.getCode(), "读取上传文件失败");
        } catch (OSSException e) {
            logger.error("OSS上传文件失败: {}", e.getMessage(), e);
            throw BizException.of(CommonErrorCode.ERROR.getCode(), "文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("上传文件异常: {}", e.getMessage(), e);
            throw BizException.of(CommonErrorCode.ERROR.getCode(), "文件上传异常");
        }
    }

    /**
     * 获取公共URL（默认存储桶）
     *
     * @param objectKey 对象键
     * @return 公共URL
     */
    public String getPublicUrl(String objectKey) {
        return getPublicUrl(objectKey, ossProperties.getBucketName());
    }

    /**
     * 获取公共URL（指定存储桶）
     *
     * @param objectKey  对象键
     * @param bucketName 存储桶名称
     * @return 公共URL
     */
    public String getPublicUrl(String objectKey, String bucketName) {
        Assert.notBlank(bucketName, "bucketName不能为空");
        Assert.notBlank(objectKey, "objectKey不能为空");
        Assert.notBlank(ossProperties.getEndpoint(), "endpoint不能为空");

        String url = ossProperties.getEndpoint();
        if (!url.startsWith("http")) {
            url = "https://" + url;
        }
        if (!url.contains(bucketName + ".")) {
            url = url.replaceFirst("https?://", "$0" + bucketName + ".");
        }

        if (!url.endsWith("/")) {
            url = url.concat("/");
        }
        url = url.concat(objectKey);
        return url;
    }

    /**
     * 获取私有URL（默认存储桶，默认过期时间）
     *
     * @param objectKey 对象键
     * @return 私有URL
     */
    public String getPrivateUrl(String objectKey) {
        return getPrivateUrl(objectKey, ossProperties.getBucketName(), null);
    }

    /**
     * 获取私有URL（默认存储桶，指定过期时间）
     *
     * @param objectKey      对象键
     * @param expirationTime 过期时间戳
     * @return 私有URL
     */
    public String getPrivateUrl(String objectKey, Long expirationTime) {
        return getPrivateUrl(objectKey, ossProperties.getBucketName(), expirationTime);
    }

    /**
     * 获取私有URL（指定存储桶，默认过期时间）
     *
     * @param objectKey  对象键
     * @param bucketName 存储桶名称
     * @return 私有URL
     */
    public String getPrivateUrl(String objectKey, String bucketName) {
        return getPrivateUrl(objectKey, bucketName, null);
    }

    /**
     * 获取私有URL（指定存储桶，指定过期时间）
     *
     * @param objectKey      对象键
     * @param bucketName     存储桶名称
     * @param expirationTime 过期时间戳
     * @return 私有URL
     */
    public String getPrivateUrl(String objectKey, String bucketName, Long expirationTime) {
        Assert.notBlank(bucketName, "bucketName不能为空");
        Assert.notBlank(objectKey, "objectKey不能为空");
        return createViewUrl(bucketName, objectKey, expirationTime);
    }


    /**
     * 删除OSS对象
     *
     * @param objectKey 对象键
     * @return 是否删除成功
     */
    public boolean deleteObject(String objectKey) {
        return deleteObject(objectKey, ossProperties.getBucketName());
    }

    /**
     * 删除OSS对象
     *
     * @param objectKey  对象键
     * @param bucketName 存储桶名称
     * @return 是否删除成功
     */
    public boolean deleteObject(String objectKey, String bucketName) {
        Assert.notBlank(bucketName, "bucketName不能为空");
        Assert.notBlank(objectKey, "objectKey不能为空");

        // 检查OSS客户端是否初始化
        if (ossClient == null) {
            logger.error("OSS客户端未初始化");
            throw BizException.of(CommonErrorCode.ERROR.getCode(), "OSS客户端未初始化");
        }

        try {
            ossClient.deleteObject(bucketName, objectKey);
            logger.info("文件删除成功: {}", objectKey);
            return true;
        } catch (Exception e) {
            logger.error("删除文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 判断对象是否存在
     *
     * @param objectKey 对象键
     * @return 是否存在
     */
    public boolean doesObjectExist(String objectKey) {
        return doesObjectExist(objectKey, ossProperties.getBucketName());
    }

    /**
     * 判断对象是否存在
     *
     * @param objectKey  对象键
     * @param bucketName 存储桶名称
     * @return 是否存在
     */
    public boolean doesObjectExist(String objectKey, String bucketName) {
        Assert.notBlank(bucketName, "bucketName不能为空");
        Assert.notBlank(objectKey, "objectKey不能为空");

        // 检查OSS客户端是否初始化
        if (ossClient == null) {
            logger.error("OSS客户端未初始化");
            throw BizException.of(CommonErrorCode.ERROR.getCode(), "OSS客户端未初始化");
        }

        try {
            return ossClient.doesObjectExist(bucketName, objectKey);
        } catch (Exception e) {
            logger.error("检查文件是否存在失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 复制对象
     *
     * @param sourceObjectKey      源对象键
     * @param destinationObjectKey 目标对象键
     * @return 是否复制成功
     */
    public boolean copyObject(String sourceObjectKey, String destinationObjectKey) {
        return copyObject(sourceObjectKey, ossProperties.getBucketName(), destinationObjectKey, ossProperties.getBucketName());
    }

    /**
     * 复制对象
     *
     * @param sourceObjectKey      源对象键
     * @param sourceBucketName     源存储桶
     * @param destinationObjectKey 目标对象键
     * @param destBucketName       目标存储桶
     * @return 是否复制成功
     */
    public boolean copyObject(String sourceObjectKey, String sourceBucketName,
                              String destinationObjectKey, String destBucketName) {
        Assert.notBlank(sourceBucketName, "源bucketName不能为空");
        Assert.notBlank(sourceObjectKey, "源objectKey不能为空");
        Assert.notBlank(destBucketName, "目标bucketName不能为空");
        Assert.notBlank(destinationObjectKey, "目标objectKey不能为空");

        // 检查OSS客户端是否初始化
        if (ossClient == null) {
            logger.error("OSS客户端未初始化");
            throw BizException.of(CommonErrorCode.ERROR.getCode(), "OSS客户端未初始化");
        }

        try {
            ossClient.copyObject(sourceBucketName, sourceObjectKey, destBucketName, destinationObjectKey);
            logger.info("文件复制成功: {} -> {}", sourceObjectKey, destinationObjectKey);
            return true;
        } catch (Exception e) {
            logger.error("复制文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 下载文件到输入流
     *
     * @param objectKey 对象键
     * @return 输入流，使用完毕需要手动关闭
     */
    public InputStream downloadObject(String objectKey) {
        return downloadObject(objectKey, ossProperties.getBucketName());
    }

    /**
     * 下载文件到输入流
     *
     * @param objectKey  对象键
     * @param bucketName 存储桶名称
     * @return 输入流，使用完毕需要手动关闭
     */
    public InputStream downloadObject(String objectKey, String bucketName) {
        Assert.notBlank(bucketName, "bucketName不能为空");
        Assert.notBlank(objectKey, "objectKey不能为空");

        // 检查OSS客户端是否初始化
        if (ossClient == null) {
            logger.error("OSS客户端未初始化");
            throw BizException.of(CommonErrorCode.ERROR.getCode(), "OSS客户端未初始化");
        }

        try {
            return ossClient.getObject(bucketName, objectKey).getObjectContent();
        } catch (Exception e) {
            logger.error("下载文件失败: {}", e.getMessage(), e);
            throw BizException.of(CommonErrorCode.ERROR.getCode(), "下载文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件元数据
     *
     * @param objectKey 对象键
     * @return 文件元数据
     */
    public ObjectMetadata getObjectMetadata(String objectKey) {
        return getObjectMetadata(objectKey, ossProperties.getBucketName());
    }

    /**
     * 获取文件元数据
     *
     * @param objectKey  对象键
     * @param bucketName 存储桶名称
     * @return 文件元数据
     */
    public ObjectMetadata getObjectMetadata(String objectKey, String bucketName) {
        Assert.notBlank(bucketName, "bucketName不能为空");
        Assert.notBlank(objectKey, "objectKey不能为空");

        // 检查OSS客户端是否初始化
        if (ossClient == null) {
            logger.error("OSS客户端未初始化");
            throw BizException.of(CommonErrorCode.ERROR.getCode(), "OSS客户端未初始化");
        }

        try {
            return ossClient.getObjectMetadata(bucketName, objectKey);
        } catch (Exception e) {
            logger.error("获取文件元数据失败: {}", e.getMessage(), e);
            throw BizException.of(CommonErrorCode.ERROR.getCode(), "获取文件元数据失败: " + e.getMessage());
        }
    }

    /**
     * 分片上传文件
     *
     * @param file     文件
     * @param fileName 文件名
     * @return 对象键
     */
    public String multipartUpload(File file, String fileName) {
        Assert.notNull(file, "文件不能为空");
        Assert.notBlank(fileName, "文件名不能为空");

        // 检查OSS客户端是否初始化
        if (ossClient == null) {
            logger.error("OSS客户端未初始化");
            throw BizException.of(CommonErrorCode.ERROR.getCode(), "OSS客户端未初始化");
        }

        String objectKey = getObjectKey(fileName);
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(ossProperties.getBucketName(), objectKey);
        InitiateMultipartUploadResult result = ossClient.initiateMultipartUpload(request);
        String uploadId = result.getUploadId();

        try {
            // 分片大小，50MB
            final long partSize = this.ossProperties.getPartSize();
            long fileLength = file.length();
            int partCount = (int) (fileLength / partSize);
            if (fileLength % partSize != 0) {
                partCount++;
            }

            List<PartETag> partETags = new ArrayList<>();
            for (int i = 0; i < partCount; i++) {
                long startPos = i * partSize;
                long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;
                try (InputStream instream = new FileInputStream(file)) {
                    instream.skip(startPos);
                    UploadPartRequest uploadPartRequest = new UploadPartRequest();
                    uploadPartRequest.setBucketName(ossProperties.getBucketName());
                    uploadPartRequest.setKey(objectKey);
                    uploadPartRequest.setUploadId(uploadId);
                    uploadPartRequest.setInputStream(instream);
                    uploadPartRequest.setPartSize(curPartSize);
                    uploadPartRequest.setPartNumber(i + 1);

                    UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
                    partETags.add(uploadPartResult.getPartETag());
                }
            }

            CompleteMultipartUploadRequest completeMultipartUploadRequest =
                    new CompleteMultipartUploadRequest(ossProperties.getBucketName(), objectKey, uploadId, partETags);
            ossClient.completeMultipartUpload(completeMultipartUploadRequest);

            logger.info("分片上传文件成功: {}", objectKey);
            return objectKey;
        } catch (Exception e) {
            logger.error("分片上传文件失败: {}", e.getMessage(), e);
            ossClient.abortMultipartUpload(new AbortMultipartUploadRequest(ossProperties.getBucketName(), objectKey, uploadId));
            throw BizException.of(CommonErrorCode.ERROR.getCode(), "分片上传文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取对象键
     *
     * @param fileName 文件名
     * @return 对象键
     */
    private String getObjectKey(String fileName) {
        String dir = getApplication().concat("/").concat(DateUtil.format(new Date(), "yyyy/MM/dd"));
        return dir.concat("/").concat(fileName);
    }

    /**
     * 获取应用名称
     *
     * @return 应用名称
     */
    private String getApplication() {
        String applicationName = applicationContext.getApplicationName();
        if (StrUtil.isBlank(applicationName)) {
            applicationName = "default";
        }
        return applicationName;
    }

    /**
     * 处理文件名
     *
     * @param originalFileName 原始文件名
     * @return 处理后的文件名
     */
    private String processFileName(String originalFileName) {
        String fileNameStrategy = ossProperties.getFileNameStrategy();
        String fileExtension = "";
        String baseName = originalFileName;

        // 提取文件扩展名
        int lastDotIndex = originalFileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            fileExtension = originalFileName.substring(lastDotIndex);
            baseName = originalFileName.substring(0, lastDotIndex);
        }

        String newFileName = switch (fileNameStrategy) {
            case "UUID" -> UUID.randomUUID() + fileExtension;
            case "TIMESTAMP" -> System.currentTimeMillis() + fileExtension;
            case "CUSTOM" -> baseName + fileExtension;
            default -> originalFileName;
        };

        // 添加前缀和后缀
        return ossProperties.getFileNamePrefix() + newFileName + ossProperties.getFileNameSuffix();
    }
}