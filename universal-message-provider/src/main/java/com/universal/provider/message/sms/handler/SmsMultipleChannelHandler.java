package com.universal.provider.message.sms.handler;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.universal.provider.message.entity.Sms;
import com.universal.provider.message.mapper.SmsMapper;
import com.universal.provider.message.sms.CacheKey;
import com.universal.provider.message.sms.SmsSender;
import com.universal.provider.message.sms.SmsStatus;

@Service
public class SmsMultipleChannelHandler implements SmsHandler {

    @Autowired
    private SmsMapper smsMapper;

    private int maxSendingTimes = 3;

    private Map<String, SmsSender> senderMap = null;

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    final static Logger logger = LoggerFactory.getLogger(SmsMultipleChannelHandler.class);

    public SmsMultipleChannelHandler() {

    }

    public SmsMultipleChannelHandler(final Map<String, SmsSender> senderMap) {

        this.senderMap = senderMap;
    }

    public void send(Sms sms) throws Exception {

        if (StringUtils.isBlank(sms.getTo()) || StringUtils.isBlank(sms.getContent()) || StringUtils.isBlank(sms.getChannel())) {

            throw new Exception(String.format("Empty phone or content: phone=%s, content=%s,channel=%s.", sms.getTo(), sms.getContent(), sms.getChannel()));
        } else {

            if (smsMapper.findQuantityToday(sms.getTo(), sms.getContent()) >= this.maxSendingTimes) {
                sms.setStatus(SmsStatus.REJ);
                smsMapper.insertSms(sms);
                throw new Exception("The maximum of " + maxSendingTimes + " per message per day has been exceeded");
            }

            sms.setStatus(SmsStatus.NEW);
            smsMapper.insertSms(sms);

            SmsSender sender = senderMap.get(sms.getChannel());
            if (sender == null) {
                sender = senderMap.get("P1");
            }

            sender.send(sms);

            redisTemplate.opsForValue().set(String.format(CacheKey.SMS, sms.getId()), sms, 1, TimeUnit.DAYS);
        }
    }

    public int getMaxSendingTimes() {
        return maxSendingTimes;
    }

    public void setMaxSendingTimes(int maxSendingTimes) {
        this.maxSendingTimes = maxSendingTimes;
    }
}