package com.universal.provider.message.sms.sender.dayu;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.AlibabaAliqinFcSmsNumSendRequest;
import com.taobao.api.response.AlibabaAliqinFcSmsNumSendResponse;
import com.universal.Environment;
import com.universal.provider.message.entity.Sms;
import com.universal.provider.message.sms.SmsChannel;
import com.universal.provider.message.sms.SmsStatus;
import com.universal.provider.message.sms.sender.AbstractSmsSender;

public class SmsDayuSender extends AbstractSmsSender implements Runnable {

    private String url = null;
    private String appkey = null;
    private String secret = null;

    private int interval = 10;
    private Thread thread = null;
    private Object lock = new Object();
    private BlockingQueue<Sms> queue = null;
    private volatile boolean stopped = false;

    private TaobaoClient taobaoClient = null;
    private ObjectMapper objectMapper = new ObjectMapper();

    private final static Logger logger = LoggerFactory.getLogger(SmsDayuSender.class);

    public SmsDayuSender(final String url, final String appkey, final String secret) {

        this.url = url;
        this.appkey = appkey;
        this.secret = secret;

        queue = new LinkedBlockingQueue<>();
    }

    @Override
    public void send(Sms sms) throws Exception {

        sms.setChannel(SmsChannel.DAYU);

        if (StringUtils.isBlank(sms.getTemplateCode())) {

            logger.error("Template code cannot be empty.");

            sms.setStatus(SmsStatus.REJ);
            reporter.onSubmittedFailed(sms);
            return;
        }

        if (findSuccessedQuantityToday(sms, SmsChannel.DAYU) >= this.maxSendingTimes) {

            logger.error("The maximum of " + maxSendingTimes + " per message per day has been exceeded");

            sms.setStatus(SmsStatus.REJ);
            reporter.onSubmittedFailed(sms);
            return;
        }

        if (!this.queue.offer(sms)) {

            logger.error("Put sms [" + sms + "] in queue failed.");
        }

    }

    @Override
    public boolean start() {

        logger.info("Starting Dayu sender ...");

        thread = new Thread(this);
        this.stopped = false;

        taobaoClient = new DefaultTaobaoClient(url, appkey, secret);
        thread.start();

        if (this.reporter != null && Environment.current() == Environment.PRD) {
            this.reporter.start();
        }

        logger.info("Sender started.");
        return true;
    }

    @Override
    public void stop() {

        logger.info("Stopping Dayu sender ...");
        synchronized (lock) {
            this.stopped = true;
            lock.notifyAll();
        }

        reporter.stop();

        if (thread != null) {
            try {
                thread.join(interval);
            } catch (InterruptedException e) {
                logger.error("Join thread failed.", e);
            }
        }
    }

    @Override
    public void run() {

        while (!stopped) {

            synchronized (lock) {

                Sms sms = null;
                try {
                    sms = this.queue.poll(interval * 100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    logger.error("Poll sms from queue failed.", e);
                }

                if (sms == null) {
                    continue;
                }

                logger.info("Sending sms: " + sms);

                AlibabaAliqinFcSmsNumSendRequest smsRequest = new AlibabaAliqinFcSmsNumSendRequest();
                smsRequest.setSmsType("normal");
                smsRequest.setSmsFreeSignName("农迈天下");
                smsRequest.setExtend(String.valueOf(sms.getId()));
                smsRequest.setRecNum(sms.getTo());
                smsRequest.setSmsTemplateCode(sms.getTemplateCode());

                try {
                    smsRequest.setSmsParamString(objectMapper.writeValueAsString(sms.getParameters()));

                    AlibabaAliqinFcSmsNumSendResponse smsResponse = taobaoClient.execute(smsRequest);

                    logger.info("Response: " + smsResponse.getBody());

                    if (smsResponse.isSuccess()) {

                        logger.info(String.format("Send sms %s sucessfully.", sms));

                        sms.setStatus(SmsStatus.SNT);
                        this.reporter.onSubmittedSuccess(sms);
                    } else {

                        logger.error(String.format("Send sms %s failed: %s", sms, smsResponse.getMsg()));

                        sms.setStatus(SmsStatus.FAL);
                        this.reporter.onSubmittedFailed(sms);
                    }

                } catch (ApiException | JsonProcessingException e) {

                    logger.error(String.format("Send sms %s failed.", sms), e);

                    sms.setStatus(SmsStatus.FAL);
                    this.reporter.onSubmittedFailed(sms);
                }

                try {
                    lock.wait(interval);
                } catch (InterruptedException e) {
                    logger.error("Wait for thread failed.", e);
                }

            }
        }

        logger.info("Stopped normally!");
    }

}
