package com.universal.api.file.service;

import java.io.IOException;

public interface QiniuService {

    String token();

    String privateToken();

    String privateUrl(String url);

    String upload(byte[] data) throws IOException;

    String upload(byte[] data, String name) throws IOException;

}
