package com.universal.provider.message.sms.sender;

import org.springframework.beans.factory.annotation.Autowired;

import com.universal.provider.message.entity.Sms;
import com.universal.provider.message.mapper.SmsMapper;
import com.universal.provider.message.sms.SmsReporter;
import com.universal.provider.message.sms.SmsSender;

public abstract class AbstractSmsSender implements SmsSender {

    @Autowired
    private SmsMapper smsMapper;

    protected int maxSendingTimes = 3;

    protected SmsReporter reporter = null;

    @Override
    public void setReporter(SmsReporter reporter) {

        this.reporter = reporter;
    }

    protected int findSuccessedQuantityToday(final Sms sms, final String smsChannel) {

        return smsMapper.findSuccessedQuantityToday(sms.getTo(), sms.getContent(), smsChannel);
    }

    public int getMaxSendingTimes() {
        return maxSendingTimes;
    }

    public void setMaxSendingTimes(int maxSendingTimes) {
        this.maxSendingTimes = maxSendingTimes;
    }
}
