package com.universal.api.message.service;

import java.util.Map;

import com.universal.api.message.bean.SmsBean;

public interface MessageService {

    String welcome();

    void send(final String channel, final String ip, final SmsBean... smses);

    String sms(final String channel, final String ip, final String phone, final String content, final String templateCode, final String jsonParameters) throws Exception;

    void send(final String channel, final String ip, final String phone, final String templateCode, final Map<String, String> parameters) throws Exception;

}
