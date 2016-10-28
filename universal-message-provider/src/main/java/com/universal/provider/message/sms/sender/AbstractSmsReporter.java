package com.universal.provider.message.sms.sender;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.universal.provider.message.entity.Sms;
import com.universal.provider.message.mapper.SmsMapper;
import com.universal.provider.message.sms.CacheKey;
import com.universal.provider.message.sms.SmsReporter;
import com.universal.provider.message.sms.SmsSender;

public abstract class AbstractSmsReporter implements SmsReporter {

    @Autowired
    private SmsMapper smsMapper;

    private SmsSender failedHandler;

    @Autowired
    protected RedisTemplate<String, Serializable> redisTemplate;

    final static Logger logger = LoggerFactory.getLogger(AbstractSmsReporter.class);

    @Override
    public boolean start() {

        return true;
    }

    @Override
    public void stop() {

    }

    @Override
    public void onSubmittedSuccess(final Sms sms) {

        if (sms != null) {
            smsMapper.updateSms(sms.getId(), sms.getStatus(), sms.getChannel());
            smsMapper.insertLog(sms.getId(), sms.getStatus(), sms.getChannel());
        }
    }

    @Override
    public void onArrivedSuccess(final Sms sms) {

    }

    @Override
    public void onSubmittedFailed(final Sms sms) {

        if (sms != null) {

            smsMapper.updateSms(sms.getId(), sms.getStatus(), sms.getChannel());
            smsMapper.insertLog(sms.getId(), sms.getStatus(), sms.getChannel());

            if (failedHandler != null) {
                try {
                    this.failedHandler.send(sms);
                } catch (Exception e) {
                    logger.error("Send " + sms.getId() + " failed.", e);
                }
            } else {
                redisTemplate.delete(String.format(CacheKey.SMS, sms.getId()));
                logger.info(String.format("Removed %d from redis, because of no failed handler.", sms.getId()));
            }
        }
    }

    @Override
    public void onArrivedFailed(final Sms sms) {

        if (sms != null) {
            if (failedHandler != null) {
                try {
                    this.failedHandler.send(sms);
                } catch (Exception e) {
                    logger.error("Send " + sms.getId() + " failed.", e);
                }
            } else {
                redisTemplate.delete(String.format(CacheKey.SMS, sms.getId()));
                logger.info(String.format("Removed %d from redis, because of no failed handler.", sms.getId()));
            }
        }
    }

    @Override
    public void setFailedHander(SmsSender sender) {

        this.failedHandler = sender;
    }

    @Override
    public SmsSender getFailedHander() {

        return this.failedHandler;
    }

}
