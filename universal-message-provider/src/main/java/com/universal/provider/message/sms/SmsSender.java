package com.universal.provider.message.sms;

import com.universal.provider.message.entity.Sms;

public interface SmsSender {

    boolean start();

    void stop();

    void send(final Sms sms) throws Exception;

    void setReporter(final SmsReporter reporter);

}
