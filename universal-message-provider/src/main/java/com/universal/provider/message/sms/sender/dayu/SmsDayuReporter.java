package com.universal.provider.message.sms.sender.dayu;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taobao.api.internal.tmc.Message;
import com.taobao.api.internal.tmc.MessageHandler;
import com.taobao.api.internal.tmc.MessageStatus;
import com.taobao.api.internal.tmc.TmcClient;
import com.taobao.api.internal.toplink.LinkException;
import com.universal.provider.message.entity.Sms;
import com.universal.provider.message.sms.CacheKey;
import com.universal.provider.message.sms.SmsStatus;
import com.universal.provider.message.sms.sender.AbstractSmsReporter;

public class SmsDayuReporter extends AbstractSmsReporter implements MessageHandler {

    private String appkey = null;
    private String secret = null;
    private TmcClient tmcClient = null;

    final static Logger logger = LoggerFactory.getLogger(SmsDayuReporter.class);

    @Override
    public boolean start() {

        tmcClient = new TmcClient(appkey, secret);
        tmcClient.setMessageHandler(this);

        try {
            tmcClient.connect("ws://mc.api.taobao.com");
        } catch (LinkException e) {
            logger.error("Connect to tmc failed.", e);
            return false;
        }

        return true;
    }

    @Override
    public void stop() {

        tmcClient.close();
    }

    @Override
    public void onMessage(Message msg, MessageStatus status) throws Exception {

        if (!msg.getTopic().equals("alibaba_aliqin_FcSmsDR")) {
            logger.warn("Unconcerned topic {}", msg.getTopic());
            return;
        }

        logger.info(msg.getContent());

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> message = objectMapper.readValue(msg.getContent(), new TypeReference<Map<String, String>>() {
        });

        if (message.containsKey("extend")) {

            String smsId = message.get("extend");
            Sms sms = (Sms) redisTemplate.opsForValue().get(String.format(CacheKey.SMS, smsId));

            if (message.containsKey("state")) {
                if (isSuccess(message.get("state"))) {
                    logger.info("Report: {} has arrived.", smsId);
                    if (sms != null) {
                        sms.setStatus(SmsStatus.ARV);
                    }
                    super.onArrivedSuccess(sms);
                } else {
                    logger.error("Report: {} arriving failed.", smsId);
                    if (sms != null) {
                        sms.setStatus(SmsStatus.FAL);
                    }
                    super.onArrivedFailed(sms);
                }
            }
        }

    }

    private static boolean isSuccess(final String state) {

        return state.equals("1");
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

}
