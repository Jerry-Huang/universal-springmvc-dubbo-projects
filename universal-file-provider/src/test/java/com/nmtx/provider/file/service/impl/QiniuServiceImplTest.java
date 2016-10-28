package com.nmtx.provider.file.service.impl;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.nmtx.provider.file.TestBase;
import com.universal.api.file.service.QiniuService;

public class QiniuServiceImplTest extends TestBase {

    @Autowired
    private QiniuService qiniuService;

    @Test
    public void testGetToken() {
        String uploadToken = qiniuService.token();
        System.out.println(uploadToken);
        Assert.assertNotNull(uploadToken);
    }
    
    @Test
    public void testGetPrivateToken() {
        String uploadToken = qiniuService.privateToken();
        System.out.println(uploadToken);
        Assert.assertNotNull(uploadToken);
    }
    
    @Test
    public void testgetAccessUrl() {
        String url = qiniuService.privateUrl("http://img02.nmtx.com/160531/FrHBel7-pKQs5OF9z1UEFE8TaAQX.jpg");
        System.out.println(url);
        Assert.assertNotNull(url);
    }

}
