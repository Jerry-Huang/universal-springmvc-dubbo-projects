package com.universal.provider.file.service.impl;

import java.io.IOException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.universal.api.file.service.QiniuService;

@Service("qiniuService")
public class QiniuServiceImpl implements QiniuService {

    @Value("${qiniu.access.key}")
    private String accessKey;

    @Value("${qiniu.secret.key}")
    private String secretKey;

    @Value("${qiniu.bucket.name}")
    private String bucketName;

    @Value("${qiniu.private.bucket.name}")
    private String privateBucketName;

    @Value("${qiniu.host.domain}")
    private String hostDomain;

    @Value("${qiniu.private.host.domain}")
    private String privateHostDomain;

    private Auth auth = null;
    private StringMap uploadPolicy = new StringMap();
    private final static long DEFAULT_EXPIRES_SECONDS = 3600L;

    private final static Logger logger = LoggerFactory.getLogger(QiniuServiceImpl.class);

    private Auth createAuth() {

        if (auth == null) {
            synchronized (this) {
                if (auth == null) {
                    auth = Auth.create(accessKey, secretKey);
                }
            }
        }

        return auth;
    }

    @Override
    public String upload(byte[] data) throws IOException {

        return upload(data, null);
    }

    @Override
    public String upload(byte[] data, String name) throws IOException {

        UploadManager uploadManager = new UploadManager();
        try {
            Response response = uploadManager.put(data, name, token(name));
            if (response != null && response.isOK()) {

                logger.debug("Response of uploading : {}", response.bodyString());
                StringMap responseMap = response.jsonToMap();
                return this.hostDomain + responseMap.get("key").toString();
            } else {

                logger.error("Upload file failed : {}", response);
                throw new IOException("Upload file failed : " + response);
            }
        } catch (QiniuException e) {

            logger.error("Upload file failed, {}.", e.response);
            throw new IOException(e.response == null ? ExceptionUtils.getFullStackTrace(e) : e.getClass().getName() + "\n        " + e.response.toString());
        }
    }

    @Override
    public String token() {

        return token(null);
    }

    public String token(String key) {

        return createAuth().uploadToken(bucketName, key, DEFAULT_EXPIRES_SECONDS, uploadPolicy.put("saveKey", DateTime.now().toString("yyMMdd") + "/$(etag)$(ext)"));
    }

    @Override
    public String privateToken() {

        return createAuth().uploadToken(privateBucketName, null, DEFAULT_EXPIRES_SECONDS, uploadPolicy.put("saveKey", DateTime.now().toString("yyMMdd") + "/$(etag)$(ext)"));
    }

    @Override
    public String privateUrl(String url) {

        if (StringUtils.isNotBlank(url)) {
            url = createAuth().privateDownloadUrl(url, DEFAULT_EXPIRES_SECONDS);
        }

        return url;
    }

}
