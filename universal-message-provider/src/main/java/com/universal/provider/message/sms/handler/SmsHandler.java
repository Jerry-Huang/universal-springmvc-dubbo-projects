package com.universal.provider.message.sms.handler;

import com.universal.provider.message.entity.Sms;

public interface SmsHandler {

    public void send(Sms sms) throws Exception;

}
